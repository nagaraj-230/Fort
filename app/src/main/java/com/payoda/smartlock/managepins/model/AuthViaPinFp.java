package com.payoda.smartlock.managepins.model;

import com.google.gson.annotations.SerializedName;

public class AuthViaPinFp {
    @SerializedName("owner-id")
    private String ownerId;
    @SerializedName("slot-key")
    private String slotKey;
    @SerializedName("en-dis")
    private String enDis;

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

    public String getEnDis() {
        return enDis;
    }

    public void setEnDis(String enDis) {
        this.enDis = enDis;
    }
}
