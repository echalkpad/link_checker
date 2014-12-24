var config = require('./config');
var compression = require('compression');
var express = require('express');
var fs = require('fs');
var hogan = require('hogan.js');
var morgan = require('morgan');
var httpProxy = require('http-proxy');


var app = express();

if (config.baseURL == null) {
    throw new Error("Must specify base URL in config!");
} else {
    console.log("Will proxy API requests to " + config.baseURL);
}

var proxy = httpProxy.createProxyServer({
    target: config.baseURL,
    xfwd: true
});

// TODO: Add request-id middleware

var forwardApi = function(req, res) {
    req.url = req.originalUrl;
    proxy.web(req, res, {}, function onError(err, req, res) {
        res
        .status(500)
        .set('Content-Type', 'application/json')
        .send({success: false, description: "Proxy error: " + err});
    });
};

app.use(compression());
app.use(morgan('short'));

app.use('/api/v1', forwardApi);

if (config.isProduction()) {
    var index = fs.readFileSync('client/dist/index.html', 'utf8');
    var compiledIndex = hogan.compile(index);
    var renderedIndex = compiledIndex.render({ CDN_SERVER: config.cdnServer });
    var serveIndex = function(req, res) {
        res.send(renderedIndex);
    };

    app.get('/', serveIndex);
    app.get('/index.html', serveIndex);

    app.use('/js', express.static('client/dist/js', {maxAge: '1 year'} ));
    app.use('/css', express.static('client/dist/css', {maxAge: '1 year'} ));
    app.use(express.static('client/dist'));

} else {
    app.use(express.static('client'));
}

app.use(function(req, res, next){
  res.status(404).send('Sorry, can\'t find that!');
});

var server = app.listen(config.port, function() {
    console.log('Express is alive! Listening on port ' + config.port);
});
