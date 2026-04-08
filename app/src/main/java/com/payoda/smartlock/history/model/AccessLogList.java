package com.payoda.smartlock.history.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class AccessLogList {

    @SerializedName("id")
    private String id;

    @SerializedName("data")
    private ArrayList<String> logs;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<String> getLogs() {
        return logs;
    }

    public void setLogs(ArrayList<String> logs) {
        this.logs = logs;
    }
}
