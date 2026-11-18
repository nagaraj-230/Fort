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

    @SerializedName(value = "scratch_code", alternate = {"scratchCode"})
    private String scratchCode;

    @SerializedName(value = "owner_id", alternate = {"ownerId"})
    private String ownerId;

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
        if (code != null && !code.isEmpty()) return code;
        return serialNumber;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getScratchCode() {
        return scratchCode;
    }

    public void setScratchCode(String scratchCode) {
        this.scratchCode = scratchCode;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
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
