package com.payoda.smartlock.users.service;

import com.google.gson.Gson;
import com.payoda.smartlock.constants.ServiceUrl;
import com.payoda.smartlock.locks.model.LockKeys;
import com.payoda.smartlock.locks.model.Locks;
import com.payoda.smartlock.notification.service.NotificationService;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.users.model.AssignUser;
import com.payoda.smartlock.users.model.RequestUser;
import com.payoda.smartlock.utils.Logger;

import java.util.ArrayList;

/**
 * Created by david on 6/16/2018.
 */

public class AssignRequestUserService {
    private static AssignRequestUserService instance;
    private ResponseHandler responseHandler;

    private AssignRequestUserService() {
    }

    /**
     * Creates Singleton Class and return current instance.
     *
     * @return Current instance of this class;
     */
    public static AssignRequestUserService getInstance() {
        if (instance == null) {
            instance = new AssignRequestUserService();
        }
        return instance;
    }
    private ResponseHandler handler = new ResponseHandler() {
        @Override
        public void onSuccess(Object data) {
            try {
                if (responseHandler != null) {
                    if (data != null) {
                        AssignUser assignUser = new Gson().fromJson(data.toString(), AssignUser.class);
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

    public void getAssignUserList(String lockId,String owner,String type,ResponseHandler handler) {
        this.responseHandler = handler;
        ServiceManager.getInstance().get(String.format(ServiceUrl.ASSIGN_USER,lockId,owner,type) , null, this.handler);
    }

    public void revokeAccessMqtt(String lockUniqueId, String userType, String userId, ResponseHandler handler){
        this.responseHandler = handler;
        if(userType.equalsIgnoreCase("master")){
            ServiceManager.getInstance().patch( String.format(  ServiceUrl.REVOKE_MASTER_MQTT, lockUniqueId, userId), null, this.handler);
        }else {
            ServiceManager.getInstance().patch( String.format(  ServiceUrl.REVOKE_USER_MQTT, lockUniqueId, userId), null, this.handler);
        }
    }
}
