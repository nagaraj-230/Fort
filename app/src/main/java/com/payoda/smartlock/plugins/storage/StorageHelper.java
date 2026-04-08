package com.payoda.smartlock.plugins.storage;

import android.content.Context;

import com.payoda.smartlock.authentication.model.Login;
import com.payoda.smartlock.fp.model.FingerPrints;
import com.payoda.smartlock.locks.model.LockKeyList;
import com.payoda.smartlock.locks.model.WifiMqttConnection;
import com.payoda.smartlock.managepins.model.OtpRequest;
import com.payoda.smartlock.managepins.model.PinRequest;
import com.payoda.smartlock.users.model.RequestUser;
import com.payoda.smartlock.users.model.RevokeUserList;
import com.payoda.smartlock.utils.Logger;

public class StorageHelper {

    private static StorageHelper instance=null;

    public static StorageHelper getInstance(){
        if(instance==null){
            instance=new StorageHelper();
        }
        return instance;
    }

    private void migrateToken(){
        String token=StorageManager.getInstance().getToken();
        if(token!=null) {
            SecuredStorageManager.getInstance().setToken(token);
        }
    }

    private void migrateDeviceToken(){
        String token=StorageManager.getInstance().getDeviceToken();
        if(token!=null) {
            SecuredStorageManager.getInstance().setDeviceToken(token);
        }
    }

    private void migrateOfflineFPs(){
        FingerPrints value=StorageManager.getInstance().getOfflineFPs();
        if(value!=null) {
            SecuredStorageManager.getInstance().setOfflineFPs(value);
        }
    }

    private void migrateOfflinePINs(){
        PinRequest value = StorageManager.getInstance().getOfflinePins();
        if(value != null){
            SecuredStorageManager.getInstance().saveOfflinePin(value);
        }
    }

    private void migrateOfflineOTPs(){

        OtpRequest value = StorageManager.getInstance().getOfflineOtps();
        if(value != null){
            SecuredStorageManager.getInstance().saveOfflineOtp(value);
        }

    }

    private void migrateRevokeUserList(){
        RevokeUserList value=StorageManager.getInstance().getRevokeUserList();
        if(value!=null && value.getRequestUserHashMap()!=null) {
            for (String requestId : value.getRequestUserHashMap().keySet()) {
                RequestUser requestUser = value.getRequestUserHashMap().get(requestId);
                SecuredStorageManager.getInstance().saveRevokeUserList(requestId, requestUser);
            }
        }

    }

    private void migrateOfflineKeys(){
        LockKeyList value=StorageManager.getInstance().getOfflineKeys();
        if(value!=null) {
            SecuredStorageManager.getInstance().setOfflineKeys(value);
        }
    }

    private void migrateUserData(){
        Login value=StorageManager.getInstance().getUserData();
        if(value!=null) {
            SecuredStorageManager.getInstance().setUserData(value);
        }
    }

    private void migratePin(){
        String value=StorageManager.getInstance().getPin();
        if(value!=null) {
            SecuredStorageManager.getInstance().setPin(value);
        }
    }

    private void migrateLaunch(){
        boolean value=StorageManager.getInstance().isFreshLaunch();
        SecuredStorageManager.getInstance().setFreshLaunch(value);
    }

    // V6.0
    private void migrateWifiMqttConfig(){
        String  wifiMqttConnectionStr =StorageManager.getInstance().isWifiMqttConfig();
        SecuredStorageManager.getInstance().setWifiConfigState(wifiMqttConnectionStr);
    }

    public void migrateAll(){
        if(!SecuredStorageManager.getInstance().isMigrated()) {
            migrateToken();
            migrateDeviceToken();
            migrateOfflineFPs();
            migrateOfflinePINs();
            migrateOfflineOTPs();
            migrateRevokeUserList();
            migrateOfflineKeys();
            migrateUserData();
            migratePin();
            migrateLaunch();
            migrateWifiMqttConfig(); // added newly V6.0
            SecuredStorageManager.getInstance().setMigrade(true);
            StorageManager.getInstance().clearData();
            Logger.d("===Migrated to New Storage===");
        }

    }

}
