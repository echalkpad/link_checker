/** @jsx React.DOM */
var React = require('react');
var RootPageStore = require('../stores/rootpagestore');

var RootPage = React.createClass({
    propTypes: {
        url: React.PropTypes.string.isRequired
    },

    render: function() {
        return(<li><span>{this.props.url}</span>
            <button onClick={this._onDelete}>Delete</button>
        </li>);
    },

    _onDelete: function() {
        // TODO: Use Dispatcher instead of direct call to store?
        RootPageStore.delete(this.props.url);
    }
});

module.exports = RootPage;