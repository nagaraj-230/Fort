package com.payoda.smartlock.model;

import com.google.gson.annotations.SerializedName;

public class MasterOtpRequest {

    @SerializedName("lock-serial-no")
    private String lockSerialNo;

    public MasterOtpRequest() {
    }

    public MasterOtpRequest(String lockSerialNo) {
        this.lockSerialNo = lockSerialNo;
    }

    public String getLockSerialNo() {
        return lockSerialNo;
    }

    public void setLockSerialNo(String lockSerialNo) {
        this.lockSerialNo = lockSerialNo;
    }
}
