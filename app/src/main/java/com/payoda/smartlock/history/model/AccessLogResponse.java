package com.payoda.smartlock.history.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by david on 10/09/18.
 */

public class AccessLogResponse {
    @SerializedName("status")
    String status;
    @SerializedName("message")
    String message;

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
