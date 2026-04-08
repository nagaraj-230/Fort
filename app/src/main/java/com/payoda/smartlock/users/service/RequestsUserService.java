package com.payoda.smartlock.users.service;

import com.google.gson.Gson;
import com.payoda.smartlock.constants.ServiceUrl;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.users.model.AssignUser;
import com.payoda.smartlock.users.model.AssignUserRequest;
import com.payoda.smartlock.users.model.AssignUserResponse;
import com.payoda.smartlock.users.model.RequestUser;
import com.payoda.smartlock.utils.Logger;

import org.json.JSONObject;

/**
 * Created by david on 6/19/2018.
 */

public class RequestsUserService {

    private static RequestsUserService instance;
    private ResponseHandler responseHandler;

    private RequestsUserService() {
    }

    /**
     * Creates Singleton Class and return current instance.
     *
     * @return Current instance of this class;
     */
    public static RequestsUserService getInstance() {
        if (instance == null) {
            instance = new RequestsUserService();
        }
        return instance;
    }

    private ResponseHandler handler = new ResponseHandler() {
        @Override
        public void onSuccess(Object data) {
            try {
                if (responseHandler != null) {
                    if (data != null) {
                        AssignUserResponse assignUserResponse = new Gson().fromJson(data.toString(), AssignUserResponse.class);
                        responseHandler.onSuccess(assignUserResponse);
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

    public void createRequest(RequestUser requestUser, ResponseHandler handler) {
        this.responseHandler = handler;
        ServiceManager.getInstance().post(ServiceUrl.CREATE_REQUEST, requestUser, this.handler);
    }

    public void updateRequest(AssignUserRequest assignUserRequest, String requestId, ResponseHandler handler) {
        this.responseHandler = handler;
        ServiceManager.getInstance().post(ServiceUrl.UPDATE_REQUEST + requestId, assignUserRequest, this.handler);
    }

    public void revokeRequest(RequestUser requestUser, String requestId, ResponseHandler handler) {
        this.responseHandler = handler;
        ServiceManager.getInstance().post(ServiceUrl.REVOKE_REQUEST + requestId, requestUser, this.handler);
    }

}
