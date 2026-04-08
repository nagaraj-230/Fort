package com.payoda.smartlock.splash.service;

import com.google.gson.Gson;
import com.payoda.smartlock.constants.ServiceUrl;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.splash.model.BrandInfoResponse;
import com.payoda.smartlock.utils.Logger;

/**
 * This class is responsible for handling request and response for Login.
 * Created by Shah.
 */

public class BrandService {
    private static BrandService instance;
    private ResponseHandler responseHandler;

    private BrandService() {
    }

    /**
     * Creates Singleton Class and return current instance.
     *
     * @return Current instance of this class;
     */
    public static BrandService getInstance() {
        if (instance == null) {
            instance = new BrandService();
        }
        return instance;
    }

    private ResponseHandler handler = new ResponseHandler() {
        @Override
        public void onSuccess(Object data) {
            try {
                if (responseHandler != null) {
                    if (data != null) {
                        BrandInfoResponse brandInfoResponse = new Gson().fromJson(data.toString(), BrandInfoResponse.class);
                        responseHandler.onSuccess(brandInfoResponse);
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
        ServiceManager.getInstance().get(ServiceUrl.BRAND_INFO, null, this.handler);
    }
}
