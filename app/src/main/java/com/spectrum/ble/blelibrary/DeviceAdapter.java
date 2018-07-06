package com.spectrum.ble.blelibrary;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.spectrum.ble.BluetoothLeConfig;

import java.util.ArrayList;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder>{

    private ArrayList<BluetoothDevice> devices;
    private Context mContext;

    public DeviceAdapter(Context context) {
        devices = new ArrayList<>();
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final BluetoothDevice device = devices.get(position);
        final String deviceName = device.getName();
        if (deviceName != null && deviceName.length() > 0)
            holder.deviceName.setText(deviceName);
        else
            holder.deviceName.setText("N/A");
        holder.deviceAddress.setText(device.getAddress());

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothDevice device = devices.get(position);
                Intent intent = new Intent(mContext, DeviceControlActivity.class);
                intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public void addDevice(BluetoothDevice device) {
        if(!devices.contains(device)) {
            devices.add(device);
        }
    }

    public void clear() {
        devices.clear();
    }

    public BluetoothDevice getDevice(int position) {
        return devices.get(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView deviceName;
        private TextView deviceAddress;
        private LinearLayout linearLayout;

        public ViewHolder(View itemView) {
            super(itemView);

            deviceName = itemView.findViewById(R.id.device_name);
            deviceAddress = itemView.findViewById(R.id.device_address);
            linearLayout = itemView.findViewById(R.id.linearlayout);
        }
    }


}
