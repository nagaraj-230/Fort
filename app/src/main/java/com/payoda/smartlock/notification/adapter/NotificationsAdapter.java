package com.payoda.smartlock.notification.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.payoda.smartlock.R;
import com.payoda.smartlock.notification.model.Notification;
import com.payoda.smartlock.utils.DateTimeUtils;

import java.util.ArrayList;

/**
 * Created by david on 6/16/2018.
 */

public class NotificationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Notification> itemList;

    public NotificationsAdapter(ArrayList<Notification> notificationsList) {
        this.itemList = notificationsList;
    }

    class NotificationsItemViewHolder extends RecyclerView.ViewHolder {

        private TextView lblNotification, lblDate, lblTime;
        private ImageView imgNotificationStatus;
        public NotificationsItemViewHolder(View itemView) {
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
        viewHolder = new NotificationsItemViewHolder(inflater.inflate(R.layout.layout_notifications_item, viewGroup, false));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NotificationsItemViewHolder notificationsItemViewHolder = (NotificationsItemViewHolder) holder;
        final Notification mNotification = itemList.get(position);
        notificationsItemViewHolder.lblNotification.setText(mNotification.getMessage());
        //notificationsItemViewHolder.lblDate.setText(DateTimeUtils.getDate(mNotification.getCreatedDate(), DateTimeUtils.DATE_TIME_FORMAT));
        //notificationsItemViewHolder.lblTime.setText(DateTimeUtils.getTime(mNotification.getCreatedDate(), DateTimeUtils.DATE_TIME_FORMAT));
        //notificationsItemViewHolder.lblDate.setText(DateTimeUtils.getDateAndTime(DateTimeUtils.getLocalDateFromGMT(mNotification.getCreatedDate()),DateTimeUtils.DATE_TIME_NOTIFICATION_FORMAT));
        notificationsItemViewHolder.lblDate.setText(DateTimeUtils.getLocalDateFromGMT(mNotification.getCreatedDate()));
        notificationsItemViewHolder.lblTime.setText("");

    }

    @Override
    public int getItemCount() {
        if (itemList != null)
            return itemList.size();
        return 0;
    }

    public Notification getItem(int position) {
        if (itemList != null)
            return itemList.get(position);
        return null;
    }

    public ArrayList<Notification> getItemList() {
        return itemList;
    }

    public void setItemList(ArrayList<Notification> itemList) {
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
