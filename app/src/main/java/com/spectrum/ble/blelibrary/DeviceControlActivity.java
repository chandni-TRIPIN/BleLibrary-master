package com.spectrum.ble.blelibrary;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import com.spectrum.ble.BluetoothLeConfig;
import com.spectrum.ble.GattListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DeviceControlActivity extends Activity {
    private final static String TAG = "BLE";

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private Button mIdentify;
    private Button mCertificate;

    private TextView mConnectionState;
    private TextView mDataField;
    //private String mDeviceName;
    //private String mDeviceAddress;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothLeConfig mBluetoothLeConfig;
    private ExpandableListView mGattServicesList;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private BluetoothGattCharacteristic mCharIdentify;
    private BluetoothGattCharacteristic mCharAuthentication;
    private BluetoothGattCharacteristic mCharCommandTx;


    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.


    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    if (mGattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic =
                                mGattCharacteristics.get(groupPosition).get(childPosition);
                        mBluetoothLeConfig.setCharacteristicNotification(characteristic);
                        return true;
                    }
                    return false;
                }
    };

    private void clearUI() {
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText("No data");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);

        final Intent intent = getIntent();
        //mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        String address = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        Log.i("Test", address);
        mBluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
        Log.i("Test", mBluetoothDevice.getName());

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mBluetoothDevice.getAddress());
        mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        mGattServicesList.setOnChildClickListener(servicesListClickListner);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data_value);

        mIdentify =  findViewById(R.id.identify);
        mCertificate =  findViewById(R.id.read_cart);


        //getActionBar().setTitle(mBluetoothDevice.getName());
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        mBluetoothLeConfig =  new BluetoothLeConfig(this, mBluetoothDevice, mGattListener);
        mBluetoothLeConfig.bindService();
        mIdentify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCharIdentify.setValue(new byte[]{(byte) 0x01});
                boolean status = mBluetoothLeConfig.mBluetoothLeService.writeCharacteristic(mCharIdentify);
                Log.v(TAG, "enableIndication state : " + status);
                pairDevice(mBluetoothDevice);
            }
        });

        mCertificate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCharCommandTx.setValue(new byte[]{(byte) 0x60, (byte) 0x00});
                boolean status = mBluetoothLeConfig.mBluetoothLeService.writeCharacteristic(mCharCommandTx);
                Log.v(TAG, "mCharCommandTx  0x6000 state : " + status);
            }
        });
    }

    public void pairDevice(BluetoothDevice device)
    {
        String ACTION_PAIRING_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST";
        Intent intent = new Intent(ACTION_PAIRING_REQUEST);
        String EXTRA_DEVICE = "android.bluetooth.device.extra.DEVICE";
        intent.putExtra(EXTRA_DEVICE, device);
        String EXTRA_PAIRING_VARIANT = "android.bluetooth.device.extra.PAIRING_VARIANT";
        int PAIRING_VARIANT_PIN = 1234;
        intent.putExtra(EXTRA_PAIRING_VARIANT, PAIRING_VARIANT_PIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private GattListener mGattListener = new GattListener() {
        @Override
        public void onDeviceConnected() {
            Log.i(TAG,"Connected");
            updateConnectionState("Connected");
        }

        @Override
        public void onDeviceDisconnected() {
            Log.i(TAG,"Disonnected");
            updateConnectionState("Disconnected");
            clearUI();
        }

        @Override
        public void onServiceDiscovered(List<BluetoothGattService> services) {
            Log.i(TAG,"Services Discovered");
            displayGattServices(services);
        }

        @Override
        public void onAdvDataDiscovered(byte[] characteristic, String data) {
            Log.i("Test","Characteristic: " + data);
            displayData(data);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mBluetoothLeConfig.registerReceiver();
        if (mBluetoothLeConfig.bluetoothLeServiceIsActive()) {
            final boolean result = mBluetoothLeConfig.connect();
            Log.d(TAG, "Connect request result=" + result);
        }

        final IntentFilter pairingRequestFilter = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
        pairingRequestFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY - 1);
        registerReceiver(mPairingRequestRecevier, pairingRequestFilter);

    }

    private final BroadcastReceiver mPairingRequestRecevier = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(intent.getAction()))
            {
                final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int type = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, BluetoothDevice.ERROR);

                if (type == BluetoothDevice.PAIRING_VARIANT_PIN)
                {
                    device.setPin(new byte[]{(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00});
                    Log.v(TAG , "Device Pair: " + type);
                    abortBroadcast();
                }
                else
                {
                    Log.v(TAG , "Unexpected pairing type: " + type);
                }
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        mBluetoothLeConfig.unregisterReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothLeConfig.unbindService();
        mBluetoothLeConfig.endBluetoothLeService();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeConfig.connect();
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeConfig.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final String resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    private void displayData(String data) {
        if (data != null) {
            mDataField.setText(data);
        }
    }


    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = "Unknown Service";
        String unknownCharaString = "Unknown Characteristic";
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<>();
        mGattCharacteristics = new ArrayList<>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, unknownServiceString);
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);
            Log.v(TAG, "Service UUID : " + uuid);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                Log.v(TAG, "Charactristics : " + uuid);

                if(uuid.equalsIgnoreCase("4D050081-766C-42C4-8944-42BC98FC2D09")) {
                    mCharIdentify = gattCharacteristic;
                    boolean statusNotifiaction = mBluetoothLeConfig.mBluetoothLeService.
                            enableIndication(mCharIdentify, true);
                    Log.v(TAG, "Enable Notification : " + statusNotifiaction);

                    gattCharacteristic.setValue(new byte[]{(byte) 0x01});
                    gattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    boolean status = mBluetoothLeConfig.mBluetoothLeService.writeCharacteristic(gattCharacteristic);

                    Log.v(TAG, "Write Char Status : " + status);
                }

                if(uuid.equalsIgnoreCase("4D050082-766C-42C4-8944-42BC98FC2D09")) {
                    mCharAuthentication = gattCharacteristic;
                    Log.v(TAG, "Authentication char initialize");
                }

                if(uuid.equalsIgnoreCase("4D050017-766C-42C4-8944-42BC98FC2D09")) {
                    mCharCommandTx = gattCharacteristic;
                    Log.v(TAG, "CommandTx char initialize");
                }

                currentCharaData.put(
                        LIST_NAME, unknownCharaString);
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );
        mGattServicesList.setAdapter(gattServiceAdapter);
    }
}
