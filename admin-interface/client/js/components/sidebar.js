/** @jsx React.DOM */
var React = require('react/addons');

var Sidebar = React.createClass({
    render: function() {
        return(
            <div id="sidebar">
                <ul>
                    <li>Root Pages</li>
                </ul>
            </div>
            );
    }
});

module.exports = Sidebar;
