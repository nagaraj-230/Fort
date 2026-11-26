package com.payoda.smartlock.sensors;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.payoda.smartlock.App;
import com.payoda.smartlock.R;
import com.payoda.smartlock.authentication.BaseFragment;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.constants.DoorSensorServiceUrl;
import com.payoda.smartlock.locks.LockManager;
import com.payoda.smartlock.locks.model.Lock;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.plugins.storage.SecuredStorageManager;
import com.payoda.smartlock.plugins.wifi.WifiUtilManager;
import com.payoda.smartlock.sensors.model.Sensor;
import com.payoda.smartlock.sensors.service.DoorSensorService;
import com.payoda.smartlock.utils.AppDialog;
import com.payoda.smartlock.utils.Loader;
import com.payoda.smartlock.utils.Logger;

import org.json.JSONObject;

import java.util.ArrayList;

public class SensorDetailFragment extends BaseFragment {

    public static final String TAG = "### SensorDetailFragment";

    private Sensor mSensor;
    private TextView tvSensorTitle, tvSensorCodeDisplay, tvBattery, lblBatteryLabel, tvFactoryReset;
    private EditText etSensorTitle;
    private ImageView ivEdit, ivSave, ivSettings;
    private Button btnSensorHistory;

    // Pre-Activation & Lifecycle Views
    private LinearLayout llSensorEngageContainer;
    private TextView tvEngageSensor;
    private ImageView ivEngage;
    private TextView tvEngageToProceed;
    private View dividerEngage;
    private LinearLayout llSensorContainer;
    private LinearLayout llSensorActionsContainer;

    public SensorDetailFragment() {
        // Required empty public constructor
    }

