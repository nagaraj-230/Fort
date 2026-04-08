package com.payoda.smartlock.managepins.service;

import com.google.gson.Gson;
import com.payoda.smartlock.constants.ServiceUrl;
import com.payoda.smartlock.locks.model.LockKeys;
import com.payoda.smartlock.managepins.model.OtpRequest;
import com.payoda.smartlock.managepins.model.ReWriteOtpMqtt;
import com.payoda.smartlock.model.BaseResponse;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.utils.Logger;

/**
 * Created by Mohamed Shah on 22/05/21.
 */

public class OtpAddService {
    private static OtpAddService instance;
    private ResponseHandler responseHandler;

    private OtpAddService() {
    }

    /**
     * Creates Singleton Class and return current instance.
     *
     * @return Current instance of this class;
     */
    public static OtpAddService getInstance() {
        if (instance == null) {
            instance = new OtpAddService();
        }
        return instance;
    }

    private ResponseHandler handler = new ResponseHandler() {
        @Override
        public void onSuccess(Object data) {
            try {
                if (responseHandler != null) {
                    if (data != null) {
                        BaseResponse response = new Gson().fromJson(data.toString(), BaseResponse.class);
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

    public void addOtp(OtpRequest otpRequest, ResponseHandler handler) {

        Logger.d("### OtpService addOTP");
        this.responseHandler = handler;
        ServiceManager.getInstance().post(ServiceUrl.ADD_OTPS, otpRequest, this.handler);

    }

    public void addOtpMqtt(String lockUniqueId, ReWriteOtpMqtt reWriteOtpMqtt, ResponseHandler handler) {
        this.responseHandler = handler;
        ServiceManager.getInstance().post(String.format(ServiceUrl.REWRITE_OTP_MQTT, lockUniqueId), reWriteOtpMqtt, this.handler);
    }
}
