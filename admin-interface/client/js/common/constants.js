module.exports = {
    // Actions triggered by UI

    // ADD_MONITORED_PAGE. Payload: {url: url to add}
    ADD_MONITORED_PAGE: "ADD_MONITORED_PAGE",

    // DELETE_MONITORED_PAGE. Payload: {url: url to delete}
    DELETE_MONITORED_PAGE: "DELETE_MONITORED_PAGE",

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
