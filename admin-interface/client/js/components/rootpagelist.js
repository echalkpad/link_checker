/** @jsx React.DOM */
var constants = require('../common/constants');
var React = require('react/addons');
var RootPage = require('./rootpage');
var Fluxxor = require('fluxxor');
var url = require('url');
var _ = require('underscore');

var monitoredPageCompareKey = function(mp) {
    var parsed = url.parse(mp.url);
    return parsed.host.toLowerCase() + '/' + parsed.hostname.toLowerCase();
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
        // The data comes in right now as key: value pairs (key is the url). We
        // just need an ordered array.
        var sortedNodes = _.sortBy(_.values(this.props.monitored_pages), monitoredPageCompareKey);
        var new_url_status = this._validate_url(this.state.new_url);
        var self = this;

        var nodes = sortedNodes.map(function (rp) {
            var crawl_report = self.props.latest_crawls.hasOwnProperty(rp.url) ?
                                    self.props.latest_crawls[rp.url] :
                                    null;
            return(<RootPage rp={rp} key={rp.url} crawl_report={crawl_report}/>);
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
