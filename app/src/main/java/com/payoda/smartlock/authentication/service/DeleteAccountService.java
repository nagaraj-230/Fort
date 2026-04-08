package com.payoda.smartlock.authentication.service;

import com.google.gson.Gson;
import com.payoda.smartlock.constants.ServiceUrl;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ResponseModel;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.utils.Logger;

public class DeleteAccountService {
    private static DeleteAccountService instance;
    private ResponseHandler responseHandler;

    private DeleteAccountService() {
    }

    /**
     * Creates Singleton Class and return current instance.
     *
     * @return Current instance of this class;
     */
    public static DeleteAccountService getInstance() {
        if (instance == null) {
            instance = new DeleteAccountService();
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

    public void serviceRequest(ResponseHandler handler) {
        this.responseHandler = handler;
        ServiceManager.getInstance().delete( ServiceUrl.DELETE_ACCOUNT, null, this.handler);
    }
}
