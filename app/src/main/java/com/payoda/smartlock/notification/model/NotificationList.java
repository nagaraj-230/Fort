package com.payoda.smartlock.notification.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class NotificationList {
    @SerializedName("data")
    public ArrayList<Notification> notifications;

    public ArrayList<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(ArrayList<Notification> notifications) {
        this.notifications = notifications;
    }
}
