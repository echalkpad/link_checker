/** @jsx React.DOM */
var constants = require('../common/constants.js');
var Fluxxor = require('fluxxor');
var React = require('react');

var FluxMixin = Fluxxor.FluxMixin(React);

var RootPage = React.createClass({
    mixins: [FluxMixin],

    propTypes: {
        rp: React.PropTypes.object.isRequired
    },

    render: function() {
        return(
        <li>
            <span>{this.props.rp.url}</span>
            <span>Status: {this._statusToString(this.props.rp.status)}</span>
            <span>Sync Status: {this._serverStatusToString(this.props.rp.sync_status)}</span>
            <button onClick={this._onDelete}>Delete</button>
        </li>);
    },

    _onDelete: function() {
        this.getFlux().actions.deleteMonitoredPage(this.props.rp.url);
    },

    _statusToString: function(status) {
        switch(status) {
            case constants.status.ERROR:
                return "ERROR";
            case constants.status.GOOD:
                return "GOOD";
            case constants.status.UNKNOWN:
                return "UNKNOWN";
            default:
                return status;
        }
    },

    _serverStatusToString: function(status) {
        switch(status) {
            case constants.sync_status.ADD_SYNCING:
            case constants.sync_status.DELETE_SYNCING:
                return "SYNCING";
            case constants.sync_status.SYNCED:
                return "SYNCED";
            default:
                return status;
        }
    }
});

module.exports = RootPage;
