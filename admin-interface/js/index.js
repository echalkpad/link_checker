/** @jsx React.DOM */
var Header = require('./components/header');
var MainView = require('./components/mainview');
var React = require('react');
var RootPageStore = require('./stores/rootpagestore');
var Sidebar = require('./components/sidebar');

RootPageStore.startSync();

var App = React.createClass({
  render: function() {
    return <div><Header /><Sidebar /><MainView /></div>;
  }
});

React.renderComponent(<App />, document.getElementById('container'));
