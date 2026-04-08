package com.payoda.smartlock.locks.model;

public enum LockFlow {
    ENGAGE("0"),
    TRANSFER_OWNER("1"),
    REVOKE_KEY("2");

    LockFlow(String s) {
    }
}
