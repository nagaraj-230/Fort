package com.payoda.smartlock.locks.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.payoda.smartlock.R;
import com.payoda.smartlock.plugins.bluetooth.BleDevice;

import java.util.ArrayList;

public class DeviceListAdapter extends ArrayAdapter<BleDevice> {

    private ArrayList<BleDevice> itemList;

    public DeviceListAdapter(@NonNull Context context, ArrayList<BleDevice> list) {
        super(context, R.layout.layout_device_row, list);
        this.itemList = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        return rowView(convertView, position);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return rowView(convertView, position);
    }

    @Override
    public int getCount() {
        if (itemList != null)
            return itemList.size();
        return 0;
    }

    public BleDevice getItem(int position) {
        if (itemList != null)
            return itemList.get(position);
        return null;
    }

    public ArrayList<BleDevice> getItemList() {
        return itemList;
    }

    public void setItemList(ArrayList<BleDevice> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }

    private class viewHolder {
        TextView txtTitle;
        ImageView imageView;
    }

    @SuppressLint("InflateParams")
    private View rowView(View convertView, int position) {
        BleDevice mBleDevice = getItem(position);
        viewHolder holder;
        View mView = convertView;
        if (mView == null) {
            holder = new viewHolder();
            LayoutInflater mLayoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (mLayoutInflater != null) {
                mView = mLayoutInflater.inflate(R.layout.layout_device_row, null, false);
                holder.txtTitle = mView.findViewById(R.id.row_title);
                mView.setTag(holder);
            }

        } else {
            holder = (viewHolder) mView.getTag();
        }
        holder.txtTitle.setText(mBleDevice.getName());
        return mView;
    }


}