package com.payoda.smartlock.users.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by david on 10/12/18.
 */

public class ScheduleResponse {
    @SerializedName("status")
    private String status;
    @SerializedName("message")
    private String message;

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
}
