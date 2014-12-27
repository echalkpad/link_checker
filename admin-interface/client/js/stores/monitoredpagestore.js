var constants = require('../common/constants.js');
var Fluxxor = require('fluxxor');

var MonitoredPageStore = Fluxxor.createStore({
    initialize: function() {
        this.monitored_pages = {};
        this.USER_ADDED_PAGE = "user_added_page";
        this.USER_DELETED_PAGE = "user_deleted_page";

        this.bindActions(
            constants.ADD_MONITORED_PAGE, this.onAddMonitoredPage,
            constants.DELETE_MONITORED_PAGE, this.onDeleteMonitoredPage
        );
    },

    onAddMonitoredPage: function(payload) {
        var url = payload.url;

        var changed = false;

        if (this.monitored_pages.hasOwnProperty(url)) {
            if (this.monitored_pages[url].sync_status === constants.sync_status.DELETE_SYNCING) {
                this.monitored_pages[url].sync_status = constants.sync_status.ADD_SYNCING;
                changed = true;
            }

            // If it's not DELETE_SYNCING that means it's already been added
            // and we can ignore the update.
        } else {
            this.monitored_pages[url] = {url: url, status: constants.status.UNKNOWN, sync_status: constants.sync_status.ADD_SYNCING};
            changed = true;
        }

        if (changed) {
            this.emit('change');
            this.emit(this.USER_ADDED_PAGE, url);
        }
    },

    onDeleteMonitoredPage: function(payload) {
        var url = payload.url;
        if (this.monitored_pages.hasOwnProperty(url)) {
            var item = this.monitored_pages[url];
            console.log(item);
            if (item.sync_status !== constants.sync_status.DELETE_SYNCING) {
                item.sync_status = constants.sync_status.DELETE_SYNCING;
                this.emit('change');
                this.emit(this.USER_DELETED_PAGE, url);
            }
        }
    }
});

module.exports = MonitoredPageStore;
