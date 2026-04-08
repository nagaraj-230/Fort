package com.payoda.smartlock.locks.service;

import com.payoda.smartlock.constants.ServiceUrl;
import com.payoda.smartlock.locks.model.LockKeys;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.utils.Logger;

/**
 * Created by sambathkumar.k
 */

public class WifiMqttConfigurationService {

    private static WifiMqttConfigurationService instance;
    private ResponseHandler responseHandler;

    private WifiMqttConfigurationService() {
    }

    /**
     * Creates Singleton Class and return current instance.
     *
     * @return Current instance of this class;
     */
    public static WifiMqttConfigurationService getInstance() {
        if (instance == null) {
            instance = new WifiMqttConfigurationService();
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

    //To get given wifi ssid and password  is connected to lock or not by remote server
    public void pushWifiConfig(Object obj, ResponseHandler handler) {
        this.responseHandler = handler;
        ServiceManager.getInstance().post(ServiceUrl.PUSH_WIFI_CONFIG_STATUS , obj, this.handler);
    }

}
