/** @jsx React.DOM */
var constants = require('../common/constants.js');
var Fluxxor = require('fluxxor');
var LatestCrawl = require('./latestcrawl');
var React = require('react/addons');

var FluxMixin = Fluxxor.FluxMixin(React);

var RootPage = React.createClass({
    mixins: [FluxMixin],

    propTypes: {
        rp: React.PropTypes.object.isRequired
    },

    render: function() {
        var cx = React.addons.classSet;
        var classes = cx({
            "fa": true,
            "fa-caret-right": !this.props.rp.expanded,
            "fa-caret-down": this.props.rp.expanded,
            "fa-2x": true,
            "clickable": true
        });

        var latest_crawl = this._getLatestCrawl();

        return(
        <li>
            <div>
                <i className={classes} onClick={this._toggleExpanded}></i>
                <span>{this.props.rp.url}</span>
                <span>Status: {this._statusToString(this.props.rp.status)}</span>
                <span>Sync Status: {this._serverStatusToString(this.props.rp.sync_status)}</span>
                <button onClick={this._onDelete}>Delete</button>
            </div>
            {latest_crawl}
        </li>);
    },

    _getLatestCrawl: function() {
        if (this.props.rp.expanded) {
            return(<LatestCrawl report={this.props.crawl_report} />);
        }

        return [];
    },

    _onDelete: function() {
        this.getFlux().actions.deleteMonitoredPage(this.props.rp.url);
    },

    _toggleExpanded: function() {
        this.getFlux().actions.toggleExpandedStatus(this.props.rp.url);
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
