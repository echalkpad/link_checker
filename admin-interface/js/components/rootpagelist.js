/** @jsx React.DOM */
var React = require('react');
var RootPage = require('./rootpage');
var RootPageStore = require('../stores/rootpagestore');
var url = require('url');

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

var RootPageList = React.createClass({
    getInitialState: function() {
        return { new_url: "" }
    },

    render: function() {
        var sortedNodes = this.props.data.slice().sort(urlHostNameCompare);

        var nodes = sortedNodes.map(function (rp) {
            return(<RootPage key={rp.url} url={rp.url} />);
        });

        var parsedUrl = url.parse(this.state.new_url);
        parsedUrl.path = '/';
        if (parsedUrl.protocol === null) {
            console.log('protocol not found');
        }

        if (parsedUrl.hostname === null) {
            console.log('hostname not found');
        }

        return(
            <ul className="rootpagelist">
                {nodes}
                <li>
                    <input type="text" placeholder="Enter new URL..." value={this.state.new_url} onChange={this._onNewUrlChanged} />
                    <button onClick={this._onNew}>Add New</button>
                </li>
            </ul>
            );
    },

    _onNewUrlChanged: function(event) {
        this.setState({ new_url: event.target.value });
    },

    _onNew: function() {
        // TODO: Should not be directly calling into here
        RootPageStore.add({url: this.state.new_url });
    }

});

module.exports = RootPageList;