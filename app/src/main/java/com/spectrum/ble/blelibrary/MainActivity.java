package com.spectrum.ble.blelibrary;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.spectrum.ble.BluetoothConfig;
import com.spectrum.ble.ScanBuilder;
import com.spectrum.ble.ScanListener;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DeviceAdapter adapter;
    private BluetoothConfig mBluetoothConfig;
    private ScanBuilder mScanBuilder;
    private Button mStartScan;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recyclerview);
        mStartScan = findViewById(R.id.start_scan);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mBluetoothConfig = new BluetoothConfig(this);
        mScanBuilder = new ScanBuilder(this).setListener(mScanListener);
        adapter = new DeviceAdapter(MainActivity.this);
        recyclerView.setAdapter(adapter);

        mStartScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mScanBuilder.scanLeDevice(true);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothConfig.isBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mScanBuilder.scanLeDevice(false);
        adapter.clear();
    }

    private ScanListener mScanListener = new ScanListener() {
        @Override
        public void onDeviceScanned(final ScanResult result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.addDevice(result.getDevice());
                    adapter.notifyDataSetChanged();
                    if (result.getDevice().getName() != null) {
                        Toast.makeText(MainActivity.this, result.getDevice().getName(), Toast.LENGTH_SHORT).show();
                        Toast.makeText(MainActivity.this, Arrays.toString(result.getScanRecord().getBytes()), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        @Override
        public void onCompleted() {
            Toast.makeText(MainActivity.this, adapter.getItemCount() + " devices found", Toast.LENGTH_SHORT).show();
        }
    };
}
