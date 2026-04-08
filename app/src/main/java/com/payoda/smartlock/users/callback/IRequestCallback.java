package com.payoda.smartlock.users.callback;

import com.payoda.smartlock.locks.model.Lock;

/**
 * Created by david on 6/16/2018.
 */

public interface IRequestCallback {
    public void onRequestAction(Lock lock,boolean isAccepted);
}