    public static SensorDetailFragment getInstance() {
        return new SensorDetailFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey(Constant.SCREEN_DATA)) {
            String data = bundle.getString(Constant.SCREEN_DATA);
            if (!TextUtils.isEmpty(data)) {
                mSensor = new Gson().fromJson(data, Sensor.class);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sensor_detail, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshSensorFromStorage();
        updateUI();
    }

    private void refreshSensorFromStorage() {
        if (mSensor == null) return;
        ArrayList<Sensor> list = SecuredStorageManager.getInstance().getSensorList();
        for (Sensor s : list) {
            if ((s.getId() != null && s.getId().equalsIgnoreCase(mSensor.getId())) ||
                    (s.getSerialNumber() != null && s.getSerialNumber().equalsIgnoreCase(mSensor.getSerialNumber())) ||
                    (s.getCode() != null && s.getCode().equalsIgnoreCase(mSensor.getCode()))) {
                mSensor = s;
                break;
            }
        }
        if (mSensor != null && TextUtils.isEmpty(mSensor.getOwnerId())) {
            String ownerId = SecuredStorageManager.getInstance().getSensorOwnerId(mSensor.getId());
            if (TextUtils.isEmpty(ownerId)) {
                ownerId = SecuredStorageManager.getInstance().getSensorOwnerId(mSensor.getSerialNumber());
            }
            if (TextUtils.isEmpty(ownerId)) {
                ownerId = SecuredStorageManager.getInstance().getSensorOwnerId(mSensor.getCode());
            }
            if (!TextUtils.isEmpty(ownerId)) {
                mSensor.setOwnerId(ownerId);
            }
        }
    }

    private void initView(View view) {
        ((TextView) view.findViewById(R.id.tv_title)).setText(getString(R.string.sensor_details));

        view.findViewById(R.id.iv_back).setOnClickListener(v -> {
            if (getActivity() != null) {
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });

        tvSensorTitle = view.findViewById(R.id.tv_sensor_title);
        etSensorTitle = view.findViewById(R.id.et_sensor_title);
        tvSensorCodeDisplay = view.findViewById(R.id.tv_sensor_code_display);
        tvBattery = view.findViewById(R.id.tv_sensor_battery);
        lblBatteryLabel = view.findViewById(R.id.lblBatteryLabel);
        tvFactoryReset = view.findViewById(R.id.tv_factory_reset);
        ivEdit = view.findViewById(R.id.iv_edit);
        ivSave = view.findViewById(R.id.iv_save);
        ivSettings = view.findViewById(R.id.iv_settings);
        btnSensorHistory = view.findViewById(R.id.btn_sensor_history);

        // Pre-Activation Views
        llSensorEngageContainer = view.findViewById(R.id.ll_sensor_engage_container);
        tvEngageSensor = view.findViewById(R.id.tv_engage_sensor);
        ivEngage = view.findViewById(R.id.iv_engage);
        tvEngageToProceed = view.findViewById(R.id.tv_engage_to_proceed);
        dividerEngage = view.findViewById(R.id.divider_engage);
        llSensorContainer = view.findViewById(R.id.ll_sensor_container);
        llSensorActionsContainer = view.findViewById(R.id.ll_sensor_actions_container);

        if (ivEngage != null) {
            ivEngage.setOnClickListener(v -> handleEngageSensorClick());
        }

        if (ivEdit != null) {
            ivEdit.setOnClickListener(v -> {
                ivEdit.setVisibility(View.GONE);
                if (ivSave != null) ivSave.setVisibility(View.VISIBLE);
                etSensorTitle.setVisibility(View.VISIBLE);
                tvSensorTitle.setVisibility(View.INVISIBLE);
            });
        }

        if (ivSave != null) {
            ivSave.setOnClickListener(v -> {
                String newName = etSensorTitle.getText() != null ? etSensorTitle.getText().toString().trim() : "";
                if (!TextUtils.isEmpty(newName) && mSensor != null) {
                    mSensor.setName(newName);
                    tvSensorTitle.setText(newName);
                    SecuredStorageManager.getInstance().addSensor(mSensor);

                    if (mSensor.getId() != null) {
                        try {
                            JSONObject payload = new JSONObject();
                            payload.put("name", newName);
                            DoorSensorService.getInstance().updateSensorName(mSensor.getId(), payload, new ResponseHandler() {
                                @Override
                                public void onSuccess(Object data) {
                                    Logger.d(TAG, "Sensor name updated on server: " + data);
                                    if (getActivity() != null) {
                                        String alertMsg = "TARGET URL:\n" + DoorSensorServiceUrl.UPDATE_DOOR_SENSOR + mSensor.getId() + "\n\n"
                                                + "PAYLOAD SENT:\n" + payload.toString() + "\n\n"
                                                + "SERVER RESPONSE:\n" + (data != null ? data.toString() : "empty");
                                        AppDialog.showAlertDialog(getActivity(), "[TEST ALERT] Rename Sensor API Response", alertMsg);
                                    }
                                }

                                @Override
                                public void onAuthError(String message) {
                                    if (getActivity() != null) {
                                        AppDialog.showAlertDialog(getActivity(), "[TEST ALERT] Rename Sensor Auth Error", message);
                                    }
                                }

                                @Override
                                public void onError(String message) {
                                    if (getActivity() != null) {
                                        String alertMsg = "TARGET URL:\n" + DoorSensorServiceUrl.UPDATE_DOOR_SENSOR + mSensor.getId() + "\n\n"
                                                + "PAYLOAD SENT:\n" + payload.toString() + "\n\n"
                                                + "ERROR:\n" + message;
                                        AppDialog.showAlertDialog(getActivity(), "[TEST ALERT] Rename Sensor Error", alertMsg);
                                    }
                                }
                            });
                        } catch (Exception e) {
                            Logger.e(e);
                        }
                    }

                    Toast.makeText(getContext(), "Sensor name updated", Toast.LENGTH_SHORT).show();
                }
                ivSave.setVisibility(View.GONE);
                if (ivEdit != null) ivEdit.setVisibility(View.VISIBLE);
                etSensorTitle.setVisibility(View.INVISIBLE);
                tvSensorTitle.setVisibility(View.VISIBLE);
            });
        }

        if (ivSettings != null) {
            ivSettings.setOnClickListener(v -> {
                if (mSensor != null && getActivity() != null) {
                    Lock sensorAsLock = new Lock();
                    sensorAsLock.setId(mSensor.getId());
                    sensorAsLock.setName(mSensor.getName());
                    sensorAsLock.setSerialNumber(mSensor.getSerialNumber() != null ? mSensor.getSerialNumber() : mSensor.getCode());
                    sensorAsLock.setScratchCode(mSensor.getScratchCode());
                    sensorAsLock.setSensor(true);
                    App.getInstance().showFullScreen(getActivity(), Constant.SCREEN.WIFI_MQTT_CONFIGURATION, new Gson().toJson(sensorAsLock));
                }
            });
        }

        if (btnSensorHistory != null) {
            btnSensorHistory.setOnClickListener(v -> {
                if (mSensor != null && getActivity() != null) {
                    Lock sensorAsLock = new Lock();
                    sensorAsLock.setId(mSensor.getId());
                    sensorAsLock.setName(mSensor.getName());
                    sensorAsLock.setSerialNumber(mSensor.getSerialNumber() != null ? mSensor.getSerialNumber() : mSensor.getCode());
                    sensorAsLock.setSensor(true);
                    App.getInstance().showFullScreen(getActivity(), Constant.SCREEN.HISTORY, new Gson().toJson(sensorAsLock));
                }
            });
        }

        if (tvFactoryReset != null) {
            tvFactoryReset.setOnClickListener(v -> performFactoryReset());
        }

        updateUI();
    }

    private void updateUI() {
        if (mSensor == null) return;

        if (tvSensorTitle != null) tvSensorTitle.setText(mSensor.getName());
        if (etSensorTitle != null) etSensorTitle.setText(mSensor.getName());
        if (tvSensorCodeDisplay != null) {
            String code = mSensor.getSerialNumber() != null ? mSensor.getSerialNumber() : mSensor.getCode();
            tvSensorCodeDisplay.setText("Sensor Code: " + (code != null ? code : ""));
        }

        updateBattery();

        String ownerId = mSensor.getOwnerId();
        if (TextUtils.isEmpty(ownerId)) {
            ownerId = SecuredStorageManager.getInstance().getSensorOwnerId(mSensor.getId());
        }
        if (TextUtils.isEmpty(ownerId)) {
            ownerId = SecuredStorageManager.getInstance().getSensorOwnerId(mSensor.getSerialNumber());
        }
        if (TextUtils.isEmpty(ownerId)) {
            ownerId = SecuredStorageManager.getInstance().getSensorOwnerId(mSensor.getCode());
        }

        boolean isActivated = !TextUtils.isEmpty(ownerId) && mSensor.getStatus() != null && mSensor.getStatus().equalsIgnoreCase(Constant.ACTIVE);

        if (!isActivated) {
            // Pre-Activation State (Screenshot 1)
            if (llSensorEngageContainer != null) llSensorEngageContainer.setVisibility(View.VISIBLE);
            if (tvEngageToProceed != null) tvEngageToProceed.setVisibility(View.VISIBLE);
            if (dividerEngage != null) dividerEngage.setVisibility(View.VISIBLE);
            if (llSensorContainer != null) llSensorContainer.setVisibility(View.GONE);
            if (llSensorActionsContainer != null) llSensorActionsContainer.setVisibility(View.GONE);

            // Hide header controls until activated
            if (ivEdit != null) ivEdit.setVisibility(View.GONE);
            if (ivSettings != null) ivSettings.setVisibility(View.GONE);
            if (ivSave != null) ivSave.setVisibility(View.GONE);

        } else {
            // Activated State (Screenshot 3)
            if (llSensorEngageContainer != null) llSensorEngageContainer.setVisibility(View.GONE);
            if (tvEngageToProceed != null) tvEngageToProceed.setVisibility(View.GONE);
            if (dividerEngage != null) dividerEngage.setVisibility(View.GONE);
            if (llSensorContainer != null) llSensorContainer.setVisibility(View.VISIBLE);
            if (llSensorActionsContainer != null) llSensorActionsContainer.setVisibility(View.VISIBLE);

            // Show header controls
            if (ivEdit != null) ivEdit.setVisibility(View.VISIBLE);
            if (ivSettings != null) ivSettings.setVisibility(View.VISIBLE);
            if (ivSave != null) ivSave.setVisibility(View.GONE);
        }
    }

    private void updateBattery() {
        if (mSensor == null || tvBattery == null) return;
        int batteryPercent = 100;
        try {
            if (!TextUtils.isEmpty(mSensor.getBattery())) {
                batteryPercent = Integer.parseInt(mSensor.getBattery().trim());
            }
        } catch (Exception e) {
            batteryPercent = 100;
        }

        tvBattery.setText(batteryPercent + "%");

        if (batteryPercent <= 20) {
            tvBattery.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_battery_empty, 0, 0, 0);
        } else if (batteryPercent <= 50) {
            tvBattery.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_battery_half, 0, 0, 0);
        } else {
            tvBattery.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_battery_full, 0, 0, 0);
        }
    }

    private void handleEngageSensorClick() {
        if (getActivity() == null || mSensor == null) return;

        // Step 1: Pre-flight check for Location & Wi-Fi (Triggers Screenshot 2 dialog if Location is disabled)
        String status = LockManager.getInstance().preCheckBLEVersion6(getActivity());
        if (!status.equalsIgnoreCase("success")) {
            AppDialog.showAlertDialog(getActivity(), status);
            return;
        }

        // Step 2: Show Activate Dialog matching Lock UX
        AppDialog.showAlertDialog(getContext(), getString(R.string.activate_sensor), getString(R.string.engage_sensor_prompt),
                "OK", (dialog, which) -> {
                    getLocationPermission(new RequestPermissionAction() {
                        @Override
                        public void permissionGranted() {
                            doActivateSensor();
                        }

                        @Override
                        public void permissionDenied() {
                        }
                    });
                },
                "CANCEL", (dialog, which) -> {});
    }

    private void doActivateSensor() {
        if (mSensor == null || getContext() == null) return;
        Loader.getInstance().showLoader(getContext());

        String SSID = mSensor.getSerialNumber() != null ? mSensor.getSerialNumber() : mSensor.getCode();
        String password = mSensor.getScratchCode();
        if (password != null && password.length() > 8) {
            password = password.substring(0, 8);
        }

        WifiUtilManager wifiUtilManager = new WifiUtilManager(getContext(), new WifiUtilManager.WifiListener() {
            @Override
            public void connectionSuccess() {
                Logger.d(TAG, "Sensor connected for Activation");
                new Handler(Looper.getMainLooper()).postDelayed(() -> doSensorAddRequest(), Constant.WIFI_DELAY_TIME);
            }

            @Override
            public void connectionTimeOut() {
                Loader.getInstance().hideLoader();
                AppDialog.showAlertDialog(getActivity(), "Sensor connection timeout. Please ensure the sensor button was held for 3 seconds.");
            }
        });
        wifiUtilManager.startScanning("", SSID, password);
    }

    private void doSensorAddRequest() {
        JSONObject payload = new JSONObject();
        final String deviceCode = mSensor.getSerialNumber() != null ? mSensor.getSerialNumber() : mSensor.getCode();
        try {
            payload.put("device-code", deviceCode);
        } catch (Exception e) {
            Logger.e(e);
        }

        Logger.d(TAG, "Posting to /add for activation with device-code: " + deviceCode);
        ServiceManager.getInstance().post(DoorSensorServiceUrl.WIFI_DOOR_SENSOR_ADD, payload, new ResponseHandler() {
            @Override
            public void onSuccess(Object data) {
                Logger.d(TAG, "Sensor /add success: " + data);
                String ownerId = "";
                int errorCode = 0;
                String errorMsg = "";
                try {
                    if (data != null) {
                        JSONObject res = new JSONObject(data.toString());
                        errorCode = res.has("error-code") ? res.optInt("error-code", 0) : res.optInt("error", 0);
                        errorMsg = res.has("error-message") ? res.optString("error-message", "") : res.optString("message", "");
                        JSONObject respObj = res.optJSONObject("response");
                        if (respObj != null) {
                            ownerId = respObj.optString("owner-id", "");
                        }
                    }
                } catch (Exception e) {
                    Logger.e(e);
                }

                if (TextUtils.isEmpty(ownerId)) {
                    if (errorCode == 1007 || (errorMsg != null && errorMsg.toLowerCase().contains("already added"))) {
                        String savedOwnerId = SecuredStorageManager.getInstance().getSensorOwnerId(mSensor.getId());
                        if (TextUtils.isEmpty(savedOwnerId)) {
                            savedOwnerId = SecuredStorageManager.getInstance().getSensorOwnerId(deviceCode);
                        }
                        if (!TextUtils.isEmpty(savedOwnerId)) {
                            ownerId = savedOwnerId;
                        }
                    }
                }

                if (TextUtils.isEmpty(ownerId)) {
                    Loader.getInstance().hideLoader();
                    forgetSensorHotspot();
                    AppDialog.showAlertDialog(getActivity(), "Activation Failed", "No valid owner-id received from sensor.");
                    return;
                }

                // 1. Save owner-id
                if (mSensor.getId() != null) {
                    SecuredStorageManager.getInstance().updateSensorOwnerId(mSensor.getId(), ownerId);
                }
                if (!TextUtils.isEmpty(deviceCode)) {
                    SecuredStorageManager.getInstance().updateSensorOwnerId(deviceCode, ownerId);
                }
                mSensor.setOwnerId(ownerId);

                // 2. Set Activated status
                mSensor.setStatus(Constant.ACTIVE);
                SecuredStorageManager.getInstance().addSensor(mSensor);

                // 3. Disconnect from Sensor AP
                forgetSensorHotspot();

                // 4. Best-effort server sync
                if (mSensor.getId() != null && getContext() != null && ServiceManager.getInstance().isNetworkAvailable(getContext())) {
                    try {
                        JSONObject updatePayload = new JSONObject();
                        updatePayload.put("status", Constant.ACTIVE);
                        DoorSensorService.getInstance().updateSensorName(mSensor.getId(), updatePayload, null);
                    } catch (Exception ignored) {}
                }

                Loader.getInstance().hideLoader();
                if (getActivity() != null) {
                    String alertMsg = "TARGET URL:\n" + DoorSensorServiceUrl.WIFI_DOOR_SENSOR_ADD + "\n\n"
                            + "RESPONSE:\n" + (data != null ? data.toString() : "empty") + "\n\n"
                            + "OWNER-ID: " + ownerId + "\n\n"
                            + "Sensor Activated Successfully!";
                    AppDialog.showAlertDialog(getActivity(), "[TEST ALERT] Sensor Activation Success", alertMsg, (dialog, which) -> {
                        updateUI();
                    });
                } else {
                    updateUI();
                }
            }

            @Override
            public void onAuthError(String message) {
                Logger.d(TAG, "Sensor /add onAuthError: " + message);
                handleActivationError(deviceCode, message);
            }

            @Override
            public void onError(String message) {
                Logger.d(TAG, "Sensor /add onError: " + message);
                handleActivationError(deviceCode, message);
            }
        });
    }

    private void handleActivationError(String deviceCode, String message) {
        boolean isAlreadyAdded = message != null && (message.contains("1007") || message.toLowerCase().contains("already added"));
        if (isAlreadyAdded) {
            String savedOwnerId = SecuredStorageManager.getInstance().getSensorOwnerId(mSensor.getId());
            if (TextUtils.isEmpty(savedOwnerId)) {
                savedOwnerId = SecuredStorageManager.getInstance().getSensorOwnerId(deviceCode);
            }
            if (!TextUtils.isEmpty(savedOwnerId)) {
                mSensor.setOwnerId(savedOwnerId);
                mSensor.setStatus(Constant.ACTIVE);
                SecuredStorageManager.getInstance().addSensor(mSensor);
                forgetSensorHotspot();
                Loader.getInstance().hideLoader();
                if (getActivity() != null) {
                    AppDialog.showAlertDialog(getActivity(), "[TEST ALERT] Sensor Already Registered",
                            "Sensor was already paired on hardware (Error 1007). Activated using saved owner-id: " + savedOwnerId,
                            (dialog, which) -> updateUI());
                } else {
                    updateUI();
                }
                return;
            }
        }
        Loader.getInstance().hideLoader();
        forgetSensorHotspot();
        AppDialog.showAlertDialog(getActivity(), "Activation Failed", "Error: " + message);
    }

    private void forgetSensorHotspot() {
        if (mSensor == null || getContext() == null) return;
        String SSID = mSensor.getSerialNumber() != null ? mSensor.getSerialNumber() : mSensor.getCode();
        WifiUtilManager.forgetMyNetwork(getContext(), SSID);
    }

    private void performFactoryReset() {
        if (mSensor == null || getContext() == null) return;

        AppDialog.showAlertDialog(getActivity(), "Factory Reset Sensor",
                "Please hold the sensor button for 3 seconds until it enters configuration mode, then click \"Reset\".",
                "Reset", (dialog, which) -> {
                    Loader.getInstance().showLoader(getContext());

                    String SSID = mSensor.getSerialNumber() != null ? mSensor.getSerialNumber() : mSensor.getCode();
                    String password = mSensor.getScratchCode();
                    if (password != null && password.length() > 8) {
                        password = password.substring(0, 8);
                    }

                    WifiUtilManager wifiUtilManager = new WifiUtilManager(getContext(), new WifiUtilManager.WifiListener() {
                        @Override
                        public void connectionSuccess() {
                            Logger.d(TAG, "Sensor Wi-Fi connected for Reset");
                            new Handler(Looper.getMainLooper()).postDelayed(() -> doSensorResetRequest(), Constant.WIFI_DELAY_TIME);
                        }

                        @Override
                        public void connectionTimeOut() {
                            Loader.getInstance().hideLoader();
                            AppDialog.showAlertDialog(getActivity(), "Sensor Connection Timeout",
                                    "Could not connect to sensor Wi-Fi.\n\nDo you want to reset and remove this sensor from your server account anyway?",
                                    "Reset on Server", (d, w) -> {
                                        doServerReset(null);
                                    },
                                    "Cancel", (d, w) -> {});
                        }
                    });
                    wifiUtilManager.startScanning("", SSID, password);
                },
                "Cancel", (dialog, which) -> {});
    }

    private void doSensorResetRequest() {
        JSONObject payload = new JSONObject();
        try {
            String deviceCode = mSensor.getSerialNumber() != null ? mSensor.getSerialNumber() : mSensor.getCode();
            payload.put("device-code", deviceCode);
        } catch (Exception e) {
            Logger.e(e);
        }

        Logger.d(TAG, "Posting to /reset-device with payload: " + payload.toString());
        ServiceManager.getInstance().post(DoorSensorServiceUrl.WIFI_DOOR_SENSOR_RESET, payload, new ResponseHandler() {
            @Override
            public void onSuccess(Object data) {
                Logger.d(TAG, "Sensor reset success: " + data);
                forgetSensorHotspot();

                // Wait 1.5s for Android to drop sensor SoftAP and restore Internet connection (Mobile Data / Home Wi-Fi)
                Loader.getInstance().showLoader(getContext());
                new Handler(Looper.getMainLooper()).postDelayed(() -> doServerReset(data), 1500);
            }

            @Override
            public void onAuthError(String message) {
                Loader.getInstance().hideLoader();
                forgetSensorHotspot();
                String alertMsg = "TARGET URL:\n" + DoorSensorServiceUrl.WIFI_DOOR_SENSOR_RESET + "\n\n"
                        + "PAYLOAD SENT:\n" + payload.toString() + "\n\n"
                        + "AUTH ERROR:\n" + message;
                AppDialog.showAlertDialog(getActivity(), "[TEST ALERT] Sensor Reset Auth Error", alertMsg);
            }

            @Override
            public void onError(String message) {
                Loader.getInstance().hideLoader();
                forgetSensorHotspot();
                String alertMsg = "TARGET URL:\n" + DoorSensorServiceUrl.WIFI_DOOR_SENSOR_RESET + "\n\n"
                        + "PAYLOAD SENT:\n" + payload.toString() + "\n\n"
                        + "ERROR:\n" + message;
                AppDialog.showAlertDialog(getActivity(), "[TEST ALERT] Sensor Reset Error", alertMsg);
            }
        });
    }

    private void doServerReset(Object hardwareData) {
        if (getContext() == null || mSensor == null) return;
        Loader.getInstance().showLoader(getContext());

        // Ensure all available details are populated on mSensor before calling reset
        if (TextUtils.isEmpty(mSensor.getScratchCode())) {
            ArrayList<Sensor> list = SecuredStorageManager.getInstance().getSensorList();
            if (list != null) {
                for (Sensor s : list) {
                    if ((s.getId() != null && s.getId().equalsIgnoreCase(mSensor.getId())) ||
                            (s.getSerialNumber() != null && s.getSerialNumber().equalsIgnoreCase(mSensor.getSerialNumber()))) {
                        if (!TextUtils.isEmpty(s.getScratchCode())) {
                            mSensor.setScratchCode(s.getScratchCode());
                        }
                        break;
                    }
                }
            }
        }
        if (TextUtils.isEmpty(mSensor.getSerialNumber()) && !TextUtils.isEmpty(mSensor.getCode())) {
            mSensor.setSerialNumber(mSensor.getCode());
        }

        final Sensor sensorToReset = mSensor;
        final String sensorTargetUrl = DoorSensorServiceUrl.UPDATE_DOOR_SENSOR + (sensorToReset.getId() != null ? sensorToReset.getId() : "");
        final JSONObject sensorPayload = new JSONObject();
        try {
            sensorPayload.put("id", sensorToReset.getId());
            sensorPayload.put("name", sensorToReset.getName() != null ? sensorToReset.getName() : "");
            String serial = sensorToReset.getSerialNumber() != null ? sensorToReset.getSerialNumber() : sensorToReset.getCode();
            if (serial != null) {
                sensorPayload.put("serial_number", serial);
                sensorPayload.put("serialNumber", serial);
            }
            if (!TextUtils.isEmpty(sensorToReset.getScratchCode())) {
                sensorPayload.put("scratch_code", sensorToReset.getScratchCode());
                sensorPayload.put("scratchCode", sensorToReset.getScratchCode());
            }
            if (!TextUtils.isEmpty(sensorToReset.getOwnerId())) {
                sensorPayload.put("owner_id", sensorToReset.getOwnerId());
            }
            if (!TextUtils.isEmpty(sensorToReset.getBattery())) {
                sensorPayload.put("battery", sensorToReset.getBattery());
            }
            sensorPayload.put("status", Constant.FACTORY_RESET);
        } catch (Exception ignored) {}

        DoorSensorService.getInstance().resetSensorOnServer(sensorToReset, new ResponseHandler() {
            @Override
            public void onSuccess(Object serverData) {
                Loader.getInstance().hideLoader();
                Logger.d(TAG, "Sensor server reset success: " + serverData);

                // Delete locally now that server has processed the reset
                deleteSensorLocally(sensorToReset);

                if (getActivity() != null) {
                    String alertMsg = "TARGET URL:\n" + sensorTargetUrl + "\n\n"
                            + "PAYLOAD SENT:\n" + sensorPayload.toString() + "\n\n"
                            + "SERVER RESPONSE:\n" + (serverData != null ? serverData.toString() : "empty");
                    AppDialog.showAlertDialog(getActivity(), "[TEST ALERT] Sensor Factory Reset Server Response", alertMsg, (d, w) -> {
                        if (getActivity() != null) {
                            requireActivity().getOnBackPressedDispatcher().onBackPressed();
                        }
                    });
                } else if (getActivity() != null) {
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            }

            @Override
            public void onAuthError(String message) {
                Loader.getInstance().hideLoader();
                deleteSensorLocally(sensorToReset);

                if (getActivity() != null) {
                    String alertMsg = "TARGET URL:\n" + sensorTargetUrl + "\n\n"
                            + "PAYLOAD SENT:\n" + sensorPayload.toString() + "\n\n"
                            + "AUTH ERROR:\n" + message;
                    AppDialog.showAlertDialog(getActivity(), "[TEST ALERT] Sensor Factory Reset Auth Error", alertMsg, (d, w) -> {
                        if (getActivity() != null) {
                            requireActivity().getOnBackPressedDispatcher().onBackPressed();
                        }
                    });
                } else if (getActivity() != null) {
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            }

            @Override
            public void onError(String message) {
                Loader.getInstance().hideLoader();
                deleteSensorLocally(sensorToReset);

                if (getActivity() != null) {
                    String alertMsg = "TARGET URL:\n" + sensorTargetUrl + "\n\n"
                            + "PAYLOAD SENT:\n" + sensorPayload.toString() + "\n\n"
                            + "ERROR:\n" + message;
                    AppDialog.showAlertDialog(getActivity(), "[TEST ALERT] Sensor Factory Reset Error", alertMsg, (d, w) -> {
                        if (getActivity() != null) {
                            requireActivity().getOnBackPressedDispatcher().onBackPressed();
                        }
                    });
                } else if (getActivity() != null) {
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void deleteSensorLocally(Sensor sensor) {
        if (sensor == null) return;
        if (sensor.getId() != null) {
            SecuredStorageManager.getInstance().deleteSensor(sensor.getId());
        }
        if (sensor.getSerialNumber() != null) {
            SecuredStorageManager.getInstance().deleteSensor(sensor.getSerialNumber());
        }
        if (sensor.getCode() != null) {
            SecuredStorageManager.getInstance().deleteSensor(sensor.getCode());
        }
    }

    private void finishResetWithNotice(Object hardwareData, String serverNotice) {
        Loader.getInstance().hideLoader();
        String alertMsg = (hardwareData != null ? "HARDWARE RESET:\nSuccess\n\n" : "")
                + "SERVER SYNC:\n" + serverNotice + "\n\n"
                + "Sensor removed locally from app.";
        AppDialog.showAlertDialog(getActivity(), "[TEST ALERT] Sensor Reset Result", alertMsg, (d, w) -> {
            if (getActivity() != null) {
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }
}
