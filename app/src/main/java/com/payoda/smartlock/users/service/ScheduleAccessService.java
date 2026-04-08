package com.payoda.smartlock.users.service;

import com.google.gson.Gson;
import com.payoda.smartlock.constants.ServiceUrl;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.users.model.AssignUser;
import com.payoda.smartlock.users.model.Schedule;
import com.payoda.smartlock.users.model.ScheduleResponse;
import com.payoda.smartlock.utils.Logger;

/**
 * Created by david on 10/12/18.
 */

public class ScheduleAccessService {
    private static ScheduleAccessService instance;
    private ResponseHandler responseHandler;

    private ScheduleAccessService() {
    }

    /**
     * Creates Singleton Class and return current instance.
     *
     * @return Current instance of this class;
     */
    public static ScheduleAccessService getInstance() {
        if (instance == null) {
            instance = new ScheduleAccessService();
        }
        return instance;
    }
    private ResponseHandler handler = new ResponseHandler() {
        @Override
        public void onSuccess(Object data) {
            try {
                if (responseHandler != null) {
                    if (data != null) {
                        ScheduleResponse assignUser = new Gson().fromJson(data.toString(), ScheduleResponse.class);
                        responseHandler.onSuccess(assignUser);
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

    public void saveSchedule(String keyId, Schedule schedule, ResponseHandler handler) {
        this.responseHandler = handler;
        ServiceManager.getInstance().post(ServiceUrl.SCHEDULE+keyId , schedule, this.handler);
    }
}
