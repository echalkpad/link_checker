var constants = require('../common/constants.js');
var Fluxxor = require('fluxxor');

var MonitoredPageStore = Fluxxor.createStore({
    initialize: function() {
        this.monitored_pages = [];

        this.bindActions(
            constants.ADD_MONITORED_PAGE, this.onAddMonitoredPage,
            constants.DELETE_MONITORED_PAGE, this.onDeleteMonitoredPage
        );
    },

    onAddMonitoredPage: function(payload) {
        this.monitored_pages.push({url: payload.url, status: payload.status, sync_id: payload.sync_id, sync_status: payload.sync_status});
        this.emit('change');
    },

    onDeleteMonitoredPage: function(payload) {
        this.monitored_pages = this.monitored_pages.filter(
            function excludeUrl(x) {
                return x.url !== payload.url;
            }
        );
        this.emit('change');
    }
});

module.exports = MonitoredPageStore;