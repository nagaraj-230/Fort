package com.payoda.smartlock.locks;

import static com.payoda.smartlock.constants.Constant.HW_VERSION_6_0;
import static com.payoda.smartlock.constants.Constant.WIFI_DELAY_TIME;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.payoda.smartlock.App;
import com.payoda.smartlock.BuildConfig;
import com.payoda.smartlock.R;
import com.payoda.smartlock.authentication.BaseFragment;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.constants.ServiceUrl;
import com.payoda.smartlock.fp.presenter.FPPresenter;
import com.payoda.smartlock.fp.service.FPService;
import com.payoda.smartlock.locks.model.Lock;
import com.payoda.smartlock.locks.model.LockKeyList;
import com.payoda.smartlock.locks.model.LockKeys;
import com.payoda.smartlock.locks.model.Locks;
import com.payoda.smartlock.locks.model.WifiLockResponse;
import com.payoda.smartlock.locks.model.WifiMqttConfig;
import com.payoda.smartlock.locks.model.WifiMqttConfigV6;
import com.payoda.smartlock.locks.model.WifiMqttConnection;
import com.payoda.smartlock.locks.service.LockListService;
import com.payoda.smartlock.locks.service.WifiMqttConfigurationService;
import com.payoda.smartlock.model.BaseResponse;
import com.payoda.smartlock.plugins.bluetooth.BleManager;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.plugins.storage.SecuredStorageManager;
import com.payoda.smartlock.plugins.storage.lock.LockDBClient;
import com.payoda.smartlock.plugins.wifi.WifiLockManager;
import com.payoda.smartlock.plugins.wifi.WifiUtilManager;
import com.payoda.smartlock.service.AESEncryption;
import com.payoda.smartlock.service.SyncScheduler;
import com.payoda.smartlock.splash.model.BrandInfoResponse;
import com.payoda.smartlock.utils.AppDialog;
import com.payoda.smartlock.utils.Loader;
import com.payoda.smartlock.utils.Logger;

import java.util.ArrayList;
import java.util.Arrays;

public class WifiMqttConfigurationFragment extends BaseFragment implements View.OnClickListener {

    /**
     * A native method that is implemented by the "native-lib" native library,
     * which is packaged with this application.
     */
    public static native String getMqttIp(String buildVariant);

    public static native String getMqttPort(String buildVariant);

    public static final String TAG = "### WifiMqttConfigurationFragment";

    private ImageView ivEdit;
    private Lock mLock;

    //Version 4.0
    private TextInputLayout tl_wifiSSID, tl_wifiPassword;
    private EditText et_editLockName, et_wifiSSID, et_wifiPassword;
    private LinearLayout ll_wifiSecurityGroup;
    private int wifiSec = 0;

    public WifiMqttConfigurationFragment() {

    }

    public static WifiMqttConfigurationFragment getInstance() {
        return new WifiMqttConfigurationFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();

        if (bundle != null) {
            mLock = new Gson().fromJson(bundle.getString(Constant.SCREEN_DATA), Lock.class);
            Logger.d("###### lock wifi mqtt : " + mLock.toString());
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Logger.d(TAG, TAG);
        return initialiseView(inflater.inflate(R.layout.fragment_wifi_mqtt_configuration, container, false));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private View initialiseView(View view) {

        TextView tvTitle = view.findViewById(R.id.tv_title);
        tvTitle.setText(getString(R.string.wifi_mqtt_config));
        ivEdit = view.findViewById(R.id.iv_edit);
        ivEdit.setVisibility(View.GONE);

        view.findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();

            }
        });

        et_editLockName = view.findViewById(R.id.et_editLockName);
        if (mLock != null) {
            et_editLockName.setText(mLock.getName());
        }

        tl_wifiSSID = view.findViewById(R.id.editHomeWifiSSIDWrapper);
        tl_wifiPassword = view.findViewById(R.id.editHomeWifiPasswordWrapper);
        et_wifiSSID = view.findViewById(R.id.et_editHomeWifiSSID);
        et_wifiPassword = view.findViewById(R.id.et_editHomeWifiPassword);

