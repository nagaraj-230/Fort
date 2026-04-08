package com.payoda.smartlock.plugins.bluetooth;

public interface ICharCallBack {


    void readCallBack(String data);

    void writeCallback(String data);

    void onAuthFailure();

    void onFailure();
}
