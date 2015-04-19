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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by Such on 4/18/2015.
 */
public class LightModule extends CenterModule {
    public float value = 0.6f;

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

    @Override
    public View InflateUniversal(LayoutInflater inflater, ViewGroup root) {
        View view =  inflater.inflate(R.layout.item_control_light, root, false);

        SeekBar bar = (SeekBar)view.findViewById(R.id.seekBar);
        bar.setProgress((int) (value*100));
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                LightModule.this.value = progress / 100.0f;
                SetBrightness(1.0f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        return view;
    }

    public static byte [] float2ByteArray (float value)
    {
        return ByteBuffer.allocate(4).putFloat(value).array();
    }

    public void SetBrightness(float value) {
        if(connection != null)
            connection.Send(float2ByteArray(value*this.value));
    }

    @Override
    public void OnClick(ActionBarActivity context, ImageView view) {
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
            if(this.value == 0.0f)
                this.value = 0.6f;
            else
                this.value = 0.0f;
            SetBrightness(1.0f);
        }
    }
}
