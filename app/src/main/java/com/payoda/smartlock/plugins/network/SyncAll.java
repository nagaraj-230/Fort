package com.payoda.smartlock.plugins.network;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.fp.model.FPUser;
import com.payoda.smartlock.fp.model.FingerPrints;
import com.payoda.smartlock.fp.service.FPService;
import com.payoda.smartlock.history.model.AccessLogList;
import com.payoda.smartlock.history.model.DeviceLog;
import com.payoda.smartlock.history.service.HistoryService;
import com.payoda.smartlock.locks.model.Lock;
import com.payoda.smartlock.locks.model.LockAddResponse;
import com.payoda.smartlock.locks.model.LockKeyList;
import com.payoda.smartlock.locks.model.LockKeys;
import com.payoda.smartlock.locks.model.Locks;
import com.payoda.smartlock.locks.model.WifiMqttConnection;
import com.payoda.smartlock.locks.service.LockAddService;
import com.payoda.smartlock.locks.service.LockKeyService;
import com.payoda.smartlock.locks.service.LockListService;
import com.payoda.smartlock.locks.service.WifiMqttConfigurationService;
import com.payoda.smartlock.managepins.model.OtpRequest;
import com.payoda.smartlock.managepins.model.PinRequest;
import com.payoda.smartlock.managepins.service.DigitalKeyService;
import com.payoda.smartlock.managepins.service.OtpAddService;
import com.payoda.smartlock.managepins.service.OtpKeyService;
import com.payoda.smartlock.plugins.storage.SecuredStorageManager;
import com.payoda.smartlock.plugins.storage.StorageManager;
import com.payoda.smartlock.plugins.storage.lock.LockDBClient;
import com.payoda.smartlock.users.model.AssignUserResponse;
import com.payoda.smartlock.users.model.RequestUser;
import com.payoda.smartlock.users.model.RevokeUserList;
import com.payoda.smartlock.users.service.RequestsUserService;
import com.payoda.smartlock.utils.AppUtils;
import com.payoda.smartlock.utils.Loader;
import com.payoda.smartlock.utils.Logger;

import java.util.ArrayList;

public class SyncAll {

    private static SyncAll mInstance = null;

    public static SyncAll getInstance() {
        if (mInstance == null) {
            mInstance = new SyncAll();
        }
        return mInstance;
    }

    public void pushAll(Context context) {
        Logger.d("### SyncService pushAll()");
        boolean networkAvailable = ServiceManager.getInstance().isNetworkAvailable(context);
        Logger.d("### ServiceManager.getInstance().isNetworkAvailable(context) = " + networkAvailable);
        // Push newly created locks to webservice.
        if (ServiceManager.getInstance().isNetworkAvailable(context)) {
            pushLocks(context);
            pushKeys();
            pushRevokeUserList();
            pushActivityLog();
            pushFPUser();
            pushDigiPin();
            pushDigiOtp();
            removeWifiPushState();

            //TODO confirm with backend data and its not for FORT version, So, Don't call this api

            //pushWifiConfig();
        }

    }

