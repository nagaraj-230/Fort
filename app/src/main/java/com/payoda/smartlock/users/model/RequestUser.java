package com.payoda.smartlock.users.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by david on 6/19/2018.
 */

public class RequestUser {
    @SerializedName("key_id")
    private String keyId;
    @SerializedName("user_id")
    private String userId;
    @SerializedName("slot_number")
    private String slotNumber;
    @SerializedName("key")
    private String key;
    @SerializedName("country_code")
    private String countryCode;
    @SerializedName("mobile")
    private String mobile;
    @SerializedName("status")
    private String status;
    private String requestId;
    private String requestStatus;

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(String slotNumber) {
        this.slotNumber = slotNumber;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCountryCode() { return countryCode; }

    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }
}
