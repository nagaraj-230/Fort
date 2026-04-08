package com.payoda.smartlock.managepins.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class PinRequest {

    @SerializedName("lock_pins")
    private ArrayList<Pin> lockPins;
    @SerializedName("lock_id")
    private String lockId;

    public ArrayList<Pin> getLockPins() {
        return lockPins;
    }

    public void setLockPins(ArrayList<Pin> lockPins) {
        this.lockPins = lockPins;
    }

    public String getLockId() {
        return lockId;
    }

    public void setLockId(String lockId) {
        this.lockId = lockId;
    }
}
