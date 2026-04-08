package com.payoda.smartlock.fp.service;

import com.google.gson.JsonObject;
import com.payoda.smartlock.constants.ServiceUrl;
import com.payoda.smartlock.fp.model.FPUser;
import com.payoda.smartlock.locks.model.LockKeys;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;

public class FPService {

    private static FPService instance;

    private FPService() {
    }

    /**
     * Creates Singleton Class and return current instance.
     *
     * @return Current instance of this class;
     */
    public static FPService getInstance() {
        if (instance == null) {
            instance = new FPService();
        }
        return instance;
    }

    public void addFPRequest(FPUser fpUser, ResponseHandler handler) {
        ServiceManager.getInstance().post(ServiceUrl.ADD_FP, fpUser, handler);
    }

    public void getUserList(String lockId, ResponseHandler handler) {
        ServiceManager.getInstance().get(ServiceUrl.USER_LIST+lockId,null, handler);
    }

    public void updateGuestName(LockKeys lockKeys, ResponseHandler handler) {
        ServiceManager.getInstance().post(ServiceUrl.UPDATE_NAME, lockKeys, handler);
    }

    public void addOrRevokePrivilege(JsonObject payload, ResponseHandler handler) {
        ServiceManager.getInstance().post(ServiceUrl.ADD_PRIVILEGE, payload, handler);
    }

    public void revokeFpIdPrivilegeMqtt(String lockUniqueId, String keyId, String fId, ResponseHandler handler) {
        ServiceManager.getInstance().delete(String.format( ServiceUrl.DELETE_FP_MQTT, lockUniqueId, keyId, fId), null, handler);
    }

    public void revokeRfidPrivilegeMqtt(String lockUniqueId, int rfid, ResponseHandler handler) {
        ServiceManager.getInstance().delete(String.format(  ServiceUrl.DELETE_RFID_MQTT, lockUniqueId, rfid), null, handler);
    }
}
