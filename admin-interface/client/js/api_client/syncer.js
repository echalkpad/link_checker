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
            setTimeout(function() { sendWithRetry(senderFn); }, timeout);
        });

    return retPromise.promise;
};

var Syncer = {
    updateMonitoredPages: function() {
        var get = sendWithRetry(function() {
            return request.get('/api/v1/monitored_page/').q();
        });

        get.then(function resolveData(data) {
            console.log(data.body);
        }, function onError(why) {
            console.log("Error: " + why);
        });
    }
};

module.exports = Syncer;
