package com.payoda.smartlock.locks.model;

import com.google.gson.annotations.SerializedName;

public class WifiMqttConfigV6 {

    @SerializedName("owner-id")
    private String ownerId;
    @SerializedName("slot-key")
    private String slotKey;
    @SerializedName("wifi-ssid")
    private String wifiSsid;
    @SerializedName("wifi-pass")
    private String wifiPass;
    @SerializedName("wifi-sec")
    private Integer wifiSec;

    @SerializedName("http-ip")
    private String mqttIp;
    @SerializedName("http-port")
    private Integer mqttPort;

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getSlotKey() {
        return slotKey;
    }

    public void setSlotKey(String slotKey) {
        this.slotKey = slotKey;
    }

    public String getWifiSsid() {
        return wifiSsid;
    }

    public void setWifiSsid(String wifiSsid) {
        this.wifiSsid = wifiSsid;
    }

    public String getWifiPass() {
        return wifiPass;
    }

    public void setWifiPass(String wifiPass) {
        this.wifiPass = wifiPass;
    }

    public Integer getWifiSec() {
        return wifiSec;
    }

    public void setWifiSec(Integer wifiSec) {
        this.wifiSec = wifiSec;
    }

    public String getMqttIp() {
        return mqttIp;
    }

    public void setMqttIp(String mqttIp) {
        this.mqttIp = mqttIp;
    }

    public Integer getMqttPort() {
        return mqttPort;
    }

    public void setMqttPort(Integer mqttPort) {
        this.mqttPort = mqttPort;
    }

}
