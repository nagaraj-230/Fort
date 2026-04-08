package com.payoda.smartlock.request.callback;

import com.payoda.smartlock.request.model.Request;

/**
 * Created by david on 6/16/2018.
 */

public interface IRequestCallback {
    public void onRequestAction(Request request, boolean isAccepted);
}
