package com.payoda.smartlock.locks.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by david on 6/19/2018.
 */
@Entity
public class RequestDetail implements Serializable {
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "request_id")
    @SerializedName("id")
    private String id;
    @ColumnInfo(name = "request_lock_id")
    @SerializedName("lock_id")
    private String lockId;
    @SerializedName("key_id")
    private String keyId;
    @SerializedName("request_to")
    private String requestTo;
    @ColumnInfo(name = "request_status")
    @SerializedName("status")
    private String status;
    @SerializedName("modified_date")
    private String modifiedDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getRequestTo() {
        return requestTo;
    }

    public void setRequestTo(String requestTo) {
        this.requestTo = requestTo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(String modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
}
