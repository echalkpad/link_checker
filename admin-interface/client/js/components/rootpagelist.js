/** @jsx React.DOM */
var constants = require('../common/constants');
var React = require('react');
var RootPage = require('./rootpage');
var Fluxxor = require('fluxxor');
var url = require('url');
var _ = require('underscore');

var urlHostNameCompare = function(url1, url2) {
    var parsed1 = url.parse(url1.url).host.toLowerCase();
    var parsed2 = url.parse(url2.url).host.toLowerCase();

    if (parsed1 < parsed2) {
        return -1;
    } else if (parsed1 > parsed2) {
        return 1;
    } else {
        return 0;
    }
};

var FluxMixin = Fluxxor.FluxMixin(React);

var RootPageList = React.createClass({
    mixins: [FluxMixin],

    getInitialState: function() {
        return {
            new_url: "",
        };
    },

    render: function() {
        var sortedNodes = _.map(this.props.data, function(value, key) {
            return {url: value.url, status: value.status, sync_status: value.sync_status };
        }).sort(urlHostNameCompare);
        var new_url_status = this._validate_url(this.state.new_url);

        var nodes = sortedNodes.map(function (rp) {
            return(<RootPage rp={rp} key={rp.url} />);
        });

        return(
            <ul className="rootpagelist">
                {nodes}
                <li>
                    <input type="text" placeholder="Enter new URL..." value={this.state.new_url} onChange={this._onNewUrlChanged} />

                    <button onClick={this._onNew} disabled={!new_url_status.status}>Add New</button>
                    <span className="validation">{new_url_status.msg}</span>
                </li>
            </ul>
            );
    },

    _onNewUrlChanged: function(event) {
        this.setState({ new_url: event.target.value });
    },

    _validate_url: function(newUrl) {
        var parsedUrl = url.parse(newUrl);
        parsedUrl.path = '/';
        if (parsedUrl.protocol !== "http:" &&
            parsedUrl.protocol !== "https:") {
            return ({status: false, msg: "Must start with http or https"});
        }

        if (parsedUrl.hostname === null) {
            return ({status: false, msg: "Must specify a hostname"});
        }

        return ({status: true, msg: ""});
    },

    _onNew: function() {
        this.getFlux().actions.addMonitoredPage(this.state.new_url);
    }

});

module.exports = RootPageList;
