package com.payoda.smartlock.plugins.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.payoda.smartlock.authentication.model.Login;
import com.payoda.smartlock.fp.model.FPUser;
import com.payoda.smartlock.fp.model.FingerPrints;
import com.payoda.smartlock.history.model.AccessLogList;
import com.payoda.smartlock.history.model.DeviceLog;
import com.payoda.smartlock.locks.model.Lock;
import com.payoda.smartlock.locks.model.LockKeyList;
import com.payoda.smartlock.locks.model.LockKeys;
import com.payoda.smartlock.locks.model.Locks;
import com.payoda.smartlock.locks.model.WifiMqttConnection;
import com.payoda.smartlock.managepins.model.Otp;
import com.payoda.smartlock.managepins.model.OtpRequest;
import com.payoda.smartlock.managepins.model.Pin;
import com.payoda.smartlock.managepins.model.PinRequest;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.users.model.RequestUser;
import com.payoda.smartlock.users.model.RevokeUserList;
import com.payoda.smartlock.utils.DateTimeUtils;
import com.payoda.smartlock.utils.Logger;

import java.util.ArrayList;

public class StorageManager {

    private static StorageManager instance;
    private SharedPreferences mSharedPreferences;
    private static final String PREFERENCE = "SmartLock";
    private static final String TOKEN = "token";
    private static final String USER = "user";
    private static final String DEVICE_TOKEN = "device_token";
    private static final String PIN = "pin";
    private static final String RESET_PIN = "reset_pin";
    private static final String LOCKS = "locks";
    private static final String OFFLINE_LOCKS = "offline_locks";
    private static final String OFFLINE_KEYS = "offline_keys";
    private static final String TIME_OUT_SECONDS = "seconds";
    private static final String FRESH_LAUNCH = "fresh_launch";
    private static final String FRESH_LAUNCH_DATE = "fresh_launch_date";
    private static final String ACCESS_LOG = "access_log";
    private static final String FP_LOG = "fp_log";
    private static final String REVOKE_USER="revike_user";
    private static final String WIFI = "wifi_config";
    //private static final String IS_UPDATE="is_update";

    private StorageManager() {
    }

    public static StorageManager getInstance() {
        if (instance == null) {
            instance = new StorageManager();
        }
        return instance;
    }

    public void init(Context context) {
        try {
            mSharedPreferences = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        } catch (Exception e) {
            Logger.e(e);
        }
    }


    private String getString(String key) {
        return getString(key, null);
    }

    private String getString(String key, String data) {
        if (mSharedPreferences == null) return null;
        return mSharedPreferences.getString(key, data);
    }

    private void setString(String key, String data) {
        if (mSharedPreferences == null) return;
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(key, data);
        editor.apply();
    }


    public void clearData() {
        clearToken();
        clearResetPin();
        clearUserData();
        clearPin();
    }


    public String getToken() {
        return getString(TOKEN);
    }

    public void setToken(String data) {
        data = "Bearer " + data;
        setString(TOKEN, data);
        ServiceManager.getInstance().setToken(data);
    }

    public void clearToken() {
        ServiceManager.getInstance().setToken(null);
        setString(TOKEN, null);
    }

    public String getDeviceToken() {
        return getString(DEVICE_TOKEN, "NA");
    }

    public void setDeviceToken(String data) {
        setString(DEVICE_TOKEN, data);
    }

    public Login getUserData() {
        String data = getString(USER);
        if (!TextUtils.isEmpty(data)) {
            return new Gson().fromJson(data, Login.class);
        }
        return null;
    }

    public void setUserData(Login data) {
        if (data == null) return;
        setString(USER, new Gson().toJson(data));
    }

    public void clearUserData() {
        setString(USER, null);
    }


    public String getPin() {
        return getString(PIN);
    }

    public void setPin(String data) {
        setString(PIN, data);
    }

    public void clearPin() {
        setString(PIN, null);
    }

    public boolean isResetPin() {
        return getString(RESET_PIN) != null;
    }

