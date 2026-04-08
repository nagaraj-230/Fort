package com.payoda.smartlock.fp.model;

import com.google.gson.annotations.SerializedName;
import com.payoda.smartlock.users.model.User;

import java.util.ArrayList;

public class UserListResponse {
    @SerializedName("message")
    private String message;
    @SerializedName("status")
    private String status;
    @SerializedName("data")
    private ArrayList<User> users;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }
}
