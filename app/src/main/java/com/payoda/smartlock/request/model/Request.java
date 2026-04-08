package com.payoda.smartlock.request.model;

import com.google.gson.annotations.SerializedName;
import com.payoda.smartlock.locks.model.Lock;
import com.payoda.smartlock.locks.model.LockKeys;
import com.payoda.smartlock.plugins.network.ResponseModel;
import com.payoda.smartlock.users.model.User;

public class Request {
    @SerializedName("message")
    private String message;
    @SerializedName("id")
    public String id;
    @SerializedName("key_id")
    public String keyId;
    @SerializedName("status")
    public String requestStatus;
    @SerializedName("lock")
    public Lock lock;
    @SerializedName("key")
    public LockKeys key;
    @SerializedName("fromUser")
    public User fromUser;
    @SerializedName("toUser")
    public User toUser;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }

    public Lock getLock() {
        return lock;
    }

    public void setLock(Lock lock) {
        this.lock = lock;
    }

    public LockKeys getKey() {
        return key;
    }

    public void setKey(LockKeys key) {
        this.key = key;
    }

    public User getFromUser() {
        return fromUser;
    }

    public void setFromUser(User fromUser) {
        this.fromUser = fromUser;
    }

    public User getToUser() {
        return toUser;
    }

    public void setToUser(User toUser) {
        this.toUser = toUser;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }
}