    public void setResetPin() {
        setString(RESET_PIN, RESET_PIN);
    }

    public void clearResetPin() {
        setString(RESET_PIN, null);
    }

    public long getTimeOutSeconds() {
        if (mSharedPreferences == null) return 0;
        return mSharedPreferences.getLong(TIME_OUT_SECONDS, 0);
    }

    public void setTimeOutSeconds() {
        long seconds = System.currentTimeMillis() / 1000;
        if (mSharedPreferences == null) return;
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putLong(TIME_OUT_SECONDS, seconds);
        editor.apply();
    }

    public boolean isFreshLaunch() {
        if (mSharedPreferences == null) return true;
        return mSharedPreferences.getBoolean(FRESH_LAUNCH, false);
    }



    public void setFreshLaunch(boolean isFreshLaunch) {
        if (mSharedPreferences == null) return;
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(FRESH_LAUNCH, isFreshLaunch);
        editor.apply();
    }

    public String getUpdateDate() {
        if (mSharedPreferences == null)
            return null;


        return mSharedPreferences.getString(FRESH_LAUNCH_DATE, DateTimeUtils.getTodayDate());
    }

    public void setUpdateDate() {
        if (mSharedPreferences == null)
            return;
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        String nextDate=DateTimeUtils.getNextDate();
        editor.putString(FRESH_LAUNCH_DATE, nextDate);
        editor.apply();
    }

    public Locks getLocks() {
        Locks locks = null;
        String data = getString(LOCKS);
        if (!TextUtils.isEmpty(data)) {
            locks = new Gson().fromJson(data, Locks.class);
        }
        ArrayList<Lock> lockList;
        if (locks == null) {
            locks = new Locks();
            lockList = new ArrayList<>();
            locks.setLocks(lockList);
        } else {
            lockList = locks.getLocks();
            if (lockList == null) {
                lockList = new ArrayList<>();
                locks.setLocks(lockList);
            }
        }
        return locks;
    }

    public void setLocks(Locks locks) {
        ArrayList<Lock> lockList;
        if (locks == null) {
            locks = new Locks();
            lockList = new ArrayList<>();
            locks.setLocks(lockList);
        } else {
            lockList = locks.getLocks();
            if (lockList == null) {
                lockList = new ArrayList<>();
                locks.setLocks(lockList);
            }
        }
        setString(LOCKS, new Gson().toJson(locks));
    }

    public Locks getOfflineLocks() {
        Locks locks = null;
        String data = getString(OFFLINE_LOCKS);
        if (!TextUtils.isEmpty(data)) {
            locks = new Gson().fromJson(data, Locks.class);
        }
        ArrayList<Lock> lockList;
        if (locks == null) {
            locks = new Locks();
            lockList = new ArrayList<>();
            locks.setLocks(lockList);
        } else {
            lockList = locks.getLocks();
            if (lockList == null) {
                lockList = new ArrayList<>();
                locks.setLocks(lockList);
            }
        }
        return locks;
    }

    public void setOfflineLocks(Locks locks) {
        ArrayList<Lock> lockList;
        if (locks == null) {
            locks = new Locks();
            lockList = new ArrayList<>();
            locks.setLocks(lockList);
        } else {
            lockList = locks.getLocks();
            if (lockList == null) {
                lockList = new ArrayList<>();
                locks.setLocks(lockList);
            }
        }
        setString(OFFLINE_LOCKS, new Gson().toJson(locks));
    }

    public LockKeyList getOfflineKeys() {
        LockKeyList mLockKeyList = null;
        String data = getString(OFFLINE_KEYS);
        if (!TextUtils.isEmpty(data)) {
            mLockKeyList = new Gson().fromJson(data, LockKeyList.class);
        }
        ArrayList<LockKeys> lockList;
        if (mLockKeyList == null) {
            mLockKeyList = new LockKeyList();
            lockList = new ArrayList<>();
            mLockKeyList.setLockKeys(lockList);
        } else {
            lockList = mLockKeyList.getLockKeys();
            if (lockList == null) {
                lockList = new ArrayList<>();
                mLockKeyList.setLockKeys(lockList);
            }
        }
        return mLockKeyList;
    }

