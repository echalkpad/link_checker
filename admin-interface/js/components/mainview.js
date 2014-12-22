/** @jsx React.DOM */
var React = require('react');
var RootPageList = require('./rootpagelist');
var Fluxxor = require('fluxxor');

var FluxMixin = Fluxxor.FluxMixin(React),
    StoreWatchMixin = Fluxxor.StoreWatchMixin;

var MainView = React.createClass({
    mixins: [FluxMixin, StoreWatchMixin("MonitoredPageStore")],

    getStateFromFlux: function() {
        var store = this.getFlux().store("MonitoredPageStore");
        return {
            data: store.monitored_pages
        };
    },

    render: function() {
        return(
            <div id="main">
                <RootPageList data={this.state.data} />
            </div>
        );
    }
});

module.exports = MainView;