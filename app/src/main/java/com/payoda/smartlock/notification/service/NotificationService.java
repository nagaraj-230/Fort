package com.payoda.smartlock.notification.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.payoda.smartlock.constants.ServiceUrl;
import com.payoda.smartlock.locks.model.Lock;
import com.payoda.smartlock.locks.model.Locks;
import com.payoda.smartlock.locks.service.LockListService;
import com.payoda.smartlock.notification.model.Notification;
import com.payoda.smartlock.notification.model.NotificationList;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.utils.Logger;

import java.util.Locale;

/**
 * Created by nivetha.m on 6/16/2018.
 */

public class NotificationService {
    private static NotificationService instance;
    private ResponseHandler responseHandler;

    private NotificationService() {
    }

    /**
     * Creates Singleton Class and return current instance.
     *
     * @return Current instance of this class;
     */
    public static NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }

    private ResponseHandler handler = new ResponseHandler() {
        @Override
        public void onSuccess(Object data) {
            try {
                if (responseHandler != null) {
                    if (data != null) {
                        NotificationList notification = new Gson().fromJson(data.toString(), NotificationList.class);
                        responseHandler.onSuccess(notification);
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

    public void getResponse(String limit,String offset,ResponseHandler handler) {
        this.responseHandler = handler;
        ServiceManager.getInstance().get(String.format(Locale.ENGLISH,ServiceUrl.NOTIFICATION,limit,offset), null, this.handler);
    }

}
