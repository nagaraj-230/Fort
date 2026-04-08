package com.payoda.smartlock.transfer.service;

import com.google.gson.Gson;
import com.payoda.smartlock.constants.ServiceUrl;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.transfer.model.Transfer;
import com.payoda.smartlock.utils.Logger;

public class TransferService {
    private static TransferService instance;
    private ResponseHandler responseHandler;

    private TransferService() {
    }

    public static TransferService getInstance() {
        if (instance == null) {
            instance = new TransferService();
        }
        return instance;
    }

    private ResponseHandler handler = new ResponseHandler() {
        @Override
        public void onSuccess(Object data) {
            try {
                if (responseHandler != null) {
                    if (data != null) {
                        Transfer transfer = new Gson().fromJson(data.toString(), Transfer.class);
                        responseHandler.onSuccess(transfer);
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

    public void serviceRequest(Transfer transfer, ResponseHandler handler) {
        this.responseHandler = handler;
        ServiceManager.getInstance().post(ServiceUrl.TRANSFER_OWNER, transfer, this.handler);
    }
}
