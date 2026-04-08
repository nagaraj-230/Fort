package com.payoda.smartlock.model;

import com.payoda.smartlock.fp.model.FPUser;
import com.payoda.smartlock.locks.model.Lock;
import com.payoda.smartlock.locks.model.LockKeys;

import java.util.ArrayList;

public class ScreenData {

    private Lock lock;
    private FPUser fpUser;
    private LockKeys lockKeys;
    private ArrayList<LockKeys> fpExistingList;

    public Lock getLock() {
        return lock;
    }

    public void setLock(Lock lock) {
        this.lock = lock;
    }

    public LockKeys getLockKeys() {
        return lockKeys;
    }

    public void setLockKeys(LockKeys lockKeys) {
        this.lockKeys = lockKeys;
    }

    public FPUser getFpUser() {
        return fpUser;
    }

    public void setFpUser(FPUser fpUser) {
        this.fpUser = fpUser;
    }

    public ArrayList<LockKeys> getFpExistingList() {
        return fpExistingList;
    }

    public void setFpExistingList(ArrayList<LockKeys> fpExistingList) {
        this.fpExistingList = fpExistingList;
    }
}
