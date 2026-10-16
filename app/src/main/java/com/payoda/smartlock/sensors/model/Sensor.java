package com.payoda.smartlock.sensors.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Sensor implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("code")
    private String code;

    @SerializedName("serial_number")
    private String serialNumber;

    @SerializedName("battery")
    private String battery;

    @SerializedName("ssid")
    private String ssid;

    @SerializedName("status")
    private String status;

    private boolean isSync = true;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getBattery() {
        return battery != null ? battery : "100";
    }

    public void setBattery(String battery) {
        this.battery = battery;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isSync() {
        return isSync;
    }

    public void setSync(boolean sync) {
        isSync = sync;
    }
}