        et_wifiSSID.addTextChangedListener(new WifiMqttConfigurationFragment.MyTextWatcher(et_wifiSSID));
        et_wifiPassword.addTextChangedListener(new WifiMqttConfigurationFragment.MyTextWatcher(et_wifiPassword));

        setupWifiGroupSpinner(view);

        Button btnCancel = view.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(this);
        Button btnAdd = view.findViewById(R.id.btnSave);
        btnAdd.setOnClickListener(this);

        // After wifi configuration, it will take some time to disconnect from the lock"s wifi.
        // So the newly added lock won"t be available in the lock list.
        // Fetch the lock list from the server and update it in the local db.

        if (mLock != null && mLock.getId() == null) {
            LockListService.getInstance().getLock(100, 0, new ResponseHandler() {
                @Override
                public void onSuccess(Object data) {
                    if (data != null) {
                        Locks mLocks = (Locks) data;
                        ArrayList<Lock> onLineLockList = mLocks.getLocks();

                        LockDBClient.getInstance().getAll(getContext(), new LockDBClient.DBCallback() {
                            @Override
                            public void onLockList(ArrayList<Lock> offLineLockList) {

                                if (offLineLockList != null && offLineLockList.size() > 0) {

                                    for (Lock onlineLock : onLineLockList) {
                                        boolean isFound = false;
                                        for (Lock offlineLock : offLineLockList) {
                                            if (onlineLock.getSerialNumber().equalsIgnoreCase(offlineLock.getSerialNumber())
                                                    && onlineLock.getId().equalsIgnoreCase(offlineLock.getId())) {
                                                isFound = true;
                                                break;
                                            }
                                        }

                                        if (!isFound) {
                                            Logger.d("Lock id --> ", onlineLock.getId());
                                            Logger.d("Serial Number --> ", onlineLock.getSerialNumber());

                                            LockDBClient.getInstance().save(onlineLock, getContext());
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onSuccess(String msg) {

                            }
                        });
                    }
                }

                @Override
                public void onAuthError(String message) {
                    Logger.d("onAuthError Wifi Config Lock List -->", message);
                }

                @Override
                public void onError(String message) {
                    Logger.d("onError Wifi Config Lock List -->", message);
                }
            });
        }

        return view;
    }

    private void setupWifiGroupSpinner(View view) {

        Spinner spinner = (Spinner) view.findViewById(R.id.editWifiSecurityGroupSpinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.wifi_group_array_v6_0, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        spinner.setSelection(3);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Logger.d("Select WiFi Group Name --> ", String.valueOf(position));
                wifiSec = position;
                String selectedLockVersion = parent.getItemAtPosition(position).toString();
                if (selectedLockVersion.equalsIgnoreCase("OPEN")) {
                    tl_wifiPassword.setVisibility(View.GONE);
                } else {
                    tl_wifiPassword.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCancel:
                if (getActivity() != null)
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                break;
            case R.id.btnSave:
                if (!validateWifiSsid()) {
                    return;
                }

                if (!validateWifiPassword()) {
                    return;
                }

                getLocationPermission(new RequestPermissionAction() {
                    @Override
                    public void permissionDenied() {

                    }

                    @Override
                    public void permissionGranted() {
                        doSaveConfiguration();
                    }
                });
                break;
        }
    }

    private void doSaveConfiguration() {

        if (mLock != null && mLock.isSensor()) {
            AppDialog.showAlertDialog(getActivity(), "Activate Sensor", "Please put the sensor into configuration mode and click \"OK\".",
                    "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            Loader.getInstance().showLoader(getContext());

                            // Door Sensor: Use sensor serial/address directly without FORT_ or F_ prefix
                            String SSID = mLock.getSerialNumber();
                            String password = mLock.getScratchCode();

                            WifiUtilManager wifiUtilManager = new WifiUtilManager(getContext(), new WifiUtilManager.WifiListener() {
                                @Override
                                public void connectionSuccess() {
                                    Logger.d("Sensor Connection Success");

                                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            doMqttWifiConfiguration(getContext(), mLock);
                                        }
                                    }, WIFI_DELAY_TIME);
                                }

                                @Override
                                public void connectionTimeOut() {
                                    Loader.getInstance().hideLoader();
                                    AppDialog.showAlertDialog(getActivity(), "Sensor connection timeout.");
                                }
                            });
                            wifiUtilManager.startScanning("", SSID, password);
                        }
                    });
            return;
        }

