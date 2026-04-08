package com.payoda.smartlock.users.model;

import com.google.gson.annotations.SerializedName;
import com.payoda.smartlock.locks.model.Lock;
import com.payoda.smartlock.locks.model.LockKeys;
import com.payoda.smartlock.plugins.network.ResponseModel;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by david on 6/19/2018.
 */

public class AssignUser extends ResponseModel implements Serializable {
    @SerializedName("data")
    public ArrayList<LockKeys> keyList;

    public ArrayList<LockKeys> getKeys() {
        return keyList;
    }

    public void setKeys(ArrayList<LockKeys> keyList) {
        this.keyList = keyList;
    }


}
