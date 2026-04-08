package com.payoda.smartlock.users.model;

import com.google.gson.annotations.SerializedName;
import com.payoda.smartlock.plugins.network.ResponseModel;

import java.io.Serializable;

/**
 * Created by david on 6/19/2018.
 */

public class AssignUserResponse  extends ResponseModel implements Serializable {
    @SerializedName("data")
    private AddUser addUser;

    public AddUser getAddUser() {
        return addUser;
    }

    public void setAddUser(AddUser addUser) {
        this.addUser = addUser;
    }
}
