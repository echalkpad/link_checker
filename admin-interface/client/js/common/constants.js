module.exports = {
    // Actions triggered by UI

    // MonitoredPage Actions
    // ADD_MONITORED_PAGE. Payload: {url: url to add}
    ADD_MONITORED_PAGE: "ADD_MONITORED_PAGE",

    // DELETE_MONITORED_PAGE. Payload: {url: url to delete}
    DELETE_MONITORED_PAGE: "DELETE_MONITORED_PAGE",

    // Server syncer has updated server status.
    //Payload: {url: url, op: original action (eg: ADD_MONITORED_PAGE)}
    UPDATE_SERVER_SYNC_STATUS: "UPDATE_SERVER_SYNC_STATUS",

    // Server has sent a new list of monitored pages, send these
    // into the store.
    // Payload: [{url, status} tuples]
    UPDATE_MONITORED_PAGES_FROM_SERVER: "UPDATE_MONITORED_PAGES",

    // MonitoredPage statuses and sync_statuses
    // XXX probably doesn't need to be part of constants.js
    status: {
        UNKNOWN: 0,
        GOOD: 1,
        ERROR: 2
    },

    sync_status: {
        ADD_SYNCING: 0,
        DELETE_SYNCING: 1,
        SYNCED: 1
    }
};
