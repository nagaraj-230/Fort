package com.payoda.smartlock.fp.model;

import java.util.ArrayList;

public class FingerPrints {
    private ArrayList<FPUser> fpUsers;

    public ArrayList<FPUser> getFpUsers() {
        return fpUsers;
    }

    public void setFpUsers(ArrayList<FPUser> fpUsers) {
        this.fpUsers = fpUsers;
    }
}
