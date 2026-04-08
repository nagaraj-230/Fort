package com.payoda.smartlock.users.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by david on 6/19/2018.
 */

public class AddUser {
    @SerializedName("lock_id")
    private String lockId;
    @SerializedName("key_id")
    private String keyId;
    @SerializedName("status")
    private String status;
    @SerializedName("id")
    private String id;

    public String getLockId() {
        return lockId;
    }

    public void setLockId(String lockId) {
        this.lockId = lockId;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
