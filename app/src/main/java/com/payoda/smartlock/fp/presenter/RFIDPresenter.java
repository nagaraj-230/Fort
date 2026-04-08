package com.payoda.smartlock.fp.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.google.gson.Gson;
import com.payoda.smartlock.App;
import com.payoda.smartlock.BuildConfig;
import com.payoda.smartlock.R;
import com.payoda.smartlock.authentication.BaseFragment;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.constants.ServiceUrl;
import com.payoda.smartlock.fp.view.FPListFragment;
import com.payoda.smartlock.fp.view.RFIDListFragment;
import com.payoda.smartlock.locks.LockManager;
import com.payoda.smartlock.locks.model.Lock;
import com.payoda.smartlock.locks.model.LockKeys;
import com.payoda.smartlock.locks.model.WifiLock;
import com.payoda.smartlock.locks.model.WifiLockResponse;
import com.payoda.smartlock.plugins.bluetooth.BleManager;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.plugins.storage.SecuredStorageManager;
import com.payoda.smartlock.plugins.storage.StorageManager;
import com.payoda.smartlock.plugins.wifi.WifiLockManager;
import com.payoda.smartlock.plugins.wifi.WifiUtilManager;
import com.payoda.smartlock.service.AESEncryption;
import com.payoda.smartlock.service.SyncScheduler;
import com.payoda.smartlock.users.model.AssignUserResponse;
import com.payoda.smartlock.users.model.RequestUser;
import com.payoda.smartlock.users.service.RequestsUserService;
import com.payoda.smartlock.utils.AppDialog;
import com.payoda.smartlock.utils.Loader;
import com.payoda.smartlock.utils.Logger;
import com.payoda.smartlock.utils.SLViewBinder;

import static com.payoda.smartlock.constants.Constant.HW_VERSION_6_0;
import static com.payoda.smartlock.constants.Constant.RFID_EMPTY;
import static com.payoda.smartlock.constants.Constant.WIFI_DELAY_TIME;

public class RFIDPresenter {

    private int rfid_count = 1;
    private SLViewBinder viewBinder;

    private final String RF_NO_DETECT_OR_MATCH = "RF_NO_DETECT_OR_MATCH";
    private final String RF_NO_FREE_SLOTS = "RF_NO_FREE_SLOTS";
    private final String RF_ALREADY_EXISTS = "RF_ALREADY_EXISTS";
    private final String RF_ID_NOT_EXISTS = "RF_ID_NOT_EXISTS";
    private final String RF_ID_ALREADY_EMPTY = "RF_ID_ALREADY_EMPTY";
    private final String RF_FOB_ENROLLED = "RF_FOB_ENROLLED";
    private final String RF_MAX_TRIES_EXIT = "RF_MAX_TRIES_EXIT";

    private final int RFID_ADD=1;
    private final int RFID_REVOKE=2;

    public RFIDPresenter(SLViewBinder viewBinder) {
        this.viewBinder = viewBinder;
    }

