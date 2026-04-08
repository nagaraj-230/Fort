package com.payoda.smartlock.managepins.model;

import com.google.gson.annotations.SerializedName;

public class ReWritePin {
    @SerializedName("owner-id")
    private String ownerId;
    @SerializedName("slot-key")
    private String slotKey;

    @SerializedName("pin-1")
    private String pin1;
    @SerializedName("pin-2")
    private String pin2;
    @SerializedName("pin-3")
    private String pin3;
    @SerializedName("pin-4")
    private String pin4;
    @SerializedName("pin-5")
    private String pin5;
    @SerializedName("pin-6")
    private String pin6;
    @SerializedName("pin-7")
    private String pin7;
    @SerializedName("pin-8")
    private String pin8;
    @SerializedName("pin-9")
    private String pin9;

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

    public String getPin1() {
        return pin1;
    }

    public void setPin1(String pin1) {
        this.pin1 = pin1;
    }

    public String getPin2() {
        return pin2;
    }

    public void setPin2(String pin2) {
        this.pin2 = pin2;
    }

    public String getPin3() {
        return pin3;
    }

    public void setPin3(String pin3) {
        this.pin3 = pin3;
    }

    public String getPin4() {
        return pin4;
    }

    public void setPin4(String pin4) {
        this.pin4 = pin4;
    }

    public String getPin5() {
        return pin5;
    }

    public void setPin5(String pin5) {
        this.pin5 = pin5;
    }

    public String getPin6() {
        return pin6;
    }

    public void setPin6(String pin6) {
        this.pin6 = pin6;
    }

    public String getPin7() {
        return pin7;
    }

    public void setPin7(String pin7) {
        this.pin7 = pin7;
    }

    public String getPin8() {
        return pin8;
    }

    public void setPin8(String pin8) {
        this.pin8 = pin8;
    }

    public String getPin9() {
        return pin9;
    }

    public void setPin9(String pin9) {
        this.pin9 = pin9;
    }
}