    public void setOfflineKeys(LockKeyList locks) {
        ArrayList<LockKeys> lockList;
        if (locks == null) {
            locks = new LockKeyList();
            lockList = new ArrayList<>();
            locks.setLockKeys(lockList);
        } else {
            lockList = locks.getLockKeys();
            if (lockList == null) {
                lockList = new ArrayList<>();
                locks.setLockKeys(lockList);
            }
        }
        setString(OFFLINE_KEYS, new Gson().toJson(locks));
    }



    public AccessLogList getAccessLogs(String serialNumber,String lockId) {
        AccessLogList accessLogList = new AccessLogList();
        String data = getString(ACCESS_LOG);
        if (!TextUtils.isEmpty(data)) {
            DeviceLog deviceLog = new Gson().fromJson(data, DeviceLog.class);
            if(lockId!=null && (!TextUtils.isEmpty(lockId)) && deviceLog.getLogs().containsKey(lockId)){
                // Get log against Device Device ID
                ArrayList<String> logList=deviceLog.getLogs().get(lockId);
                accessLogList.setLogs(logList);
            }
            else if(deviceLog.getLogs().containsKey(serialNumber)){
                // Get log against Device serial number
                ArrayList<String> logList=deviceLog.getLogs().get(serialNumber);
                accessLogList.setLogs(logList);
            }
        }
        if (accessLogList.getLogs() == null) {
            accessLogList.setLogs(new ArrayList<String>());
        }
        return accessLogList;
    }

    public void replaceDeviceLog(String serialNumber,String lockId) {
        String data = getString(ACCESS_LOG);
        if (!TextUtils.isEmpty(data)) {
            DeviceLog deviceLog = new Gson().fromJson(data, DeviceLog.class);
            if(deviceLog.getLogs().containsKey(serialNumber)){
                ArrayList<String> logs=deviceLog.getLogs().get(serialNumber);
                deviceLog.getLogs().put(lockId,logs);
                deviceLog.getLogs().remove(serialNumber);
            }
            setString(ACCESS_LOG, new Gson().toJson(deviceLog));
        }
    }

    private ArrayList<String> getDeviceLogByKey(String key){
        ArrayList<String> logs=new ArrayList<>();
        String data = getString(ACCESS_LOG);
        if (!TextUtils.isEmpty(data)) {
            DeviceLog deviceLog = new Gson().fromJson(data, DeviceLog.class);
            if(deviceLog.getLogs().containsKey(key)){
                logs=deviceLog.getLogs().get(key);
            }
        }
        return logs;
    }

    public DeviceLog getEntireDeviceLog(){
        DeviceLog deviceLog=new DeviceLog();
        String data = getString(ACCESS_LOG);
        if (!TextUtils.isEmpty(data)) {
            deviceLog = new Gson().fromJson(data, DeviceLog.class);
        }
        return deviceLog;
    }

    public void setAccessLogs(String serialNumber,String lockId,ArrayList<String> data) {
        if(lockId!=null && (!TextUtils.isEmpty(lockId))){
            replaceDeviceLog(serialNumber,lockId);
            serialNumber=lockId;
        }
        ArrayList<String> alLogs = getDeviceLogByKey(serialNumber);
        alLogs.addAll(data);
        DeviceLog deviceLog=getEntireDeviceLog();
        deviceLog.getLogs().put(serialNumber,alLogs);
        setString(ACCESS_LOG, new Gson().toJson(deviceLog));
    }

    public void removeSyncedLog(String lockId){
        DeviceLog deviceLog=getEntireDeviceLog();
        deviceLog.getLogs().remove(lockId);
        setString(ACCESS_LOG, new Gson().toJson(deviceLog));
    }


