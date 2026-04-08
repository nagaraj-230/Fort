package com.payoda.smartlock.model;

import com.google.gson.annotations.SerializedName;

public class DeviceTokenModel {
    @SerializedName("deviceToken")
    private String deviceToken;
    @SerializedName("appid")
    private String appid;


    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public DeviceTokenModel() {
    }

    public DeviceTokenModel(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }
}
