package com.payoda.smartlock.managepins.model;

import com.google.gson.annotations.SerializedName;

public class ReWriteOtp {
    @SerializedName("owner-id")
    private String ownerId;
    @SerializedName("slot-key")
    private String slotKey;

    @SerializedName("otp-1")
    private String otp1;
    @SerializedName("otp-2")
    private String otp2;
    @SerializedName("otp-3")
    private String otp3;
    @SerializedName("otp-4")
    private String otp4;
    @SerializedName("otp-5")
    private String otp5;

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

    public String getOtp1() {
        return otp1;
    }

    public void setOtp1(String otp1) {
        this.otp1 = otp1;
    }

    public String getOtp2() {
        return otp2;
    }

    public void setOtp2(String otp2) { this.otp2 = otp2; }

    public String getOtp3() {
        return otp3;
    }

    public void setOtp3(String otp3) {
        this.otp3 = otp3;
    }

    public String getOtp4() {
        return otp4;
    }

    public void setOtp4(String otp4) {
        this.otp4 = otp4;
    }

    public String getOtp5() {
        return otp5;
    }

    public void setOtp5(String otp5) {
        this.otp5 = otp5;
    }
}
