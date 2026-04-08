package com.payoda.smartlock.notification.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by david on 6/16/2018.
 */

public class Notification {
    @SerializedName("id")
    private int id;
    @SerializedName("notify_id")
    private String notifyId;
    @SerializedName("notify_to")
    private String notifyTo;
    @SerializedName("message")
    private String message;
    @SerializedName("created_date")
    private String createdDate;
    @SerializedName("status")
    private int status;

    public Notification() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNotifyId() {
        return notifyId;
    }

    public void setNotifyId(String notifyId) {
        this.notifyId = notifyId;
    }

    public String getNotifyTo() {
        return notifyTo;
    }

    public void setNotifyTo(String notifyTo) {
        this.notifyTo = notifyTo;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}