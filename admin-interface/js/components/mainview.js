/** @jsx React.DOM */
var React = require('react');
var RootPageStore = require('../stores/rootpagestore');
var RootPageList = require('./rootpagelist');

var MainView = React.createClass({
    componentDidMount: function() {
        RootPageStore.addChangeListener(this._rootPagesUpdated);
    },

    componentWillUnmount: function() {
        RootPageStore.removeChangeListener(this._rootPagesUpdated);
    },

    getInitialState: function() {
        return { data: [] };
    },

    render: function() {
        return(
            <div id="main">
                <RootPageList data={this.state.data} />
            </div>
        );
    },

    _rootPagesUpdated: function() {
        data = RootPageStore.getAll();
        this.setState({data: data});
    },

    _onDelete: function() {
        console.log("onDelete");
    }
});

module.exports = MainView;