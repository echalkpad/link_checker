/** @jsx React.DOM */
var React = require('react')

var HelloWorld = React.createClass({
  render: function() {
    return <div>If you can see this, browserify and react are working.</div>
  }
});

React.renderComponent(<HelloWorld />, document.getElementById('container'));
