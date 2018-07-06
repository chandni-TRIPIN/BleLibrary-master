package com.spectrum.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

public class BluetoothConfig {

    private final static String TAG = BluetoothConfig.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    /**
     *
     * @param context Application context.
     */
    public BluetoothConfig(Context context) {
        mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
    }

    /**
     * Check if bluetooth is supported.
     *
     * @return Return true if Bluetooth is supported.
     */
    public boolean isBluetoothSupported() {
        if (mBluetoothManager == null) {
            Log.e(TAG, "Unable to initialize BluetoothManager.");
            return false;
        }

        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Check if bluetooth is enabled.
     *
     * @return Returns true if bluetooth is enabled.
     */
    public boolean isBluetoothEnabled() {
        if(!isBluetoothSupported())
            return false;
        return mBluetoothAdapter.isEnabled();
    }

    /**
     * Enable or disable bluetooth.
     *
     * @param enable true to enable bluetooth
     *               false to disable bluetooth.
     * @return Returns true if bluetooth enabled/disabled successully.
     */
    public boolean setBluetooth(boolean enable) {
        boolean isEnabled = mBluetoothAdapter.isEnabled();
        if (enable && !isEnabled) {
            return mBluetoothAdapter.enable();
        }
        else if(!enable && isEnabled) {
            return mBluetoothAdapter.disable();
        }
        return true;
    }

}
