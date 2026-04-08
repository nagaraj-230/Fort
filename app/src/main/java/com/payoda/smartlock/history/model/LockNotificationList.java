package com.payoda.smartlock.history.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by david on 19/11/18.
 */

public class LockNotificationList {
    @SerializedName("status")
    private String status;
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private ArrayList<LockNotification> lockNotifications;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ArrayList<LockNotification> getLockNotifications() {
        return lockNotifications;
    }

    public void setLockNotifications(ArrayList<LockNotification> lockNotifications) {
        this.lockNotifications = lockNotifications;
    }
}
