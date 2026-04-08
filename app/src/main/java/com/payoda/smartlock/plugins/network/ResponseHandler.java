package com.payoda.smartlock.plugins.network;

/**
 * ResponseHandler to handle the success, authentication error and general failure functionality.
 * Created by david.
 */
public interface ResponseHandler {
    /**
     * Handle success data.
     *
     * @param data response object.
     */
    void onSuccess(Object data);

    /**
     * Handle success data.
     *
     * @param message Authentication failure message.
     */
    void onAuthError(String message);

    /**
     * Handle success data.
     *
     * @param message General failure message.
     */
    void onError(String message);
}
