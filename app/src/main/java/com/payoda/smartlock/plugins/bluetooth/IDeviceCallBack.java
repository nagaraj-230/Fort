package com.payoda.smartlock.plugins.bluetooth;

public interface IDeviceCallBack {

    void onDeviceConnected();

    void onServiceDiscovered();

    void onAuthFailure();

    void onDeviceDisconnected();

    void onError(String error);

    void onLocationPermissionError(String error);

    void onPermissionError(String error);

    void onUpdateMacAddress(String ssid, String macAddress);

}