    public void doAddRFID(Context context, int reqCode, Lock mLock,LockKeys rfidkey) {
        rfid_count = 1;

        String status;
        String msg;
        if (mLock != null && mLock.getLockVersion().equalsIgnoreCase(HW_VERSION_6_0)){
            status = LockManager.getInstance().preCheckBLEVersion6((Activity) context);
            msg = "Long Press \"1\" on the Lock and Click \"OK\"";
        }else {
            status = LockManager.getInstance().preCheckBLE((Activity) context);
            msg = "Please switch on the lock and try again.";
        }
        if (!status.equalsIgnoreCase("success")) {
            AppDialog.showAlertDialog((Activity) context, status);
            return;
        }

        AppDialog.showAlertDialog(context, "Activate Lock", msg,
                "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {


                doAddOrDeleteRFIDViaWifi(context, mLock,rfidkey,reqCode,RFID_ADD);

            }
        }, "CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
    }

    private void doAddRFIDRequest(Context context, int reqCode, Lock mLock,LockKeys rfidKey) {
        if (!WifiLockManager.getInstance().isWifiEnabled(context)) {
            AppDialog.showAlertDialog(context, context.getString(R.string.turn_on_wifi_connect_lock));
        }
        else if (!WifiLockManager.getInstance().isWifiLockConnected(context)) {
            AppDialog.showAlertDialog(context, context.getString(R.string.no_wifi_lock), context.getString(R.string.connect_wifi_lock));
        }
        else if (!WifiLockManager.getInstance().isSameWifiLockConnected(context, mLock.getSsid())) {
            AppDialog.showAlertDialog(context, context.getString(R.string.no_wifi_lock), context.getString(R.string.connect_same_wifi_lock));
        }
        else {
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
            mWifiLock.setOwnerId(AESEncryption.getInstance().decrypt(id,mLock.isEncrypted()));
            mWifiLock.setSlotKey(AESEncryption.getInstance().decrypt(key,mLock.isEncrypted()));
            ServiceManager.getInstance().post(ServiceUrl.WIFI_LOCK_ENROLL_RF, mWifiLock, new ResponseHandler() {
                @Override
                public void onSuccess(Object data) {
                    Loader.getInstance().hideLoader();
                    Logger.d(data.toString());
                    WifiLockResponse response = new Gson().fromJson(data.toString(), WifiLockResponse.class);
                    if (response != null && response.getErrorMessage() != null) {
                        if (response.getErrorMessage().equalsIgnoreCase(RF_FOB_ENROLLED)) {
                            forgetWIFINetwork(context,mLock);
                            AppDialog.showAlertDialog(context, "Success", "RFID Added Successfully",
                                    new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //viewBinder.onViewUpdate(reqCode, response);
                                    doUpdateRFID(context,rfidKey.getId(),response.getResponse().getRfid(),reqCode);
                                }
                            });
                        } else {
                            rfid_count++;
                            AppDialog.showAlertDialog(context, "Add RFID", "Please place the RFID " + rfid_count + " of 3", "NEXT", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Loader.getInstance().showLoader(context);
                                    doAddRFIDRequest(context, reqCode, mLock,rfidKey);
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
                    if(message != null && message.equals("No Internet. Please Check your network connection.")){
                        AppDialog.showAlertDialog(context,"Lock timed out. Please try again. ");
                    }else {
                        try {
                            WifiLockResponse mWifiLockResponse = new Gson().fromJson(message, WifiLockResponse.class);
                            if (mWifiLockResponse.getErrorMessage().equalsIgnoreCase(RF_NO_DETECT_OR_MATCH)) {
                                AppDialog.showAlertDialog(context, "Error", "RF fob key is not detected or not matched with previous swipe. Please try again", "NEXT", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Loader.getInstance().showLoader(context);
                                        doAddRFIDRequest(context, reqCode, mLock, rfidKey);
                                    }
                                });
                            } else if (mWifiLockResponse.getErrorMessage().equalsIgnoreCase(RF_NO_FREE_SLOTS)) {
                                forgetWIFINetwork(context, mLock);
                                AppDialog.showAlertDialog(context, "Error", "No empty slots available to add new RF Fob key.", "Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        viewBinder.onViewUpdate(reqCode, null);
                                    }
                                });
                            }
                            else if (mWifiLockResponse.getErrorMessage().equalsIgnoreCase(RF_ALREADY_EXISTS)) {
                                forgetWIFINetwork(context, mLock);
                                AppDialog.showAlertDialog(context, "Error", "RF fob key already exists or enrolled.", "Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        viewBinder.onViewUpdate(reqCode, null);
                                    }
                                });
                            }
                            else if (mWifiLockResponse.getErrorMessage().equalsIgnoreCase(RF_ID_NOT_EXISTS)) {
                                forgetWIFINetwork(context, mLock);
                                AppDialog.showAlertDialog(context, "Error", "RF fob key does not exists.", "Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        viewBinder.onViewUpdate(reqCode, null);
                                    }
                                });
                            }
                            else if (mWifiLockResponse.getErrorMessage().equalsIgnoreCase(RF_MAX_TRIES_EXIT)) {
                                forgetWIFINetwork(context, mLock);
                                AppDialog.showAlertDialog(context, "Error", "RF fob key maximum swipes exceeded. ", "Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        viewBinder.onViewUpdate(reqCode, null);
                                    }
                                });
                            }
                            else if (mWifiLockResponse.getErrorMessage().equalsIgnoreCase(RF_FOB_ENROLLED)) {
                                forgetWIFINetwork(context, mLock);
                                AppDialog.showAlertDialog(context, "Success", "RFID Added Successfully", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //viewBinder.onViewUpdate(reqCode, response);
                                        doUpdateRFID(context, rfidKey.getId(), mWifiLockResponse.getResponse().getRfid(), reqCode);
                                    }
                                });
                            }
                            else if (mWifiLockResponse.getErrorMessage().equalsIgnoreCase(FPListFragment.AUTHVIA_FP_DISABLED)) {
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

    public void doDeleteRFID(Context context, int reqCode, Lock mLock,LockKeys rfidKey) {
        rfid_count = 1;

        String status;
        String msg;
        if (mLock != null && mLock.getLockVersion().equalsIgnoreCase(HW_VERSION_6_0)){
            status = LockManager.getInstance().preCheckBLEVersion6((Activity) context);
            msg = "Long Press \"1\" on the Lock and Click \"OK\"";
        }else {
            status = LockManager.getInstance().preCheckBLE((Activity) context);
            msg = "Please switch on the lock and try again.";
        }
        if (!status.equalsIgnoreCase("success")) {
            AppDialog.showAlertDialog((Activity) context, status);
            return;
        }

        AppDialog.showAlertDialog(context, "Activate Lock", msg, "OK",
                (dialogInterface, i) -> doAddOrDeleteRFIDViaWifi(context,mLock,rfidKey,reqCode, RFID_REVOKE),
                "CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
    }

    private void doAddOrDeleteRFIDViaWifi(Context context,Lock mLock,LockKeys rfidKey,int reqCode,int type){
        Loader.getInstance().showLoader(context);
        String SSID = BleManager.MANUFACTURER_CODE + mLock.getSerialNumber();
        String password = mLock.getScratchCode();
        String lockVersion = mLock.getLockVersion();
        WifiUtilManager wifiUtilManager=new WifiUtilManager(context, new WifiUtilManager.WifiListener() {
            @Override
            public void connectionSuccess() {
                Logger.d("Connection Success");
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(type==RFID_REVOKE) {
                            doDeleteRFIDRequest(context, reqCode, mLock, rfidKey);
                        }else if(type==RFID_ADD){
                            doAddRFIDRequest(context,reqCode,mLock,rfidKey);
                        }
                    }
                }, WIFI_DELAY_TIME);

            }

            @Override
            public void connectionTimeOut() {
                Loader.getInstance().hideLoader();
                forgetWIFINetwork(context,mLock);
                Toast.makeText(context,"Please try again later",Toast.LENGTH_LONG).show();
            }
        });
        wifiUtilManager.startScanning(lockVersion,SSID,password);
    }

    private void doDeleteRFIDRequest(Context context, int reqCode, Lock mLock,LockKeys rfidKey) {
        if (!WifiLockManager.getInstance().isWifiEnabled(context)) {
            AppDialog.showAlertDialog(context, context.getString(R.string.turn_on_wifi_connect_lock));
        } else if (!WifiLockManager.getInstance().isWifiLockConnected(context)) {
            AppDialog.showAlertDialog(context, context.getString(R.string.no_wifi_lock), context.getString(R.string.connect_wifi_lock));
        }
        else if (!WifiLockManager.getInstance().isSameWifiLockConnected(context, mLock.getSsid())) {
            AppDialog.showAlertDialog(context, context.getString(R.string.no_wifi_lock), context.getString(R.string.connect_same_wifi_lock));
        }
        else {
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
            mWifiLock.setOwnerId(AESEncryption.getInstance().decrypt(id,mLock.isEncrypted()));
            mWifiLock.setSlotKey(AESEncryption.getInstance().decrypt(key,mLock.isEncrypted()));
            mWifiLock.setRfId(Integer.parseInt(rfidKey.getKey()));
            ServiceManager.getInstance().post(ServiceUrl.WIFI_LOCK_DELETE_RF, mWifiLock, new ResponseHandler() {
                @Override
                public void onSuccess(Object data) {
                    Loader.getInstance().hideLoader();
                    Logger.d(data.toString());

                    WifiLockResponse response = new Gson().fromJson(data.toString(), WifiLockResponse.class);
                    if (response != null && response.getStatus() != null && response.getStatus().equalsIgnoreCase("success")) {
                        forgetWIFINetwork(context,mLock);
                        AppDialog.showAlertDialog(context, "Success", "RFID Deleted Successfully", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //viewBinder.onViewUpdate(reqCode, response);
                                doUpdateRFID(context,rfidKey.getId(),Constant.RFID_EMPTY,reqCode);
                            }
                        });
                    }else{
                        AppDialog.showAlertDialog(context, "Please try again after sometimes.");
                    }
                }

                @Override
                public void onAuthError(String message) {
                    Loader.getInstance().hideLoader();
                    forgetWIFINetwork(context,mLock);
                    AppDialog.showAlertDialog(context, "Invalid Lock Key. Please contact support.");
                }

                @Override
                public void onError(String message) {
                    Loader.getInstance().hideLoader();
                    if(message != null && message.equals("No Internet. Please Check your network connection.")){
                        AppDialog.showAlertDialog(context,"Lock timed out. Please try again.");
                    }else {
                        try {
                            WifiLockResponse mWifiLockResponse = new Gson().fromJson(message, WifiLockResponse.class);
                            if (mWifiLockResponse.getErrorMessage().equalsIgnoreCase(RF_ID_ALREADY_EMPTY)) {
                                forgetWIFINetwork(context, mLock);
                                AppDialog.showAlertDialog(context, "Error", "Delete the RF slot when its already empty.", "Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //viewBinder.onViewUpdate(reqCode, null);
                                        doUpdateRFID(context, rfidKey.getId(), RFID_EMPTY, reqCode);
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

    private void doUpdateRFID(Context context,String id,String key,int reqCode) {
        final RequestUser payload = new RequestUser();
        payload.setRequestId(id);
        payload.setKey(key);
        payload.setStatus("1");

        if (ServiceManager.getInstance().isNetworkAvailable(context)) {
            ((Activity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Loader.getInstance().showLoader(context);
                    RequestsUserService.getInstance().revokeRequest(payload, id, new ResponseHandler() {
                        @Override
                        public void onSuccess(Object data) {
                            Loader.getInstance().hideLoader();
                            if (data != null) {
                                AssignUserResponse assignUserResponse = (AssignUserResponse) data;
                                if (assignUserResponse.getStatus().equalsIgnoreCase("success")) {
                                    viewBinder.onViewUpdate(reqCode,"success");
                                }
                            }
                        }

                        @Override
                        public void onAuthError(String message) {
                            Loader.getInstance().hideLoader();
                            storeOffline(payload,(Activity)context,reqCode);
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
                            viewBinder.onViewUpdate(reqCode,"failure");
                            storeOffline(payload,(Activity)context,reqCode);
                            AppDialog.showAlertDialog(context, message);
                        }
                    });
                }
            });

        } else {
            storeOffline(payload,(Activity)context,reqCode);
        }

    }

    private void storeOffline(RequestUser payload,Activity activity,int reqCode) {
        SecuredStorageManager.getInstance().saveRevokeUserList(payload.getRequestId(), payload);
        SyncScheduler.getInstance().schedule(activity, Constant.JOBS.ALL);
        viewBinder.onViewUpdate(reqCode,"success");
    }

    private void forgetWIFINetwork(Context context,Lock mLock) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                String SSID = BleManager.MANUFACTURER_CODE + mLock.getSerialNumber();
                WifiUtilManager.forgetMyNetwork(context,SSID);
            }
        }, 500);


    }

}
