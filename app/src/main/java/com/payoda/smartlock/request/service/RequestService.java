package com.payoda.smartlock.request.service;

import com.google.gson.Gson;
import com.payoda.smartlock.constants.ServiceUrl;
import com.payoda.smartlock.locks.model.LockKeys;
import com.payoda.smartlock.model.RemoteAccessModel;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ResponseModel;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.request.model.Request;
import com.payoda.smartlock.request.model.RequestAccept;
import com.payoda.smartlock.request.model.RequestList;
import com.payoda.smartlock.utils.Logger;

import org.json.JSONObject;

import java.util.Locale;


public class RequestService {
    private static RequestService instance;
    private ResponseHandler responseHandler;

    private RequestService() {
    }
    public static RequestService getInstance() {
        if (instance == null) {
            instance = new RequestService();
        }
        return instance;
    }
    private ResponseHandler handler = new ResponseHandler() {
        @Override
        public void onSuccess(Object data) {
            try {
                if (responseHandler != null) {
                    if (data != null) {
                        RequestList request = new Gson().fromJson(data.toString(), RequestList.class);
                        responseHandler.onSuccess(request);
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
    private ResponseHandler mResponseHandler = new ResponseHandler() {
        @Override
        public void onSuccess(Object data) {
            try {
                if (responseHandler != null) {
                    if (data != null) {
                        ResponseModel  response= new Gson().fromJson(data.toString(), ResponseModel.class);
                        responseHandler.onSuccess(response);
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

    public void getRequest(String limit, String offset,ResponseHandler handler) {
        this.responseHandler = handler;
        ServiceManager.getInstance().get(String.format(Locale.ENGLISH,ServiceUrl.REQUEST_LIST,limit,offset), null, this.handler);
    }

    public void acceptRequest(String id, RequestAccept data, ResponseHandler handler) {
        this.responseHandler = handler;
        ServiceManager.getInstance().post(ServiceUrl.TRANSFER_OWNER_ACCEPT + id, data, this.mResponseHandler);
    }

    public void acceptRequestViaMqtt(String lockUniqueId, String requestId, ResponseHandler handler) {
        this.responseHandler = handler;
        ServiceManager.getInstance().post(String.format(  ServiceUrl.TRANSFER_OWNER_ACCEPT_MQTT, lockUniqueId, requestId), null, this.mResponseHandler);
    }

    public void rejectRequest(String id, ResponseModel data, ResponseHandler handler) {
        this.responseHandler = handler;
        ServiceManager.getInstance().post(ServiceUrl.TRANSFER_OWNER_REJECT + id, data, this.mResponseHandler);
    }

    public void remoteAccess( RemoteAccessModel data, ResponseHandler handler) {
        this.responseHandler = handler;
        ServiceManager.getInstance().post(ServiceUrl.REMOTE_ACCESS , data, this.mResponseHandler);
    }

}
