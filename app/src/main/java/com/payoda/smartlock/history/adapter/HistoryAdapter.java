package com.payoda.smartlock.history.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.payoda.smartlock.R;
import com.payoda.smartlock.history.model.History;
import com.payoda.smartlock.utils.DateTimeUtils;
import com.payoda.smartlock.utils.Logger;

import java.util.ArrayList;

public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<History> itemList;

    public static final String TAG = "### HistoryAdapter";

    public HistoryAdapter(ArrayList<History> historyList) {
        this.itemList = historyList;
    }

    private class HistoryItemViewHolder extends RecyclerView.ViewHolder {

        private TextView lblHistory, lblDateHistory, lblTimeHistory;
        private ImageView imgHistoryStatus;


        private HistoryItemViewHolder(View itemView) {
            super(itemView);
            lblHistory = itemView.findViewById(R.id.lblHistory);
            lblDateHistory = itemView.findViewById(R.id.lblDateHistory);
            lblTimeHistory = itemView.findViewById(R.id.lblTimeHistory);
            imgHistoryStatus = itemView.findViewById(R.id.imgHistoryStatus);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        viewHolder = new HistoryItemViewHolder(inflater.inflate(R.layout.layout_history_item, viewGroup, false));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        HistoryItemViewHolder historyItemViewHolder = (HistoryItemViewHolder) holder;

        final History history = getItem(position);

        TextView lblHistory = historyItemViewHolder.lblHistory;
        TextView lblDateHistory = historyItemViewHolder.lblDateHistory;
        TextView lblTimeHistory = historyItemViewHolder.lblTimeHistory;
        ImageView imgHistoryStatus = historyItemViewHolder.imgHistoryStatus;

        String data = "";

        if (history != null && history.getUserDetails() != null && history.getUserDetails().getUsername() != null) {
            data = history.getUserDetails().getUsername();

        }

        if (history != null && history.getRegistrationDetails() != null && history.getRegistrationDetails().getName() != null) {
            data = history.getRegistrationDetails().getName();
        }

        if (history != null) {

            if (history.getRole() == null && history.getType() != null && (history.getType().equalsIgnoreCase("F")
                    || history.getType().equalsIgnoreCase("FP")
                    || history.getType().equalsIgnoreCase("FM")
                    || history.getType().equalsIgnoreCase("FO")
                    || history.getType().equalsIgnoreCase("F0"))) {

                data = data + " User (Fingerprint) engaged the lock";


            } else if (history.getRole() == null) {
                data = "User engaged the lock";
            } else {
                data = data + " (" + history.getRole() + ") engaged the lock";

            }
        }


        if (history != null && history.getType() != null) {
            if (history.getType().equalsIgnoreCase("DO")) {
                data = "Door Opened";
            } else if (history.getType().equalsIgnoreCase("DC")) {
                data = "Door Closed";
            } else if (history.getType().equalsIgnoreCase("AD")) {
                data = "Sensor Paired";
            } else if (history.getType().equalsIgnoreCase("CF")) {
                data = "Sensor Wi-Fi Configured";
            } else if (history.getType().equalsIgnoreCase("RS")) {
                data = "Sensor Reset";
            } else if (history.getType().equalsIgnoreCase("ST")) {
                data = "Sensor Status Update";
            }
        }

        Logger.d("###### data history : " + data);
        lblHistory.setText(data);

        if (history != null && !TextUtils.isEmpty(history.getTime()) && !TextUtils.isEmpty(history.getDate())) {
            lblDateHistory.setText(DateTimeUtils.getLocalDateFromGMT(history.getDate(), history.getTime()));
        } else {
            lblDateHistory.setText("");
        }


        if (history != null && history.getType() != null) {

            int iTypeIcon = R.mipmap.ic_wifi;

            if (history.getType().equalsIgnoreCase("W")) { // Wifi
                iTypeIcon = R.mipmap.ic_wifi;
            }
            else if (history.getType().equalsIgnoreCase("B")) { //Ble
                iTypeIcon = R.mipmap.ic_bluetooth;
            }
            else if (history.getType().equalsIgnoreCase("R") || history.getType().equalsIgnoreCase("RF")) { //RFID
                iTypeIcon = R.mipmap.ic_rfid;
                String message = String.format("RFID (%s) engaged the lock", history.getSlot());
                lblHistory.setText(message);
            }
            else if (history.getType().equalsIgnoreCase("F")
                    || history.getType().equalsIgnoreCase("FP")
                    || history.getType().equalsIgnoreCase("FO")
                    || history.getType().equalsIgnoreCase("F0")) { //Finger
                iTypeIcon = R.drawable.ic_fp;
            }
            else if (history.getType().equalsIgnoreCase("FM")) { // Master Fingerprint
                iTypeIcon = R.drawable.ic_fp;
                String message = ("Master Fingerprint engaged the lock");
                lblHistory.setText(message);
            }
            else if (history.getType().equalsIgnoreCase("P") || history.getType().equalsIgnoreCase("TP")) { // PIN
                iTypeIcon = R.drawable.ic_log_pin;
            }
            else if (history.getType().equalsIgnoreCase("O")) { //OTP
                iTypeIcon = R.drawable.ic_log_otp;
                String message = String.format("OTP (%s) engaged the lock", history.getSlot());
                lblHistory.setText(message);
            }
            else if (history.getType().equalsIgnoreCase("TO")) {//OTP V6.0
                iTypeIcon = R.drawable.ic_log_otp;
                // String message = String.format("OTP (%s) engaged the lock", history.getSlot());
                String message = ("OTP engaged the lock");
                lblHistory.setText(message);
            }
            else if (history.getType().equalsIgnoreCase("MO")) { // Master OTP
                iTypeIcon = R.drawable.ic_log_otp;
                String message = ("Master OTP engaged the lock");
                lblHistory.setText(message);
            }
            else if (history.getType().equalsIgnoreCase("MA")) { // Master Access
                iTypeIcon = R.mipmap.ic_wifi;
                String message = ("Master Access engaged the lock");
                lblHistory.setText(message);
            }
            else if (history.getType().equalsIgnoreCase("I")) { // V4.0
                iTypeIcon = R.mipmap.ic_cloud;
            }
            else if (history.getType().equalsIgnoreCase("PO")) { //Passage Mode Open for V6.0
                iTypeIcon = R.mipmap.ic_passage_mode;
                String message = "Passage mode enabled";
                lblHistory.setText(message);
            }
            else if (history.getType().equalsIgnoreCase("PC")) { //Passage Mode Close for V6.0
                iTypeIcon = R.mipmap.ic_passage_mode;
                String message = "Passage mode disabled";
                lblHistory.setText(message);
            }
            else if (history.getType().equalsIgnoreCase("RM")) { //Remote Access for V6.0
                iTypeIcon = R.drawable.ic_remote_access;
                String message = "Remote access request";
                lblHistory.setText(message);
            }
            else if (history.getType().equalsIgnoreCase("DO")) { // Door Open
                iTypeIcon = R.mipmap.ic_door;
                lblHistory.setText("Door Opened");
            }
            else if (history.getType().equalsIgnoreCase("DC")) { // Door Closed
                iTypeIcon = R.drawable.ic_grey_door;
                lblHistory.setText("Door Closed");
            }
            else if (history.getType().equalsIgnoreCase("AD")) { // P2P Add
                iTypeIcon = R.mipmap.ic_wifi;
                lblHistory.setText("Sensor Paired");
            }
            else if (history.getType().equalsIgnoreCase("CF")) { // P2P Config
                iTypeIcon = R.mipmap.ic_wifi;
                lblHistory.setText("Sensor Wi-Fi Configured");
            }
            else if (history.getType().equalsIgnoreCase("RS")) { // P2P Reset
                iTypeIcon = R.mipmap.ic_door;
                lblHistory.setText("Sensor Reset");
            }
            else if (history.getType().equalsIgnoreCase("ST")) { // Sensor Status
                iTypeIcon = R.mipmap.ic_wifi;
                lblHistory.setText("Sensor Status Update");
            }

            imgHistoryStatus.setBackgroundResource(iTypeIcon);

        }

    }

    @Override
    public int getItemCount() {
        if (itemList != null)
            return itemList.size();
        return 0;
    }

    public History getItem(int position) {
        if (itemList != null)
            return itemList.get(position);
        return new History();
    }

    public ArrayList<History> getItemList() {
        return itemList;
    }

    public void setItemList(ArrayList<History> itemList) {
        //this.itemList = itemList;
        if (this.itemList != null) {
            this.itemList.addAll(itemList);
        } else {
            this.itemList = itemList;
        }
        notifyDataSetChanged();
    }

    public void clearList() {
        if (itemList != null) {
            this.itemList.clear();
        }
    }
}