package com.payoda.smartlock.authentication.service;

import com.google.gson.Gson;
import com.payoda.smartlock.authentication.model.Login;
import com.payoda.smartlock.constants.ServiceUrl;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.utils.Logger;

/**
 * This class is responsible for handling request and response for Login.
 * Created by david.
 */
public class LoginService {

    private static LoginService instance;
    private ResponseHandler responseHandler;

    private LoginService() {
    }

    /**
     * Creates Singleton Class and return current instance.
     *
     * @return Current instance of this class;
     */
    public static LoginService getInstance() {
        if (instance == null) {
            instance = new LoginService();
        }
        return instance;
    }

    private ResponseHandler handler = new ResponseHandler() {
        @Override
        public void onSuccess(Object data) {

            Logger.d("###  login response : " + data);
            try {

                if (responseHandler != null) {
                    if (data != null) {
                        Login login = new Gson().fromJson(data.toString(), Login.class);
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
            Logger.d("###  login onError : " + message);
            if (responseHandler != null) {
                responseHandler.onError(message);
            }
        }
    };

    public void serviceRequest(Login login, ResponseHandler handler) {
        this.responseHandler = handler;
        ServiceManager.getInstance().post(ServiceUrl.LOGIN, login, this.handler);
    }
}
