package com.payoda.smartlock.plugins.bluetooth;

import java.util.ArrayList;

public interface BleResponseHandler {

    void onScanResult(ArrayList<BleDevice> devices);

    void onTimeOut();

    void onError(String error);

    void onLocationPermissionError(String error);

    void onPermissionError(String error);

}