    public void pushLocks(Context context) {

        Logger.d("### pushLocks ");

        LockDBClient.getInstance().getOfflineLocks(context, new LockDBClient.DBCallback() {
            @Override
            public void onLockList(ArrayList<Lock> lockList) {


                if (lockList.size() > 0) {

                    for (final Lock lock : lockList) {

                        if (lock.getId() != null && lock.getId().contains(Constant.OFFLINE_LOCK_ID)) {

                            Logger.d("### add lock request");

                            AppUtils.getInstance().printJavaObject(lock);

                            LockAddService.getInstance().addLock(lock, new ResponseHandler() {
                                @Override
                                public void onSuccess(Object data) {
                                    LockAddResponse response = (LockAddResponse) data;
                                    if (response != null && response.getStatus() != null &&
                                            response.getStatus().equalsIgnoreCase("success")) {
                                        Logger.d("=====> Id " + response.getData().getId());
                                        SecuredStorageManager.getInstance().replaceDeviceLog(response.getData().getSerialNumber(),
                                                response.getData().getId());
                                        removeLock(context, lock);
                                        pushActivityLog();
                                    }
                                }

                                @Override
                                public void onAuthError(String message) {
                                    if (message != null && message.contains("Serial")) {
                                        removeLock(context, lock);
                                    }
                                    pushActivityLog();
                                }

                                @Override
                                public void onError(String message) {
                                    if (message != null && message.contains("Serial")) {
                                        removeLock(context, lock);
                                    }
                                    pushActivityLog();
                                }
                            });

                        } else {
                            Logger.d("### update lock request");
                            AppUtils.getInstance().printJavaObject(lock);
                            LockListService.getInstance().updateLock(lock, new ResponseHandler() {
                                @Override
                                public void onSuccess(Object data) {
                                    Logger.d("### pushLocks success");
                                    //removeLock(context,lock);
                                    lock.setSync(true);
                                    LockDBClient.getInstance().save(lock, context);
                                }

                                @Override
                                public void onAuthError(String message) {

                                }

                                @Override
                                public void onError(String message) {
                                    Logger.d("### pushLocks error");
                                    if (message.contains("exist")) {
                                        lock.setSync(true);
                                        LockDBClient.getInstance().save(lock, context);
                                    }
                                }
                            });

                        }
                    }
                }
                else {
                    Logger.d("### pushLocks else");
                    pushActivityLog();
                }

            }

            @Override
            public void onSuccess(String msg) {

            }

//            @Override
//            public void onLockInfo(Lock lock) {
//
//            }
        });

    }

    private void removeLock(Context context, Lock lock) {
        Logger.d("### removeLock ");
        LockDBClient.getInstance().deleteLockById(context, lock, new LockDBClient.DBCallback() {
            @Override
            public void onLockList(ArrayList<Lock> lockList) {

            }

            @Override
            public void onSuccess(String msg) {

            }

//            @Override
//            public void onLockInfo(Lock lock) {
//
//            }
        });

    }

    public void pushActivityLog() {

        Logger.d("### pushActivityLog ");
        Logger.d("### pushActivityLog "+SecuredStorageManager.getInstance().getEntireDeviceLog());
        DeviceLog deviceLog = SecuredStorageManager.getInstance().getEntireDeviceLog();

        AppUtils.getInstance().printJavaObject(deviceLog);

        for (String lockId : deviceLog.getLogs().keySet()) {


            if (lockId != null && lockId.length() < 10) {

                AccessLogList data = SecuredStorageManager.getInstance().getAccessLogs(lockId, lockId);

                Logger.d("### AccessLogList = " + data);
                Logger.d("### AccessLogList size = " + data.getLogs().size());

                if (data.getLogs() != null && data.getLogs().size() > 0) {

                    Logger.d("### activity log make http request for 'addactivity  ");

                    SecuredStorageManager.getInstance().removeSyncedLog(lockId);
                    HistoryService.getInstance().serviceRequest(lockId, data, new ResponseHandler() {
                        @Override
                        public void onSuccess(Object data) {

                            Logger.d("### pushActivityLog onSuccess = data = " + data);
                            // AccessLogResponse response = (AccessLogResponse) data;
                        /*if (response != null && response.getStatus().equalsIgnoreCase("success")) {

                        }*/
                        }

                        @Override
                        public void onAuthError(String message) {
                            Logger.d("### pushActivityLog onAuthError = " + message);
                        }

                        @Override
                        public void onError(String message) {
                            Logger.d("### pushActivityLog onError = " + message);
                        }
                    });
                }
            }

        }

    }

    private void pushRevokeUserList() {

        Logger.d("### pushRevokeUserList ");

        RevokeUserList revokeUserList = SecuredStorageManager.getInstance().getRevokeUserList();

        AppUtils.getInstance().printJavaObject(revokeUserList);

        for (String requestId : revokeUserList.getRequestUserHashMap().keySet()) {
            RequestUser payload = revokeUserList.getRequestUserHashMap().get(requestId);
            RequestsUserService.getInstance().revokeRequest(payload, requestId, new ResponseHandler() {
                @Override
                public void onSuccess(Object data) {
                    Logger.d("### pushRevokeUserList onSuccess ");
                    Loader.getInstance().hideLoader();
                    if (data != null) {
                        AssignUserResponse assignUserResponse = (AssignUserResponse) data;
                        if (assignUserResponse.getStatus().equalsIgnoreCase("success")) {
                            SecuredStorageManager.getInstance().removeSyncedRevokeUser(requestId);
                        }
                    }
                }

                @Override
                public void onAuthError(String message) {
                }

                @Override
                public void onError(String message) {

                }
            });
        }

    }

