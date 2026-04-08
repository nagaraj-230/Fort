package com.payoda.smartlock;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.model.RemoteAccessModel;
import com.payoda.smartlock.model.VersionControlModel;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ResponseModel;
import com.payoda.smartlock.plugins.pushnotification.RemoteDataEvent;
import com.payoda.smartlock.plugins.storage.SecuredStorageManager;
import com.payoda.smartlock.plugins.storage.StorageManager;
import com.payoda.smartlock.request.model.Request;
import com.payoda.smartlock.request.model.RequestAccept;
import com.payoda.smartlock.request.service.RequestService;
import com.payoda.smartlock.service.VersionControlService;
import com.payoda.smartlock.utils.AppDialog;
import com.payoda.smartlock.utils.InactiveTimeoutUtil;
import com.payoda.smartlock.utils.Loader;
import com.payoda.smartlock.utils.Logger;

import static com.payoda.smartlock.utils.InactiveTimeoutUtil.SESSION_TIMEOUT;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity implements InactiveTimeoutUtil.TimeOutListener {

    public static final String TAG = "### FullscreenActivity";
    String strScreen = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);


        Logger.d(TAG, TAG);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            Constant.SCREEN screenName = Constant.SCREEN.LOGIN;
            strScreen = bundle.getString(Constant.SCREEN_NAME, null);
            if (strScreen != null) {
                screenName = Constant.SCREEN.valueOf(strScreen);
            }
            App.getInstance().navigateDetail(getSupportFragmentManager(), screenName, bundle.getString(Constant.SCREEN_DATA, null));
        } else {
            App.getInstance().navigateDetail(getSupportFragmentManager(), Constant.SCREEN.LOGIN);
        }

        //getVersionControl();


        OnBackPressedDispatcher();

    }

    private void OnBackPressedDispatcher() {

        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                Logger.d("### OnBackPressedCallback Fullscreen handleOnBackPressed");
                if (isNotPIN()) {
                    if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                        getSupportFragmentManager().popBackStack();
                        Logger.d("### OnBackPressedCallback Fullscreen 2 popBackStack");
                    } else {
                        Logger.d("### OnBackPressedCallback Fullscreen 3 finish ");
                        finish();
                    }
                }
            }
        };

        OnBackPressedDispatcher onBackPressedDispatcher = getOnBackPressedDispatcher();
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback);

    }

    private void startTimer() {
        if (isValidScreen(strScreen)) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    InactiveTimeoutUtil.startTimer(FullscreenActivity.this, FullscreenActivity.this);
                }
            }, 1200);
        } else {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    InactiveTimeoutUtil.stopTimer();
                }
            }, 1200);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTimer();
        if (isValidScreen(strScreen)) {
            validateTimeout();
        }

    }

    private boolean isValidScreen(String screenName) {
        if (screenName == null || screenName.isEmpty() ||
                screenName.equalsIgnoreCase(Constant.SCREEN.TIME_OUT_PIN.name())
                || screenName.equalsIgnoreCase(Constant.SCREEN.LOGIN.name()) ||
                screenName.equalsIgnoreCase(Constant.SCREEN.SIGNUP.name())
                || screenName.equalsIgnoreCase(Constant.SCREEN.TERMS.name())
                || screenName.equalsIgnoreCase(Constant.SCREEN.PRIVACY_POLICY.name())
                || screenName.equalsIgnoreCase(Constant.SCREEN.FORGOT_PASSWORD.name())
                || screenName.equalsIgnoreCase(Constant.SCREEN.LOCK_ADD.name())
                || screenName.equalsIgnoreCase(Constant.SCREEN.WIFI_MQTT_CONFIGURATION.name())
                || screenName.equalsIgnoreCase(Constant.SCREEN.FORGOT_PASSWORD_SUCCESS.name())) {
            SecuredStorageManager.getInstance().setTimeOutSeconds();
            return false;
        }
        return true;
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

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        if (isValidScreen(strScreen)) {
            InactiveTimeoutUtil.startTimer(FullscreenActivity.this, this);
            SecuredStorageManager.getInstance().setTimeOutSeconds();
        } else {
            InactiveTimeoutUtil.stopTimer();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(RemoteDataEvent remoteDataEvent) {

        // Remote Access for V6.0
        if (remoteDataEvent != null && (remoteDataEvent.getCommand().equalsIgnoreCase(Constant.REMOTE_ACCESS_COMMAND))) {

            Logger.d("### Fullscreen OnMsg Event");

            if (isValidScreen(strScreen)) {

                Logger.d("### Fullscreen OnMsg command id =  " + remoteDataEvent.getStatus());

                Loader.getInstance().hideLoader();

                if (!FullscreenActivity.this.isFinishing() && AppDialog.alertDialog != null && AppDialog.alertDialog.isShowing()) {
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
                        }
                        /*,Constant.DIALOG_DISMISS_SECS*/

                );
            }
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

                            Toast.makeText(FullscreenActivity.this, responseModel.getMessage(), Toast.LENGTH_SHORT).show();

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

    private boolean isNotPIN() {
        if (strScreen.equalsIgnoreCase(Constant.SCREEN.TIME_OUT_PIN.toString()) ||
                strScreen.equalsIgnoreCase(Constant.SCREEN.PIN.toString()) ||
                strScreen.equalsIgnoreCase(Constant.SCREEN.PIN_SUCCESS.toString()))
            return false;
        return true;
    }

    @Override
    public void onInactiveTimeOut() {
        FullscreenActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showDialog();
            }
        });
    }

    private void showDialog() {
        App.getInstance().showFullScreen(this, Constant.SCREEN.TIME_OUT_PIN, null);
    }

    @Override
    protected void onDestroy() {


        super.onDestroy();
        Loader.getInstance().dismissLoader();
        InactiveTimeoutUtil.stopTimer();

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (AppDialog.alertDialog != null && AppDialog.alertDialog.isShowing()) {
            AppDialog.alertDialog.dismiss();
            Logger.d("### Alert showing dismissed on pause ");
        }

        Loader.getInstance().dismissLoader();

        if (isValidScreen(strScreen)) {
            SecuredStorageManager.getInstance().setTimeOutSeconds();
        }

        InactiveTimeoutUtil.stopTimer();

    }

    @Override
    protected void onStart() {
        super.onStart();
        Logger.d("### Fullscreen Activity onStart");
        if (isValidScreen(strScreen)) {
            EventBus.getDefault().register(this);
            InactiveTimeoutUtil.startTimer(FullscreenActivity.this, this);
        }

    }

    @Override
    public void onStop() {

        super.onStop();

        Logger.d("### Fullscreen Activity onStop");
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK) {
            String state = intent.getStringExtra("state");
            if (state != null && state.equalsIgnoreCase("back")) {
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        }
        Logger.d("=====onActivityResult=======");
    }


}


