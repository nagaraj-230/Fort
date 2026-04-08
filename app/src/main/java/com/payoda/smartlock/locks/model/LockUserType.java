package com.payoda.smartlock.locks.model;

public enum LockUserType {
    OwnerId ("OwnerID"),
    Owner ("Owner"),
    Master("Master"),
    User("User");

    LockUserType(String s) {
    }
}
