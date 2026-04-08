package com.payoda.smartlock.locks.model;

import com.google.gson.annotations.SerializedName;
import com.payoda.smartlock.plugins.network.ResponseModel;

import java.io.Serializable;
import java.util.ArrayList;

public class Locks extends ResponseModel {

    @SerializedName("data")
    public ArrayList<Lock> locks;

    public ArrayList<Lock> getLocks() {
        return locks;
    }

    public void setLocks(ArrayList<Lock> locks) {
        this.locks = locks;
    }
}
