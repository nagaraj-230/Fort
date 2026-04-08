package com.payoda.smartlock.users.model;

import com.payoda.smartlock.locks.model.LockKeys;

/**
 * Created by david on 11/12/18.
 */

public class ScheduleUserKeys {

    private String lockUserType;
    private LockKeys lockKeys = null;
    private boolean isEditable;

    public String getLockUserType() {
        return lockUserType;
    }

    public void setLockUserType(String lockUserType) {
        this.lockUserType = lockUserType;
    }

    public LockKeys getLockKeys() {
        return lockKeys;
    }

    public void setLockKeys(LockKeys lockKeys) {
        this.lockKeys = lockKeys;
    }

    public boolean isEditable() {
        return isEditable;
    }

    public void setEditable(boolean editable) {
        isEditable = editable;
    }
}
