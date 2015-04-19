package com.example.such.smartcenter;

import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by Such on 4/18/2015.
 */
public class LightModule extends CenterModule {
    static public  LightModule Self;
    public LightModule() {
        Self = this;
    }

    static public class BluetoothConnection {
        private BluetoothGatt bluetoothGatt;

        public BluetoothConnection(BluetoothDevice device, Context context) {
            bluetoothGatt = device.connectGatt(context, false, btleGattCallback);
        }

        public void Send(byte[] value) {
            List<BluetoothGattService> services = bluetoothGatt.getServices();
            for (BluetoothGattService service : services) {
                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                for(BluetoothGattCharacteristic c : characteristics) {
                    c.setValue(value);
                    bluetoothGatt.writeCharacteristic(c);
                    break;
                }
            }
        }

        private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
                // this will get called anytime you perform a read or write characteristic operation

            }

            @Override
            public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
                // this will get called when a device connects or disconnects
                bluetoothGatt.discoverServices();
            }

            @Override
            public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {

            }
        };
    }
    public BluetoothConnection connection;
    @Override
    public int GetResourceId() {
        return R.drawable.ic_light;
    }

    public static byte [] float2ByteArray (float value)
    {
        return ByteBuffer.allocate(4).putFloat(value).array();
    }

    public void SetBrightness(float value) {
        connection.Send(float2ByteArray(value));
    }

    @Override
    public void OnClick(ActionBarActivity context) {
        if(connection == null) {
            LeDevicesFragment fragment = new LeDevicesFragment();
            fragment.module = this;
            context.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container,fragment )
                    .addToBackStack(null)
                    .commit();
        }
        else {
            float value = 0.8f;
            SetBrightness(value);
        }
    }
}
