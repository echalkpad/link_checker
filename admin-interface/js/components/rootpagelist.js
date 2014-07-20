/** @jsx React.DOM */
var React = require('react');
var RootPage = require('./rootpage');
var RootPageStore = require('../stores/rootpagestore');
var url = require('url');

var RootPageList = React.createClass({
    getInitialState: function() {
        return { new_url: "" }
    },

    render: function() {
        var nodes = this.props.data.map(function (rp) {
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