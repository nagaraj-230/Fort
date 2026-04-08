package com.payoda.smartlock.request.model;

import com.google.gson.annotations.SerializedName;
import com.payoda.smartlock.locks.model.LockKeys;

import java.util.ArrayList;

public class RequestList {
    @SerializedName("data")
    public ArrayList<Request> request;

    public ArrayList<Request> getRequest() {
        return request;
    }

    public void setRequest(ArrayList<Request> request) {
        this.request = request;
    }
}
