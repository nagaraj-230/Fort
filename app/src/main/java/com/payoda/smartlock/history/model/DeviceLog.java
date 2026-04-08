package com.payoda.smartlock.history.model;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by david on 10/09/18.
 */

public class DeviceLog {
    private HashMap<String,ArrayList<String>> logs=new HashMap();

    public HashMap<String,ArrayList<String>> getLogs() {
        return logs;
    }

    @Override
    public String toString() {
        return "DeviceLog{" +
                "logs=" + logs +
                '}';
    }

}
