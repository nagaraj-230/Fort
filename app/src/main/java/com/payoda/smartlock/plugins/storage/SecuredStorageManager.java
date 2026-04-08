package com.payoda.smartlock.plugins.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;

import com.google.gson.Gson;
import com.payoda.smartlock.authentication.model.Login;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.fp.model.FPUser;
import com.payoda.smartlock.fp.model.FingerPrints;
import com.payoda.smartlock.history.model.AccessLogList;
import com.payoda.smartlock.history.model.DeviceLog;
import com.payoda.smartlock.locks.model.LockKeyList;
import com.payoda.smartlock.locks.model.LockKeys;
import com.payoda.smartlock.locks.model.WifiMqttConfig;
import com.payoda.smartlock.locks.model.WifiMqttConnection;
import com.payoda.smartlock.managepins.model.Otp;
import com.payoda.smartlock.managepins.model.OtpRequest;
import com.payoda.smartlock.managepins.model.Pin;
import com.payoda.smartlock.managepins.model.PinRequest;
import com.payoda.smartlock.model.LockVersionConfig;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.plugins.pushnotification.RemoteDataEvent;
import com.payoda.smartlock.splash.model.BrandInfoResponse;
import com.payoda.smartlock.users.model.RequestUser;
import com.payoda.smartlock.users.model.RevokeUserList;
import com.payoda.smartlock.utils.DateTimeUtils;
import com.payoda.smartlock.utils.Logger;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SecuredStorageManager {

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
    private static final String REVOKE_USER = "revike_user";
    private static final String OFFLINE_FP = "OFFLINE_FP";
    private static final String OFFLINE_PIN = "offline_pin";
    private static final String OFFLINE_OTP = "offline_otp";
    private static final String IS_MIGRATE = "is_migrate";
    private static final String LOCK_VERSION_CONFIG = "lock_version_config";
    private static final String BRAND = "brand";

    public static class SecurePreferencesException extends RuntimeException {

        public SecurePreferencesException(Throwable e) {
            super(e);
        }

    }

    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String KEY_TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final String SECRET_KEY_HASH_TRANSFORMATION = "SHA-256";
    private static final String CHARSET = "UTF-8";

    private boolean encryptKeys;
    private Cipher writer;
    private Cipher reader;
    private Cipher keyWriter;
    private SharedPreferences preferences;
    private static SecuredStorageManager instance = null;

    private static final String PREFERENCE = "SmartLock_Encrypted";
    private static final String IV = "fldsjfodasjifudslfjdsaofshaufihadsf";
    private static final String KEY = "smartlock_key";
    private static final String WIFI = "wifi_config";


    public void init(Context context) throws SecurePreferencesException {
        try {
            this.writer = Cipher.getInstance(TRANSFORMATION);
            this.reader = Cipher.getInstance(TRANSFORMATION);
            this.keyWriter = Cipher.getInstance(KEY_TRANSFORMATION);
            initCiphers(KEY);
            this.preferences = context.getSharedPreferences(PREFERENCE,
                    Context.MODE_PRIVATE);
            this.encryptKeys = true;
        } catch (GeneralSecurityException e) {
            throw new SecurePreferencesException(e);
        } catch (UnsupportedEncodingException e) {
            throw new SecurePreferencesException(e);
        }
    }

    public static SecuredStorageManager getInstance() {
        if (instance == null) {
            instance = new SecuredStorageManager();
        }
        return instance;
    }

    protected void initCiphers(String secureKey)
            throws UnsupportedEncodingException, NoSuchAlgorithmException,
            InvalidKeyException, InvalidAlgorithmParameterException {
        IvParameterSpec ivSpec = getIv();
        SecretKeySpec secretKey = getSecretKey(secureKey);

        writer.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        reader.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
        keyWriter.init(Cipher.ENCRYPT_MODE, secretKey);
    }

    protected IvParameterSpec getIv() {
        byte[] iv = new byte[writer.getBlockSize()];
        System.arraycopy(IV.getBytes(), 0,
                iv, 0, writer.getBlockSize());
        return new IvParameterSpec(iv);
    }

    protected SecretKeySpec getSecretKey(String key)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {
        byte[] keyBytes = createKeyBytes(key);
        return new SecretKeySpec(keyBytes, TRANSFORMATION);
    }

    protected byte[] createKeyBytes(String key)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest
                .getInstance(SECRET_KEY_HASH_TRANSFORMATION);
        md.reset();
        byte[] keyBytes = md.digest(key.getBytes(CHARSET));
        return keyBytes;
    }

    public void putString(String key, String value) {
        if (value == null) {
            preferences.edit().remove(toKey(key)).commit();
        } else {
            putValue(toKey(key), value);
        }
    }

    public void putBoolean(String key, Boolean value) {
        if (value == null) {
            preferences.edit().remove(toKey(key)).commit();
        } else {
            putValue(toKey(key), Boolean.toString(value));
        }
    }

    public void putLong(String key, long value) {

        putValue(toKey(key), Long.toString(value));

    }

    /*public void putInt(String key, int value) {

        putValue(toKey(key), Integer.toString(value));

    }*/

    public boolean containsKey(String key) {
        return preferences.contains(toKey(key));
    }

    public void removeValue(String key) {
        preferences.edit().remove(toKey(key)).commit();
    }

    public String getString(String key, String defaultValue)
            throws SecurePreferencesException {
        if (preferences.contains(toKey(key))) {
            String securedEncodedValue = preferences.getString(toKey(key), defaultValue);
            return decrypt(securedEncodedValue);
        }
        return "";
    }

    public long getLong(String key, long value)
            throws SecurePreferencesException {
        if (preferences.contains(toKey(key))) {
            String securedEncodedValue = preferences.getString(toKey(key), "");
            return Long.parseLong(decrypt(securedEncodedValue));
        }
        return value;
    }

    public boolean getBoolean(String key, boolean value)
            throws SecurePreferencesException {
        if (preferences.contains(toKey(key))) {
            String securedEncodedValue = preferences.getString(toKey(key), "");
            return Boolean.parseBoolean(decrypt(securedEncodedValue));
        }
        return value;
    }

    /*public int getInt(String key, int value) throws SecurePreferencesException {
        if (preferences.contains(toKey(key))) {
            String securedEncodedValue = preferences.getString(toKey(key), "");
            return Integer.parseInt(decrypt(securedEncodedValue));
        }
        return value;
    }*/

    public void commit() {
        preferences.edit().commit();
    }

    public void clear() {
        preferences.edit().clear().commit();
    }

    public void clearData() {
        clearToken();
        clearResetPin();
        clearUserData();
        clearPin();
    }

    private String toKey(String key) {
        if (encryptKeys)
            return encrypt(key, keyWriter);
        else
            return key;
    }

    private void putValue(String key, String value)
            throws SecurePreferencesException {
        String secureValueEncoded = encrypt(value, writer);
        preferences.edit().putString(key, secureValueEncoded).commit();
    }

    protected String encrypt(String value, Cipher writer)
            throws SecurePreferencesException {
        byte[] secureValue;
        try {
            secureValue = convert(writer, value.getBytes(CHARSET));
        } catch (UnsupportedEncodingException e) {
            throw new SecurePreferencesException(e);
        }
        String secureValueEncoded = Base64.encodeToString(secureValue,
                Base64.NO_WRAP);

        return secureValueEncoded;
    }

    protected String decrypt(String securedEncodedValue) {
        byte[] securedValue = Base64
                .decode(securedEncodedValue, Base64.NO_WRAP);
        byte[] value = convert(reader, securedValue);
        try {
            return new String(value, CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new SecurePreferencesException(e);
        }
    }

    private static byte[] convert(Cipher cipher, byte[] bs)
            throws SecurePreferencesException {
        try {
            return cipher.doFinal(bs);
        } catch (Exception e) {
            throw new SecurePreferencesException(e);
        }
    }

    public String getToken() {
        return getString(TOKEN, "");
    }

    // Authorize Token
    public void setToken(String data) {
        data = "Bearer " + data;
        putString(TOKEN, data);
        ServiceManager.getInstance().setToken(data);
    }

    public void clearToken() {
        ServiceManager.getInstance().setToken(null);
        putString(TOKEN, null);
    }

    // Push Notification Token
    public String getDeviceToken() {
        return getString(DEVICE_TOKEN, "NA");
    }

    public void setDeviceToken(String data) {
        putString(DEVICE_TOKEN, data);
    }

    // PIN Save and Retrieve
    public PinRequest getOfflinePin() {

        PinRequest pinRequest = null;

        String data = getString(OFFLINE_PIN, "");
        if (!TextUtils.isEmpty(data)) {
            pinRequest = new Gson().fromJson(data, PinRequest.class);
        }

        ArrayList<Pin> pinArrayList;

        if (pinRequest == null) {
            pinRequest = new PinRequest();
            pinArrayList = new ArrayList<Pin>();
            pinRequest.setLockPins(pinArrayList);

        } else {
            pinArrayList = pinRequest.getLockPins();
            if (pinArrayList == null) {
                pinArrayList = new ArrayList<Pin>();
                pinRequest.setLockPins(pinArrayList);
            }
        }
        return pinRequest;
    }

    public void saveOfflinePin(PinRequest pinRequest) {
        ArrayList<Pin> pinArrayList;
        if (pinRequest == null) {
            pinRequest = new PinRequest();
            pinArrayList = new ArrayList<>();
            pinRequest.setLockPins(pinArrayList);
        } else {
            pinArrayList = pinRequest.getLockPins();
            if (pinArrayList == null) {
                pinArrayList = new ArrayList<>();
                pinRequest.setLockPins(pinArrayList);
            }
        }
        putString(OFFLINE_PIN, new Gson().toJson(pinRequest));
    }

    // OTP Save and Retrieve
    public OtpRequest getOfflineOtp() {
        OtpRequest otpRequest = null;
        String data = getString(OFFLINE_OTP, "");
        if (!TextUtils.isEmpty(data)) {
            otpRequest = new Gson().fromJson(data, OtpRequest.class);
        }
        ArrayList<Otp> otpArrayList;
        if (otpRequest == null) {
            otpRequest = new OtpRequest();
            otpArrayList = new ArrayList<Otp>();
            otpRequest.setLockOtps(otpArrayList);
        } else {
            otpArrayList = otpRequest.getLockOtps();
            if (otpArrayList == null) {
                otpArrayList = new ArrayList<Otp>();
                otpRequest.setLockOtps(otpArrayList);
            }
        }
        return otpRequest;
    }

    public void saveOfflineOtp(OtpRequest otpRequest) {
        ArrayList<Otp> otpArrayList;
        if (otpRequest == null) {
            otpRequest = new OtpRequest();
            otpArrayList = new ArrayList<>();
            otpRequest.setLockOtps(otpArrayList);
        } else {
            otpArrayList = otpRequest.getLockOtps();
            if (otpArrayList == null) {
                otpArrayList = new ArrayList<>();
                otpRequest.setLockOtps(otpArrayList);
            }
        }
        putString(OFFLINE_OTP, new Gson().toJson(otpRequest));
    }

    // Lock version config
    public void saveLockVersionConfig(LockVersionConfig lockVersionConfig) {
        if (lockVersionConfig == null) {
            lockVersionConfig = new LockVersionConfig();
        }
        putString(LOCK_VERSION_CONFIG, new Gson().toJson(lockVersionConfig));
    }

    public LockVersionConfig getLockVersionConfig() {
        LockVersionConfig lockVersionConfig = null;
        String data = getString(LOCK_VERSION_CONFIG, "");
        if (!TextUtils.isEmpty(data)) {
            lockVersionConfig = new Gson().fromJson(data, LockVersionConfig.class);
        }

        if (lockVersionConfig == null) {
            lockVersionConfig = new LockVersionConfig();
        }
        return lockVersionConfig;
    }

    public LockVersionConfig.ConfigData getLockVersionData(String hardwareVersion) {
        LockVersionConfig lockVersionConfig = SecuredStorageManager.getInstance().getLockVersionConfig();
        LockVersionConfig.ConfigData configData = null;
        if (lockVersionConfig != null) {
            LockVersionConfig.LockVersionData lockVersionData = lockVersionConfig.getLockVersionData();
            if (lockVersionData != null) {
                switch (hardwareVersion) {
                    case Constant.HW_VERSION_2:
                        configData = lockVersionData.getVersionTwo();
                        break;
                    case Constant.HW_VERSION_2_1:
                        configData = lockVersionData.getVersionTwoOne();
                        break;
                    case Constant.HW_VERSION_3:
                        configData = lockVersionData.getVersionThree();
                    case Constant.HW_VERSION_3_1:
                        configData = lockVersionData.getVersionThreeOne();
                        break;
                    case Constant.HW_VERSION_3_2:
                        configData = lockVersionData.getVersionThreeTwo();
                        break;
                    case Constant.HW_VERSION_4_0:
                        configData = lockVersionData.getVersionThreeOne();
                        break;
                    case Constant.HW_VERSION_6_0:
                        configData = lockVersionData.getVersionSix();
                        break;
                    case Constant
                            .HW_VERSION_1:
                    default:
                        configData = lockVersionData.getVersionOne();
                        break;
                }
            }
        }
        return configData;
    }

    // FP Save and Retrieve
    public FingerPrints getOfflineFPs() {
        FingerPrints fingerPrints = null;
        String data = getString(OFFLINE_FP, "");
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
        putString(OFFLINE_FP, new Gson().toJson(fingerPrints));
    }

    // Revoke Key Details
    public void saveRevokeUserList(String requestId, RequestUser revokeUser) {
        RevokeUserList revokeUserList = new RevokeUserList();
        String data = getString(REVOKE_USER, "");
        if (!TextUtils.isEmpty(data)) {
            revokeUserList = new Gson().fromJson(data, RevokeUserList.class);
        }
        revokeUserList.getRequestUserHashMap().put(requestId, revokeUser);
        putString(REVOKE_USER, new Gson().toJson(revokeUserList));
    }

    public RevokeUserList getRevokeUserList() {
        RevokeUserList revokeUserList = new RevokeUserList();
        String data = getString(REVOKE_USER, "");
        if (!TextUtils.isEmpty(data)) {
            revokeUserList = new Gson().fromJson(data, RevokeUserList.class);
        }
        return revokeUserList;
    }

    public void removeSyncedRevokeUser(String requestId) {
        RevokeUserList revokeUserList = getRevokeUserList();
        revokeUserList.getRequestUserHashMap().remove(requestId);
        putString(REVOKE_USER, new Gson().toJson(revokeUserList));
    }

    // Access Log
    public AccessLogList getAccessLogs(String serialNumber, String lockId) {
        AccessLogList accessLogList = new AccessLogList();
        String data = getString(ACCESS_LOG, "");
        if (!TextUtils.isEmpty(data)) {
            DeviceLog deviceLog = new Gson().fromJson(data, DeviceLog.class);
            if (lockId != null && (!TextUtils.isEmpty(lockId)) && deviceLog.getLogs().containsKey(lockId)) {
                // Get log against Device Device ID
                ArrayList<String> logList = deviceLog.getLogs().get(lockId);
                accessLogList.setLogs(logList);
            } else if (deviceLog.getLogs().containsKey(serialNumber)) {
                // Get log against Device serial number
                ArrayList<String> logList = deviceLog.getLogs().get(serialNumber);
                accessLogList.setLogs(logList);
            }
        }
        if (accessLogList.getLogs() == null) {
            accessLogList.setLogs(new ArrayList<String>());
        }
        return accessLogList;
    }

    public void replaceDeviceLog(String serialNumber, String lockId) {
        String data = getString(ACCESS_LOG, "");
        if (!TextUtils.isEmpty(data)) {
            DeviceLog deviceLog = new Gson().fromJson(data, DeviceLog.class);
            if (deviceLog.getLogs().containsKey(serialNumber)) {
                ArrayList<String> logs = deviceLog.getLogs().get(serialNumber);
                deviceLog.getLogs().put(lockId, logs);
                deviceLog.getLogs().remove(serialNumber);
            }
            putString(ACCESS_LOG, new Gson().toJson(deviceLog));
        }
    }

    private ArrayList<String> getDeviceLogByKey(String key) {
        ArrayList<String> logs = new ArrayList<>();
        String data = getString(ACCESS_LOG, "");
        if (!TextUtils.isEmpty(data)) {
            DeviceLog deviceLog = new Gson().fromJson(data, DeviceLog.class);
            if (deviceLog.getLogs().containsKey(key)) {
                logs = deviceLog.getLogs().get(key);
            }
        }
        return logs;
    }

    public DeviceLog getEntireDeviceLog() {

        DeviceLog deviceLog = new DeviceLog();
        String data = getString(ACCESS_LOG, "");

        if (!TextUtils.isEmpty(data)) {
            deviceLog = new Gson().fromJson(data, DeviceLog.class);

        }


        return deviceLog;

    }

    public void setAccessLogs(String serialNumber, String lockId, ArrayList<String> data) {

        if (lockId != null && (!TextUtils.isEmpty(lockId))) {
            replaceDeviceLog(serialNumber, lockId);
            serialNumber = lockId;
        }

        ArrayList<String> alLogs = getDeviceLogByKey(serialNumber);
        alLogs.addAll(data);
        DeviceLog deviceLog = getEntireDeviceLog();
        deviceLog.getLogs().put(serialNumber, alLogs);
        putString(ACCESS_LOG, new Gson().toJson(deviceLog));

        Logger.d("### Set Access Log List = " + data.size());

    }

    public void removeSyncedLog(String lockId) {
        DeviceLog deviceLog = getEntireDeviceLog();
        deviceLog.getLogs().remove(lockId);
        putString(ACCESS_LOG, new Gson().toJson(deviceLog));
    }

    // Key Details

    public LockKeyList getOfflineKeys() {
        LockKeyList mLockKeyList = null;
        String data = getString(OFFLINE_KEYS, "");
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
        putString(OFFLINE_KEYS, new Gson().toJson(locks));
    }

    // Handle User Data

    public Login getUserData() {
        String data = getString(USER, "");
        if (!TextUtils.isEmpty(data)) {
            return new Gson().fromJson(data, Login.class);
        }
        return null;
    }

    public void setUserData(Login data) {
        if (data == null) return;
        putString(USER, new Gson().toJson(data));
    }

    public void clearUserData() {
        putString(USER, null);
    }


    // PIN Details
    public String getPin() {
        return getString(PIN, "");
    }

    public void setPin(String data) {
        putString(PIN, data);
    }

    public void clearPin() {
        putString(PIN, null);
    }

    // RESET PIN Details

    public boolean isResetPin() {
        return !TextUtils.isEmpty(getString(RESET_PIN, null));
    }

    public void setResetPin() {
        putString(RESET_PIN, RESET_PIN);
    }

    public void clearResetPin() {
        putString(RESET_PIN, null);
    }


    // Timeout Seconds
    public long getTimeOutSeconds() {
        return getLong(TIME_OUT_SECONDS, 0);
    }

    public void setTimeOutSeconds() {
        long seconds = System.currentTimeMillis() / 1000;

        putLong(TIME_OUT_SECONDS, seconds);
    }

    public boolean isFreshLaunch() {
        return getBoolean(FRESH_LAUNCH, false);
    }

    public void setFreshLaunch(boolean isFreshLaunch) {
        putBoolean(FRESH_LAUNCH, isFreshLaunch);
    }

    public String getUpdateDate() {
        return getString(FRESH_LAUNCH_DATE, DateTimeUtils.getTodayDate());
    }

    public void setUpdateDate() {
        String nextDate = DateTimeUtils.getNextDate();
        putString(FRESH_LAUNCH_DATE, nextDate);
    }

    public boolean isMigrated() {
        return getBoolean(IS_MIGRATE, false);
    }

    public void setMigrade(boolean isMigrade) {
        putBoolean(IS_MIGRATE, isMigrade);
    }

    // Brand details
    public BrandInfoResponse.BrandInfo getBrandInfo() {
        String data = getString(BRAND, "");
        if (!TextUtils.isEmpty(data)) {
            return new Gson().fromJson(data, BrandInfoResponse.BrandInfo.class);
        }
        return null;
    }

    public void setBrandInfo(BrandInfoResponse.BrandInfo data) {
        if (data == null) return;
        putString(BRAND, new Gson().toJson(data));
    }

    public RemoteDataEvent getRemoteInfo() {

        String data = getString(Constant.REMOTE_ACCESS_DATA, "");

        Logger.d("### getRemoteInfo Sec Manager =  " + data);

        if (data != null && !TextUtils.isEmpty(data)) {
            return new Gson().fromJson(data, RemoteDataEvent.class);
        }

        return null;

    }

    public void setRemoteInfo(String dataStr) {
        if (dataStr == null) putString(Constant.REMOTE_ACCESS_DATA, null);
        else putString(Constant.REMOTE_ACCESS_DATA, (dataStr));
    }

    // Wifi Mqtt API push state

    public WifiMqttConnection getWifiConfigState() {

        WifiMqttConnection wifiMqttConnection = new WifiMqttConnection();
        String data = getString(WIFI, "");
        data = data != null ? data : "";
        if (!TextUtils.isEmpty(data)) {
            wifiMqttConnection = new Gson().fromJson(data, WifiMqttConnection.class);
        }else {
            wifiMqttConnection = null;
        }
        return wifiMqttConnection;
    }

    public void setWifiConfigState(String wifiMqttConnection) {
        putString(WIFI, wifiMqttConnection);
    }

}
