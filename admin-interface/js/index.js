/** @jsx React.DOM */
var React = require('react')

var HelloWorld = React.createClass({
  render: function() {
    return <div><p>If you can see this, browserify and react are working.</p><p>abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz</p></div>
  }
});

React.renderComponent(<HelloWorld />, document.getElementById('container'));
