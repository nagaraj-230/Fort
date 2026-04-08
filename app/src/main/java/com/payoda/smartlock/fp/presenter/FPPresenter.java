package com.payoda.smartlock.fp.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.payoda.smartlock.App;
import com.payoda.smartlock.BuildConfig;
import com.payoda.smartlock.R;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.constants.ServiceUrl;
import com.payoda.smartlock.fp.model.FPUser;
import com.payoda.smartlock.fp.model.FingerPrints;
import com.payoda.smartlock.fp.model.UserListResponse;
import com.payoda.smartlock.fp.service.FPService;
import com.payoda.smartlock.fp.view.FPListFragment;
import com.payoda.smartlock.locks.LockManager;
import com.payoda.smartlock.locks.model.Lock;
import com.payoda.smartlock.locks.model.LockKeys;
import com.payoda.smartlock.locks.model.Locks;
import com.payoda.smartlock.locks.model.WifiLock;
import com.payoda.smartlock.locks.model.WifiLockResponse;
import com.payoda.smartlock.model.BaseResponse;
import com.payoda.smartlock.plugins.bluetooth.BleManager;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.plugins.storage.SecuredStorageManager;
import com.payoda.smartlock.plugins.storage.StorageManager;
import com.payoda.smartlock.plugins.wifi.WifiLockManager;
import com.payoda.smartlock.plugins.wifi.WifiUtilManager;
import com.payoda.smartlock.service.AESEncryption;
import com.payoda.smartlock.service.SyncScheduler;
import com.payoda.smartlock.users.model.AssignUser;
import com.payoda.smartlock.users.model.AssignUserResponse;
import com.payoda.smartlock.users.model.RequestUser;
import com.payoda.smartlock.users.service.AssignRequestUserService;
import com.payoda.smartlock.users.service.RequestsUserService;
import com.payoda.smartlock.utils.AppDialog;
import com.payoda.smartlock.utils.Loader;
import com.payoda.smartlock.utils.Logger;
import com.payoda.smartlock.utils.SLViewBinder;

import java.util.ArrayList;

import static com.payoda.smartlock.constants.Constant.HW_VERSION_6_0;
import static com.payoda.smartlock.constants.Constant.WIFI_DELAY_TIME;

public class FPPresenter {

    private SLViewBinder viewBinder;

    private final String FP_NEXT_SAMPLE = "FP_NEXT_SAMPLE";
    private final String FP_MAX_USR = "FP_MAX_USR";
    private final String FP_NOT_PLACED = "FP_NOT_PLACED";
    private final String FP_TRY_OTHER_FINGER = "FP_TRY_OTHER_FINGER";
    private final String FP_USER_ID_NOT_EXISTS = "FP_USER_ID_NOT_EXISTS";
    private final String FP_ID_ALREADY_EMPTY = "FP_ID_ALREADY_EMPTY";
    private final String FP_MAX_TRIES_EXIT = "FP_MAX_TRIES_EXIT";
    private final String FP_UID_ENROLLED = "FP_UID_ENROLLED";
    private final String FP_BAD_FINGER = "FP_BAD_FINGER";
    private final String FP_NOT_CONNECTED = "FP_NOT_CONNECTED";

    private int fp_count = 1;

    public static final int FP_ADD = 1;
    public static final int FP_REVOKE = 2;
    private int revokeCount = 0;

    public FPPresenter(SLViewBinder viewBinder) {
        this.viewBinder = viewBinder;
    }

    public String doUpdateUI(Lock mLock){

        String fpText = "";

        if (mLock != null && mLock.getLockVersion().equalsIgnoreCase(HW_VERSION_6_0)) {
            fpText = "Long Press \"1\" on the Lock and Click \"Press\"";
        } else {
            fpText = "Please place the finger on lock. And follow the instructions.";
        }

        return fpText;
    }

