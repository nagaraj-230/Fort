package com.payoda.smartlock.password.service;

import com.google.gson.Gson;
import com.payoda.smartlock.password.model.ForgotPassword;
import com.payoda.smartlock.constants.ServiceUrl;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.utils.Logger;

public class ForgotPasswordService {
    private static ForgotPasswordService instance;
    private ResponseHandler responseHandler;

    private ForgotPasswordService() {
    }

    /**
     * Creates Singleton Class and return current instance.
     *
     * @return Current instance of this class;
     */
    public static ForgotPasswordService getInstance() {
        if (instance == null) {
            instance = new ForgotPasswordService();
        }
        return instance;
    }

    private ResponseHandler handler = new ResponseHandler() {
        @Override
        public void onSuccess(Object data) {
            try {
                if (responseHandler != null) {
                    if (data != null) {
                        ForgotPassword password = new Gson().fromJson(data.toString(), ForgotPassword.class);
                        responseHandler.onSuccess(password);
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

    public void serviceRequest(ForgotPassword password, ResponseHandler handler) {
        this.responseHandler = handler;
        ServiceManager.getInstance().post(ServiceUrl.FORGOT_PASSWORD, password, this.handler);
    }
}
