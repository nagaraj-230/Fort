package com.payoda.smartlock.locks.service;

import com.google.gson.Gson;
import com.payoda.smartlock.constants.ServiceUrl;
import com.payoda.smartlock.locks.model.Lock;
import com.payoda.smartlock.locks.model.LockKeys;
import com.payoda.smartlock.locks.model.Locks;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.utils.Logger;

import java.util.Locale;

/**
 * Created by nivetha.m on 6/14/2018.
 */

public class LockListService {

    private static LockListService instance;
    private ResponseHandler responseHandler;

    private LockListService() {
    }

    /**
     * Creates Singleton Class and return current instance.
     *
     * @return Current instance of this class;
     */
    public static LockListService getInstance() {
        if (instance == null) {
            instance = new LockListService();
        }
        return instance;
    }

    private ResponseHandler handler = new ResponseHandler() {
        @Override
        public void onSuccess(Object data) {
            try {
                if (responseHandler != null) {
                    if (data != null) {
                        Locks locks = new Gson().fromJson(data.toString(), Locks.class);
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

    public void getLock(int limit,int offset,ResponseHandler handler) {
        this.responseHandler = handler;
        ServiceManager.getInstance().get(String.format(Locale.ENGLISH, ServiceUrl.LOCK,
                String.valueOf(limit),String.valueOf(offset)), null, this.handler);
    }

    public void updateLock(Lock lock, ResponseHandler handler) {
        this.responseHandler = handler;
        ServiceManager.getInstance().post(ServiceUrl.UPDATE_LOCK + lock.getId(), lock, this.handler);
    }

}
