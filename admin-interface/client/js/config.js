var calculateURL = function() {
    if (typeof(document) === 'undefined' || document.location.protocol === 'file:') {
        return 'http://localhost:8080';
    } else {
        return document.location.protocol + '//' + document.location.hostname + ':8080';
    }
};

module.exports = {
    baseURL: calculateURL()
};