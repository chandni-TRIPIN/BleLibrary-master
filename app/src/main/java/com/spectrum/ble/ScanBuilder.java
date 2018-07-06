package com.spectrum.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;

public class ScanBuilder {

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanListener mScanListener;
    private int mScanMode = ScanSettings.SCAN_MODE_LOW_POWER;
    private long mScanDelayMillis = 30000;
    private Context mContext;
    private boolean mScanning;
    private Handler mHandler;

    /**
     *
     * @param context Application context.
     */
    public ScanBuilder(Context context) {
        mContext = context;
        mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mHandler = new Handler();
    }

    /**
     * Set ScanListener callback.
     */
    public ScanBuilder setListener(ScanListener scanListener) {
        mScanListener = scanListener;
        return this;
    }

    /**
     * Set scan mode for Bluetooth LE scan.
     *
     * @param mode The scan mode can be one of
     *            {@link ScanSettings#SCAN_MODE_LOW_POWER},
     *            {@link ScanSettings#SCAN_MODE_BALANCED} or
     *            {@link ScanSettings#SCAN_MODE_LOW_LATENCY}.
     * @throws IllegalArgumentException If the {@code mode} is invalid.
     */
    public ScanBuilder setScanMode(int mode) {
        if (mode < ScanSettings.SCAN_MODE_OPPORTUNISTIC || mode > ScanSettings.SCAN_MODE_LOW_LATENCY) {
            throw new IllegalArgumentException("invalid scan mode " + mode);
        }
        mScanMode = mode;
        return this;
    }

    /**
     * Set report delay timestamp for Bluetooth LE scan.
     *
     * @param delayMillis Delay of report in milliseconds. Set to 0 to be notified of
     *            results immediately.
     * @throws IllegalArgumentException If {@code delayMillis} is invalid.
     */
    public ScanBuilder setScanDelay(long delayMillis) {
        if (delayMillis < 0) {
            throw new IllegalArgumentException("reportDelay must be > 0");
        }
        mScanDelayMillis = delayMillis;
        return this;
    }

    /**
     * Start or stop scan for bluetoothLe devices.
     *
     * @param enable true to start scan
     *               false to stop scan.
     */
    public void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothLeScanner.stopScan(mScanCallback);
                    mScanListener.onCompleted();
                }
            },mScanDelayMillis);

            mScanning = true;
            /*ScanFilter filter = new ScanFilter.Builder()
                    .setServiceUuid(ParcelUuid.fromString("00001805-0000-1000-8000-00805f9b34fb"))
                    .build();
            List<ScanFilter> filters = new ArrayList<>();
            filters.add(filter); */
            ScanSettings scanSettings = new ScanSettings.Builder()
                    .setScanMode(mScanMode)
                    //.setReportDelay(mScanDelayMillis)
                    .build();
            mBluetoothLeScanner.startScan(null, scanSettings, mScanCallback);
            //mBluetoothLeScanner.startScan(mScanCallback);
        } else {
            mScanning = false;
            mBluetoothLeScanner.stopScan(mScanCallback);
        }
    }

    /**
     * Device scan callback.
     */
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            super.onScanResult(callbackType, result);
            mScanListener.onDeviceScanned(result);
        }
    };
}
