package com.payoda.smartlock.users.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by david on 6/20/2018.
 */

public class AssignUserRequest {
    @SerializedName("status")
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
