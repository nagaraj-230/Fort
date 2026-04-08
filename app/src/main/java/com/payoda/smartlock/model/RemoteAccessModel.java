package com.payoda.smartlock.model;

import com.google.gson.annotations.SerializedName;

public class RemoteAccessModel {

    @SerializedName("request_id")
    private String requestId;

    @SerializedName("lock_status")
    private String lockStatus;

    @SerializedName("lock-serial-no")
    private String lockSerialNo;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getLockStatus() {
        return lockStatus;
    }

    public void setLockStatus(String lockStatus) {
        this.lockStatus = lockStatus;
    }

    public String getLockSerialNo() {
        return lockSerialNo;
    }

    public void setLockSerialNo(String lockSerialNo) {
        this.lockSerialNo = lockSerialNo;
    }
}
