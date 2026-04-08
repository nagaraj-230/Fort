package com.payoda.smartlock.managepins;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.payoda.smartlock.App;
import com.payoda.smartlock.BuildConfig;
import com.payoda.smartlock.FullscreenActivity;
import com.payoda.smartlock.R;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.constants.ServiceUrl;
import com.payoda.smartlock.locks.LockDetailFragment;
import com.payoda.smartlock.locks.LockManager;
import com.payoda.smartlock.locks.model.Lock;
import com.payoda.smartlock.locks.model.LockKeys;
import com.payoda.smartlock.locks.model.WifiLockResponse;
import com.payoda.smartlock.locks.service.LockListService;
import com.payoda.smartlock.managepins.model.AuthViaPinFp;
import com.payoda.smartlock.managepins.model.ManagePinPrivilege;
import com.payoda.smartlock.model.RemoteAccessModel;
import com.payoda.smartlock.plugins.bluetooth.BleManager;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ResponseModel;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.plugins.pushnotification.RemoteDataEvent;
import com.payoda.smartlock.plugins.storage.SecuredStorageManager;
import com.payoda.smartlock.plugins.storage.lock.LockDBClient;
import com.payoda.smartlock.plugins.wifi.WifiLockManager;
import com.payoda.smartlock.plugins.wifi.WifiUtilManager;
import com.payoda.smartlock.request.service.RequestService;
import com.payoda.smartlock.service.AESEncryption;
import com.payoda.smartlock.service.SyncScheduler;
import com.payoda.smartlock.utils.AppDialog;
import com.payoda.smartlock.utils.InactiveTimeoutUtil;
import com.payoda.smartlock.utils.Loader;
import com.payoda.smartlock.utils.Logger;

import java.util.ArrayList;

