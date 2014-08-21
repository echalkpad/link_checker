var $ = require('jquery');
var config = require('../config');
var EventEmitter = require('events').EventEmitter;
var merge = require('react/lib/merge');

var CHANGE_EVENT = 'change';
var endpoint = '/api/v1/monitored_page';
var timer;
var updateInterval = 10000;

var _rootpages = [];

var updateModelFromAjax = function(data) {
    _rootpages = [];
    data.forEach(function(elem) {
        _rootpages.push(elem);
    });

    RootPageStore.emitChanged();
};

var retrievePages = function() {
    $.ajax(
        config.baseURL + endpoint,
        {
            cache: true,
            dataType: 'json',
            ifModified: true
        }
    ).done(updateModelFromAjax)
     .fail(function () {
        // TODO: eventually emit failure event for ui?
        console.log("Failure!");
    });
};

var syncNewPageToServer = function(data) {
    var url = config.baseURL + endpoint + '/' + data.url;
    $.ajax(
        url,
        {
            data: JSON.stringify(data),
            type: 'PUT',
            processData: false,
            contentType: 'application/json'
        }
    ).done(retrievePages)
     .fail(function() {
        console.log("Put failed!")
     });
};

var deletePageFromServer = function(url) {
    var url = config.baseURL + endpoint + '/' + url;
    $.ajax(
        url,
        {
            type: 'DELETE'
        }
    ).done(retrievePages);
};

var urlMatches = function(url) {
    return function isDuplicate(element) {
        return element.url === url;
    };
};

var RootPageStore = merge(EventEmitter.prototype, {
    getAll: function() {
        return _rootpages;
    },

    emitChanged: function() {
        this.emit(CHANGE_EVENT);
    },

    /**
     * @param {function} callback
     */
    addChangeListener: function(callback) {
        this.on(CHANGE_EVENT, callback);
    },

    /**
     * @param {function} callback
     */
    removeChangeListener: function(callback) {
        this.removeListener(CHANGE_EVENT, callback);
    },

    startSync: function() {
        retrievePages();
        timer = setInterval(retrievePages, updateInterval);
    },

    /**
     * Add a new RootPage to the store. Will automatically sync
     * to the server.
     * @param data RootPage to add
     */
    add: function(data) {
        if (_rootpages.some(urlMatches(data.url))) {
            console.log("DUPE");
            return;
        }

        _rootpages.push(data);
        syncNewPageToServer(data);
    },

    /**
     * Delete a RootPage from the store with the given url.
     * @param url URL to delete
     */
    delete: function(url) {
        _rootpages = _rootpages.filter(urlMatches(url));
        deletePageFromServer(url);
    }
});

module.exports = RootPageStore;