package com.payoda.smartlock.locks.service;

import com.payoda.smartlock.constants.ServiceUrl;
import com.payoda.smartlock.locks.model.LockKeys;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.utils.Logger;

/**
 * Created by sambathkumar.k
 */

public class LockKeyService {

    private static LockKeyService instance;
    private ResponseHandler responseHandler;

    private LockKeyService() {
    }

    /**
     * Creates Singleton Class and return current instance.
     *
     * @return Current instance of this class;
     */
    public static LockKeyService getInstance() {
        if (instance == null) {
            instance = new LockKeyService();
        }
        return instance;
    }

    private ResponseHandler handler = new ResponseHandler() {
        @Override
        public void onSuccess(Object data) {
            try {
                if (responseHandler != null) {
                    if (data != null) {
                        responseHandler.onSuccess(data);
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

    public void transferKeys(LockKeys lock, ResponseHandler handler, String tag) {
        this.responseHandler = handler;
        String id=lock.getId();
        lock.setId(null);
        ServiceManager.getInstance().post(ServiceUrl.UPDATE_KEY +id, lock, this.handler, tag);
    }

}
