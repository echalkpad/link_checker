var constants = require('./common/constants');

module.exports = {
    /**
     * Add a new monitored page with the given fields
     * @param payload fields
     */
    addMonitoredPage: function(url) {
        this.dispatch(constants.ADD_MONITORED_PAGE, {url: url});
    },

    /**
     * Delete a monitored page with the given URL
     * @param url URL to delete
     */
    deleteMonitoredPage: function(url) {
        this.dispatch(constants.DELETE_MONITORED_PAGE, {url: url});
    },

    updateMonitoredPages: function(payload) {
        this.dispatch(constants.UPDATE_MONITORED_PAGES_FROM_SERVER, payload);
    },

    addSynced: function(url) {
        this.dispatch(constants.UPDATE_SERVER_SYNC_STATUS, {url: url, op: constants.ADD_MONITORED_PAGE});
    },

    deleteSynced: function(url) {
        this.dispatch(constants.UPDATE_SERVER_SYNC_STATUS, {url: url, op: constants.DELETE_MONITORED_PAGE});
    },

    toggleExpandedStatus: function(url) {
        this.dispatch(constants.TOGGLE_MONITORED_PAGE_EXPANDED_VIEW, {url: url});
    },

    updateCrawlReport: function(payload) {
        this.dispatch(constants.UPDATE_LATEST_CRAWL_REPORT, payload);
    }
};