import static com.payoda.smartlock.constants.Constant.HW_VERSION_6_0;
import static com.payoda.smartlock.constants.Constant.WIFI_DELAY_TIME;
import static com.payoda.smartlock.utils.InactiveTimeoutUtil.SESSION_TIMEOUT;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class TabManagePinsActivity extends AppCompatActivity implements InactiveTimeoutUtil.TimeOutListener {

    public static final String TAG = "### TabManagePinsActivity";

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private Switch managePinSimpleSwitch;
    private View managePinDivider;
    private TextView tvNoAccess;
    private TabLayout tabLayout;
    private ViewPager mViewPager;
    private DigitalKeysFragment digitalKeysFragment;
    private OtpKeysFragment otpKeysFragment;

    private Lock mLock;
    private boolean allowAPICall = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tab_manage_pins);
        Logger.d(TAG, TAG);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mLock = new Gson().fromJson(bundle.getString(Constant.SCREEN_DATA), Lock.class);
        }

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        managePinSimpleSwitch = (Switch) findViewById(R.id.managePinSimpleSwitch);
        tvNoAccess = (TextView) findViewById(R.id.tv_no_pin_access);
        managePinDivider = (View) findViewById(R.id.v_manage_privilege_divider);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.manage_pin_container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        //LinearLayout header = findViewById(R.id.header);
        ((TextView) findViewById(R.id.tv_title)).setText(getString(R.string.manage_pin));
        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TabManagePinsActivity.this.finish();
            }
        });

        managePinSimpleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (allowAPICall) {
                    if (mLock.getLockVersion().equalsIgnoreCase(Constant.HW_VERSION_4_0)) {
                        ManagePinPrivilege managePinPrivilege = new ManagePinPrivilege();
                        managePinPrivilege.setEnablePin(isChecked ? "1" : "0");

                        Loader.getInstance().showLoader(TabManagePinsActivity.this);
                        ServiceManager.getInstance().patch(String.format(ServiceUrl.MANAGE_PIN_MQTT, mLock.getSerialNumber()), managePinPrivilege, new ResponseHandler() {
                            @Override
                            public void onSuccess(Object data) {
                                Loader.getInstance().hideLoader();
                            }

                            @Override
                            public void onAuthError(String message) {
                                Loader.getInstance().hideLoader();
                                allowAPICall = false;
                                managePinSimpleSwitch.setChecked(!isChecked);
                                AppDialog.showAlertDialog(TabManagePinsActivity.this, message);
                            }

                            @Override
                            public void onError(String message) {
                                Loader.getInstance().hideLoader();
                                allowAPICall = false;
                                managePinSimpleSwitch.setChecked(!isChecked);
                                AppDialog.showAlertDialog(TabManagePinsActivity.this, message);
                            }
                        });
                    } else {
                        doUpdatePinPrivilege(isChecked);
                    }
                } else {
                    allowAPICall = true;
                }
            }
        });

        tabLayout = (TabLayout) findViewById(R.id.manage_pins_tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        digitalKeysFragment = DigitalKeysFragment.getInstance();
        digitalKeysFragment.setArguments(bundle);

        otpKeysFragment = OtpKeysFragment.getInstance();
        otpKeysFragment.setArguments(bundle);

        boolean pinAccess = (mLock.getEnablePin() != null && mLock.getEnablePin().equalsIgnoreCase("1")) ? true : false;
        if (pinAccess) {
            allowAPICall = false;
            managePinSimpleSwitch.setChecked(true);
            showTabAndOtherControls();
        } else {
            managePinSimpleSwitch.setChecked(false);
            hideTabAndOtherControls();
        }
    }

    private void hideTabAndOtherControls() {
        managePinDivider.setVisibility(View.GONE);
        tabLayout.setVisibility(View.GONE);
        mViewPager.setVisibility(View.GONE);

        tvNoAccess.setVisibility(View.VISIBLE);
    }

    private void showTabAndOtherControls() {
        managePinDivider.setVisibility(View.VISIBLE);
        tabLayout.setVisibility(View.VISIBLE);
        mViewPager.setVisibility(View.VISIBLE);

        tvNoAccess.setVisibility(View.GONE);
    }

    private void doUpdatePinPrivilege(boolean isChecked) {


        String status;
        String msg;
        if (mLock != null && mLock.getLockVersion().equalsIgnoreCase(HW_VERSION_6_0)) {
            status = LockManager.getInstance().preCheckBLEVersion6(TabManagePinsActivity.this);
            msg = "Long Press \"1\" on the Lock and Click \"OK\"";
        } else {
            status = LockManager.getInstance().preCheckBLE(TabManagePinsActivity.this);
            msg = "Please switch on the lock and try again.";
        }
        if (!status.equalsIgnoreCase("success")) {
            AppDialog.showAlertDialog(TabManagePinsActivity.this, status);
            return;
        }

        AppDialog.showAlertDialog(TabManagePinsActivity.this, "Activate Lock", msg,
                "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                doAuthViaPin(isChecked);
            }
        }, "CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                allowAPICall = false;
                managePinSimpleSwitch.setChecked(!isChecked);
            }
        });
    }

    private void doAuthViaPin(boolean isChecked) {
        Loader.getInstance().showLoader(TabManagePinsActivity.this);
        String SSID = BleManager.MANUFACTURER_CODE + mLock.getSerialNumber();
        String password = mLock.getScratchCode();
        String lockVersion = mLock.getLockVersion();
        WifiUtilManager wifiUtilManager = new WifiUtilManager(TabManagePinsActivity.this, new WifiUtilManager.WifiListener() {
            @Override
            public void connectionSuccess() {
                Logger.d("Connection Success");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doAuthViaPinRequest(TabManagePinsActivity.this, isChecked);
                    }
                }, WIFI_DELAY_TIME);
            }

            @Override
            public void connectionTimeOut() {
                allowAPICall = false;
                managePinSimpleSwitch.setChecked(!isChecked);
                Loader.getInstance().hideLoader();
                Toast.makeText(TabManagePinsActivity.this, "Please try again later", Toast.LENGTH_LONG).show();
            }
        });
        wifiUtilManager.startScanning(lockVersion, SSID, password);
    }

    private void doAuthViaPinRequest(Context context, boolean isChecked) {
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
            AuthViaPinFp authViaPinFp = new AuthViaPinFp();
            authViaPinFp.setOwnerId(AESEncryption.getInstance().decrypt(id, mLock.isEncrypted()));
            authViaPinFp.setSlotKey(AESEncryption.getInstance().decrypt(key, mLock.isEncrypted()));
            authViaPinFp.setEnDis(isChecked ? "1" : "0");

            ServiceManager.getInstance().post(ServiceUrl.WIFI_LOCK_AUTH_VIA_PIN, authViaPinFp, new ResponseHandler() {
                @Override
                public void onSuccess(Object data) {
                    Loader.getInstance().hideLoader();
                    if (data != null) {
                        WifiLockResponse authViaPinResponse = new Gson().fromJson(data.toString(), WifiLockResponse.class);
                        if (authViaPinResponse != null && authViaPinResponse.getStatus() != null
                                && authViaPinResponse.getStatus().equalsIgnoreCase("success")) {

                            hideTabAndOtherControls();
                            mLock.setEnablePin(isChecked ? "1" : "0");
                            LockDetailFragment.mLock.setEnablePin(isChecked ? "1" : "0");
                            String msg = isChecked ? "enabled" : "disabled";
                            forgetWIFINetwork(context, mLock);
                            AppDialog.showAlertDialog(context, "Success", "PIN access " + msg + " successfully",
                                    new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    doOfflineUpdateLock(context);
                                }
                            });

                        } else {
                            onError(data.toString());
                        }
                    } else {
                        AppDialog.showAlertDialog(TabManagePinsActivity.this, "Invalid response from Lock. Please try again later.");
                    }
                }

                @Override
                public void onAuthError(String message) {
                    allowAPICall = false;
                    managePinSimpleSwitch.setChecked(!isChecked);
                    Loader.getInstance().hideLoader();
                    Logger.d(message);
                    AppDialog.showAlertDialog(context, "Invalid Lock Key. Please contact support.");
                }

                @Override
                public void onError(String message) {
                    allowAPICall = false;
                    managePinSimpleSwitch.setChecked(!isChecked);
                    Loader.getInstance().hideLoader();
                    try {
                        if (message != null && message.equals("No Internet. Please Check your network connection.")) {
                            AppDialog.showAlertDialog(context, message);
                        } else {
                            WifiLockResponse mWifiLockResponse = new Gson().fromJson(message, WifiLockResponse.class);
                            String errorMsg = "";
                            if (mWifiLockResponse != null && mWifiLockResponse.getErrorMessage() != null && mWifiLockResponse.getErrorMessage().equalsIgnoreCase(ManagePinConstant.AUTHVIA_TP_DISABLED)) {
                                forgetWIFINetwork(context, mLock);
                                errorMsg = context.getString(R.string.no_pin_access_msg);
                            } else {
                                errorMsg = context.getString(R.string.error_parser);
                            }
                            AppDialog.showAlertDialog(context, "Error", errorMsg, "Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                        }
                    } catch (Exception e) {
                        AppDialog.showAlertDialog(context, "Unable to connect the lock. Please try again.");
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void doOfflineUpdateLock(Context context) {
        mLock.setSync(false);
        LockDBClient.getInstance().save(mLock, context);
        if (context != null) {
            getOnBackPressedDispatcher().onBackPressed();
        }
    }

    @Override
    public void onInactiveTimeOut() {
        super.onUserInteraction();
        TabManagePinsActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showDialog();
            }
        });
    }

    private void showDialog() {
        App.getInstance().showFullScreen(this, Constant.SCREEN.TIME_OUT_PIN, null);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 1) {
                return otpKeysFragment;
            } else {
                return digitalKeysFragment;
            }
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SyncScheduler.getInstance().schedule(TabManagePinsActivity.this, Constant.JOBS.ALL);
        validateTimeout();
        InactiveTimeoutUtil.startTimer(TabManagePinsActivity.this, TabManagePinsActivity.this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        InactiveTimeoutUtil.startTimer(TabManagePinsActivity.this, this);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(RemoteDataEvent remoteDataEvent) {

        if (remoteDataEvent != null && remoteDataEvent.getStatus().equalsIgnoreCase(Constant.SUCCESS) &&
                ((remoteDataEvent.getCommand().equalsIgnoreCase(Constant.PIN_ON_COMMAND) ||
                        remoteDataEvent.getCommand().equalsIgnoreCase(Constant.PIN_OFF_COMMAND)))) {

            Loader.getInstance().hideLoader();
            String pinStatus = remoteDataEvent.getCommand().equalsIgnoreCase(Constant.PIN_ON_COMMAND) ? "1" : "0";

            LockDetailFragment.mLock.setEnablePin(pinStatus);

            AppDialog.showAlertDialog(this, remoteDataEvent.getTitle(), remoteDataEvent.getBody(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (TabManagePinsActivity.this != null)
                        getOnBackPressedDispatcher().onBackPressed();
                }
            });
        }
        else if (remoteDataEvent != null && remoteDataEvent.getStatus().equalsIgnoreCase(Constant.FAILURE) &&
                ((remoteDataEvent.getCommand().equalsIgnoreCase(Constant.PIN_ON_COMMAND) ||
                        remoteDataEvent.getCommand().equalsIgnoreCase(Constant.PIN_OFF_COMMAND)))) {
            Loader.getInstance().hideLoader();
            AppDialog.showAlertDialog(this, remoteDataEvent.getBody());
        }
        else if (remoteDataEvent != null && (remoteDataEvent.getCommand().equalsIgnoreCase(Constant.REMOTE_ACCESS_COMMAND))) {
             // Remote access v6.0
            Loader.getInstance().hideLoader();
            if (!TabManagePinsActivity.this.isFinishing() &&AppDialog.alertDialog != null && AppDialog.alertDialog.isShowing()) {
                AppDialog.alertDialog.dismiss();
                Logger.d("### Alert showing dismissed");
            }
            AppDialog.showAlertDialog(this, getString(R.string.app_name), remoteDataEvent.getBody(),

                    "Accept", (dialog, which) -> {


                        String input = remoteDataEvent.getStatus();

                        if (input != null) {

                            String[] parts = input.split(",");

                            if (parts.length == 2) {
                                String requestId = parts[0];
                                String lockSerialNo = parts[1];
                                doAcceptOrRejectRequest(Constant.ACCEPT, lockSerialNo, requestId);
                            }

                        } else {
                            Toast.makeText(this, "Failed, Please try again", Toast.LENGTH_SHORT).show();

                        }


                        dialog.dismiss();

                    },
                    "Reject", (dialog, which) -> {

                        String input = remoteDataEvent.getStatus();

                        if (input != null) {

                            String[] parts = input.split(",");

                            if (parts.length == 2) {
                                String requestId = parts[0];
                                String lockSerialNo = parts[1];
                                doAcceptOrRejectRequest(Constant.REJECT, lockSerialNo, requestId);
                            }

                        } else {
                            Toast.makeText(this, "Failed, Please try again", Toast.LENGTH_SHORT).show();

                        }

                        dialog.dismiss();
                    },
                    "", (dialog, which) -> {
                    }/*,Constant.DIALOG_DISMISS_SECS*/
            );
        }

    }

    // Remote Access for V6.0
    private void doAcceptOrRejectRequest(String status, String lockSerialNo, String requestId) {

        Loader.getInstance().showLoader(this);

        try {

            ResponseHandler handler = new ResponseHandler() {
                @Override
                public void onSuccess(Object data) {
                    if (data != null) {
                        ResponseModel responseModel = (ResponseModel) data;
                        if (responseModel.getStatus().equalsIgnoreCase("success")) {

                            Logger.d(TAG, "success");
                            Loader.getInstance().hideLoader();

                            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            notificationManager.cancel(1);

                            Toast.makeText(TabManagePinsActivity.this, responseModel.getMessage(), Toast.LENGTH_SHORT).show();


                        } else {
                            Logger.d(TAG, "Failed");
                            Loader.getInstance().hideLoader();
                        }
                    }
                }

                @Override
                public void onAuthError(String message) {
                    Logger.d(TAG, "onAuthError");
                    Loader.getInstance().hideLoader();
                }

                @Override
                public void onError(String message) {
                    Logger.d(TAG, "onError");
                    Loader.getInstance().hideLoader();
                }

            };

            RemoteAccessModel mData = new RemoteAccessModel();

            if (status.equalsIgnoreCase(Constant.ACCEPT)) { // accept
                mData.setLockStatus(Constant.ACCEPT);
            } else { // reject
                mData.setLockStatus(Constant.REJECT);
            }

            mData.setLockSerialNo(lockSerialNo);
            mData.setRequestId(requestId);

            RequestService.getInstance().remoteAccess(mData, handler);

        } catch (Exception e) {
            Logger.e(e);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (AppDialog.alertDialog != null && AppDialog.alertDialog.isShowing()) {
            AppDialog.alertDialog.dismiss();
            Logger.d("### Alert showing dismissed onDestroy ");
        }
        Loader.getInstance().dismissLoader();
        InactiveTimeoutUtil.stopTimer();
    }

    @Override
    protected void onDestroy() {
        if (AppDialog.alertDialog != null && AppDialog.alertDialog.isShowing()) {
            AppDialog.alertDialog.dismiss();
            Logger.d("### Alert showing dismissed onDestroy ");
        }
        super.onDestroy();
        InactiveTimeoutUtil.stopTimer();
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        InactiveTimeoutUtil.startTimer(TabManagePinsActivity.this, this);
        SecuredStorageManager.getInstance().setTimeOutSeconds();
    }

    private void validateTimeout() {
        long curSeconds = System.currentTimeMillis() / 1000;
        long timeOutSeconds = SecuredStorageManager.getInstance().getTimeOutSeconds();

        if ((curSeconds - timeOutSeconds) < SESSION_TIMEOUT) {
            if (SecuredStorageManager.getInstance().isFreshLaunch()) {
                showDialog();
                SecuredStorageManager.getInstance().setFreshLaunch(false);
            }
        } else {
            showDialog();
        }
    }

    private void forgetWIFINetwork(Context context, Lock mLock) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                String SSID = BleManager.MANUFACTURER_CODE + mLock.getSerialNumber();
                WifiUtilManager.forgetMyNetwork(context,SSID);
            }
        }, 500);
    }
}