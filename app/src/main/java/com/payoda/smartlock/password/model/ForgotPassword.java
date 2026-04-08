package com.payoda.smartlock.password.model;

import com.google.gson.annotations.SerializedName;
import com.payoda.smartlock.plugins.network.ResponseModel;

public class ForgotPassword  {

    @SerializedName("email")
    private String email;

    public ForgotPassword() {
    }

    public ForgotPassword(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}





