package com.payoda.smartlock.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VersionControlModel {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("error-code")
    @Expose
    private Integer errorCode;
    @SerializedName("fort-android-version")
    @Expose
    private String fortAndroidVersion;
    @SerializedName("machamp-android-version")
    @Expose
    private String machampAndroidVersion;
    @SerializedName("fort-ios-version")
    @Expose
    private String fortIosVersion;
    @SerializedName("machamp-ios-version")
    @Expose
    private String machampIosVersion;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getFortAndroidVersion() {
        return fortAndroidVersion;
    }

    public void setFortAndroidVersion(String fortAndroidVersion) {
        this.fortAndroidVersion = fortAndroidVersion;
    }

    public String getMachampAndroidVersion() {
        return machampAndroidVersion;
    }

    public void setMachampAndroidVersion(String machampAndroidVersion) {
        this.machampAndroidVersion = machampAndroidVersion;
    }

    public String getFortIosVersion() {
        return fortIosVersion;
    }

    public void setFortIosVersion(String fortIosVersion) {
        this.fortIosVersion = fortIosVersion;
    }

    public String getMachampIosVersion() {
        return machampIosVersion;
    }

    public void setMachampIosVersion(String machampIosVersion) {
        this.machampIosVersion = machampIosVersion;
    }
}
