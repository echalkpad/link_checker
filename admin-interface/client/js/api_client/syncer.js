var Q = require('q');
var request = require('superagent');
require('q-superagent')(request);

var min_retry = 1000;
var retry = min_retry;
var max_retry = 30000;

var newRetry = function() {
    return Math.max(max_retry, retry * retry);
};

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

var Syncer = function(flux) {
    this.flux = flux;

    // Subscribe to MPS events
    var store = flux.store("MonitoredPageStore");
    var self = this;

    store.on(store.USER_ADDED_PAGE, function(url) {
        self.addMonitoredPage(url);
    });

    store.on(store.USER_DELETED_PAGE, function(url) {
        self.deleteMonitoredPage(url);
    });
};

Syncer.prototype.updateMonitoredPages = function() {
    var get = sendWithRetry(function() {
        return request.get('/api/v1/monitored_page/').q();
    });

    var self = this;
    get.then(function resolveData(data) {
        self.flux.actions.updateMonitoredPages(data.body);
    }, function onError(why) {
        console.log("Error: " + why);
    });
};

Syncer.prototype.addMonitoredPage = function(url) {
    var self = this;
    var post = sendWithRetry(function() {
        return request
                .post('/api/v1/monitored_page')
                .send({url: url})
                .q();

    });

    post.then(function resolveData(data) {
        self.flux.actions.addSynced(url);
    },

    function error(why) {
        // TODO: fill me in
    });
};

Syncer.prototype.deleteMonitoredPage = function(url) {
    var self = this;
    var deleteOp = sendWithRetry(function() {
        return request
                .del('/api/v1/monitored_page/' + url)
                .q();
    });

    deleteOp.then(function resolveData(data) {
        self.flux.actions.deleteSynced(url);
    },

    function error(why) {
        // TODO: fill me in
    });
};

module.exports = Syncer;
