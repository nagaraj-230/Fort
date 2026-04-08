package com.payoda.smartlock.locks.service;

import com.payoda.smartlock.constants.ServiceUrl;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.utils.Logger;

public class EngageLockService {
    private static EngageLockService instance;
    private ResponseHandler responseHandler;

    private EngageLockService(){

    }

    /**
     * Creates Singleton Class and return current instance.
     *
     * @return Current instance of this class;
     */
    public static EngageLockService getInstance() {
        if (instance == null) {
            instance = new EngageLockService();
        }
        return instance;
    }

    private ResponseHandler handler = new ResponseHandler() {
        @Override
        public void onSuccess(Object data) {
            try {
                if (responseHandler != null) {
                    if (data != null) {
                        responseHandler.onSuccess(data);
                    }
                }
            } catch (Exception e) {
                Logger.e(e);
                if (responseHandler != null) {
                    responseHandler.onError(ServiceManager.getInstance().getErrorMessage(e));
                }
            }
        }

        @Override
        public void onAuthError(String message) {
            if (responseHandler != null) {
                responseHandler.onAuthError(message);
            }
        }

        @Override
        public void onError(String message) {
            if (responseHandler != null) {
                responseHandler.onError(message);
            }
        }
    };

    public void engageLockMqtt(String serialNumber, ResponseHandler handler) {
        this.responseHandler = handler;
        ServiceManager.getInstance().post(String.format(ServiceUrl.ENGAGE_LOCK_MQTT,serialNumber), null, handler);
    }
}
