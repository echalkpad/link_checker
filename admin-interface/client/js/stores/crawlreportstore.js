var constants = require('../common/constants');
var Fluxxor = require('fluxxor');

var CrawlReportStore = Fluxxor.createStore({
        initialize: function() {
            this.latest_crawl_reports = {};
            this.bindActions(
                constants.UPDATE_LATEST_CRAWL_REPORT, this.onUpdateLatestCrawlReport
            );
        },

        onUpdateLatestCrawlReport: function(payload) {
            this.latest_crawl_reports[payload.url] = payload.links;
            this.emit('change');
        }
});

module.exports = CrawlReportStore;