    public void doAddFingerPrint(Context context, int reqCode, Lock mLock, FPUser fpUser, int ACTION) {
        fp_count = 1;

        String status;
        String msg;

        if (mLock != null && mLock.getLockVersion().equalsIgnoreCase(HW_VERSION_6_0)) {
            status = LockManager.getInstance().preCheckBLEVersion6((Activity) context);
            msg = "Long Press \"1\" on the Lock and Click \"OK\"";
        } else {
            status = LockManager.getInstance().preCheckBLE((Activity) context);
            msg = "Please switch on the lock and try again.";
        }

        if (!status.equalsIgnoreCase("success")) {
            AppDialog.showAlertDialog((Activity) context, status);
            return;
        }

        AppDialog.showAlertDialog(context, "Activate Lock", msg, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                doAddOrDeleteFPViaWifi(context, mLock, fpUser, reqCode, ACTION);
            }
        }, "CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
    }

    private void doAddOrDeleteFPViaWifi(Context context, Lock mLock, FPUser fpUser, int reqCode, int type) {

        Loader.getInstance().showLoader(context);
        String SSID = BleManager.MANUFACTURER_CODE + mLock.getSerialNumber();
        String password = mLock.getScratchCode();
        String lockVersion = mLock.getLockVersion();
        WifiUtilManager wifiUtilManager = new WifiUtilManager(context, new WifiUtilManager.WifiListener() {
            @Override
            public void connectionSuccess() {
                Logger.d("Connection Success");
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if (type == FP_REVOKE) {
                            revokeCount = 0;
                            doRevokePrintRequest(context, reqCode, mLock, fpUser);
                        } else if (type == FP_ADD) {
                            doAddFingerPrintRequest(context, reqCode, mLock, fpUser);
                        }
                    }
                }, WIFI_DELAY_TIME);

            }

            @Override
            public void connectionTimeOut() {
                Loader.getInstance().hideLoader();
                forgetWIFINetwork(context, mLock);
                Toast.makeText(context, "Please try again later", Toast.LENGTH_LONG).show();
            }
        });
        wifiUtilManager.startScanning(lockVersion, SSID, password);
    }

    private void doAddFingerPrintRequest(Context context, int reqCode, Lock mLock, FPUser fpUser) {
        if (!WifiLockManager.getInstance().isWifiEnabled(context)) {
            AppDialog.showAlertDialog(context, context.getString(R.string.turn_on_wifi_connect_lock));
        } else if (!WifiLockManager.getInstance().isWifiLockConnected(context)) {
            AppDialog.showAlertDialog(context, context.getString(R.string.no_wifi_lock), context.getString(R.string.connect_wifi_lock));
        } else if (!WifiLockManager.getInstance().isSameWifiLockConnected(context, mLock.getSsid())) {
            AppDialog.showAlertDialog(context, context.getString(R.string.no_wifi_lock), context.getString(R.string.connect_same_wifi_lock));
        } else {
            String id = null;
            String key = null;
            if (mLock.isOffline()) {
                id = mLock.getLockIds().get(0).getKey();
                key = mLock.getLockKeys().get(0).getKey();
            } else {
                //String tempKey = null;
                for (LockKeys lockKeys : mLock.getLockKeys()) {
                    if (lockKeys.getUserType().equalsIgnoreCase(Constant.OWNER_ID)) {
                        id = lockKeys.getKey();
                    } else {
                        key = lockKeys.getKey();
                    }
                }
            }
            final WifiLock mWifiLock = new WifiLock();
            mWifiLock.setOwnerId(AESEncryption.getInstance().decrypt(id, mLock.isEncrypted()));
            mWifiLock.setSlotKey(AESEncryption.getInstance().decrypt(key, mLock.isEncrypted()));

            ServiceManager.getInstance().post(ServiceUrl.WIFI_LOCK_ENROLL_FP, mWifiLock, new ResponseHandler() {
                @Override
                public void onSuccess(Object data) {

                    Loader.getInstance().hideLoader();
                    Logger.d(data.toString());
                    WifiLockResponse response = new Gson().fromJson(data.toString(), WifiLockResponse.class);

                    if (response != null && response.getErrorMessage() != null
                            && response.getErrorMessage().equalsIgnoreCase(FP_UID_ENROLLED)) {
                        ArrayList<String> fpId = fpUser.getKeys();
                        fpId.add(String.format("%02d", Integer.parseInt(response.getResponse().getFpId())));

                        fpUser.setKeys(fpId);
                        forgetWIFINetwork(context, mLock);

                        AppDialog.showAlertDialog(context, "Success", "Finger Print Added Successfully",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        Logger.d("######### fpUser.getId() : "+fpUser.getId());

                                        if (TextUtils.isEmpty(fpUser.getId())) {
                                            doAddFingerPrintServer(context, fpUser, reqCode);
                                        } else {
                                            doUpdateFingerPrintServer(context, fpUser, reqCode);
                                        }
                                    }
                                });
                    }
                }

                @Override
                public void onAuthError(String message) {
                    Loader.getInstance().hideLoader();
                    AppDialog.showAlertDialog(context, "Invalid Lock Key. Please contact support.");
                }

                @Override
                public void onError(String message) {

                    Loader.getInstance().hideLoader();

                    if (message != null && message.equals("No Internet. Please Check your network connection.")) {

                        AppDialog.showAlertDialog(context, "Lock timed out. Please try again. ");

                    } else {
                        try {
                            WifiLockResponse mWifiLockResponse = new Gson().fromJson(message, WifiLockResponse.class);

                            Logger.d("### fp_count = " + fp_count);
                            Logger.d("### fp_count getErrorMessage = " + mWifiLockResponse.getErrorMessage());

                            if (mWifiLockResponse.getErrorMessage().equalsIgnoreCase(FP_NEXT_SAMPLE)) {
                                fp_count++;
                                AppDialog.showAlertDialog(context, "Add Finger Print", "Please place the same finger again " +
                                        fp_count + " of 3", "NEXT", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Loader.getInstance().showLoader(context);
                                        doAddFingerPrintRequest(context, reqCode, mLock, fpUser);
                                    }
                                });
                            } else if (mWifiLockResponse.getErrorMessage().equalsIgnoreCase(FP_NOT_PLACED)) {
                                AppDialog.showAlertDialog(context, "Error", "Its seems finger not placed properly. Please try again",
                                        "NEXT", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Loader.getInstance().showLoader(context);
                                                doAddFingerPrintRequest(context, reqCode, mLock, fpUser);
                                            }
                                        });
                            } else if (mWifiLockResponse.getErrorMessage().equalsIgnoreCase(FP_BAD_FINGER)) {
                                AppDialog.showAlertDialog(context, "Error", "Bad Finger detected. Please try again",
                                        "NEXT", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Loader.getInstance().showLoader(context);
                                                doAddFingerPrintRequest(context, reqCode, mLock, fpUser);
                                            }
                                        });
                            } else if (mWifiLockResponse.getErrorMessage().equalsIgnoreCase(FP_MAX_TRIES_EXIT)) {
                                forgetWIFINetwork(context, mLock);
                                AppDialog.showAlertDialog(context, "Error", "Please try after some time. The maximum number try exists",
                                        "DONE", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                viewBinder.onViewUpdate(reqCode, null);
                                            }
                                        });

                            } else if (mWifiLockResponse.getErrorMessage().equalsIgnoreCase(FP_MAX_USR)) {
                                AppDialog.showAlertDialog(context, "Error", "The maximum number of user exists",
                                        "DONE", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                viewBinder.onViewUpdate(reqCode, null);
                                            }
                                        });
                            } else if (mWifiLockResponse.getErrorMessage().equalsIgnoreCase(FP_NOT_CONNECTED)) {
                                AppDialog.showAlertDialog(context, "Error", "Finger print not connected",
                                        "DONE", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                viewBinder.onViewUpdate(reqCode, null);
                                            }
                                        });
                            } else if (mWifiLockResponse.getErrorMessage().equalsIgnoreCase(FP_TRY_OTHER_FINGER)) {
                                AppDialog.showAlertDialog(context, "Error", "Please try with different finger", "NEXT", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Loader.getInstance().showLoader(context);
                                        doAddFingerPrintRequest(context, reqCode, mLock, fpUser);
                                    }
                                });
                            } else if (mWifiLockResponse.getErrorMessage().equalsIgnoreCase(FP_UID_ENROLLED)) {
                                ArrayList<String> fpId = fpUser.getKeys();
                                fpId.add(String.format("%02d", Integer.parseInt(mWifiLockResponse.getResponse().getFpId())));
                                fpUser.setKeys(fpId);
                                forgetWIFINetwork(context, mLock);
                                AppDialog.showAlertDialog(context, "Success", "Finger Print Added Successfully",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                if (TextUtils.isEmpty(fpUser.getId())) {
                                                    doAddFingerPrintServer(context, fpUser, reqCode);
                                                } else {
                                                    doUpdateFingerPrintServer(context, fpUser, reqCode);
                                                }
                                            }
                                        });
                            } else if (mWifiLockResponse.getErrorMessage().equalsIgnoreCase(FPListFragment.AUTHVIA_FP_DISABLED)) {
                                String errorMsg = context.getString(R.string.no_fp_access_msg_master);
                                AppDialog.showAlertDialog(context, "Error", errorMsg, "DONE", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        viewBinder.onViewUpdate(reqCode, null);
                                    }
                                });
                            }

                        } catch (Exception e) {
                            AppDialog.showAlertDialog(context, message);
                            e.printStackTrace();
                        }
                    }
                }

            });
        }
    }

    public void doRevokePrintRequest(Context context, int reqCode, Lock mLock, FPUser fpUser) {
        if (!WifiLockManager.getInstance().isWifiEnabled(context)) {
            AppDialog.showAlertDialog(context, context.getString(R.string.turn_on_wifi_connect_lock));
        } else if (!WifiLockManager.getInstance().isWifiLockConnected(context)) {
            AppDialog.showAlertDialog(context, context.getString(R.string.no_wifi_lock), context.getString(R.string.connect_wifi_lock));
        } else if (!WifiLockManager.getInstance().isSameWifiLockConnected(context, mLock.getSsid())) {
            AppDialog.showAlertDialog(context, context.getString(R.string.no_wifi_lock), context.getString(R.string.connect_same_wifi_lock));
        } else {
            String id = null;
            String key = null;
            if (mLock.isOffline()) {
                id = mLock.getLockIds().get(0).getKey();
                key = mLock.getLockKeys().get(0).getKey();
            } else {
                //String tempKey = null;
                for (LockKeys lockKeys : mLock.getLockKeys()) {
                    if (lockKeys.getUserType().equalsIgnoreCase(Constant.OWNER_ID)) {
                        id = lockKeys.getKey();
                    } else {
                        key = lockKeys.getKey();
                    }
                }
            }
            WifiLock mWifiLock = new WifiLock();
            mWifiLock.setOwnerId(AESEncryption.getInstance().decrypt(id, mLock.isEncrypted()));
            mWifiLock.setSlotKey(AESEncryption.getInstance().decrypt(key, mLock.isEncrypted()));
            final int numberOfFP = fpUser.getKeys().size();
            for (String fpId : fpUser.getKeys()) {
                int fpIntId = Integer.parseInt(fpId);
                mWifiLock.setFpId(fpIntId);
                ServiceManager.getInstance().post(ServiceUrl.WIFI_LOCK_DELETE_FP, mWifiLock, new ResponseHandler() {
                    @Override
                    public void onSuccess(Object data) {
                        Loader.getInstance().hideLoader();
                        Logger.d(data.toString());
                        WifiLockResponse response = new Gson().fromJson(data.toString(), WifiLockResponse.class);
                        if (response != null && response.getStatus() != null
                                && response.getStatus().equalsIgnoreCase("success")) {
                            revokeCount++;
                            if (numberOfFP == revokeCount) {
                                forgetWIFINetwork(context, mLock);
                                AppDialog.showAlertDialog(context, "Success", "Finger Print Deleted Successfully", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        ArrayList<String> emptyKey = new ArrayList<>();
                                        fpUser.setKeys(emptyKey);
                                        doRevokeFingerPrint(context, fpUser, reqCode, false);
                                    }
                                });
                            }

                        }
                    }

                    @Override
                    public void onAuthError(String message) {
                        Loader.getInstance().hideLoader();
                        AppDialog.showAlertDialog(context, "Invalid Lock Key. Please contact support.");
                    }

                    @Override
                    public void onError(String message) {
                        Loader.getInstance().hideLoader();
                        if (message != null && message.equals("No Internet. Please Check your network connection.")) {
                            AppDialog.showAlertDialog(context, "Lock timed out. Please try again. ");
                        } else {
                            try {
                                WifiLockResponse response = new Gson().fromJson(message, WifiLockResponse.class);
                                if (response != null && response.getStatus() != null
                                        && response.getStatus().equalsIgnoreCase("success")) {
                                    revokeCount++;
                                    if (numberOfFP == revokeCount) {
                                        forgetWIFINetwork(context, mLock);
                                        AppDialog.showAlertDialog(context, "Success", "Finger Print Deleted Successfully", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                ArrayList<String> emptyKey = new ArrayList<>();
                                                fpUser.setKeys(emptyKey);
                                                doAddFingerPrintServer(context, fpUser, reqCode);
                                            }
                                        });
                                    }

                                } else if (response.getErrorMessage().equalsIgnoreCase(FP_USER_ID_NOT_EXISTS)) {
                                    forgetWIFINetwork(context, mLock);
                                    AppDialog.showAlertDialog(context, "Error", "The deleted fingerprint ID not exists.", "DONE", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                        }
                                    });
                                } else if (response.getErrorMessage().equalsIgnoreCase(FP_ID_ALREADY_EMPTY)) {
                                    forgetWIFINetwork(context, mLock);
                                    AppDialog.showAlertDialog(context, "Error", "The Fingerprint ID is already empty.", "DONE", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                        }
                                    });
                                }
                            } catch (Exception e) {
                                AppDialog.showAlertDialog(context, message);
                                e.printStackTrace();
                            }
                        }

                    }
                });
            }
        }
    }

    public void getUserList(Context context, Lock lock, ResponseHandler handler) {
        Loader.getInstance().showLoader(context);
        FPService.getInstance().getUserList(lock.getId(), new ResponseHandler() {
            @Override
            public void onSuccess(Object data) {
                Loader.getInstance().hideLoader();
                if (data != null) {
                    try {
                        UserListResponse response = new Gson().fromJson(data.toString(), UserListResponse.class);
                        handler.onSuccess(response);
                    } catch (Exception e) {
                        handler.onError(context.getString(R.string.error_parser));
                    }
                } else {
                    handler.onError(context.getString(R.string.error_parser));
                }
            }

            @Override
            public void onAuthError(String message) {
                Loader.getInstance().hideLoader();
                handler.onAuthError(message);
            }

            @Override
            public void onError(String message) {
                Loader.getInstance().hideLoader();
                handler.onError(message);
            }
        });
    }

    public void addOrRevokePrivilege(Context context, JsonObject payload, int reqCode) {
        Loader.getInstance().showLoader(context);
        FPService.getInstance().addOrRevokePrivilege(payload, new ResponseHandler() {
            @Override
            public void onSuccess(Object data) {
                Loader.getInstance().hideLoader();
                if (data != null) {
                    viewBinder.onViewUpdate(reqCode, data);
                } else {
                    onError(context.getString(R.string.error_parser));
                }
            }

            @Override
            public void onAuthError(String message) {
                Loader.getInstance().hideLoader();
                AppDialog.showAlertDialog(context, message);
            }

            @Override
            public void onError(String message) {
                Loader.getInstance().hideLoader();
                AppDialog.showAlertDialog(context, message);
            }
        });
    }

    public void getRFIDOrFPList(Activity activity, String lockId, String type, int reqCode) {
        AssignRequestUserService.getInstance().getAssignUserList(lockId, "0", type, new ResponseHandler() {
            @Override
            public void onSuccess(Object data) {
                Loader.getInstance().hideLoader();
                if (data != null) {
                    AssignUser assignUser = (AssignUser) data;
                    viewBinder.onViewUpdate(reqCode, assignUser.getKeys());

                }

            }

            @Override
            public void onAuthError(String message) {
                Loader.getInstance().hideLoader();
                if (activity != null) {
                    AppDialog.showAlertDialog(activity, message, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            App.getInstance().showLogin(activity);
                        }
                    });
                }
            }

            @Override
            public void onError(String message) {
                Loader.getInstance().hideLoader();
                AppDialog.showAlertDialog(activity, message);

            }
        });
    }

    public void updateGuestName(Context context, LockKeys lockKeys, int reqCode) {
        Loader.getInstance().showLoader(context);
        FPService.getInstance().updateGuestName(lockKeys, new ResponseHandler() {
            @Override
            public void onSuccess(Object data) {
                Loader.getInstance().hideLoader();
                if (data != null) {
                    try {
                        BaseResponse response = new Gson().fromJson(data.toString(), BaseResponse.class);
                        viewBinder.onViewUpdate(reqCode, response);
                    } catch (Exception e) {
                        onError(context.getString(R.string.error_parser));
                    }
                } else {
                    onError(context.getString(R.string.error_parser));
                }
            }

            @Override
            public void onAuthError(String message) {
                Loader.getInstance().hideLoader();
                if (context != null) {
                    AppDialog.showAlertDialog(context, message, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            App.getInstance().showLogin((Activity) context);
                        }
                    });
                }
            }

            @Override
            public void onError(String message) {
                Loader.getInstance().hideLoader();
                AppDialog.showAlertDialog(context, message);
            }
        });
    }

    private void doAddFingerPrintServer(Context context, FPUser fpUser, int reqCode) {

        if (ServiceManager.getInstance().isNetworkAvailable(context)) {
            Loader.getInstance().showLoader(context);
            fpUser.setOriginalKey(new Gson().toJson(fpUser.getKeys()));
            FPService.getInstance().addFPRequest(fpUser, new ResponseHandler() {
                @Override
                public void onSuccess(Object data) {
                    Loader.getInstance().hideLoader();
                    if (data != null) {
                        try {
                            BaseResponse response = new Gson().fromJson(data.toString(), BaseResponse.class);
                            viewBinder.onViewUpdate(reqCode, "success");
                        } catch (Exception e) {
                            onError(context.getString(R.string.error_parser));
                        }
                    } else {
                        onError(context.getString(R.string.error_parser));
                    }
                }

                @Override
                public void onAuthError(String message) {
                    Loader.getInstance().hideLoader();
                    storeOffLine(fpUser, reqCode);
                    if (context != null) {
                        AppDialog.showAlertDialog(context, message, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                App.getInstance().showLogin((Activity) context);
                            }
                        });
                    }
                }

                @Override
                public void onError(String message) {
                    Logger.d("##### offline fpUser : "+fpUser.toString());
                    Logger.d("##### offline reqCode : "+reqCode);
                    Loader.getInstance().hideLoader();
                    storeOffLine(fpUser, reqCode);
                    AppDialog.showAlertDialog(context, message);
                }
            });
        }
        else {
            Logger.d("##### offline fpUser : "+fpUser.toString());
            Logger.d("##### offline reqCode : "+reqCode);
            storeOffLine(fpUser, reqCode);
        }
    }

    private void doUpdateFingerPrintServer(Context context, FPUser fpUser, int reqCode) {
        RequestUser requestUser = new RequestUser();
        try {
            requestUser.setRequestId(fpUser.getId());
            requestUser.setUserId(fpUser.getUserId());
            requestUser.setKey(new Gson().toJson(fpUser.getKeys()));
            requestUser.setStatus("2");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (ServiceManager.getInstance().isNetworkAvailable(context)) {
            Loader.getInstance().showLoader(context);
            if (ServiceManager.getInstance().isNetworkAvailable(context)) {
                Loader.getInstance().showLoader(context);
                RequestsUserService.getInstance().revokeRequest(requestUser, requestUser.getRequestId(), new ResponseHandler() {
                    @Override
                    public void onSuccess(Object data) {
                        Loader.getInstance().hideLoader();
                        viewBinder.onViewUpdate(reqCode, "success");
                    }

                    @Override
                    public void onAuthError(String message) {
                        Loader.getInstance().hideLoader();
                        storeOffline(requestUser, context, reqCode, false);
                    }

                    @Override
                    public void onError(String message) {
                        Loader.getInstance().hideLoader();
                        storeOffline(requestUser, context, reqCode, false);
                    }
                });

            } else {
                storeOffline(requestUser, context, reqCode, false);
            }

        } else {
            storeOffline(requestUser, context, reqCode, false);
        }
    }

    private void storeOffLine(FPUser fpUser, int reqCode) {
        Logger.d("######## fpUser : "+fpUser.toString());
        Logger.d("######## reqCode : "+reqCode);

        FingerPrints fingerPrints = SecuredStorageManager.getInstance().getOfflineFPs();
        ArrayList<FPUser> extFPUser = fingerPrints.getFpUsers();
        Logger.d("######## extFPUser before add : "+extFPUser);
        extFPUser.add(fpUser);
        Logger.d("######## extFPUser after add : "+extFPUser);
        fingerPrints.setFpUsers(extFPUser);
        Logger.d("######## fingerPrints after add : "+fingerPrints);
        SecuredStorageManager.getInstance().setOfflineFPs(fingerPrints);
        Logger.d("######## getOffline FPs : "+SecuredStorageManager.getInstance().getOfflineFPs().toString());
        viewBinder.onViewUpdate(reqCode, "success");
    }

    public void doRevokeFingerPrint(Context context, FPUser fpUser, int reqCode, boolean isMQTT) {
        RequestUser requestUser = new RequestUser();
        try {
            requestUser.setRequestId(fpUser.getId());
            requestUser.setUserId(fpUser.getUserId());
            requestUser.setKey("[]");
            requestUser.setStatus("0");
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (ServiceManager.getInstance().isNetworkAvailable(context)) {
            Loader.getInstance().showLoader(context);
            RequestsUserService.getInstance().revokeRequest(requestUser, requestUser.getRequestId(), new ResponseHandler() {
                @Override
                public void onSuccess(Object data) {
                    Loader.getInstance().hideLoader();
                    if (viewBinder != null && !isMQTT) {
                        viewBinder.onViewUpdate(reqCode, "success");
                    }
                }

                @Override
                public void onAuthError(String message) {
                    Loader.getInstance().hideLoader();
                    storeOffline(requestUser, context, reqCode, isMQTT);
                }

                @Override
                public void onError(String message) {
                    Loader.getInstance().hideLoader();
                    storeOffline(requestUser, context, reqCode, isMQTT);
                }
            });

        } else {
            storeOffline(requestUser, context, reqCode, isMQTT);
        }

    }

    private void forgetWIFINetwork(Context context, Lock mLock) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                String SSID = BleManager.MANUFACTURER_CODE + mLock.getSerialNumber();
                WifiUtilManager.forgetMyNetwork(context, SSID);
            }
        }, 500);


    }

    private void storeOffline(RequestUser requestUser, Context context, int reqCode, boolean isMQTT) {
        SecuredStorageManager.getInstance().saveRevokeUserList(requestUser.getRequestId(), requestUser);
        SyncScheduler.getInstance().schedule(context, Constant.JOBS.ALL);
        Toast.makeText(context, "Fingerprint access updated successfully", Toast.LENGTH_LONG).show();
        if (viewBinder != null && !isMQTT) {
            viewBinder.onViewUpdate(reqCode, "success");
        }
    }


}
