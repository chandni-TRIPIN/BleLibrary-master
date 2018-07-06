package com.spectrum.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import static android.content.Context.BIND_AUTO_CREATE;

public class BluetoothLeConfig {
    private final static String TAG = BluetoothLeConfig.class.getSimpleName();

    public BluetoothLeService mBluetoothLeService;
    private BluetoothDevice mBluetoothDevice;
    private boolean mConnected = false;
    private GattListener mGattListener;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private Context mContext;

    public BluetoothLeConfig(Context context, BluetoothDevice device, GattListener gattListener) {
        mContext = context;
        mBluetoothDevice = device;
        mGattListener = gattListener;
    }

    public BluetoothLeConfig(Context context, GattListener gattListener) {
        mContext = context;
        mGattListener = gattListener;

    }

    public void setDevice(BluetoothDevice device) {
        this.mBluetoothDevice = device;
    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
            }


            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mBluetoothDevice.getAddress());
            registerReceiver();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
            unregisterReceiver();
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                mGattListener.onDeviceConnected();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                mGattListener.onDeviceDisconnected();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                mGattListener.onServiceDiscovered(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                byte[] charas = intent.getByteArrayExtra(BluetoothLeService.ACTION_GATT_EXTRA_DATA);
                String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                mGattListener.onAdvDataDiscovered(charas, data);
            }
        }
    };

    public void bindService()
    {
        Intent gattServiceIntent = new Intent(mContext, BluetoothLeService.class);
        mContext.bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    public void unbindService()
    {
        mContext.unbindService(mServiceConnection);
    }

    public boolean connect()
    {
        return mBluetoothLeService.connect(mBluetoothDevice.getAddress());
    }

    public void disconnect()
    {
        mBluetoothLeService.disconnect();
    }

    public void registerReceiver() {
        mContext.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    public void unregisterReceiver() {
        mContext.unregisterReceiver(mGattUpdateReceiver);
    }

    public void endBluetoothLeService()
    {
        mBluetoothLeService = null;
    }

    public boolean bluetoothLeServiceIsActive()
    {
        return mBluetoothLeService != null;
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic) {
        final int charaProp = characteristic.getProperties();
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            // If there is an active notification on a characteristic, clear
            // it first so it doesn't update the data field on the user interface.
            if (mNotifyCharacteristic != null) {
                mBluetoothLeService.setCharacteristicNotification(
                        mNotifyCharacteristic, false);
                mNotifyCharacteristic = null;
            }
            mBluetoothLeService.readCharacteristic(characteristic);
        }
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            mNotifyCharacteristic = characteristic;
            mBluetoothLeService.setCharacteristicNotification(
                    characteristic, true);
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

}
