package com.payoda.smartlock.locks.callback;

import java.util.ArrayList;

public interface IKeyListener {

    void onLockIds(ArrayList<String> alIds);

    void onLockKeys(ArrayList<String> alKeys);

    void onBatteryUpdate(String battery);

    void onAccessLog(ArrayList<String> alLogs);

    void onLockActivated();

    void onDeviceNotConnected();

    void onMacAddressUpdate(String ssid, String macAddress);
}
