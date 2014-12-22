/** @jsx React.DOM */
var Header = require('./components/header');
var MainView = require('./components/mainview');
var MonitoredPageStore = require('./stores/monitoredpagestore');
var React = require('react');
var Sidebar = require('./components/sidebar');
var Fluxxor = require('fluxxor');

var FluxMixin = Fluxxor.FluxMixin(React),
    StoreWatchMixin = Fluxxor.StoreWatchMixin;

var stores = {
    'MonitoredPageStore': new MonitoredPageStore()
};

var actions = {

};

var flux = new Fluxxor.Flux(stores, actions);

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

React.render(<App flux={flux}/>, document.getElementById('container'));
