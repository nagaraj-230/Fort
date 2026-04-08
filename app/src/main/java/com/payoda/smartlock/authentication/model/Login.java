package com.payoda.smartlock.authentication.model;

import com.google.gson.annotations.SerializedName;
import com.payoda.smartlock.plugins.network.ResponseModel;
import com.payoda.smartlock.utils.Logger;

/**
 * This class contains POJO model for Login
 * Created by david.
 */
public class Login extends ResponseModel {
    @SerializedName("email")
    private String username;
    @SerializedName("password")
    private String password;
    @SerializedName("deviceId")
    private String deviceId;
    @SerializedName("deviceToken")
    private String deviceToken;
    @SerializedName("deviceType")
    private String deviceType = "Android";
    @SerializedName("authenticationToken")
    private String token;
    @SerializedName("name")
    private String name;

    public Login() {
    }

    public Login(String username, String password, String deviceId, String deviceToken) {
        this.username = username;
        this.password = password;
        this.deviceId = deviceId;
        this.deviceToken = deviceToken;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
