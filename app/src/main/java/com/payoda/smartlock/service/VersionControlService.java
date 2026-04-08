package com.payoda.smartlock.service;

import com.google.gson.Gson;
import com.payoda.smartlock.constants.ServiceUrl;
import com.payoda.smartlock.model.VersionControlModel;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.utils.Logger;

public class VersionControlService {

    private static VersionControlService instance;

    private ResponseHandler responseHandler;

    private VersionControlService() {
    }

    public static VersionControlService getInstance() {
        if (instance == null) {
            instance = new VersionControlService();
        }
        return instance;
    }

    private  ResponseHandler handler = new ResponseHandler() {
        @Override
        public void onSuccess(Object data) {
            try {
                if (responseHandler != null) {
                    if (data != null) {
                        VersionControlModel versionControlModel = new Gson().fromJson(data.toString(), VersionControlModel.class);
                        responseHandler.onSuccess(versionControlModel);
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

    public void getVersionControl(ResponseHandler handler) {
        this.responseHandler = handler;
        ServiceManager.getInstance().get(ServiceUrl.GET_VERSION_CONTROL , null, this.handler);
    }
}
