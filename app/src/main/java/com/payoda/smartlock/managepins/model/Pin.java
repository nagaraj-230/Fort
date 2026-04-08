package com.payoda.smartlock.managepins.model;

import com.google.gson.annotations.SerializedName;

public class Pin {
    @SerializedName("name")
    private String name;
    @SerializedName("pin")
    private String pin;
    @SerializedName("slot_number")
    private String slotNumber;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(String slotNumber) {
        this.slotNumber = slotNumber;
    }
}
