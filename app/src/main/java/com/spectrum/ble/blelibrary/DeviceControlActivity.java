package com.spectrum.ble.blelibrary;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DeviceControlActivity extends Activity {
    private final static String TAG = "BLE";

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRA_BLUETOOTH_DEVICE = "DEVICE_ADDRESS";

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
        mBluetoothDevice = getBTDeviceExtra();
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
                Log.v(TAG, "Writing 01 to identify state : " + status);

                boolean statusNotifiaction = mBluetoothLeConfig.mBluetoothLeService.
                        enableIndication(mCharAuthentication, true);
                Log.v(TAG, "Enable Notification 4D050082 : " + statusNotifiaction);

                boolean statusReadAuth = mBluetoothLeConfig.mBluetoothLeService.
                        readCharacteristic(mCharAuthentication);
                Log.v(TAG, "Read Authentication characteristics status : " + statusReadAuth);
                boolean bountStatus = mBluetoothDevice.createBond();

                Log.v(TAG, "Device bound  status : " + bountStatus + " Bound Status : " + mBluetoothDevice.getBondState());

//                boolean statusReadAuth1 = mBluetoothLeConfig.mBluetoothLeService.
//                        readCharacteristic(mCharAuthentication);
//                Log.v(TAG, "Read Authentication characteristics status : " + statusReadAuth1);

//                BluetoothGattDescriptor descriptor =
//                        mCharAuthentication.getDescriptor(UUID.fromString("4D050082-766C-42C4-8944-42BC98FC2D09"));
//
//                descriptor.setValue(
//                        BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//                mBluetoothLeConfig.mBluetoothLeService.writeDescripter(descriptor);

//                pairDevice(mBluetoothDevice);
//                byte[] command = {0x60, 0x00};
//                mCharCommandTx.setValue(command);
//                mCharCommandTx.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
//                boolean statusCommandTx = mBluetoothLeConfig.mBluetoothLeService.writeCharacteristic(mCharCommandTx);
//              Log.v(TAG, "Write 6000 4D050017 : " + statusCommandTx);

            }
        });

        mCertificate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean statusNotifiaction = mBluetoothLeConfig.mBluetoothLeService.
                        enableIndication(mCharAuthentication, true);
                Log.v(TAG, "Read Auth Char After Write : " + statusNotifiaction);

                mCharAuthentication.setValue(new byte[]{(byte) 0x60,(byte) 0x00});
//                mCharCommandTx.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                boolean statusCommandTx = mBluetoothLeConfig.mBluetoothLeService.writeCharacteristic(mCharAuthentication);
                Log.v(TAG, "Write 6000 into 4D050017 : " + statusCommandTx);
                if(statusCommandTx) {

                    boolean statusReadAuth = mBluetoothLeConfig.mBluetoothLeService.
                            readCharacteristic(mCharAuthentication);
                    Log.v(TAG, "Read Auth Char After Write : " + statusReadAuth);

                }
            }
        });
        }


//    public void pairDevice(BluetoothDevice device)
//    {
//        String ACTION_PAIRING_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST";
//        Intent intent = new Intent(ACTION_PAIRING_REQUEST);
//        String EXTRA_DEVICE = "android.bluetooth.device.extra.DEVICE";
//        intent.putExtra(EXTRA_DEVICE, device);
//        String EXTRA_PAIRING_VARIANT = "android.bluetooth.device.extra.PAIRING_VARIANT";
//        int PAIRING_VARIANT_PIN = 1234;
//        intent.putExtra(EXTRA_PAIRING_VARIANT, PAIRING_VARIANT_PIN);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
//    }

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
            Log.i("Test","Characteristic Raw: " + characteristic);

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

