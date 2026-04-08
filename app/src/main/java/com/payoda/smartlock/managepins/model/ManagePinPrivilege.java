package com.payoda.smartlock.managepins.model;

import com.google.gson.annotations.SerializedName;

public class ManagePinPrivilege {
    @SerializedName("enable_pin")
    private String enablePin;
    public String getEnablePin() {
        return enablePin;
    }
    public void setEnablePin(String enablePin) {
        this.enablePin = enablePin;
    }
}
