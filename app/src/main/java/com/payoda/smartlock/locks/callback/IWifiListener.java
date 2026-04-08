package com.payoda.smartlock.locks.callback;

import com.payoda.smartlock.locks.model.Lock;

public interface IWifiListener {
    void onLockActivated(Lock lock);
}
