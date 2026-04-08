package com.payoda.smartlock.request.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by david on 6/20/2018.
 */

public class RequestAccept {
    @SerializedName("request_id")
    private String requestId;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
