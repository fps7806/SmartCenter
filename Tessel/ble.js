var tessel = require('tessel');
var blelib = require('ble-ble113a');
var myPin = tessel.port['GPIO'].analog[0];

var value = 1.0;
setInterval(function readPin () {
  myPin.write(value);
}, 10);

var ble = blelib.use(tessel.port['A']);

ble.on('ready', function(err) {
  console.log('Advertising...');
  ble.startAdvertising();
});

ble.on('connect', function(master) {
  console.log('connected...');
  
});

ble.on( 'remoteWrite', function(connection, index, valueWritten) {
	value = valueWritten.readFloatBE(0);
	console.log(value);
  } );

ble.on('disconnect', function(master) {
  console.log('disconnect...');
  console.log('Advertising...');
  ble.startAdvertising();
});