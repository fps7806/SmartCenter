package com.example.such.smartcenter;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.support.v4.app.ListFragment;
import android.widget.TextView;

import java.io.Console;
import java.util.List;

import static android.bluetooth.le.ScanSettings.CALLBACK_TYPE_ALL_MATCHES;


public class LeDevicesFragment extends ListFragment implements View.OnClickListener {
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private BluetoothGatt bluetoothGatt;
    public LightModule module;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 100000;
    private static final int REQUEST_ENABLE_BT = 5;

    @Override
    public void onClick(View v) {
        mHandler = new Handler();
        scanLeDevice(true);
    }

    private void scanLeDevice(final boolean enable) {
        if(mScanning == false) {
            if (enable) {
                // Stops scanning after a pre-defined scan period.
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mScanning = false;
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    }
                }, SCAN_PERIOD);

                mScanning = true;
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                mScanning = false;
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Log.d("Smart", "onItemClick");
        BluetoothDevice device = mAdapter.getItem(position);
        module.connection = new LightModule.BluetoothConnection(device, getActivity());
        getActivity().getSupportFragmentManager().popBackStack();
    }

    public class LeDevicesAdapter extends ArrayAdapter<BluetoothDevice> {
        private final Context context;
        public LeDevicesAdapter(Context context) {
            super(context, R.layout.item_le_device);
            this.context = context;
        }

        public void addDevice(BluetoothDevice device) {
            this.add(device);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.item_le_device, parent, false);
            TextView textView = (TextView) rowView.findViewById(R.id.device_id);
            BluetoothDevice device = getItem(position);
            textView.setText(device.getName());

            return rowView;
        }
    }
    private LeDevicesAdapter mAdapter;
    private ListView mListView;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LeDevicesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
        }

        BluetoothManager btManager = (BluetoothManager)getActivity().getSystemService(Context.BLUETOOTH_SERVICE);

        mBluetoothAdapter = btManager.getAdapter();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_le_devices_list, container, false);
        Button find = (Button)view.findViewById(R.id.device_find);
        find.setOnClickListener(this);
        // Set the adapter
        mListView = (ListView) view.findViewById(android.R.id.list);
        mAdapter = new LeDevicesAdapter((getActivity()));
        mListView.setAdapter(mAdapter);
        return view;
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (device.getName() != null) {
                                mAdapter.addDevice(device);
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }

            };
}