        String status;
        String msg;
        if (mLock != null && mLock.getLockVersion().equalsIgnoreCase(HW_VERSION_6_0)) {
            status = LockManager.getInstance().preCheckBLEVersion6(getActivity());
            msg = "Long Press \"1\" on the Lock and Click \"OK\"";
        } else {
            status = LockManager.getInstance().preCheckBLE(getActivity());
            msg = "Please switch on the lock and try again.";
        }
        if (!status.equalsIgnoreCase("success")) {
            AppDialog.showAlertDialog(getActivity(), status);
            return;
        }

        AppDialog.showAlertDialog(getActivity(), "Activate Lock", msg,
                "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Loader.getInstance().showLoader(getContext());

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
                                        doMqttWifiConfiguration(getContext(), mLock);
                                    }
                                }, WIFI_DELAY_TIME);
                            }

                            @Override
                            public void connectionTimeOut() {

                                if (mLock.getLockVersion() != null && mLock.getLockVersion().equalsIgnoreCase(HW_VERSION_6_0)) {

                                    Loader.getInstance().hideLoader();
                                    AppDialog.showAlertDialog(getActivity(), "Lock connection timeout.");

                                }
                            }
                        });

                        wifiUtilManager.startScanning(lockVersion, SSID, password);

                    }
                }, "CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
    }

    private void doMqttWifiConfiguration(Context context, Lock lock) {

        if (!WifiLockManager.getInstance().isWifiEnabled(context)) {
            AppDialog.showAlertDialog(context, context.getString(R.string.turn_on_wifi_connect_lock));
        } else if (!WifiLockManager.getInstance().isWifiLockConnected(context)) {
            AppDialog.showAlertDialog(context, context.getString(R.string.no_wifi_lock), context.getString(R.string.connect_wifi_lock));
        } else if (!WifiLockManager.getInstance().isSameWifiLockConnected(context, mLock.getSsid())) {
            AppDialog.showAlertDialog(context, context.getString(R.string.no_wifi_lock), context.getString(R.string.connect_same_wifi_lock));
        } else {

            String id = null;
            String key = null;

            if (lock.isOffline()) {
                id = lock.getLockIds().get(0).getKey();
                key = lock.getLockKeys().get(0).getKey();
            } else {
                for (LockKeys lockKeys : lock.getLockKeys()) {
                    if (lockKeys.getUserType().equalsIgnoreCase(Constant.OWNER_ID)) {
                        id = lockKeys.getKey();
                    } else {
                        key = lockKeys.getKey();
                    }
                }
            }

            WifiMqttConfigV6 wifiMqttConfig = new WifiMqttConfigV6();
            wifiMqttConfig.setOwnerId(AESEncryption.getInstance().decrypt(id, mLock.isEncrypted()));
            wifiMqttConfig.setSlotKey(AESEncryption.getInstance().decrypt(key, mLock.isEncrypted()));
            wifiMqttConfig.setWifiSsid(et_wifiSSID.getText().toString());
            wifiMqttConfig.setWifiPass(et_wifiPassword.getText().toString());
            wifiMqttConfig.setWifiSec(wifiSec);

            BrandInfoResponse.BrandInfo brandInfo = SecuredStorageManager.getInstance().getBrandInfo();

            // Extract port from brandInfo if present, otherwise default to "443" (or C-lib port)
            String mqttPort = getMqttPort(BuildConfig.BUILD_VARIANT);
            if (mqttPort == null || mqttPort.isEmpty()) {
                mqttPort = "443";
            }
            if (brandInfo != null && brandInfo.getMqttServiceUrl() != null && !brandInfo.getMqttServiceUrl().isEmpty()) {
                int colonIndex = brandInfo.getMqttServiceUrl().lastIndexOf(":");
                if (colonIndex != -1) {
                    mqttPort = brandInfo.getMqttServiceUrl().substring(colonIndex + 1);
                }
            }

            // Enforce build flavor condition for target server URL (http-ip)
            String mqttIp;
            boolean isStaging = "S".equalsIgnoreCase(BuildConfig.BUILD_VARIANT) || BuildConfig.BASE_URL.contains("staging");
            if (mLock != null && mLock.isSensor()) {
                // Door Sensor target URL
                if (isStaging) {
                    mqttIp = "https://techlocstaging.in/door_sensor/api/web/v1/activities";
                } else {
                    mqttIp = "https://techloc.in/door_sensor/api/web/v1/activities";
                }
            } else if (isStaging) {
                // Lock Staging target URL
                mqttIp = "https://techlocstaging.in/api/web/v1/activities";
            } else {
                // Lock Production: Use brandInfo URL if provided, otherwise default to https://techloc.in
                if (brandInfo != null && brandInfo.getMqttServiceUrl() != null && !brandInfo.getMqttServiceUrl().isEmpty()) {
                    int colonIndex = brandInfo.getMqttServiceUrl().lastIndexOf(":");
                    if (colonIndex != -1) {
                        mqttIp = brandInfo.getMqttServiceUrl().substring(0, colonIndex);
                    } else {
                        mqttIp = brandInfo.getMqttServiceUrl();
                    }
                } else {
                    mqttIp = "https://techloc.in/api/web/v1/activities";
                }
            }

            System.out.println("==================================================");
            System.out.println("####### [WIFI_CONFIG] Final mqttIp (http-ip): " + mqttIp);
            System.out.println("####### [WIFI_CONFIG] Final mqttPort (http-port): " + mqttPort);
            System.out.println("####### [WIFI_CONFIG] isStaging: " + isStaging + ", BUILD_VARIANT: " + BuildConfig.BUILD_VARIANT);

            wifiMqttConfig.setMqttIp(mqttIp);
            wifiMqttConfig.setMqttPort(Integer.parseInt(mqttPort));

            sendWifiConfigToLock(context, lock, wifiMqttConfig);
        }
    }

    private void sendWifiConfigToLock(final Context context, final Lock lock, final WifiMqttConfigV6 wifiMqttConfig) {
        BrandInfoResponse.BrandInfo brandInfo = SecuredStorageManager.getInstance().getBrandInfo();
        String brandInfoUrl = (brandInfo != null && brandInfo.getMqttServiceUrl() != null) ? brandInfo.getMqttServiceUrl() : "None/Null";
        String clibFallback = getMqttIp(BuildConfig.BUILD_VARIANT) + ":" + getMqttPort(BuildConfig.BUILD_VARIANT);

        String alertMessage = "TARGET URL:\n" + ServiceUrl.WIFI_LOCK_CONFIG_HTTP + "\n\n"
                + "DATA BEING SENT TO LOCK:\n"
                + "• Server IP (http-ip): " + wifiMqttConfig.getMqttIp() + "\n"
                + "• Server Port (http-port): " + wifiMqttConfig.getMqttPort() + "\n"
                + "• Wi-Fi SSID: " + wifiMqttConfig.getWifiSsid() + "\n"
                + "• Wi-Fi Pass: " + wifiMqttConfig.getWifiPass() + "\n"
                + "• Wi-Fi Sec: " + wifiMqttConfig.getWifiSec() + "\n"
                + "• Owner ID: " + wifiMqttConfig.getOwnerId() + "\n\n"
                + "CONFIG SOURCES:\n"
                + "• BrandInfo mqtt_url: " + brandInfoUrl + "\n"
                + "• C-lib Fallback: " + clibFallback;

        AppDialog.showAlertDialog(getContext(), "WIFI_CONFIG Sending Data", alertMessage, "Send to Lock", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doActualSendWifiConfigToLock(context, lock, wifiMqttConfig);
            }
        });
    }

    private void doActualSendWifiConfigToLock(final Context context, final Lock lock, final WifiMqttConfigV6 wifiMqttConfig) {
        if (context != null) {
            Toast.makeText(context, "[TEST ALERT] Sending to Lock:\nSSID: " + wifiMqttConfig.getWifiSsid() + "\nMQTT: " + wifiMqttConfig.getMqttIp() + ":" + wifiMqttConfig.getMqttPort(), Toast.LENGTH_LONG).show();
        }
        Loader.getInstance().showLoader(context);
        ServiceManager.getInstance().post(ServiceUrl.WIFI_LOCK_CONFIG_HTTP, wifiMqttConfig, new ResponseHandler() {

            @Override
            public void onSuccess(Object data) {

                Loader.getInstance().hideLoader();

                if (data != null) {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "[TEST ALERT] Lock Wi-Fi Response:\n" + data.toString(), Toast.LENGTH_LONG).show();
                    }

                    AppDialog.showAlertDialog(getContext(), "Lock Wi-Fi Response", "Sent Data:\nServer: " + wifiMqttConfig.getMqttIp() + ":" + wifiMqttConfig.getMqttPort() + "\nSSID: " + wifiMqttConfig.getWifiSsid() + "\n\nLock Response:\n" + data.toString(), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            WifiLockResponse lockResponse = new Gson().fromJson(data.toString(), WifiLockResponse.class);

                            if (lockResponse != null && lockResponse.getStatus() != null
                                    && lockResponse.getStatus().equalsIgnoreCase("success")) {

                                forgetWIFINetwork(context, mLock);

                                AppDialog.showAlertDialog(getContext(), "Wifi configuration updated Successfully.",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                doPushWifiAddedServer(lock);

                                                if (getActivity() != null) {
                                                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                                                }
                                            }
                                        });
                            } else {
                                onError(data.toString());
                            }
                        }
                    });
                } else {
                    AppDialog.showAlertDialog(getContext(), "Invalid response from Lock. Please try again later.");
                }
            }

            @Override
            public void onAuthError(String message) {
                Loader.getInstance().hideLoader();
            }

            @Override
            public void onError(String message) {

                Loader.getInstance().hideLoader();
                if (getContext() != null) {
                    Toast.makeText(getContext(), "[TEST ALERT] Lock Wi-Fi Error:\n" + message, Toast.LENGTH_LONG).show();
                }

                AppDialog.showAlertDialog(getContext(), "Lock Wi-Fi Error", "Sent Data:\nServer: " + wifiMqttConfig.getMqttIp() + ":" + wifiMqttConfig.getMqttPort() + "\nSSID: " + wifiMqttConfig.getWifiSsid() + "\n\nError Message:\n" + message, null);

                if (message != null && message.equals("No Internet. Please Check your network connection.")) {
                    AppDialog.showAlertDialog(context, message);
                } else {
                    WifiLockResponse response = new Gson().fromJson(message, WifiLockResponse.class);
                    if (response != null) {
                        String errorMessage = "";
                        if (response.getErrorMessage().equalsIgnoreCase(Constant.WIFI_NOT_CONNECTED)) {
                            errorMessage = context.getString(R.string.home_wifi_not_connected);
                        } else if (response.getErrorMessage().equalsIgnoreCase(Constant.MQTT_NOT_CONNECTED)) {
                            errorMessage = context.getString(R.string.mqtt_not_connected);
                        } else {
                            errorMessage = message;
                        }

                        AppDialog.showAlertDialog(getContext(), errorMessage);

                    } else {
                        AppDialog.showAlertDialog(getContext(), message);
                    }
                }
            }
        });
    }

    private boolean validateWifiSsid() {
        String wifiSsid = et_wifiSSID.getText().toString();
        if (wifiSsid.isEmpty()) {
            tl_wifiSSID.setError(getString(R.string.enter_wifi_ssid));
            return false;
        }
        tl_wifiSSID.setErrorEnabled(false);
        return true;
    }

    private boolean validateWifiPassword() {

        if (wifiSec == 0) {
            tl_wifiPassword.setErrorEnabled(false);
            return true;
        } else {

            String wifiPassword = et_wifiPassword.getText().toString();

            if (wifiPassword.isEmpty()) {
                tl_wifiPassword.setError(getString(R.string.enter_wifi_password));
                return false;
            } else if (wifiPassword.length() < 8) {
                tl_wifiPassword.setError(getString(R.string.wifi_password_length));
                return false;
            }

            tl_wifiPassword.setErrorEnabled(false);
            return true;

        }
    }

    private class MyTextWatcher implements TextWatcher {
        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        public void afterTextChanged(Editable editable) {

            switch (view.getId()) {

                case R.id.et_editHomeWifiSSID:
                    validateWifiSsid();
                    break;
                case R.id.et_editHomeWifiPassword:
                    validateWifiPassword();
                    break;
            }
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

    // Whether a provided Wi-Fi network configuration is associated with a specific smart lock
    // can be determined remotely through server access.
    // Is this functionality available?
    private void doPushWifiAddedServer(Lock lock) {

        String id = null;
        String key = null;

        if (lock.isOffline()) {
            id = lock.getLockIds().get(0).getKey();
            key = lock.getLockKeys().get(0).getKey();
        } else {
            for (LockKeys lockKeys : lock.getLockKeys()) {
                if (lockKeys.getUserType().equalsIgnoreCase(Constant.OWNER_ID)) {
                    id = lockKeys.getKey();
                } else {
                    key = lockKeys.getKey();
                }
            }
        }

        WifiMqttConnection wifiMqttConnection = new WifiMqttConnection();
        // TODO Confirm below data with backend
        // wifiMqttConnection.setOwnerId(AESEncryption.getInstance().enrypt(id, lock.isEncrypted()));
        wifiMqttConnection.setOwnerId(id); // check id is encrypted or not, only encrypted id
        wifiMqttConnection.setSerialNumber(lock.getSerialNumber());

        doOfflineWifiConfig(wifiMqttConnection);

        // TODO Confirm With Backend data and not to be executed for FORT

       /* if (ServiceManager.getInstance().isNetworkAvailable(getContext())) {

            WifiMqttConfigurationService.getInstance().pushWifiConfig( wifiMqttConnection,new ResponseHandler() {
                @Override
                public void onSuccess(Object data) {
                    Loader.getInstance().hideLoader();
                    if (data != null) {
                        try {
                            BaseResponse response = new Gson().fromJson(data.toString(), BaseResponse.class);
                            doOfflineWifiConfig(null);
                        } catch (Exception e) {
                            onError(getContext().getString(R.string.error_parser));
                        }
                    } else {
                        onError(getContext().getString(R.string.error_parser));
                    }
                }

                @Override
                public void onAuthError(String message) {
                    Loader.getInstance().hideLoader();
                    doOfflineWifiConfig(wifiMqttConnection);
                }

                @Override
                public void onError(String message) {
                    Loader.getInstance().hideLoader();
                    doOfflineWifiConfig(wifiMqttConnection);
                }
            });
        } else {

        }*/
    }

    private void doOfflineWifiConfig(WifiMqttConnection data) {

        Logger.d("#### doOfflineWifiConfig");

        String dataStr = new Gson().toJson(data);

        Logger.d("#### doOfflineWifiConfig dataStr : " + dataStr);

        SecuredStorageManager.getInstance().setWifiConfigState(dataStr);

        SyncScheduler.getInstance().schedule(getActivity(), Constant.JOBS.ALL);

    }


}

