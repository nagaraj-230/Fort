package com.payoda.smartlock.history.model;

import com.google.gson.annotations.SerializedName;
import com.payoda.smartlock.plugins.network.ResponseModel;

public class History extends ResponseModel {

    @SerializedName("id")
    private String id;
    @SerializedName("date")
    private String date;
    @SerializedName("time")
    private String time;
    @SerializedName("type")
    private String type;
    @SerializedName("role")
    private String role;
    @SerializedName("userDetails")
    private UserDetails userDetails;
    @SerializedName("slot")
    private String slot;
    @SerializedName("registrationDetails")
    private UserDetails registrationDetails;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public UserDetails getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(UserDetails userDetails) {
        this.userDetails = userDetails;
    }

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

    public class UserDetails {

        @SerializedName("id")
        private String id;
        @SerializedName("username")
        private String username;
        @SerializedName("name")
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public UserDetails getRegistrationDetails() {
        return registrationDetails;
    }

    public void setRegistrationDetails(UserDetails registrationDetails) {
        this.registrationDetails = registrationDetails;
    }
}