//        final IntentFilter pairingRequestFilter = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
//        pairingRequestFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY - 1);
//        registerReceiver(mPairingRequestRecevier, pairingRequestFilter);

        IntentFilter intent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mPairReceiver, intent);

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
                Log.v(TAG, "Charactristics : " + uuid  + " Value : " + gattCharacteristic.getValue() + " Properties " + gattCharacteristic.getProperties());

                if(uuid.equalsIgnoreCase("4D050081-766C-42C4-8944-42BC98FC2D09")) {
                    mCharIdentify = gattCharacteristic;
                    boolean statusNotifiaction = mBluetoothLeConfig.mBluetoothLeService.
                            enableIndication(mCharIdentify, true);
                    Log.v(TAG, "Enable Notification : " + statusNotifiaction);

//                    mCharIdentify.setValue(new byte[]{(byte) 0x01});
//                    mCharIdentify.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
//                    boolean status = mBluetoothLeConfig.mBluetoothLeService.writeCharacteristic(mCharIdentify);

//                    Log.v(TAG, "Write Char Status : " + status);
                }

                if(uuid.equalsIgnoreCase("0000fff4-0000-1000-8000-00805f9b34fb")) {
                    mCharAuthentication = gattCharacteristic;

                    List<BluetoothGattDescriptor> listDesc = mCharAuthentication.getDescriptors();
                    for(BluetoothGattDescriptor tempdesc : listDesc) {
                        Log.v(TAG, "BluetoothGattDescriptor UUID : " + tempdesc.getUuid() + " Value : " + tempdesc.getValue());
                        tempdesc.setValue(
                                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

                    }
                    Log.v(TAG, "Authentication char initialize : Value : " + mCharAuthentication.getValue());
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

    private final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state        = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState    = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    Log.v(TAG, "Device is Paired : " + state + " Bount State : " + prevState);
//                    boolean statusNotifiaction = mBluetoothLeConfig.mBluetoothLeService.
//                            enableIndication(mCharAuthentication, true);
//                    Log.v(TAG, "Enable Notification 4D050082 : " + statusNotifiaction);

//                    byte[] commandCertificate = {(byte)0x60, (byte)0x00};
//                    mCharCommandTx.setValue(commandCertificate);
////                    mCharCommandTx.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
//                    boolean statusCertificate = mBluetoothLeConfig.mBluetoothLeService.writeCharacteristic(mCharCommandTx);
//                    Log.v(TAG, "Write commandCertificate into  4D050017 : " + statusCertificate);

//                    byte[] commandChallenge = {(byte)61,(byte)00};
//                    mCharCommandTx.setValue(commandChallenge);
//                    mCharCommandTx.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
//                    boolean statusChallenge = mBluetoothLeConfig.mBluetoothLeService.writeCharacteristic(mCharCommandTx);
//                    Log.v(TAG, "Write commandChallenge into  4D050017 : " + statusChallenge);
//
//
//                    byte[] commandSignatureSend = {0x63,0x20 ,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0A,0x0B,0x0C,0x0D,0x0E,0x0F,0x10,0x11,0x21,0x31,0x41,0x51,0x61,0x71,(byte) 0x81,(byte)0x91,(byte)0xA1,(byte)0xB1,(byte)0xC1,(byte)0xD1,(byte)0xE1,(byte)0xF2,0x0};
//                    mCharCommandTx.setValue(commandSignatureSend);
//                    mCharCommandTx.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
//                    boolean statusSignatureSend = mBluetoothLeConfig.mBluetoothLeService.writeCharacteristic(mCharCommandTx);
//                    Log.v(TAG, "Write commandSignature into  4D050017 : " + statusSignatureSend);
//
//
//                    byte[] commandChallengeSend = {0x62,0x20 ,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0A,0x0B,0x0C,0x0D,0x0E,0x0F,0x10,0x11,0x21,0x31,0x41,0x51,0x61,0x71,(byte) 0x81,(byte)0x91,(byte)0xA1,(byte)0xB1,(byte)0xC1,(byte)0xD1,(byte)0xE1,(byte)0xF2,0x0};
//                    mCharCommandTx.setValue(commandChallengeSend);
//                    mCharCommandTx.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
//                    boolean statusChallengeSend = mBluetoothLeConfig.mBluetoothLeService.writeCharacteristic(mCharCommandTx);
//                    Log.v(TAG, "Write 6000 into  4D050017 : " + statusChallengeSend);

                    boolean statusReadAuth = mBluetoothLeConfig.mBluetoothLeService.
                                readCharacteristic(mCharAuthentication);
                    Log.v(TAG, "Read Authentication characteristics status : " + statusReadAuth);


                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
                    Log.v(TAG, "Device is Un Paired");
                }

            }
        }
    };
    private boolean pairDevice(BluetoothDevice device) {
        Boolean bool = false;
        try {
            Log.i("Log", "service method is called ");
            Class cl = Class.forName("android.bluetooth.BluetoothDevice");
            Class[] par = {};
            Method method = cl.getMethod("createBond", par);
            Object[] args = {};
            bool = (Boolean) method.invoke(device);//, args);// this invoke creates the detected devices paired.
            //Log.i("Log", "This is: "+bool.booleanValue());
            //Log.i("Log", "devicesss: "+bdDevice.getName());
        } catch (Exception e) {
            Log.i("Log", "Inside catch of serviceFromDevice Method");
            e.printStackTrace();
        }
        return bool.booleanValue();
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BluetoothDevice getBTDeviceExtra() {
        Intent intent = getIntent();
        if (intent == null) {
            return null;
        }

        Bundle extras = intent.getExtras();
        if (extras == null) {
            return null;
        }

        return extras.getParcelable(EXTRA_BLUETOOTH_DEVICE);
    }
}
