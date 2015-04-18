package com.example.such.smartcenter;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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


public class LeDevicesFragment extends ListFragment implements AbsListView.OnItemClickListener, View.OnClickListener {
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private static final int REQUEST_ENABLE_BT = 5;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onClick(View v) {
        scanLeDevice(true);
    }

    private void scanLeDevice(final boolean enable) {
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
            textView.setText(getItem(position).getName());

            return rowView;
        }
    }
    private LeDevicesAdapter mAdapter;
    private AbsListView mListView;


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
        mAdapter = new LeDevicesAdapter((getActivity()));
        // TODO: Change Adapter to display your content
        setListAdapter(mAdapter);

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
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        mListView.setOnItemClickListener(this);
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
                            mAdapter.addDevice(device);
                            mAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_ENABLE_BT) {

        }
    }
}
