var config = require('./config');
var compression = require('compression')
var express = require('express');
var morgan = require('morgan');

var app = express();

app.use(compression());
app.use(morgan('short'));
app.use(express.static('client'));

app.use(function(req, res, next){
  res.status(404).send('Sorry, can\'t find that!');
});

var server = app.listen(config.port, function() {
    console.log('Express is alive! Listening on port ' + config.port);
});
