package com.payoda.smartlock.locks;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.payoda.smartlock.App;
import com.payoda.smartlock.BuildConfig;
import com.payoda.smartlock.R;
import com.payoda.smartlock.authentication.BaseFragment;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.constants.ServiceUrl;
import com.payoda.smartlock.locks.adapter.LockListAdapter;
import com.payoda.smartlock.locks.callback.IKeyListener;
import com.payoda.smartlock.locks.callback.ILockSelectCallback;
import com.payoda.smartlock.locks.model.Lock;
import com.payoda.smartlock.locks.model.LockKeys;
import com.payoda.smartlock.locks.model.Locks;
import com.payoda.smartlock.locks.model.WifiLock;
import com.payoda.smartlock.locks.service.LockListService;
import com.payoda.smartlock.plugins.bluetooth.BleManager;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.plugins.storage.SecuredStorageManager;
import com.payoda.smartlock.plugins.storage.StorageManager;
import com.payoda.smartlock.plugins.storage.lock.LockDBClient;
import com.payoda.smartlock.plugins.wifi.WifiLockManager;
import com.payoda.smartlock.plugins.wifi.WifiUtilManager;
import com.payoda.smartlock.service.AESEncryption;
import com.payoda.smartlock.service.SyncScheduler;
import com.payoda.smartlock.splash.model.BrandInfoResponse;
import com.payoda.smartlock.utils.AppDialog;
import com.payoda.smartlock.utils.AppUtils;
import com.payoda.smartlock.utils.Loader;
import com.payoda.smartlock.utils.Logger;

import java.util.ArrayList;

import static com.payoda.smartlock.constants.Constant.HW_VERSION_6_0;
import static com.payoda.smartlock.constants.Constant.WIFI_DELAY_TIME;

/**
 * Created by david on 31/10/18.
 */

public class OwnersLockFragment extends BaseFragment implements ILockSelectCallback {

    public static final String TAG = "#### OwnersLockFragment";

    private RecyclerView mRecyclerView;
    private LockListAdapter mLockListAdapter;
    private FloatingActionButton fb_add;
    private TextView tv_no_lock_error;
    private LinearLayout linHeader;
    private TextView tv_title;
    private ImageView iv_back;
    private String fromScreen;
    private Lock mLock;
    private final int REQ_FACTORY_RESET = 2;
    private int RESET_ATTEMPT_COUNT = 1;

    public OwnersLockFragment() {
        // Required empty public constructor
    }

