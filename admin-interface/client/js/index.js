/** @jsx React.DOM */
var constants = require('./common/constants');
var Header = require('./components/header');
var MainView = require('./components/mainview');
var MonitoredPageStore = require('./stores/monitoredpagestore');
var React = require('react');
var Sidebar = require('./components/sidebar');
var Syncer = require('./api_client/syncer');
var Fluxxor = require('fluxxor');
var _ = require('underscore');


var FluxMixin = Fluxxor.FluxMixin(React),
    StoreWatchMixin = Fluxxor.StoreWatchMixin;

var stores = {
    'MonitoredPageStore': new MonitoredPageStore()
};

var actions = {
    /**
     * Add a new monitored page with the given fields
     * @param payload fields
     */
    addMonitoredPage: function(payload) {
        var sync_id = _.uniqueId();
        payload.sync_id = sync_id;

        this.dispatch({type: constants.ADD_MONITORED_PAGE, payload: payload});
    },

    /**
     * Delete a monitored page with the given URL
     * @param url URL to delete
     */
    deleteMonitoredPage: function(url) {
        this.dispatch({type: constants.DELETE_MONITORED_PAGE, payload: {url: url}});
    }
};

var flux = new Fluxxor.Flux(stores, actions);

/* DEVONLY */
flux.on("dispatch", function(type, payload) {
    if (console && console.log) {
        console.log("[Dispatch]", type, payload);
    }
});
/* ENDDEVONLY */


var App = React.createClass({
    mixins: [FluxMixin, StoreWatchMixin("MonitoredPageStore")],

    getStateFromFlux: function() {
        var flux = this.getFlux();

        return {
            monitoredPageData: flux.store("MonitoredPageStore")
        };
    },

    render: function() {
        return <div><Header /><Sidebar /><MainView /></div>;
    }
});

React.render(
    <App flux={flux} />,
    document.getElementById('container')
);

setInterval(Syncer.updateMonitoredPages(), 5000);
