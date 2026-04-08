package com.payoda.smartlock.history.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by david on 19/11/18.
 */

public class LockNotification {
    @SerializedName("log_msg")
    private String logMsg;
    @SerializedName("log_datetime")
    private String logDateTime;

    public String getLogMsg() {
        return logMsg;
    }

    public void setLogMsg(String logMsg) {
        this.logMsg = logMsg;
    }

    public String getLogDateTime() {
        return logDateTime;
    }

    public void setLogDateTime(String logDateTime) {
        this.logDateTime = logDateTime;
    }
}
