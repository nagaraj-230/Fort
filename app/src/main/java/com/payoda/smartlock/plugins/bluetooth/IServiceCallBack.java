package com.payoda.smartlock.plugins.bluetooth;

import android.bluetooth.BluetoothGattService;

public interface IServiceCallBack {

    void onServiceConnected(BluetoothGattService service);

    void onServiceDisconnected();

}
