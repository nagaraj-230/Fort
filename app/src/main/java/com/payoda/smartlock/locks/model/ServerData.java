package com.payoda.smartlock.locks.model;

import com.google.gson.annotations.SerializedName;

public class ServerData {
    @SerializedName("server_time")
    private String server_time;

    public String getServer_time() {
        return server_time;
    }

    public void setServer_time(String server_time) {
        this.server_time = server_time;
    }
}
