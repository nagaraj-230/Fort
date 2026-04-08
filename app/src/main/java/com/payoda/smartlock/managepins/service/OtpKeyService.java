package com.payoda.smartlock.managepins.service;

import com.google.gson.Gson;
import com.payoda.smartlock.constants.ServiceUrl;
import com.payoda.smartlock.locks.model.LockKeys;
import com.payoda.smartlock.managepins.model.OtpRequest;
import com.payoda.smartlock.managepins.model.PinRequest;
import com.payoda.smartlock.model.BaseResponse;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.utils.Logger;

import java.util.Locale;

/**
 * Created by Mohamed Shah on 22/05/21.
 */

public class OtpKeyService {
    private static OtpKeyService instance;
    private ResponseHandler responseHandler;

    private OtpKeyService() {
    }

    /**
     * Creates Singleton Class and return current instance.
     *
     * @return Current instance of this class;
     */
    public static OtpKeyService getInstance() {
        if (instance == null) {
            instance = new OtpKeyService();
        }
        return instance;
    }

    private ResponseHandler handler = new ResponseHandler() {
        @Override
        public void onSuccess(Object data) {
            try {
                if (responseHandler != null) {
                    if (data != null) {
                        LockKeys response = new Gson().fromJson(data.toString(), LockKeys.class);
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

    public void getOtp(String lockId, String next, ResponseHandler handler){
        this.responseHandler = handler;
        ServiceManager.getInstance().get(String.format(ServiceUrl.GET_OTP,lockId,next),null,handler);
    }


}