    public static OwnersLockFragment getInstance() {
        return new OwnersLockFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Logger.d(TAG, TAG);
        return initialiseView(inflater.inflate(R.layout.fragment_owner_lock_list, container, false));
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private View initialiseView(View rootView) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            fromScreen = bundle.getString(Constant.SCREEN_DATA);
        }
        linHeader = rootView.findViewById(R.id.linHeader);
        tv_title = rootView.findViewById(R.id.tv_title);
        iv_back = rootView.findViewById(R.id.iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });
        fb_add = rootView.findViewById(R.id.fb_add);

        if (fromScreen != null && fromScreen.equalsIgnoreCase(Constant.NAVIGATION_LOCK_TRANSFER_OWNER)) {
            tv_title.setText(getResources().getString(R.string.transfer_owner));
        }
        else if (fromScreen != null && fromScreen.equalsIgnoreCase(Constant.NAVIGATION_LOCK_FACTORY_RESET)) {
            tv_title.setText(getResources().getString(R.string.factory_reset));
        }
        tv_no_lock_error = rootView.findViewById(R.id.tv_no_lock_error);

        mRecyclerView = rootView.findViewById(R.id.lockListDetailView);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        // Set Layout Manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(OwnersLockFragment.this.getContext());
        mRecyclerView.setLayoutManager(linearLayoutManager);
        // Limiting the size
        mRecyclerView.setHasFixedSize(true);
        mLockListAdapter = new LockListAdapter(getContext(),null, this);
        mRecyclerView.setAdapter(mLockListAdapter);
        doLockListRequest();
        return rootView;
    }

    private void doLockListRequest() {
        Loader.getInstance().showLoader(getContext());
        LockListService.getInstance().getLock(20,0,new ResponseHandler() {
            @Override
            public void onSuccess(Object data) {
                Loader.getInstance().hideLoader();
                if (data != null) {
                    Locks mLocks = (Locks) data;
                    prepopulateData(mLocks);
                }
            }

            @Override
            public void onAuthError(String message) {
                Loader.getInstance().hideLoader();
                if (getActivity() != null) {
                    AppDialog.showAlertDialog(getActivity(), message, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            App.getInstance().showLogin(getActivity());
                        }
                    });
                }
            }

            @Override
            public void onError(String message) {
                Loader.getInstance().hideLoader();
            }
        });

    }

    /**
     * Method to load data to adapter
     *
     * @param locks
     */
    private void prepopulateData(Locks locks) {
        try {
            ArrayList<Lock> tempAddList = new ArrayList<>();
            if (locks != null && locks.getLocks() != null) {
                for (Lock lock : locks.getLocks()) {
                    if (isStatusOne(lock)) {
                        tempAddList.add(lock);
                    }


                }
            }
            mLockListAdapter.clearList();
            mLockListAdapter.setItemList(tempAddList);
            changeViewVisibility();
        } catch (Exception e)

        {
            e.printStackTrace();
        }

    }

    private boolean isStatusOne(Lock lock) {
        boolean isOwnerRole = false;
        if (fromScreen != null && (fromScreen.equalsIgnoreCase(Constant.NAVIGATION_LOCK_TRANSFER_OWNER) ||
                fromScreen.equalsIgnoreCase(Constant.NAVIGATION_LOCK_FACTORY_RESET))) {
            if (lock.getStatus().equalsIgnoreCase("1") &&
                    lock.getLockKeys() != null && lock.getLockKeys().size() > 0) {
                ArrayList<LockKeys> lockKeys = lock.getLockKeys();
                if (lockKeys.get(0).getStatus().equalsIgnoreCase("1") && lockKeys.get(1).getUserType().equalsIgnoreCase(Constant.OWNER)) {
                    isOwnerRole = true;
                }
            }
        }
        return isOwnerRole;
    }

    private void changeViewVisibility() {
        if (mLockListAdapter != null && mLockListAdapter.getItemCount() > 0) {
            //Show List
            mRecyclerView.setVisibility(View.VISIBLE);
            tv_no_lock_error.setVisibility(View.GONE);
        } else {
            //Hide List
            mRecyclerView.setVisibility(View.GONE);
            tv_no_lock_error.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLockItemSelect(Lock lock) {

        mLock = lock;

        // TODO if this app support all lock version means use this
        // TODO if this app support V6.0 only means delete this and you need to update Brand info-manufacturerCode from Backend as 'FORT_'
        // TODO if this app support from v1.0 to V3.2 means delete this and you need to update Brand info- manufacturerCode from Backend as 'ASTRIX_'
        // TODO currently brand info - manufacturer code from Backend is 'FORT_'
        // TODO Make sure Aware of this
//        WifiLockManager.getInstance().updateManufactureCode(mLock);
//        BleManager.getInstance().updateManufactureCode(mLock);

        if (lock != null) {
            if (fromScreen != null && fromScreen.equalsIgnoreCase(Constant.NAVIGATION_LOCK_TRANSFER_OWNER)) {

                if (ServiceManager.getInstance().isNetworkAvailable(getContext())) {

                    // TODO confirm this flow with raahul
                    if (lock.getLockVersion().equalsIgnoreCase(HW_VERSION_6_0) && mLock.getEnablePassage() != null &&
                            lock.getEnablePassage().equalsIgnoreCase(Constant.PASSAGE_ON)) {
                        // Disabled the passage mode before transfer owner
                        AppDialog.showAlertDialog(getContext(), getString(R.string.passage_is_enabled));
                    }
                    else {
                        if (getActivity() != null)
                            App.getInstance().showFullScreen(getActivity(), Constant.SCREEN.TRANSFER_OWNER, new Gson().toJson(lock));
                    }

                } else {
                    AppDialog.showAlertDialog(getContext(), getString(R.string.no_internet));
                }

            }
            else if (fromScreen != null && fromScreen.equalsIgnoreCase(Constant.NAVIGATION_LOCK_FACTORY_RESET)) {
                //Factory Reset
                factoryReset();
            }
        }
        else {
            AppDialog.showAlertDialog(getContext(), getString(R.string.lock_detail_error));
        }

    }

    private void factoryReset() {

        String status;
        String msg;
        if (mLock != null && mLock.getLockVersion().equalsIgnoreCase(HW_VERSION_6_0)){
            status = LockManager.getInstance().preCheckBLEVersion6(getActivity());
            msg = "Long Press \"1\" on the Lock and Click \"OK\"";
        }else {
            status = LockManager.getInstance().preCheckBLE(getActivity());
            msg = "Please switch on the lock and try again.";
        }



        if (!status.equalsIgnoreCase("success")) {
            AppDialog.showAlertDialog(getActivity(), status);
            return;
        }

        AppDialog.showAlertDialog(getContext(), "Activate Lock", msg,
                "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getLocationPermission(new RequestPermissionAction() {
                    @Override
                    public void permissionDenied() {

                    }

                    @Override
                    public void permissionGranted() {
                        doFactoryReset();
                    }
                });
            }
        }, "CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
    }

    private void doFactoryReset() {
        AppDialog.showAlertDialog(getContext(), getString(R.string.factory_reset), getString(R.string.reset_confirm_message),
                "YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mLock != null && mLock.getLockKeys() != null && !mLock.getLockKeys().isEmpty()) {
                    //Check same wifi lock connected or not
                    Loader.getInstance().showLoader(getContext());
                    if (WifiLockManager.getInstance().isSameWifiLockConnected(getContext(), mLock.getSsid())) {
                        doFactoryResetViaWIFI();
                    } else {
                        //connectDeviceWithWifi(REQ_FACTORY_RESET);
                        //forceStartBLE();
                        String SSID = BleManager.MANUFACTURER_CODE + mLock.getSerialNumber();
                        String password = mLock.getScratchCode();
                        String lockVersion = mLock.getLockVersion();
                        WifiUtilManager wifiUtilManager = new WifiUtilManager(getContext(), new WifiUtilManager.WifiListener() {
                            @Override
                            public void connectionSuccess() {
                                Logger.d("Connection Success");
                                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        doFactoryResetViaWIFI();
                                    }
                                }, WIFI_DELAY_TIME);

                            }

                            @Override
                            public void connectionTimeOut() {
                                Logger.d("Connection Timeout");
                                if (mLock.getLockVersion() != null && !mLock.getLockVersion().equalsIgnoreCase(HW_VERSION_6_0)){
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        getBluetoothScanConnectPermission(new RequestPermissionAction() {
                                            @Override
                                            public void permissionDenied() {
                                                BleManager.getInstance().checkBluetoothScanPermission(getActivity());
                                            }

                                            @Override
                                            public void permissionGranted() {
                                                doFactoryResetViaBLE();
                                            }
                                        });
                                    }else {
                                        doFactoryResetViaBLE();
                                    }
                                }
                            }
                        });
                        wifiUtilManager.startScanning(lockVersion, SSID, password);
                    }
                }
            }
        }, "NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

    }

    private String getAuthenticationKey() {
        String key = null;
        if (mLock != null && mLock.getLockKeys() != null && mLock.getLockKeys().size() > 0) {
            if (mLock.isOffline()) {
                key = AESEncryption.getInstance().decrypt(mLock.getLockIds().get(0).getKey(),mLock.isEncrypted());
                key = key + AESEncryption.getInstance().decrypt(mLock.getLockKeys().get(0).getKey(),mLock.isEncrypted());
            } else {
                String id = null;
                String tempKey = null;
                for (LockKeys lockKeys : mLock.getLockKeys()) {
                    if (lockKeys.getUserType().equalsIgnoreCase(Constant.OWNER_ID)) {
                        id = AESEncryption.getInstance().decrypt(lockKeys.getKey(),mLock.isEncrypted());
                    } else {
                        tempKey = AESEncryption.getInstance().decrypt(lockKeys.getKey(),mLock.isEncrypted());
                    }
                }
                key = id + tempKey;
            }
        }
        return key;
    }

    private void doFactoryResetViaBLE() {
        String ssid = BleManager.MANUFACTURER_CODE + mLock.getSerialNumber();
        LockManager.getInstance().factoryReset(getActivity(), mLock.getUuid(), ssid, getAuthenticationKey(), new IKeyListener() {
            @Override
            public void onLockIds(ArrayList<String> alIds) {
            }

            @Override
            public void onLockKeys(ArrayList<String> alKeys) {
            }

            @Override
            public void onBatteryUpdate(String battery) {

            }

            @Override
            public void onAccessLog(ArrayList<String> alLogs) {

            }

            @Override
            public void onLockActivated() {
                mLock.setStatus(Constant.FACTORY_RESET);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        doUpdateLockServiceRequest(true);
                    }
                });

            }

            @Override
            public void onDeviceNotConnected() {
                Loader.getInstance().hideLoader();
                if (ServiceManager.getInstance().isMobileDataEnabled(getContext())) {
                    AppDialog.showAlertDialog(getContext(), "Please switch-off your mobile data and try again");
                } else {
                    AppDialog.showAlertDialog(getContext(), getContext().getString(R.string.lock_failure_alert));
                }
            }

            @Override
            public void onMacAddressUpdate(String ssid, String macAddress) {

            }
        });
    }

    private void doUpdateLockServiceRequest(final boolean isGoBack) {

        Toast.makeText(getContext(),"Lock reset successfully. Please connect to the internet for the process to complete."
                ,Toast.LENGTH_LONG).show();

        forgetWIFINetwork();

        if ((ServiceManager.getInstance().isNetworkAvailable(getActivity())
                || ServiceManager.getInstance().isMobileDataEnabled(getContext()))&& mLock.getId() != null) {
            Loader.getInstance().showLoader(getContext());
            LockListService.getInstance().updateLock(mLock, new ResponseHandler() {
                @Override
                public void onSuccess(Object data) {
                    Loader.getInstance().hideLoader();
                    mLock.setSync(true);
                    LockDBClient.getInstance().save(mLock,getContext());
                    if (isGoBack && getActivity() != null) {
                        requireActivity().getOnBackPressedDispatcher().onBackPressed();
                    }
                }

                @Override
                public void onAuthError(String message) {
                    Loader.getInstance().hideLoader();
                    doOfflineUpdateLock();
                    SyncScheduler.getInstance().schedule(getActivity(), Constant.JOBS.ALL);
                    if (isGoBack && getActivity() != null) {
                        requireActivity().getOnBackPressedDispatcher().onBackPressed();
                    }
                }

                @Override
                public void onError(String message) {
                    Loader.getInstance().hideLoader();
                    doOfflineUpdateLock();
                    SyncScheduler.getInstance().schedule(getActivity(), Constant.JOBS.ALL);
                    if (isGoBack && getActivity() != null) {
                        requireActivity().getOnBackPressedDispatcher().onBackPressed();
                    }
                }
            });
        }
        else {
            //Add to offline list for update.
            Loader.getInstance().hideLoader();

            doOfflineUpdateLock();

            if (getActivity() != null) {

                SyncScheduler.getInstance().schedule(getActivity(), Constant.JOBS.ALL);

                if (isGoBack) {
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                }

            }
        }

    }

    private void doOfflineUpdateLock(){

        /*boolean isLockFound = false;
        Locks mLocks = StorageManager.getInstance().getOfflineLocks();
        for (int i = 0; i < mLocks.getLocks().size(); i++) {
            if (mLocks.getLocks().get(i).getSerialNumber().equalsIgnoreCase(mLock.getSerialNumber())) {
                mLocks.getLocks().set(i, mLock);
                isLockFound = true;
                break;
            }
        }
        if (!isLockFound) {
            mLocks.getLocks().add(mLock);
        }
        StorageManager.getInstance().setOfflineLocks(mLocks);*/

        mLock.setSync(false);
        LockDBClient.getInstance().save(mLock,getContext());
        AppUtils.getInstance().printJavaObject(mLock);

    }

    private void doFactoryResetViaWIFI() {
        if (!WifiLockManager.getInstance().isWifiEnabled(getContext())) {
            AppDialog.showAlertDialog(getContext(), getString(R.string.turn_on_wifi_connect_lock));
        }
        else if (!WifiLockManager.getInstance().isWifiLockConnected(getContext())) {
            AppDialog.showAlertDialog(getContext(), getString(R.string.no_wifi_lock), getString(R.string.connect_wifi_lock));
        }
        else if (!WifiLockManager.getInstance().isSameWifiLockConnected(getContext(), mLock.getSsid())) {
            AppDialog.showAlertDialog(getContext(), getString(R.string.no_wifi_lock), getString(R.string.connect_same_wifi_lock));
        }
        else {
            /*
             * Get Key Start
             */
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

            /*
             * Get Key End
             */
            final WifiLock mWifiLock = new WifiLock();
            mWifiLock.setOwnerId(AESEncryption.getInstance().decrypt(id,mLock.isEncrypted()));
            mWifiLock.setSlotKey(AESEncryption.getInstance().decrypt(key,mLock.isEncrypted()));
            ServiceManager.getInstance().post(ServiceUrl.WIFI_LOCK_RESET, mWifiLock, new ResponseHandler() {
                @Override
                public void onSuccess(Object data) {
                    Loader.getInstance().hideLoader();
                    mLock.setStatus(Constant.FACTORY_RESET);
                    doUpdateLockServiceRequest(true);
                }

                @Override
                public void onAuthError(String message) {
                    Loader.getInstance().hideLoader();
                    AppDialog.showAlertDialog(getContext(), "Invalid Lock Key. Please contact support.");
                }

                @Override
                public void onError(String message) {

                    RESET_ATTEMPT_COUNT++;
                    if (RESET_ATTEMPT_COUNT > 2 || (!WifiLockManager.getInstance().isWifiLockConnected(getContext()))) {

                        Loader.getInstance().hideLoader();
                        RESET_ATTEMPT_COUNT = 1;

                        if (ServiceManager.getInstance().isMobileDataEnabled(getContext())) {
                            AppDialog.showAlertDialog(getContext(), "Please switch-off your mobile data and try again");
                        } else {
                            AppDialog.showAlertDialog(getContext(), getContext().getString(R.string.lock_failure_alert));
                        }

                    } else {

                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //iv_engage.performClick();
                                doFactoryResetViaWIFI();
                            }
                        }, 2000);

                    }

                }
            });
        }
    }

    private void forgetWIFINetwork() {
        String SSID = BleManager.MANUFACTURER_CODE + mLock.getSerialNumber();
        WifiUtilManager.forgetMyNetwork(getContext(),SSID);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
