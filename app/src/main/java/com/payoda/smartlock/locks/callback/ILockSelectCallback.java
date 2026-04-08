package com.payoda.smartlock.locks.callback;

import com.payoda.smartlock.locks.model.Lock;

/**
 * Created by david on 6/12/2018.
 */

public interface ILockSelectCallback {
    void onLockItemSelect(Lock lock);
}
