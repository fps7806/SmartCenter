var tessel = require('tessel');
var blelib = require('ble-ble113a');

var ble = blelib.use(tessel.port['A']);

ble.on('ready', function(err) {
  console.log('Advertising...');
  ble.startAdvertising();
});

ble.on('connect', function(master) {
  console.log('connected...');
  
});

ble.on( 'remoteWrite', function(connection, index, valueWritten) {
	console.log(valueWritten);
  } );

ble.on('disconnect', function(master) {
  console.log('disconnect...');
  console.log('Advertising...');
  ble.startAdvertising();
});