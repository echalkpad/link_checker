var async = require('async');
var Q = require('q');
var request = require('superagent');
require('q-superagent')(request);
var _ = require('underscore');

var min_retry = 1000;
var retry = min_retry;
var max_retry = 30000;

var newRetry = function() {
    return Math.max(max_retry, retry * retry);
};

var UPDATE_PAGES = "UPDATE_PAGES";
var ADD_MONITORED_PAGE = "ADD_MONITORED_PAGE";
var DELETE_MONITORED_PAGE = "DELETE_MONITORED_PAGE";

/**
* sendWithRetry sends ajax requests with a retry on failure.
* It returns a promise (from Q) and also assumes the senderFn that is passed in
* returns on as well.
*
* If the promise returned by senderFn succeeds, that is passed back to the user;
* otherwise it will retry with expontential backoff. It retries forever.
*/
var sendWithRetry = function(senderFn) {
    var retPromise = Q.defer();

    senderFn().then(
        function success(why) {
            retry = min_retry;
            retPromise.resolve(why);
        },

        function error(why) {
            var timeout = retry;
            retry = newRetry();
            // retPromise.progres...
            setTimeout(function() { sendWithRetry(senderFn); }, timeout);
        });

    return retPromise.promise;
};

var _getRowForLink = function(link, cb) {
    // XXX could cache statusCode here so we're not always retrieving it
    var get = sendWithRetry(function() {
        return request.get('/api/v1/latest_crawl_report/' + encodeURIComponent(link.url)).q();
    });

    get.then(function resolveData(data) {
        cb(null, {url: link.url, anchorText: link.anchorText, statusCode: data.body.statusCode});
    }, function onError(why) {
        cb(why, null);
    });
};

var Syncer = function(flux) {
    this.flux = flux;
    this.queue = [];

    // Subscribe to MPS events
    this.store = flux.store("MonitoredPageStore");
    var self = this;

    this.store.on(this.store.USER_ADDED_PAGE, function(url) {
        self.addMonitoredPage(url);
    });

    this.store.on(this.store.USER_DELETED_PAGE, function(url) {
        self.deleteMonitoredPage(url);
    });

    this.store.on(this.store.USER_TOGGLED_PAGE, function(url, newState) {
        if (newState) {
            self.getCrawlStatusForUrl(url);
        }
    });
};

Syncer.prototype.getCrawlStatusForUrl = function(url) {
    var get = sendWithRetry(function() {
        return request.get('/api/v1/latest_crawl_report/' + encodeURIComponent(url)).q();
    });

    var self = this;
    var rows = [];
    var p = get.then(function resolveData(data) {
        rows.push({url: data.body.url, statusCode: data.body.statusCode, anchorText: "<self>"});
        async.map(data.body.links, _getRowForLink, function (err, results) {
            if (err !== null && err !== undefined) {
                console.log(err);
                return;
            }

            self.flux.actions.updateCrawlReport({
                url: data.body.url,
                links: rows.concat(results)
            });
        });
    }, function onError(why) {
        console.log("Error: " + why);
    });

    return p;
};

Syncer.prototype.updateMonitoredPages = function() {
    this._addOp({op: UPDATE_PAGES});
};

Syncer.prototype._processUpdateMonitoredPages = function() {
    var get = sendWithRetry(function() {
        return request.get('/api/v1/monitored_page/').q();
    });

    var self = this;
    var p = get.then(function resolveData(data) {
        self.flux.actions.updateMonitoredPages(data.body);
    }, function onError(why) {
        console.log("Error: " + why);
    });

    return p;
};

Syncer.prototype.addMonitoredPage = function(url) {
    this._addOp({op: ADD_MONITORED_PAGE, url: url});
};

Syncer.prototype._processAddMonitoredPage = function(url) {
    var self = this;
    var post = sendWithRetry(function() {
        return request
                .post('/api/v1/monitored_page')
                .send({url: url})
                .q();

    });

    var p = post.then(function resolveData(data) {
        self.flux.actions.addSynced(url);
    },

    function error(why) {
        // TODO: fill me in
    });

    return p;
};

Syncer.prototype.deleteMonitoredPage = function(url) {
    this._addOp({op: DELETE_MONITORED_PAGE, url: url});
};

Syncer.prototype._processDeleteMonitoredPage = function(url) {
    var self = this;
    var deleteOp = sendWithRetry(function() {
        return request
                .del('/api/v1/monitored_page/' + encodeURIComponent(url))
                .q();
    });

    var p = deleteOp.then(function resolveData(data) {
        self.flux.actions.deleteSynced(url);
    },

    function error(why) {
        // TODO: fill me in
    });

    return p;
};

Syncer.prototype._addOp = function(op) {
    this.queue.push(op);
    if (_.size(this.queue) == 1) {
        this._processQueue();
    }
};

Syncer.prototype._processQueue = function() {
    if (_.size(this.queue) === 0) {
        return;
    }

    var op = this.queue.shift();
    var self = this;
    var p;

    switch(op.op) {
    case UPDATE_PAGES:
        p = this._processUpdateMonitoredPages();
        break;
    case ADD_MONITORED_PAGE:
        p = this._processAddMonitoredPage(op.url);
        break;
    case DELETE_MONITORED_PAGE:
        p = this._processDeleteMonitoredPage(op.url);
        break;
    default:
        throw new Error("Unknown op " + op + "!");
    }

    p.fin(function() {
        self._processQueue();
    });
};

module.exports = Syncer;
