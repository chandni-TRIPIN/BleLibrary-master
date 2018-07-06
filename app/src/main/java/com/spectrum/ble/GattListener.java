package com.spectrum.ble;

import android.bluetooth.BluetoothGattService;

import java.util.List;

public interface GattListener {

    /**
     * Callback when device is connected.
     */
    public void onDeviceConnected();

    /**
     * Callback when device is disconnected.
     */
    public void onDeviceDisconnected();

    /**
     * Callback when service is discovered.
     *
     * @param services List of Gatt services.
     */
    public void onServiceDiscovered(List<BluetoothGattService> services);

    /**
     * Callback when data is discovered.
     *
     * @param characteristic Characteristic data.
     * @param data Data in String format.
     */
    public void onAdvDataDiscovered(byte[] characteristic, String data);
}
