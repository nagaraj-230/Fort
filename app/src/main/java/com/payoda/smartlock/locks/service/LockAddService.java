package com.payoda.smartlock.locks.service;

import com.google.gson.Gson;
import com.payoda.smartlock.constants.ServiceUrl;
import com.payoda.smartlock.locks.model.Lock;
import com.payoda.smartlock.locks.model.LockAddResponse;
import com.payoda.smartlock.locks.model.Locks;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.utils.Logger;

import java.util.Locale;

/**
 * Created by david on 29/11/18.
 */

public class LockAddService {
    private static LockAddService instance;
    private ResponseHandler responseHandler;

    private LockAddService() {
    }

    /**
     * Creates Singleton Class and return current instance.
     *
     * @return Current instance of this class;
     */
    public static LockAddService getInstance() {
        if (instance == null) {
            instance = new LockAddService();
        }
        return instance;
    }

    private ResponseHandler handler = new ResponseHandler() {
        @Override
        public void onSuccess(Object data) {
            try {
                if (responseHandler != null) {
                    if (data != null) {
                        LockAddResponse locks = new Gson().fromJson(data.toString(), LockAddResponse.class);
                        responseHandler.onSuccess(locks);
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


    public void addLock(Lock lock, ResponseHandler handler) {
        this.responseHandler = handler;
        ServiceManager.getInstance().post(ServiceUrl.ADD_LOCK, lock, this.handler);
    }

    public void addNewLock(Lock lock, ResponseHandler handler) {
        this.responseHandler = handler;
        ServiceManager.getInstance().post(ServiceUrl.ADD_LOCK_NEW, lock, this.handler);
    }


}
