/** @jsx React.DOM */
var React = require('react/addons');
var RootPageList = require('./rootpagelist');
var Fluxxor = require('fluxxor');

var FluxMixin = Fluxxor.FluxMixin(React),
    StoreWatchMixin = Fluxxor.StoreWatchMixin;

var MainView = React.createClass({
    mixins: [FluxMixin, StoreWatchMixin("MonitoredPageStore", "CrawlReportStore")],

    getStateFromFlux: function() {
        var mp_store = this.getFlux().store("MonitoredPageStore");
        var crawl_store = this.getFlux().store("CrawlReportStore");

        return {
            monitored_pages: mp_store.monitored_pages,
            latest_crawls: crawl_store.latest_crawl_reports
        };
    },

    render: function() {
        return(
            <div id="main">
                <RootPageList
                    monitored_pages={this.state.monitored_pages}
                    latest_crawls={this.state.latest_crawls} />
            </div>
        );
    }
});

module.exports = MainView;
