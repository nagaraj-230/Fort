package com.payoda.smartlock.locks.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@Entity
public class LockUser implements Serializable {
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "lock_user_id")
    @SerializedName("id")
    private String id;
    @ColumnInfo(name = "lock_user_name")
    @SerializedName("name")
    private String name;
    @ColumnInfo(name = "lock_user_username")
    @SerializedName("username")
    private String username;
    @ColumnInfo(name = "lock_user_email")
    @SerializedName("email")
    private String email;
    @ColumnInfo(name = "lock_user_country_code")
    @SerializedName("country_code")
    private String countryCode;
    @ColumnInfo(name = "lock_user_mobile")
    @SerializedName("mobile")
    private String mobile;
    @ColumnInfo(name = "lock_user_address")
    @SerializedName("address")
    private String address;
    @ColumnInfo(name = "lock_user_status")
    @SerializedName("status")
    private String status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCountryCode() { return countryCode; }

    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
