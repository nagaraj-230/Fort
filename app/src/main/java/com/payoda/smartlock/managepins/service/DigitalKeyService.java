package com.payoda.smartlock.managepins.service;

import com.google.gson.Gson;
import com.payoda.smartlock.constants.ServiceUrl;
import com.payoda.smartlock.managepins.model.PinRequest;
import com.payoda.smartlock.managepins.model.ReWritePinMqtt;
import com.payoda.smartlock.model.BaseResponse;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.utils.Logger;

/**
 * Created by Mohamed Shah on 21/05/21.
 */

public class DigitalKeyService {
    private static DigitalKeyService instance;
    private ResponseHandler responseHandler;

    private DigitalKeyService() {
    }

    /**
     * Creates Singleton Class and return current instance.
     *
     * @return Current instance of this class;
     */
    public static DigitalKeyService getInstance() {
        if (instance == null) {
            instance = new DigitalKeyService();
        }
        return instance;
    }

    private ResponseHandler handler = new ResponseHandler() {
        @Override
        public void onSuccess(Object data) {
            try {
                if (responseHandler != null) {
                    if (data != null) {
                        BaseResponse response = new Gson().fromJson(data.toString(), BaseResponse.class);
                        responseHandler.onSuccess(response);
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

    public void addUpdateDigiPin(PinRequest pinRequest, ResponseHandler handler) {

        this.responseHandler = handler;
        ServiceManager.getInstance().post(ServiceUrl.ADD_PINS, pinRequest, this.handler);

    }

    public void addUpdateDigiPinMqtt(String lockUniqueId, ReWritePinMqtt reWritePinMqtt, ResponseHandler handler) {
        this.responseHandler = handler;
        ServiceManager.getInstance().post(String.format(ServiceUrl.REWRITE_PIN_MQTT, lockUniqueId), reWritePinMqtt, this.handler);
    }


}
