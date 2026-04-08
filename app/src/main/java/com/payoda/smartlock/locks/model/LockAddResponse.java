package com.payoda.smartlock.locks.model;

import com.google.gson.annotations.SerializedName;
import com.payoda.smartlock.plugins.network.ResponseModel;

/**
 * Created by david on 29/11/18.
 */

public class LockAddResponse extends ResponseModel{
    @SerializedName("data")
    private Lock data;

    public Lock getData() {
        return data;
    }

    public void setData(Lock data) {
        this.data = data;
    }
}
