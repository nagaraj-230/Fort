package com.payoda.smartlock.history.model;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

public class AccessLog {

    @SerializedName("response")
    private HashMap<String, String> response;

    public HashMap<String, String> getResponse() {
        return response;
    }

    public void setResponse(HashMap<String, String> response) {
        this.response = response;
    }

}
