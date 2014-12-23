var Q = require('q');

function ajaxGet(url) {
    var deferred = Q.defer();
    var request = new XMLHttpRequest();
    request.open('GET', url, true);

    request.onload = function() {
        if (request.status >= 200 && request.status < 400){
            // Success!
            var data = JSON.parse(request.responseText);
            deferred.resolve(data);
        } else {
            deferred.reject(new Error("Server returned " + request.status));
        }
    };

    request.onerror = function() {
        deferred.reject("Error sending ajaxGet");
    };

    request.send();
    return deferred.promise;
}

var Syncer = {
    updateMonitoredPages: function() {
        var get = ajaxGet('/api/v1/monitored_page/');
        // need proxy API
        get.then(function resolveData(data) {
            console.log(data);
        }, function onError(why) {
            console.log("Error: " + why);
        });
    }
};

module.exports = Syncer;