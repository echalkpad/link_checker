var constants = require('../common/constants.js');
var Fluxxor = require('fluxxor');
var _ = require('underscore');

var opMatchesSyncStatus = function(op_status, sync_status) {
    return ((op_status === constants.ADD_MONITORED_PAGE && sync_status === constants.sync_status.ADD_SYNCING) ||
            (op_status === constants.DELETE_MONITORED_PAGE && sync_status === constants.sync_status.DELETE_SYNCING));
};

var MonitoredPageStore = Fluxxor.createStore({
    initialize: function() {
        this.monitored_pages = {};
        this.USER_ADDED_PAGE = "user_added_page";
        this.USER_DELETED_PAGE = "user_deleted_page";

        this.bindActions(
            constants.ADD_MONITORED_PAGE, this.onAddMonitoredPage,
            constants.DELETE_MONITORED_PAGE, this.onDeleteMonitoredPage,
            constants.UPDATE_SERVER_SYNC_STATUS, this.onUpdateServerSyncStatus,
            constants.UPDATE_MONITORED_PAGES_FROM_SERVER, this.onUpdatePagesFromServer,
            constants.TOGGLE_MONITORED_PAGE_EXPANDED_VIEW, this.onToggleExpanded
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
            this.monitored_pages[url] = {
                url: url,
                status: constants.status.UNKNOWN,
                sync_status: constants.sync_status.ADD_SYNCING,
                expanded: false
            };
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
            if (item.sync_status !== constants.sync_status.DELETE_SYNCING) {
                item.sync_status = constants.sync_status.DELETE_SYNCING;
                this.emit('change');
                this.emit(this.USER_DELETED_PAGE, url);
            }
        }
    },

    onUpdateServerSyncStatus: function(payload) {
        var url = payload.url;
        var op = payload.op;

        if (!this.monitored_pages.hasOwnProperty(url)) {
            return;
        }

        var item = this.monitored_pages[url];

        if (opMatchesSyncStatus(op, item.sync_status)) {
            if (op === constants.DELETE_MONITORED_PAGE) {
                delete this.monitored_pages[url];
            } else {
                item.sync_status = constants.sync_status.SYNCED;
            }
            this.emit('change');
        }
    },

    onUpdatePagesFromServer: function(payload) {
        var newPages = {};
        var self = this;

        payload.forEach(function(x) {
            var oldItem = self.monitored_pages[x.url];
            var expanded = (oldItem === undefined) ? false : oldItem.expanded;
            newPages[x.url] = {
                url: x.url,
                status: x.status,
                sync_status: constants.sync_status.SYNCED,
                expanded: expanded
            };
        });

        var pendingPages = this._getPendingPages();

        pendingPages.forEach(function(x) {
            newPages[x.url] = x;
        });

        this.monitored_pages = newPages;
        this.emit('change');
    },

    onToggleExpanded: function(payload) {
        var url = payload.url;

        this.monitored_pages[url].expanded = !this.monitored_pages[url].expanded;
        this.emit('change');
    },

    // can't use filter on objects
    _getPendingPages: function() {
        var ret = [];
        _.each(this.monitored_pages, function(value, key) {
            if (value.sync_status !== constants.sync_status.SYNCED) {
                ret.push(value);
            }
        });

        return ret;
    }
});

module.exports = MonitoredPageStore;
