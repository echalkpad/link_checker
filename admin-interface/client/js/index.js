/** @jsx React.DOM */
var actions = require('./actions');
var Header = require('./components/header');
var MainView = require('./components/mainview');
var CrawlReportStore = require('./stores/crawlreportstore');
var MonitoredPageStore = require('./stores/monitoredpagestore');
var React = require('react/addons');
var Sidebar = require('./components/sidebar');
var Syncer = require('./api_client/syncer');
var Fluxxor = require('fluxxor');
var _ = require('underscore');

var FluxMixin = Fluxxor.FluxMixin(React),
    StoreWatchMixin = Fluxxor.StoreWatchMixin;

var stores = {
    'CrawlReportStore': new CrawlReportStore(),
    'MonitoredPageStore': new MonitoredPageStore()
};

var flux = new Fluxxor.Flux(stores, actions);
var syncer = new Syncer(flux);

/* DEVONLY */
flux.on("dispatch", function(type, payload) {
    if (console && console.log) {
        console.log("[Dispatch]", type, payload);
    }
});
/* ENDDEVONLY */


var App = React.createClass({
    mixins: [FluxMixin],

    render: function() {
        return <div><Header /><Sidebar /><MainView /></div>;
    }
});

React.render(
    <App flux={flux} />,
    document.getElementById('container')
);

setInterval(function() { syncer.updateMonitoredPages(); }, 5000);

setInterval(function updateExpandedCrawls() {
    var expandedCrawls = stores.MonitoredPageStore.getExpandedPages();
    expandedCrawls.forEach(function(crawl) {
        setTimeout(function() {
            syncer.getCrawlStatusForUrl(crawl.url);
        }, Math.random() * 5000);
    });
}, 10000);
