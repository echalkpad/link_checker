var $ = require('jquery');
var config = require('../config');
var EventEmitter = require('events').EventEmitter;
var merge = require('react/lib/merge');

var CHANGE_EVENT = 'change';
var endpoint = '/api/v1/root_page';
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
        {cache: false, dataType: 'json'}
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

    add: function(data) {
        if (_rootpages.some(function isDuplicate(element) {
            return element.url === data.url;
        })) {
            console.log("DUPE");
            return;
        }

        _rootpages.push(data);
        syncNewPageToServer(data);
    }
});

module.exports = RootPageStore;