/** @jsx React.DOM */
var React = require('react/addons');

var LatestCrawl = React.createClass({
    render: function() {
        if (this.props.report === null) {
            return (<div><p>Retrieving from server...</p></div>);
        }

        var rows = this.props.report.map(function (r) {
            var classname = r.statusCode == 200 ? 'ok' : 'error';

            return(
                <tr className={classname} key={r.url}>
                    <td>{r.url}</td>
                    <td>{r.anchorText}</td>
                    <td>{r.statusCode}</td>
                </tr>);
        });

        return (<div>
                    <table className="crawlReport">
                        <tr><th>URL</th><th>Anchor Text</th><th>Status</th></tr>
                        {rows}
                    </table>
                </div>);
    }
});

module.exports = LatestCrawl;
