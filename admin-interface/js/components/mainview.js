/** @jsx React.DOM */
var React = require('react');
var RootPageStore = require('../stores/rootpagestore');

var MainView = React.createClass({
    componentDidMount: function() {
        RootPageStore.addChangeListener(this._rootPagesUpdated);
    },

    getInitialState: function() {
        return { data: [] };
    },

    render: function() {
        var nodes = this.state.data.map(function (rp) {
            return(<li key={rp.url}>{rp.url}</li>);
        });

        return(
            <div id="main">
            <ul>
            {nodes}
            </ul>
            </div>
        );
    },

    _rootPagesUpdated: function() {
        data = RootPageStore.getAll();
        this.setState({data: data});
    }
});

module.exports = MainView;