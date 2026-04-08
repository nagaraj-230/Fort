package com.payoda.smartlock.profile.model;

import com.google.gson.annotations.SerializedName;
import com.payoda.smartlock.plugins.network.ResponseModel;

public class Profile extends ResponseModel {
    @SerializedName("name")
    private String username;
    @SerializedName("email")
    private String email;
    @SerializedName("country_code")
    private String countryCode;
    @SerializedName("mobile")
    private String mobile;
    @SerializedName("address")
    private String address;

    public Profile() {
    }

    public Profile(String username, String email, String countryCode, String mobile, String address) {
        this.username = username;
        this.email=email;
        this.countryCode=countryCode;
        this.mobile=mobile;
        this.address=address;
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

}
