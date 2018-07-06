package com.spectrum.ble;

import android.bluetooth.le.ScanResult;

public interface ScanListener {

    /**
     * Callback when a BLE advertisement has been found.
     *
     * @param result A Bluetooth LE scan result.
     */
    public void onDeviceScanned(ScanResult result);

    /**
     * Callback at end of scan.
     */
    public void onCompleted();
}
