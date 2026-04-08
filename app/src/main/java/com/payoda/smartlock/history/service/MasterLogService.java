package com.payoda.smartlock.history.service;

import com.google.gson.Gson;
import com.payoda.smartlock.constants.ServiceUrl;
import com.payoda.smartlock.history.model.AccessLogList;
import com.payoda.smartlock.history.model.AccessLogResponse;
import com.payoda.smartlock.history.model.HistoryList;
import com.payoda.smartlock.history.model.LockNotificationList;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.utils.Logger;

import java.util.Locale;

/**
 * Created by david on 20/11/18.
 */

public class MasterLogService {
    private static MasterLogService instance;
    private ResponseHandler responseHandler;

    private MasterLogService() {
    }

    /**
     * Creates Singleton Class and return current instance.
     *
     * @return Current instance of this class;
     */
    public static MasterLogService getInstance() {
        if (instance == null) {
            instance = new MasterLogService();
        }
        return instance;
    }



    private ResponseHandler notificationHandler = new ResponseHandler() {
        @Override
        public void onSuccess(Object data) {
            try {
                if (responseHandler != null) {
                    if (data != null) {
                        LockNotificationList history = new Gson().fromJson(data.toString(), LockNotificationList.class);
                        responseHandler.onSuccess(history);
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


    public void serviceNotificationRequest(String id,int limit,int offset,ResponseHandler handler) {
        this.responseHandler = handler;
        ServiceManager.getInstance().get(String.format(Locale.ENGLISH,ServiceUrl.GET_LOCK_NOTIFICATION,id,String.valueOf(limit),
                String.valueOf(offset)), null, this.notificationHandler);
    }

}
