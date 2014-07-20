/** @jsx React.DOM */
var React = require('react');

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
        console.log("onDelete " + this.props.url);
    }
});

module.exports = RootPage;