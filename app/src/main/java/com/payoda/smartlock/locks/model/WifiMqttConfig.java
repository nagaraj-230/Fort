package com.payoda.smartlock.locks.model;

import com.google.gson.annotations.SerializedName;

public class WifiMqttConfig {
    @SerializedName("owner-id")
    private String ownerId;
    @SerializedName("slot-key")
    private String slotKey;
    @SerializedName("wifi-ssid")
    private String wifiSsid;
    @SerializedName("wifi-pass")
    private String wifiPass;
    @SerializedName("wifi-sec")
    private String wifiSec;
    @SerializedName("mqtt-ip")
    private String mqttIp;
    @SerializedName("mqtt-port")
    private String mqttPort;
    @SerializedName("mqtt-apikey")
    private String mqttApikey;

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

    public String getWifiSec() {
        return wifiSec;
    }

    public void setWifiSec(String wifiSec) {
        this.wifiSec = wifiSec;
    }

    public String getMqttIp() {
        return mqttIp;
    }

    public void setMqttIp(String mqttIp) {
        this.mqttIp = mqttIp;
    }

    public String getMqttPort() {
        return mqttPort;
    }

    public void setMqttPort(String mqttPort) {
        this.mqttPort = mqttPort;
    }

    public String getMqttApikey() {
        return mqttApikey;
    }

    public void setMqttApikey(String mqttApikey) {
        this.mqttApikey = mqttApikey;
    }
}
