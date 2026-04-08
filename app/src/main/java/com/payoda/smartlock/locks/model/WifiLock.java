package com.payoda.smartlock.locks.model;

import com.google.gson.annotations.SerializedName;

public class WifiLock {

    @SerializedName("id")
    private String id;
    @SerializedName("activation-code")
    private String activationCode;
    @SerializedName("owner-id")
    private String ownerId;
    @SerializedName("slot-key")
    private String slotKey;
    @SerializedName("date")
    private String date;
    @SerializedName("time")
    private String time;
    @SerializedName("slot-id")
    private String slotId;
    @SerializedName("rf-id")
    private int rfId;
    @SerializedName("fp-id")
    private int fpId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }

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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }

    public int getRfId() {
        return rfId;
    }

    public void setRfId(int rfId) {
        this.rfId = rfId;
    }

    public int getFpId() {
        return fpId;
    }

    public void setFpId(int fpId) {
        this.fpId = fpId;
    }
}
