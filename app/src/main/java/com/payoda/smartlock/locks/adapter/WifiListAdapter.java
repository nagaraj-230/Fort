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

import java.util.ArrayList;

/**
 * Created by david on 20/09/18.
 */

public class WifiListAdapter extends ArrayAdapter<String> {

    private ArrayList<String> itemList;

    public WifiListAdapter(@NonNull Context context, ArrayList<String> list) {
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

    public String getItem(int position) {
        if (itemList != null)
            return itemList.get(position);
        return null;
    }

    public ArrayList<String> getItemList() {
        return itemList;
    }

    public void setItemList(ArrayList<String> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }

    private class ViewHolder {
        TextView txtTitle;
        ImageView imageView;
    }

    @SuppressLint("InflateParams")
    private View rowView(View convertView, int position) {
        String value = getItem(position);
        ViewHolder holder;
        View mView = convertView;
        if (mView == null) {
            holder = new ViewHolder();
            LayoutInflater mLayoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (mLayoutInflater != null) {
                mView = mLayoutInflater.inflate(R.layout.layout_device_row, null, false);
                holder.txtTitle = mView.findViewById(R.id.row_title);
                mView.setTag(holder);
            }
        } else {
            holder = (ViewHolder) mView.getTag();
        }
        holder.txtTitle.setText(value);
        return mView;
    }

}
