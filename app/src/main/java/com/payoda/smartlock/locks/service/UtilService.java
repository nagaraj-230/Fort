package com.payoda.smartlock.locks.service;

import com.google.gson.Gson;
import com.payoda.smartlock.constants.ServiceUrl;
import com.payoda.smartlock.locks.model.ServerTime;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.utils.Logger;

import java.util.Locale;

public class UtilService {
    private static UtilService instance;
    private ResponseHandler responseHandler;

    private UtilService() {
    }

    /**
     * Creates Singleton Class and return current instance.
     *
     * @return Current instance of this class;
     */
    public static UtilService getInstance() {
        if (instance == null) {
            instance = new UtilService();
        }
        return instance;
    }

    private ResponseHandler handler = new ResponseHandler() {
        @Override
        public void onSuccess(Object data) {
            try {
                if (responseHandler != null) {
                    if (data != null) {
                        ServerTime serverTime = new Gson().fromJson(data.toString(), ServerTime.class);

                        // ServerTime serverTime1=new ServerTime();
                        // serverTime1.setServerTime("yyyy-MM-dd HH:mm:ss"); need to write logic here
                        responseHandler.onSuccess(serverTime);
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



    //
    public void getServerTime(ResponseHandler handler){
        this.responseHandler =handler;
        // call new api
        ServiceManager.getInstance().get(String.format(Locale.ENGLISH,ServiceUrl.CURRENT), null, this.handler);

    }
}
