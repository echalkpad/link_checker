/** @jsx React.DOM */
var React = require('react');

var Header = React.createClass({
    render: function() {
        return(
            <header id="header">
                Link Checker
            </header>
        );
    }
});

module.exports = Header;