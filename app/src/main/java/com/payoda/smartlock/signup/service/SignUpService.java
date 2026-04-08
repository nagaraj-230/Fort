package com.payoda.smartlock.signup.service;

import com.google.gson.Gson;
import com.payoda.smartlock.authentication.model.Login;
import com.payoda.smartlock.constants.ServiceUrl;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ResponseModel;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.users.model.User;
import com.payoda.smartlock.utils.Logger;

/**
 * This class is responsible for handling request and response for Login.
 * Created by david
 */

public class SignUpService {

    private static SignUpService instance;
    private ResponseHandler responseHandler;

    private SignUpService() {
    }

    /**
     * Creates Singleton Class and return current instance.
     * @return Current instance of this class;
     */

    public static SignUpService getInstance() {
        if (instance == null) {
            instance = new SignUpService();
        }
        return instance;
    }

    private ResponseHandler handler = new ResponseHandler() {
        @Override
        public void onSuccess(Object data) {
            try {
                if (responseHandler != null) {
                    if (data != null) {
                        ResponseModel login = new Gson().fromJson(data.toString(), ResponseModel.class);
                        responseHandler.onSuccess(login);
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

    public void serviceRequest(User model, ResponseHandler handler) {
        this.responseHandler = handler;
        ServiceManager.getInstance().post(ServiceUrl.SIGNUP, model, this.handler);
    }

}
