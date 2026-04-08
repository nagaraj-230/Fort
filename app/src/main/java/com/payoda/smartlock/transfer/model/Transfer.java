package com.payoda.smartlock.transfer.model;

import com.google.gson.annotations.SerializedName;
import com.payoda.smartlock.plugins.network.ResponseModel;

public class Transfer  {
    @SerializedName("key_id")
    private String keyId;
    @SerializedName("country_code")
    private String countryCode;
    @SerializedName("mobile")
    private String mobile;
    @SerializedName("status")
    private String status;

    public Transfer() {
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
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
}