    private void pushFPUser() {

        Logger.d("### pushFPUser ");
        FingerPrints fingerPrints = SecuredStorageManager.getInstance().getOfflineFPs();

        AppUtils.getInstance().printJavaObject(fingerPrints);

        if (fingerPrints != null && fingerPrints.getFpUsers() != null
                && fingerPrints.getFpUsers().size() > 0) {
            for (final FPUser fpUser : fingerPrints.getFpUsers()) {
                removeFPUser(fpUser);
                fpUser.setOriginalKey(new Gson().toJson(fpUser.getKeys()));
                FPService.getInstance().addFPRequest(fpUser, new ResponseHandler() {
                    @Override
                    public void onSuccess(Object data) {
                        Logger.d("### pushFPUser onSuccess");
                    }

                    @Override
                    public void onAuthError(String message) {
                        Logger.d("### pushFPUser onAuthError");
                        // Adding Synced falue in case API error
                        if (fingerPrints.getFpUsers() != null && fpUser != null) {
                            fingerPrints.getFpUsers().add(fpUser);
                            SecuredStorageManager.getInstance().setOfflineFPs(fingerPrints);
                        }
                    }

                    @Override
                    public void onError(String message) {
                        // Adding Synced falue in case API error
                        Logger.d("### pushFPUser onError");
                        if (fingerPrints.getFpUsers() != null && fpUser != null) {
                            fingerPrints.getFpUsers().add(fpUser);
                            SecuredStorageManager.getInstance().setOfflineFPs(fingerPrints);
                        }
                    }
                });
            }
        }
    }

    private void removeFPUser(FPUser syncedFPUsers) {

        Logger.d("### removeFPUser ");

        FingerPrints fingerPrints = SecuredStorageManager.getInstance().getOfflineFPs();


        ArrayList<FPUser> fpUsers = fingerPrints.getFpUsers();

        Logger.d("@@@@@@@@@ fpUsers : "+fpUsers.size());

        for (FPUser fpUser : fpUsers) {
            if (syncedFPUsers.getKeys().toString().
                    equalsIgnoreCase(fpUser.getKeys().toString())) {
                fpUsers.remove(fpUser);
            }
        }
        fingerPrints.setFpUsers(fpUsers);
        SecuredStorageManager.getInstance().setOfflineFPs(fingerPrints);
    }

    private void pushDigiPin() {

        Logger.d("### push Digit Pin key  ");

        PinRequest pinRequest = SecuredStorageManager.getInstance().getOfflinePin();
        AppUtils.getInstance().printJavaObject(pinRequest);

        if (pinRequest != null && pinRequest.getLockPins() != null
                && pinRequest.getLockPins().size() > 0) {

            DigitalKeyService.getInstance().addUpdateDigiPin(pinRequest, new ResponseHandler() {
                @Override
                public void onSuccess(Object data) {
                    Logger.d("### pushDigiPin success");
                    PinRequest pinRequest1 = null;
                    SecuredStorageManager.getInstance().saveOfflinePin(pinRequest1);
                }

                @Override
                public void onAuthError(String message) {
                    if (pinRequest.getLockPins() != null) {
                        SecuredStorageManager.getInstance().saveOfflinePin(pinRequest);
                    }
                }

                @Override
                public void onError(String message) {
                    if (pinRequest.getLockPins() != null) {
                        SecuredStorageManager.getInstance().saveOfflinePin(pinRequest);
                    }
                }
            });

        }
    }

