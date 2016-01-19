var express = require('express');
var cors = require('cors');

var env = process.env.NODE_ENV = process.env.NODE_ENV || 'development';

var app = express();
var path = require('path');
var favicon = require('serve-favicon');

var bodyParser = require('body-parser');
// var config = require('./server/config/config')[env];
var port = 9000;
// require('./server/config/express')(app, config);

// require('./server/config/mongoose')(config);

// require('./server/config/passport')();

// require('./server/config/router')(app);

app.use(cors());
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));


if (env === 'development') {
	app.use(express.static('.tmp'));
	app.use(express.static('app'));
	// app.use(favicon(__dirname + '/app/favicon.ico'));
} else if (env === 'production') {
	app.use(express.static('dist'));
	// app.use(favicon(__dirname + '/dist/images/favicon.ico'));
}

app.get('/bower_components/*', function(req, res) {
	res.sendFile(__dirname + '/bower_components/' + req.params[0]);
})

app.listen(port);
console.log('Listening on port ' + port + '...');