package com.payoda.smartlock.managepins.model;

import com.google.gson.annotations.SerializedName;

public class Otp {
    @SerializedName("pin")
    private String pin;
    @SerializedName("slot_number")
    private String slotNumber;

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

    @Override
    public String toString() {
        return "Otp{" +
                "pin='" + pin + '\'' +
                ", slotNumber='" + slotNumber + '\'' +
                '}';
    }
}