    private void pushDigiOtp() {

        Logger.d("### pushDigiOtp ");

        OtpRequest otpRequest = SecuredStorageManager.getInstance().getOfflineOtp();

        AppUtils.getInstance().printJavaObject(otpRequest);

        if (otpRequest != null && otpRequest.getLockOtps() != null
                && otpRequest.getLockOtps().size() > 0) {

            OtpAddService.getInstance().addOtp(otpRequest, new ResponseHandler() {

                @Override
                public void onSuccess(Object data) {
                    Logger.d("### pushDigiOtp success");
                    OtpRequest otpRequest1 = null;
                    SecuredStorageManager.getInstance().saveOfflineOtp(otpRequest1);
                }

                @Override
                public void onAuthError(String message) {
                    Logger.d("### pushDigiOtp onAuthError");
                    if (otpRequest.getLockOtps() != null) {
                        SecuredStorageManager.getInstance().saveOfflineOtp(otpRequest);
                    }
                }

                @Override
                public void onError(String message) {
                    Logger.d("### pushDigiOtp onError");
                    if (otpRequest.getLockOtps() != null) {
                        SecuredStorageManager.getInstance().saveOfflineOtp(otpRequest);
                    }
                }
            });
        }
    }

    public void pushKeys() {

        Logger.d("### pushKeys ");

        LockKeyList mLockKeyList = SecuredStorageManager.getInstance().getOfflineKeys();

        AppUtils.getInstance().printJavaObject(mLockKeyList);

        if (mLockKeyList != null && mLockKeyList.getLockKeys() != null && mLockKeyList.getLockKeys().size() > 0) {
            for (int i = 0; i < mLockKeyList.getLockKeys().size(); i++) {
                LockKeys lockKeys = mLockKeyList.getLockKeys().get(i);
                removeKeys(lockKeys.getId());
                LockKeyService.getInstance().transferKeys(lockKeys, new ResponseHandler() {
                    @Override
                    public void onSuccess(Object data) {
                        Logger.d("### pushKeys success");
                    }

                    @Override
                    public void onAuthError(String message) {

                    }

                    @Override
                    public void onError(String message) {
                    }
                }, lockKeys.getKey());
            }
        }
    }

    private void revokeUser() {
        RequestUser requestUser = null;
        RequestsUserService.getInstance().revokeRequest(requestUser, requestUser.getRequestId(), new ResponseHandler() {
            @Override
            public void onSuccess(Object data) {

            }

            @Override
            public void onAuthError(String message) {

            }

            @Override
            public void onError(String message) {

            }
        });

    }

    private void removeKeys(String id) {

        Logger.d("### removeKeys after push keys ");

        LockKeyList mLockKeys = SecuredStorageManager.getInstance().getOfflineKeys();

        AppUtils.getInstance().printJavaObject(mLockKeys);

        if (mLockKeys != null && mLockKeys.getLockKeys() != null && mLockKeys.getLockKeys().size() > 0) {
            for (int i = 0; i < mLockKeys.getLockKeys().size(); i++) {
                if (mLockKeys.getLockKeys().get(i).getId() != null && mLockKeys.getLockKeys().get(i).getId().equalsIgnoreCase(id)) {
                    mLockKeys.getLockKeys().remove(i);
                }
            }
        }
        SecuredStorageManager.getInstance().setOfflineKeys(mLockKeys);

    }

    // Wifi Mqtt Configuration - To get given wifi ssid and password  is connected to lock or not
    public void pushWifiConfig() {

        Logger.d("### pushWifiConfig ");

        WifiMqttConnection wifiMqttConnection = SecuredStorageManager.getInstance().getWifiConfigState();

        Logger.d("### pushWifiConfig  getStatus = " + wifiMqttConnection);

        if (wifiMqttConnection != null && wifiMqttConnection.getOwnerId() != null && !wifiMqttConnection.getOwnerId().isEmpty()) {

            WifiMqttConfigurationService.getInstance().pushWifiConfig(wifiMqttConnection, new ResponseHandler() {

                @Override
                public void onSuccess(Object data) {
                    removeWifiPushState();
                    Logger.d("### pushWifiConfig success");
                }

                @Override
                public void onAuthError(String message) {

                }

                @Override
                public void onError(String message) {
                }
            });
        }

    }

    private void removeWifiPushState() {
        Logger.d("#### removeWifiPushState");
        SecuredStorageManager.getInstance().setWifiConfigState(null);
    }

}
