package com.payoda.smartlock.managepins.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class OtpRequest {
    @SerializedName("lock_otps")
    private ArrayList<Otp> lockOtps;
    @SerializedName("lock_id")
    private String lockId;

    public ArrayList<Otp> getLockOtps() {
        return lockOtps;
    }

    public void setLockOtps(ArrayList<Otp> lockOtps) {
        this.lockOtps = lockOtps;
    }

    public String getLockId() {
        return lockId;
    }

    public void setLockId(String lockId) {
        this.lockId = lockId;
    }

    @Override
    public String toString() {
        return "OtpRequest{" +
                "lockOtps=" + lockOtps +
                ", lockId='" + lockId + '\'' +
                '}';
    }
}
