package com.payoda.smartlock.users.model;

import java.util.HashMap;

/**
 * Created by david on 11/09/18.
 */

public class RevokeUserList {
    private HashMap<String,RequestUser> requestUserHashMap=new HashMap<>();

    public HashMap<String, RequestUser> getRequestUserHashMap() {
        return requestUserHashMap;
    }

    public void setRequestUserHashMap(HashMap<String, RequestUser> requestUserHashMap) {
        this.requestUserHashMap = requestUserHashMap;
    }
}
