/** @jsx React.DOM */
var constants = require('../common/constants.js');
var Fluxxor = require('fluxxor');
var React = require('react');

var FluxMixin = Fluxxor.FluxMixin(React);

var RootPage = React.createClass({
    mixins: [FluxMixin],

    propTypes: {
        url: React.PropTypes.string.isRequired
    },

    render: function() {
        return(<li><span>{this.props.url}</span>
            <button onClick={this._onDelete}>Delete</button>
        </li>);
    },

    _onDelete: function() {
        this.getFlux().dispatcher.dispatch({type: constants.DELETE_MONITORED_PAGE, payload: {url: this.props.url}});
    }
});

module.exports = RootPage;