    public void saveRevokeUserList(String requestId,RequestUser revokeUser){
        RevokeUserList revokeUserList=new RevokeUserList();
        String data = getString(REVOKE_USER);
        if (!TextUtils.isEmpty(data)) {
            revokeUserList = new Gson().fromJson(data, RevokeUserList.class);
        }
        revokeUserList.getRequestUserHashMap().put(requestId,revokeUser);
        setString(REVOKE_USER, new Gson().toJson(revokeUserList));
    }

    public RevokeUserList getRevokeUserList(){
        RevokeUserList revokeUserList=new RevokeUserList();
        String data = getString(REVOKE_USER);
        if (!TextUtils.isEmpty(data)) {
            revokeUserList = new Gson().fromJson(data, RevokeUserList.class);
        }
        return revokeUserList;
    }

    public void removeSyncedRevokeUser(String requestId){
        RevokeUserList revokeUserList=getRevokeUserList();
        revokeUserList.getRequestUserHashMap().remove(requestId);
        setString(REVOKE_USER, new Gson().toJson(revokeUserList));
    }

    private final String OFFLINE_FP="OFFLINE_FP";

    public FingerPrints getOfflineFPs() {
        FingerPrints fingerPrints = null;
        String data = getString(OFFLINE_FP);
        if (!TextUtils.isEmpty(data)) {
            fingerPrints = new Gson().fromJson(data, FingerPrints.class);
        }
        ArrayList<FPUser> fpUsers;
        if (fingerPrints == null) {
            fingerPrints = new FingerPrints();
            fpUsers = new ArrayList<>();
            fingerPrints.setFpUsers(fpUsers);
        } else {
            fpUsers = fingerPrints.getFpUsers();
            if (fpUsers == null) {
                fpUsers = new ArrayList<>();
                fingerPrints.setFpUsers(fpUsers);
            }
        }
        return fingerPrints;
    }

    private final String OFFLINE_PIN="OFFLINE_PIN";
    public PinRequest getOfflinePins(){
        PinRequest pinRequest = null;
        String data = getString(OFFLINE_PIN, "");
        if (!TextUtils.isEmpty(data)) {
            pinRequest = new Gson().fromJson(data, PinRequest.class);
        }
        ArrayList<Pin> pinArrayList;
        if(pinRequest == null){
            pinRequest = new PinRequest();
            pinArrayList = new ArrayList<Pin>();
            pinRequest.setLockPins(pinArrayList);
        }else{
            pinArrayList = pinRequest.getLockPins();
            if (pinArrayList == null){
                pinArrayList = new ArrayList<Pin>();
                pinRequest.setLockPins(pinArrayList);
            }
        }
        return pinRequest;
    }

    private final String OFFLINE_OTP ="OFFLINE_OTP";
    public OtpRequest getOfflineOtps(){
        OtpRequest otpRequest = null;
        String data = getString(OFFLINE_OTP, "");
        if (!TextUtils.isEmpty(data)) {
            otpRequest = new Gson().fromJson(data, OtpRequest.class);
        }
        ArrayList<Otp> otpArrayList;
        if(otpRequest == null){
            otpRequest = new OtpRequest();
            otpArrayList = new ArrayList<Otp>();
            otpRequest.setLockOtps(otpArrayList);
        }else{
            otpArrayList = otpRequest.getLockOtps();
            if (otpArrayList == null){
                otpArrayList = new ArrayList<Otp>();
                otpRequest.setLockOtps(otpArrayList);
            }
        }
        return otpRequest;
    }

    public void setOfflineFPs(FingerPrints fingerPrints) {
        ArrayList<FPUser> fpUsers;
        if (fingerPrints == null) {
            fingerPrints = new FingerPrints();
            fpUsers = new ArrayList<>();
            fingerPrints.setFpUsers(fpUsers);
        } else {
            fpUsers = fingerPrints.getFpUsers();
            if (fpUsers == null) {
                fpUsers = new ArrayList<>();
                fingerPrints.setFpUsers(fpUsers);
            }
        }
        setString(OFFLINE_FP, new Gson().toJson(fingerPrints));
    }

    public String isWifiMqttConfig() {
        return getString(WIFI, "");
    }

}
   
