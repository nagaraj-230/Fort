package com.payoda.smartlock.locks.model;

import com.google.gson.annotations.SerializedName;

public class ServerTime {
    @SerializedName("status")
    private String status;
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private ServerData data;

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

    public ServerData getData() {
        return data;
    }

    public void setData(ServerData data) {
        this.data = data;
    }
}
