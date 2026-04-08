package com.payoda.smartlock.history.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.payoda.smartlock.R;
import com.payoda.smartlock.history.model.LockNotification;
import com.payoda.smartlock.utils.DateTimeUtils;
import com.payoda.smartlock.utils.Logger;

import java.util.ArrayList;

/**
 * Created by david on 19/11/18.
 */

public class LockNotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<LockNotification> itemList;
    public static final String TAG = "LockNotificationAdapter";

    public LockNotificationAdapter(ArrayList<LockNotification> notificationsList) {
        this.itemList = notificationsList;
    }

    class LockNotificationsItemViewHolder extends RecyclerView.ViewHolder {
        private TextView lblNotification, lblDate, lblTime;
        private ImageView imgNotificationStatus;

        public LockNotificationsItemViewHolder(View itemView) {
            super(itemView);
            lblNotification = itemView.findViewById(R.id.lblNotification);
            lblDate = itemView.findViewById(R.id.lblDate);
            lblTime = itemView.findViewById(R.id.lblTime);
            imgNotificationStatus = itemView.findViewById(R.id.imgNotificationStatus);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        Logger.d(TAG, TAG);
        viewHolder = new LockNotificationAdapter.LockNotificationsItemViewHolder(inflater.inflate(R.layout.layout_notifications_item, viewGroup, false));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        LockNotificationAdapter.LockNotificationsItemViewHolder notificationsItemViewHolder = (LockNotificationAdapter.LockNotificationsItemViewHolder) holder;
        final LockNotification mNotification = itemList.get(position);
        notificationsItemViewHolder.lblNotification.setText(mNotification.getLogMsg());
        //notificationsItemViewHolder.lblDate.setText(DateTimeUtils.getDate(mNotification.getCreatedDate(), DateTimeUtils.DATE_TIME_FORMAT));
        //notificationsItemViewHolder.lblTime.setText(DateTimeUtils.getTime(mNotification.getLogDateTime(), DateTimeUtils.DATE_TIME_FORMAT));
        //notificationsItemViewHolder.lblDate.setText(DateTimeUtils.getDateAndTime(mNotification.getLogDateTime(),DateTimeUtils.DATE_TIME_NOTIFICATION_FORMAT));
        notificationsItemViewHolder.lblDate.setText(DateTimeUtils.getLocalDateFromGMT(mNotification.getLogDateTime()));
        //notificationsItemViewHolder.lblDate.setText(mNotification.getLogDateTime());
        notificationsItemViewHolder.lblTime.setText("");

    }

    @Override
    public int getItemCount() {
        if (itemList != null)
            return itemList.size();
        return 0;
    }

    public LockNotification getItem(int position) {
        if (itemList != null)
            return itemList.get(position);
        return null;
    }

    public ArrayList<LockNotification> getItemList() {
        return itemList;
    }

    public void setItemList(ArrayList<LockNotification> itemList) {
        if(this.itemList!=null){
            this.itemList.addAll(itemList);
        }else{
            this.itemList=itemList;
        }
        notifyDataSetChanged();
    }

    public void clearList(){
        if(itemList!=null){
            itemList.clear();
        }
    }

}
