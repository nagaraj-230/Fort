package com.payoda.smartlock.locks;


import static com.payoda.smartlock.constants.Constant.HW_VERSION_4_0;
import static com.payoda.smartlock.constants.Constant.HW_VERSION_6_0;
import static com.payoda.smartlock.constants.Constant.SCREEN_DATA;
import static com.payoda.smartlock.constants.Constant.WIFI_DELAY_TIME;
import static com.payoda.smartlock.utils.DateTimeUtils.DDMMYY_HHMMSS;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.payoda.smartlock.App;
import com.payoda.smartlock.BuildConfig;
import com.payoda.smartlock.R;
import com.payoda.smartlock.authentication.BaseFragment;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.constants.ServiceUrl;
import com.payoda.smartlock.fp.view.FPListFragment;
import com.payoda.smartlock.history.TabHistoryActivity;
import com.payoda.smartlock.history.model.AccessLog;
import com.payoda.smartlock.history.model.AccessLogList;
import com.payoda.smartlock.history.model.AccessLogResponse;
import com.payoda.smartlock.history.service.HistoryService;
import com.payoda.smartlock.locks.callback.IKeyListener;
import com.payoda.smartlock.locks.model.Lock;
import com.payoda.smartlock.locks.model.LockFlow;
import com.payoda.smartlock.locks.model.LockKeyList;
import com.payoda.smartlock.locks.model.LockKeys;
import com.payoda.smartlock.locks.model.WifiLock;
import com.payoda.smartlock.locks.model.WifiLockResponse;
import com.payoda.smartlock.locks.service.EngageLockService;
import com.payoda.smartlock.locks.service.LockKeyService;
import com.payoda.smartlock.locks.service.LockListService;
import com.payoda.smartlock.managepins.TabManagePinsActivity;
import com.payoda.smartlock.managepins.model.AuthViaPinFp;
import com.payoda.smartlock.plugins.bluetooth.BleManager;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.plugins.network.SyncAll;
import com.payoda.smartlock.plugins.pushnotification.RemoteDataEvent;
import com.payoda.smartlock.plugins.storage.SecuredStorageManager;
import com.payoda.smartlock.plugins.storage.lock.LockDBClient;
import com.payoda.smartlock.plugins.wifi.WifiLockManager;
import com.payoda.smartlock.plugins.wifi.WifiUtilManager;
import com.payoda.smartlock.service.AESEncryption;
import com.payoda.smartlock.service.SyncScheduler;
import com.payoda.smartlock.utils.AppDialog;
import com.payoda.smartlock.utils.DateTimeUtils;
import com.payoda.smartlock.utils.HexUtils;
import com.payoda.smartlock.utils.Loader;
import com.payoda.smartlock.utils.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.math.BigInteger;
import java.util.ArrayList;

public class LockDetailFragment extends BaseFragment {

    public static final String TAG = "### LockDetailFragment";

    private EditText etDoorTitle;
    private TextView tvDoorTitle, tvEngageLock;
    private ImageView ivEdit, ivSave, iv_engage, ivSettings;
    private LinearLayout llUserHistoryV3;
    private Button btnUser, btnHistory, btnFP, btnRFID, btnManagePin, btnHistoryV3;
    public static Lock mLock;
    private TextInputLayout doorTitleWrapper;
    private TextView tvBattery, lblBatteryLabel;
    private TextView tvFactoryReset;
    private TextView tvEngageToProceed;
    private SwitchCompat switchPassageMode;
    //RadioGroup radioGroupPassage;
    private RadioButton rbWifi, rbBle;

    private String id = null;
    private String key = null;
    private int slotNumber = -1;
    private LockFlow mLockFlow = LockFlow.ENGAGE;
    private int ENGAGE_ATTEMPT_COUNT = 1;

    private boolean allowAPICall = true;
    private String userType = Constant.USER;

    public LockDetailFragment() {
        // Required empty public constructor
    }

    public static LockDetailFragment getInstance() {
        return new LockDetailFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mLock = new Gson().fromJson(bundle.getString(Constant.SCREEN_DATA), Lock.class);
            System.out.println("### lock detail lock = " + mLock);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Logger.d(TAG, TAG);
        return initializeView(inflater.inflate(R.layout.fragment_lock_detail, container, false));
    }

    @Override
    public void onResume() {
        super.onResume();
        //Check WifiLock
        setBleContainer(!WifiLockManager.getInstance().isWifiLockConnected(getContext()));
        initSyncService();
    }

    private void initSyncService() {
        try {
            SyncScheduler.getInstance().schedule(getContext(), Constant.JOBS.ALL);
        } catch (Exception e) {
            Logger.e(e);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(RemoteDataEvent remoteDataEvent) {
        if (remoteDataEvent != null && (remoteDataEvent.getStatus().equalsIgnoreCase(Constant.SUCCESS)
                || remoteDataEvent.getStatus().equalsIgnoreCase(Constant.FAILURE))
                && remoteDataEvent.getCommand().equalsIgnoreCase(Constant.ENGAGE_COMMAND)) {
            Loader.getInstance().hideLoader();
            AppDialog.showAlertDialog(getActivity(), remoteDataEvent.getBody());
        }
    }

    /**
     * Use this method to initialize the view.
     *
     * @return View Return the initialized view;
     */

    private View initializeView(final View view) {

//        updateManufactureCode();

        etDoorTitle = view.findViewById(R.id.et_door_title);
        tvDoorTitle = view.findViewById(R.id.tv_door_title);
        tvEngageLock = view.findViewById(R.id.tv_engage_lock);
        doorTitleWrapper = view.findViewById(R.id.doorTitleWrapper);
        ((TextView) view.findViewById(R.id.tv_title)).setText(getString(R.string.lock_details));
        ivEdit = view.findViewById(R.id.iv_edit);
        ivSave = view.findViewById(R.id.iv_save);
        ivSettings = view.findViewById(R.id.iv_settings);

        switchPassageMode = view.findViewById(R.id.passageModeSwitch);


        if (mLock != null && mLock.getLockVersion() != null && (mLock.getLockVersion().equalsIgnoreCase(Constant.HW_VERSION_4_0))) {

            String mLockUserType = Constant.USER;

            for (LockKeys lockKeys : mLock.getLockKeys()) {
                if (!lockKeys.getUserType().equalsIgnoreCase(Constant.OWNER_ID)) {
                    mLockUserType = lockKeys.getUserType();
                    break;
                }
            }

            if (mLockUserType.equalsIgnoreCase(Constant.OWNER)) {
                ivSettings.setVisibility(View.VISIBLE);
            } else {
                ivSettings.setVisibility(View.GONE);
            }



        } else {
            ivSettings.setVisibility(View.GONE);
        }

        if (mLock != null && mLock.getLockVersion() != null && (mLock.getLockVersion().equalsIgnoreCase(HW_VERSION_6_0))){
            tvEngageLock.setText("Long Press \"1\" on the Lock and Click \"OK\"");
        }else {
            tvEngageLock.setText("Please switch ON the Lock and Tap here to Lock/Unlock");
        }



        iv_engage = view.findViewById(R.id.iv_engage);
        btnUser = view.findViewById(R.id.btn_user);
        btnHistory = view.findViewById(R.id.btn_history);
        btnFP = view.findViewById(R.id.btn_fp);
        btnRFID = view.findViewById(R.id.btn_rfid);
        btnManagePin = view.findViewById(R.id.btn_manage_pin);
        btnHistoryV3 = view.findViewById(R.id.btn_history_v3);
        llUserHistoryV3 = view.findViewById(R.id.ll_user_history_container_v3);
        tvBattery = view.findViewById(R.id.tv_battery);
        lblBatteryLabel = view.findViewById(R.id.lblBatteryLabel);
        tvFactoryReset = view.findViewById(R.id.tv_factory_reset);
        rbWifi = view.findViewById(R.id.rb_wifi);
        rbBle = view.findViewById(R.id.rb_ble);
        tvEngageToProceed = view.findViewById(R.id.tv_engage_to_proceed);

        view.findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();

            }
        });

        ivEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivEdit.setVisibility(View.GONE);
                ivSave.setVisibility(View.VISIBLE);
                etDoorTitle.setVisibility(View.VISIBLE);
                tvDoorTitle.setVisibility(View.INVISIBLE);
                etDoorTitle.addTextChangedListener(new LockDetailFragment.MyTextWatcher(etDoorTitle));
                if (etDoorTitle.getText().length() > 0)
                    etDoorTitle.setSelection(etDoorTitle.getText().length());
            }
        });

        ivSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateDoorName())
                    backToEdit();
            }
        });

        view.findViewById(R.id.iv_engage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLock != null && mLock.getLockKeys() != null && !mLock.getLockKeys().isEmpty()) {

                    if (mLock.getLockVersion().equalsIgnoreCase(HW_VERSION_4_0)) {

                        Loader.getInstance().showLoader(getContext());

                        EngageLockService.getInstance().engageLockMqtt(mLock.getSerialNumber(), new ResponseHandler() {
                            @Override
                            public void onSuccess(Object data) {
                                Loader.getInstance().hideLoader();
                            }

                            @Override
                            public void onAuthError(String message) {
                                Loader.getInstance().hideLoader();
                                AppDialog.showAlertDialog(getContext(), message);
                            }

                            @Override
                            public void onError(String message) {
                                Loader.getInstance().hideLoader();
                                AppDialog.showAlertDialog(getContext(), message);
                            }
                        });
                    } else {
                        //Check same wifi lock connected or not
                        setBleContainer(!WifiLockManager.getInstance().isSameWifiLockConnected(getContext(), mLock.getSsid()));

                        id = null;
                        key = null;
                        slotNumber = -1;
                        mLockFlow = LockFlow.ENGAGE;

                        if (mLock.isOffline()) {
                            id = mLock.getLockIds().get(0).getKey();
                            key = mLock.getLockKeys().get(0).getKey();
                        } else {
                            //String tempKey = null;
                            for (LockKeys lockKeys : mLock.getLockKeys()) {
                                if (lockKeys.getUserType().equalsIgnoreCase(Constant.OWNER_ID)) {
                                    id = lockKeys.getKey(); // owner id
                                } else {
                                    key = lockKeys.getKey(); // slot key
                                }
                            }

                            Logger.d("### OWNER ID encrypt = " + id);
                            Logger.d("### slot key encrypt = " + key);

                            //Get slot number from unassigned keys.
                            if (mLock.getUnAssignedKeys() != null && mLock.getUnAssignedKeys().size() > 0) {
                                try {
                                    slotNumber = Integer.parseInt(mLock.getUnAssignedKeys().get(0).getSlotNumber());
                                } catch (Exception e) {
                                    Logger.e(e);
                                }
                            }

                            // Get lock engage mode
                            for (LockKeys lockKeys : mLock.getLockKeys()) {
                                if (lockKeys.getUserType().equalsIgnoreCase(Constant.OWNER_ID)) {
                                    //status - 2
                                    if (lockKeys.getStatus().equalsIgnoreCase(Constant.TRANSFER)) {
                                        mLockFlow = LockFlow.TRANSFER_OWNER;
                                    }
                                }
                            }


                        }


                        String status;
                        String msg = "";

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

                                                Logger.d("### lockFlow = " + mLockFlow);
                                                engageLock(id, key, slotNumber, mLockFlow);
                                            }
                                        });
                                    }
                                },
                                "CANCEL", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });

                    }
                } else {
                    AppDialog.showAlertDialog(getContext(), "Invalid Lock Key. Please contact support.");
                }
            }
        });

        btnUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mLock != null && mLock.getLockVersion().equalsIgnoreCase(HW_VERSION_6_0) && mLock.getEnablePassage() != null &&
                        mLock.getEnablePassage().equalsIgnoreCase(Constant.PASSAGE_ON)) {
                    AppDialog.showAlertDialog(getContext(), getString(R.string.passage_alert));
                } else {
                    if (ServiceManager.getInstance().isNetworkAvailable(getContext()) && mLock != null) {
                        App.getInstance().showFullScreen(getActivity(), Constant.SCREEN.ASSIGN_USERS, new Gson().toJson(mLock));
                    } else {
                        AppDialog.showAlertDialog(getContext(), getString(R.string.no_internet));
                    }
                }

            }
        });

        btnHistoryV3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHistoryScreen();
            }
        });

        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHistoryScreen();
            }
        });

        btnFP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mLock != null && mLock.getLockVersion().equalsIgnoreCase(HW_VERSION_6_0) && mLock.getEnablePassage() != null &&
                        mLock.getEnablePassage().equalsIgnoreCase(Constant.PASSAGE_ON)) {
                    AppDialog.showAlertDialog(getContext(), getString(R.string.passage_alert));
                } else {
                    if (ServiceManager.getInstance().isNetworkAvailable(getContext())) {
                        if (getActivity() != null)
                            App.getInstance().showFullScreen(getActivity(), Constant.SCREEN.FINGER_PRINT_LIST, new Gson().toJson(mLock));
                    } else {
                        AppDialog.showAlertDialog(getContext(), getString(R.string.no_internet));
                    }
                }

            }
        });

        btnRFID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLock != null && mLock.getLockVersion().equalsIgnoreCase(HW_VERSION_6_0) && mLock.getEnablePassage() != null &&
                        mLock.getEnablePassage().equalsIgnoreCase(Constant.PASSAGE_ON)) {
                    AppDialog.showAlertDialog(getContext(), getString(R.string.passage_alert));
                } else {
                    if (ServiceManager.getInstance().isNetworkAvailable(getContext())) {
                        App.getInstance().showFullScreen(getActivity(), Constant.SCREEN.RFID_LIST, new Gson().toJson(mLock));
                    } else {
                        AppDialog.showAlertDialog(getContext(), getString(R.string.no_internet));
                    }
                }

            }
        });

        btnManagePin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mLock != null && mLock.getLockVersion().equalsIgnoreCase(HW_VERSION_6_0) && mLock.getEnablePassage() != null &&
                        mLock.getEnablePassage().equalsIgnoreCase(Constant.PASSAGE_ON)) {
                    AppDialog.showAlertDialog(getContext(), getString(R.string.passage_alert));
                } else {
                    if (ServiceManager.getInstance().isNetworkAvailable(getContext())) {
                        try {
                            SyncAll.getInstance().pushAll(getContext());
                        } catch (Exception e) {
                            Logger.e(e);
                        }
                        Intent intent = new Intent(getContext(), TabManagePinsActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString(SCREEN_DATA, new Gson().toJson(mLock));
                        intent.putExtras(bundle);
                        startActivity(intent);
                    } else {
                        AppDialog.showAlertDialog(getContext(), getString(R.string.no_internet));
                    }
                }

            }
        });

        // V6.0
        ivSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.getInstance().showFullScreen(getActivity(), Constant.SCREEN.WIFI_MQTT_CONFIGURATION, new Gson().toJson(mLock));
            }
        });

        switchPassageMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (allowAPICall) {
                    if (mLock.getLockVersion().equalsIgnoreCase(HW_VERSION_6_0)) {
                        doUpdatePassageMode(isChecked);
                    }
                } else {
                    allowAPICall = true;
                }
            }
        });

        updateUI();

        return view;

    }

    // TODO :
    //  if this app support all lock version means use this
    //  if this app support V6.0 only means delete this and you need to update Brand info-manufacturerCode from Backend as 'FORT_'
    //  if this app support from v1.0 to V3.2 means delete this and you need to update Brand info- manufacturerCode from Backend as 'ASTRIX_'
    //  currently brand info - manufacturer code from Backend is "FORT_"
    //  Make sure aware of this
    private void updateManufactureCode() {
        WifiLockManager.getInstance().updateManufactureCode(mLock);
        BleManager.getInstance().updateManufactureCode(mLock);
    }

    private void doUpdatePassageMode(boolean isChecked) {

        String status;
        String msg = "";
        if (mLock != null && mLock.getLockVersion().equalsIgnoreCase(HW_VERSION_6_0)) {
            status = LockManager.getInstance().preCheckBLEVersion6(getActivity());// "success"
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

                        doPassageModeViaLock(isChecked);
                    }
                }, "CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        allowAPICall = false;
                        switchPassageMode.setChecked(!isChecked); // TODO false
                    }
                });

    }

    private void doPassageModeViaLock(boolean isChecked) {

        Loader.getInstance().showLoader(getContext());
        String SSID = BleManager.MANUFACTURER_CODE + mLock.getSerialNumber();
        String password = mLock.getScratchCode();
        String lockVersion = mLock.getLockVersion();

        WifiUtilManager wifiUtilManager = new WifiUtilManager(getContext(), new WifiUtilManager.WifiListener() {
            @Override
            public void connectionSuccess() {
                Logger.d("Connection Success");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doPassageModeRequest(getContext(), isChecked);
                    }
                }, WIFI_DELAY_TIME);
            }

            @Override
            public void connectionTimeOut() {
                allowAPICall = false;
                switchPassageMode.setChecked(!isChecked); // TODO false
                Loader.getInstance().hideLoader();
                Toast.makeText(getContext(), "Please try again later", Toast.LENGTH_LONG).show();
            }
        });
        wifiUtilManager.startScanning(lockVersion, SSID, password);
    }

    private void doPassageModeRequest(Context context, boolean isChecked) {

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
            authViaPinFp.setEnDis(isChecked ? "1" : "0"); // 1 enable , 0  disable

            ServiceManager.getInstance().post(ServiceUrl.WIFI_LOCK_PASSAGE_MODE, authViaPinFp, new ResponseHandler() {
                @Override
                public void onSuccess(Object data) {
                    Loader.getInstance().hideLoader();
                    if (data != null) {
                        WifiLockResponse authViaFpResponse = new Gson().fromJson(data.toString(), WifiLockResponse.class);
                        if (authViaFpResponse != null && authViaFpResponse.getStatus() != null
                                && authViaFpResponse.getStatus().equalsIgnoreCase("success")) {

                            mLock.setEnablePassage(isChecked ? "1" : "0");
                            LockDetailFragment.mLock.setEnablePassage(isChecked ? "1" : "0");
                            String msg = isChecked ? "enabled" : "disabled";
                            forgetWIFINetwork();
                            AppDialog.showAlertDialog(context, "Success", "Passage mode " + msg + " successfully",
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
                        AppDialog.showAlertDialog(getContext(), "Invalid response from Lock. Please try again later.");
                    }
                }

                @Override
                public void onAuthError(String message) {
                    allowAPICall = false;
                    switchPassageMode.setChecked(!isChecked); // TODO false

                    Loader.getInstance().hideLoader();
                    Logger.d(message);
                    AppDialog.showAlertDialog(context, "Invalid Lock Key. Please contact support.");
                }

                @Override
                public void onError(String message) {
                    allowAPICall = false;
                    switchPassageMode.setChecked(!isChecked); // TODO false

                    Loader.getInstance().hideLoader();
                    try {
                        if (message != null && message.equals("No Internet. Please Check your network connection.")) {
                            AppDialog.showAlertDialog(context, message);
                        } else {
                            WifiLockResponse mWifiLockResponse = new Gson().fromJson(message, WifiLockResponse.class);
                            String errorMsg = "";
                            if (mWifiLockResponse != null && mWifiLockResponse.getErrorMessage() != null
                                    && mWifiLockResponse.getErrorMessage().equalsIgnoreCase(FPListFragment.AUTHVIA_FP_DISABLED)) {
                                forgetWIFINetwork();

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
        if (getActivity() != null)
            requireActivity().getOnBackPressedDispatcher().onBackPressed();

    }

    private void showHistoryScreen() {
        if (ServiceManager.getInstance().isNetworkAvailable(getContext())) {
            //App.getInstance().showFullScreen(getActivity(), Constant.SCREEN.HISTORY, new Gson().toJson(mLock));
            Intent intent = new Intent(getContext(), TabHistoryActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(SCREEN_DATA, new Gson().toJson(mLock));
            intent.putExtras(bundle);
            startActivity(intent);
        } else {
            AppDialog.showAlertDialog(getContext(), getString(R.string.no_internet));
        }
    }

    private void engageLock(String id, String key, int slotNumber, LockFlow mLockFlow) {

        Loader.getInstance().showLoader(getContext());

        if (rbWifi.isChecked()) {

            engageLockWithWifi(id, key, slotNumber, mLockFlow);

        } else {

            Logger.d("### MANUFACTURER_CODE = " + BuildConfig.MANUFACTURER_CODE);

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
                            ENGAGE_ATTEMPT_COUNT = 1;
                            engageLockWithWifi(id, key, slotNumber, mLockFlow);
                        }
                    }, WIFI_DELAY_TIME);

                }

                @Override
                public void connectionTimeOut() {
                    Logger.d("Connection Timeout");

                    if (mLock.getLockVersion() != null && !mLock.getLockVersion().equalsIgnoreCase(HW_VERSION_6_0)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            getBluetoothScanConnectPermission(new RequestPermissionAction() {
                                @Override
                                public void permissionDenied() {
                                    BleManager.getInstance().checkBluetoothScanPermission(getActivity());
                                }

                                @Override
                                public void permissionGranted() {
                                    engageLockWithBLE(id, key, slotNumber, mLockFlow);
                                }
                            });
                        } else {
                            engageLockWithBLE(id, key, slotNumber, mLockFlow);
                        }

                    } else {
                        Loader.getInstance().hideLoader();
                        AppDialog.showAlertDialog(getActivity(), "Lock connection timeout.");
                    }
                }
            });

            wifiUtilManager.startScanning(lockVersion, SSID, password);

        }
    }

    private void engageLockWithWifi(String id, String key, int slotNumber, LockFlow mLockFlow) {


        if (!WifiLockManager.getInstance().isWifiEnabled(getContext())) {
            AppDialog.showAlertDialog(getContext(), getString(R.string.turn_on_wifi_connect_lock));
        } else if (!WifiLockManager.getInstance().isWifiLockConnected(getContext())) {
            AppDialog.showAlertDialog(getContext(), getString(R.string.no_wifi_lock), getString(R.string.connect_wifi_lock));
        } else if (!WifiLockManager.getInstance().isSameWifiLockConnected(getContext(), mLock.getSsid())) {
            AppDialog.showAlertDialog(getContext(), getString(R.string.no_wifi_lock), getString(R.string.connect_same_wifi_lock));
        } else {

            final WifiLock mWifiLock = new WifiLock();

            mWifiLock.setOwnerId(AESEncryption.getInstance().decrypt(id, mLock.isEncrypted()));
            mWifiLock.setSlotKey(AESEncryption.getInstance().decrypt(key, mLock.isEncrypted()));

            Logger.d("### owner id = " + mWifiLock.getOwnerId());
            Logger.d("### owner id SLOT KEY  = " + mWifiLock.getSlotKey());

            final LockFlow finalMLockFlow = mLockFlow;
            final int finalSlotNumber = slotNumber;

            //Update Date Time
            doDateTimeRequest(id, key, slotNumber, mLockFlow, mWifiLock, finalMLockFlow, finalSlotNumber);

            // Get Battery Level
            // doBatteryRequest(mWifiLock);

        }
    }

    private void forgetWIFINetwork() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {

                String SSID = BleManager.MANUFACTURER_CODE + mLock.getSerialNumber();
                WifiUtilManager.forgetMyNetwork(getContext(), SSID);
            }
        }, 500);
    }

    private void engageLockWithBLE(String id, String key, int slotNumber, LockFlow mLockFlow) {

        id = AESEncryption.getInstance().decrypt(id, mLock.isEncrypted());
        key = AESEncryption.getInstance().decrypt(key, mLock.isEncrypted());
        String ssid = BleManager.MANUFACTURER_CODE + mLock.getSerialNumber();

        LockManager.getInstance().engageLock(getActivity(), mLock.getUuid(), ssid, id + key, mLockFlow, slotNumber, new IKeyListener() {

            @Override
            public void onLockIds(ArrayList<String> alIds) {
                Logger.d("engageLock onLockIds()");
                if (alIds != null && alIds.size() > 0) {
                    storeKeys(AESEncryption.getInstance().encrypt(alIds.get(0), mLock.isEncrypted()));
                }
            }

            @Override
            public void onLockKeys(ArrayList<String> alKeys) {

            }

            @Override
            public void onBatteryUpdate(String battery) {
                Logger.d("### ======Value======" + battery);
                battery = String.valueOf(HexUtils.hexToInt(battery));
                mLock.setBattery(battery);
                doServiceRequest();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateBattery();
                        }
                    });
                }
            }

            @Override
            public void onAccessLog(ArrayList<String> alLogs) {
                if (alLogs.size() > 0)
                    SecuredStorageManager.getInstance().setAccessLogs(mLock.getSerialNumber(), mLock.getId(), alLogs);
                doHistoryUpdate();
            }

            @Override
            public void onLockActivated() {
                AppDialog.showAlertDialog(getActivity(), "Lock engaged successfully.");
            }

            @Override
            public void onDeviceNotConnected() {
                Loader.getInstance().hideLoader();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (ServiceManager.getInstance().isMobileDataEnabled(getContext())) {
                            AppDialog.showAlertDialog(getContext(), "Please switch-off your mobile data and try again");
                        } else {
                            AppDialog.showAlertDialog(getContext(), getContext().getString(R.string.lock_failure_alert));
                        }
                    }
                });

            }

            @Override
            public void onMacAddressUpdate(String ssid, String macAddress) {
                Logger.d("### Bluetooth ssid engage lock --> ", ssid);
                Logger.d("### Bluetooth macAddress engage lock --> ", macAddress);
                if (mLock != null) {

                    mLock.setUuid(macAddress);
                    mLock.setSync(false);
                    LockDBClient.getInstance().save(mLock, getContext());

                }
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void addOfflineKeys(LockKeys lockKeys) {
        LockKeyList mLockKeyList = SecuredStorageManager.getInstance().getOfflineKeys();
        ArrayList<LockKeys> localKeyList = new ArrayList<>();
        if (mLockKeyList.getLockKeys() != null) {
            localKeyList.addAll(mLockKeyList.getLockKeys());
        }
        for (int i = 0; i < localKeyList.size(); i++) {
            if (localKeyList.get(i).getId().equalsIgnoreCase(lockKeys.getId())) {
                mLockKeyList.getLockKeys().remove(i);
            }
        }
        mLockKeyList.getLockKeys().add(lockKeys);
        SecuredStorageManager.getInstance().setOfflineKeys(mLockKeyList);
        SyncScheduler.getInstance().schedule(getActivity(), Constant.JOBS.ALL);
    }

    private void updateBattery() {

        Logger.d("### battery lvl = " + mLock.getBattery());

        try {
            if (mLock != null && mLock.getBattery() != null && !TextUtils.isEmpty(mLock.getBattery())) {
                try {

                    int iBattery = Integer.parseInt(mLock.getBattery());
                    int iBatteryIcon;

                    if (iBattery <= 25 ) {
                        iBatteryIcon = R.mipmap.ic_battery_empty;
                    } else if (iBattery <= 50 && iBattery > 25) {
                        iBatteryIcon = R.mipmap.ic_battery_half;
                    }else {
                        iBatteryIcon = R.mipmap.ic_battery_full;
                    }

                    tvBattery.setCompoundDrawablesWithIntrinsicBounds(iBatteryIcon, 0, 0, 0);

                } catch (Exception e) {
                    Logger.e(e);
                }

                if (mLock.getBattery() != null) {
                    try {
                        int iBattery = Integer.parseInt(mLock.getBattery());
                        if (iBattery < 0) {
                            mLock.setBattery("0");
                        }
                        if (iBattery > 100) {
                            mLock.setBattery("100");
                        }
                    } catch (Exception e) {
                        Logger.e(e);
                    }
                }

                if (BuildConfig.BATTERY_LEVEL_VISIBILITY) {
                    tvBattery.setText(String.format("%s%%", mLock.getBattery()));
                } else {
                    tvBattery.setText("");
                }


            }
        } catch (Exception e) {
            Logger.e(e);
        }
    }

    private void updateUI() {

        if (mLock != null && mLock.getLockKeys() != null && !mLock.getLockKeys().isEmpty()) {

            tvDoorTitle.setText(mLock.getName());
            etDoorTitle.setText(mLock.getName());

            String strStatus = Constant.TRANSFER;

            for (LockKeys lockKeys : mLock.getLockKeys()) {
                String userType = lockKeys.getUserType();
                if (userType != null && userType.equalsIgnoreCase(Constant.OWNER_ID)) {
                    strStatus = lockKeys.getStatus();
                    break;
                }
            }

            if (strStatus.equalsIgnoreCase(Constant.TRANSFER)) {

                Logger.d("### LockDetailFrag UpdateUI 1 ");

                tvEngageToProceed.setVisibility(View.VISIBLE);
                switchPassageMode.setVisibility(View.GONE); // TODO GONE


            } else if (mLock.isOffline()) {

                Logger.d("### LockDetailFrag UpdateUI 2 ");

                tvEngageToProceed.setVisibility(View.GONE);
                btnHistory.setVisibility(View.VISIBLE);
                showFPAndRFID();
                btnUser.setVisibility(View.VISIBLE);
                updateBattery();
                tvBattery.setVisibility(View.VISIBLE);
                lblBatteryLabel.setVisibility(View.VISIBLE);

            } else {

                Logger.d("### LockDetailFrag UpdateUI 3 ");
                updateBattery();
                tvBattery.setVisibility(View.VISIBLE);
                lblBatteryLabel.setVisibility(View.VISIBLE);

                String mLockUserType = Constant.USER;

                for (LockKeys lockKeys : mLock.getLockKeys()) {
                    if (!lockKeys.getUserType().equalsIgnoreCase(Constant.OWNER_ID)) {
                        mLockUserType = lockKeys.getUserType();
                        break;
                    }
                }

                if (mLockUserType.equalsIgnoreCase(Constant.OWNER)) {

                    // Owner UI
                    ivEdit.setVisibility(View.VISIBLE);

                    if (mLock.getLockVersion() != null &&
                            (mLock.getLockVersion().equalsIgnoreCase(Constant.HW_VERSION_3)
                                    || mLock.getLockVersion().equalsIgnoreCase(Constant.HW_VERSION_3_1)
                                    || mLock.getLockVersion().equalsIgnoreCase(Constant.HW_VERSION_3_2)
                                    || mLock.getLockVersion().equalsIgnoreCase(HW_VERSION_4_0)
                                    || mLock.getLockVersion().equalsIgnoreCase(HW_VERSION_6_0))) {

                        btnManagePin.setVisibility(View.VISIBLE);
                        btnHistory.setVisibility(View.GONE);
                        llUserHistoryV3.setVisibility(View.VISIBLE);

                    } else {

                        btnHistory.setVisibility(View.VISIBLE);
                    }


                    if (mLock.getLockVersion() != null && (mLock.getLockVersion().equalsIgnoreCase(HW_VERSION_6_0))) {
                        updatePassageModeButtonAndWifiConfig();
                    }

                    showFPAndRFID();
                    btnUser.setVisibility(View.VISIBLE);
                    tvFactoryReset.setVisibility(View.GONE);
                    tvEngageToProceed.setVisibility(View.GONE);

                }

                else if (mLockUserType.equalsIgnoreCase(Constant.MASTER)) {

                    // Master UI
                    btnHistory.setVisibility(View.VISIBLE);
                    btnUser.setVisibility(View.VISIBLE);

                    tvEngageToProceed.setVisibility(View.GONE);

                    String previlageId = mLock.getPrivilege();
                    String lockUserId = mLock.getUserId() != null ? mLock.getUserId() : "";
                    String key1UserId = mLock.getLockKeys().get(0).getUserId() != null ? mLock.getLockKeys().get(0).getUserId() : "";
                    String key2UserId = mLock.getLockKeys().get(1).getUserId() != null ? mLock.getLockKeys().get(1).getUserId() : "";

                    Logger.d("######## previlageId : "+previlageId);
                    Logger.d("######## lockUserId : "+lockUserId);
                    Logger.d("######## key1UserId : "+key1UserId);
                    Logger.d("######## key2UserId : "+key2UserId);

                    if (!TextUtils.isEmpty(previlageId) && (lockUserId.equalsIgnoreCase(previlageId)
                            || key1UserId.equalsIgnoreCase(previlageId) || key2UserId.equalsIgnoreCase(previlageId))) {
                        showFPAndRFID();
                    }

                } else {
                    // USER UI
                    btnHistory.setVisibility(View.VISIBLE);
                    tvEngageToProceed.setVisibility(View.GONE);
                }

            }
        }
    }

    private void updatePassageModeButtonAndWifiConfig() {

        Logger.d("############## updatePassageModeButtonAndWifiConfig ");

        if (mLock != null && mLock.getLockKeys() != null && !mLock.getLockKeys().isEmpty()) { // 1

            if (mLock.getLockVersion().equalsIgnoreCase(Constant.HW_VERSION_6_0)) { // 2

                for (LockKeys lockKeys : mLock.getLockKeys()) {

                    if (!lockKeys.getUserType().equalsIgnoreCase(Constant.OWNER_ID)) {
                        userType = lockKeys.getUserType();
                        break;
                    }
                }

                boolean passageAccess = mLock.getEnablePassage() != null && mLock.getEnablePassage().equalsIgnoreCase("1");

                Logger.d("### mLock.getEnablePassage() = " + mLock.getEnablePassage());
                Logger.d("### mLock.passageAccess  = " + passageAccess);

                if (userType.equalsIgnoreCase(Constant.OWNER)) { //3

                    switchPassageMode.setVisibility(View.VISIBLE);

                    ivSettings.setVisibility(View.VISIBLE);

                    if (!passageAccess) {
                        // disabled off
                        allowAPICall = true;
                        switchPassageMode.setChecked(false); //0 off

                    } else {
                        // enabled on
                        allowAPICall = false;
                        switchPassageMode.setChecked(true); //1 on
                    }

                } else {
                    switchPassageMode.setVisibility(View.GONE);
                    ivSettings.setVisibility(View.GONE);
                }

            } else {
                switchPassageMode.setVisibility(View.GONE);
                ivSettings.setVisibility(View.GONE);
            }

        }
    }

    private void showFPAndRFID() {
        if (mLock.getLockVersion() != null && (mLock.getLockVersion().equalsIgnoreCase(Constant.HW_VERSION_2)
                || mLock.getLockVersion().equalsIgnoreCase(Constant.HW_VERSION_2_1)
                || mLock.getLockVersion().equalsIgnoreCase(Constant.HW_VERSION_3)
                || mLock.getLockVersion().equalsIgnoreCase(Constant.HW_VERSION_3_1)
                || mLock.getLockVersion().equalsIgnoreCase(Constant.HW_VERSION_3_2)
                || mLock.getLockVersion().equalsIgnoreCase(Constant.HW_VERSION_4_0)
                || mLock.getLockVersion().equalsIgnoreCase(HW_VERSION_6_0)

        )) {
            // TODO v6.0
            btnRFID.setVisibility(View.VISIBLE);
            btnFP.setVisibility(View.VISIBLE);
        }
    }

    public void backToEdit() {
        mLock.setName(etDoorTitle.getText().toString());
        tvDoorTitle.setText(etDoorTitle.getText());
        ivSave.setVisibility(View.GONE);
        tvDoorTitle.setVisibility(View.VISIBLE);
        ivEdit.setVisibility(View.VISIBLE);
        etDoorTitle.setVisibility(View.INVISIBLE);
        hideKeyboard();
        doServiceRequest();
    }

    private void doAccessLogRequest(WifiLock wifiLock) {

        ServiceManager.getInstance().post(ServiceUrl.WIFI_LOCK_ACCESS_LOG, wifiLock, new ResponseHandler() {
            @Override
            public void onSuccess(Object data) {

                ArrayList<String> alLogs = new ArrayList<>();
                AccessLog mAccessLog = new Gson().fromJson(data.toString(), AccessLog.class);
                if (mAccessLog != null && mAccessLog.getResponse() != null) {
                    for (String key : mAccessLog.getResponse().keySet()) {
                        alLogs.add(mAccessLog.getResponse().get(key));
                    }
                }
                if (alLogs.size() > 0)
                    SecuredStorageManager.getInstance().setAccessLogs(mLock.getSerialNumber(), mLock.getId(), alLogs);

                doHistoryUpdate();
                forgetWIFINetwork();

            }

            @Override
            public void onAuthError(String message) {
                String error = message;
            }

            @Override
            public void onError(String message) {
                String error = message;
            }
        });
    }

    private void doServiceRequest() {
        doServiceRequest(false);
    }

    private void doReadAllKeys(WifiLock wifiLock, BigInteger bigInt) {

        WifiLock payload = new WifiLock();
        payload.setOwnerId(wifiLock.getOwnerId());
        payload.setSlotKey(wifiLock.getSlotKey());
        payload.setSlotId(String.valueOf(bigInt));

        ServiceManager.getInstance().post(ServiceUrl.WIFI_LOCK_ALL_KEYS, payload, new ResponseHandler() {
            @Override
            public void onSuccess(Object data) {


                WifiLockResponse mWifiLockResponse = new Gson().fromJson(data.toString(), WifiLockResponse.class);

                doAccessLogRequest(wifiLock);

                parseWifiResponse(mWifiLockResponse);
            }

            @Override
            public void onAuthError(String message) {

            }

            @Override
            public void onError(String message) {

            }
        });
    }

    private void parseWifiResponse(WifiLockResponse response) {

        /*Lock mLock = new Lock();
        //Owner IDS
        ArrayList<LockKeys> alLockIds = new ArrayList<>();
        //Owner ID 0
        alLockIds.add(new LockKeys(response.getResponse().getOwnerId0(), "00"));
        mLock.setLockIds(alLockIds);
        //Owner ID 1
        alLockIds.add(new LockKeys(response.getResponse().getOwnerId1(), "01"));
        mLock.setLockIds(alLockIds);*/

        String key = AESEncryption.getInstance().encrypt(response.getResponse().getOwnerId0(), mLock.isEncrypted());
        Logger.d("###  parseWifiResponse encrypt Key = " + key);
        Logger.d("###  parseWifiResponse Key = " + response.getResponse().getOwnerId0());

        if (mLock.getUnAssignedKeys() != null && mLock.getUnAssignedKeys().get(0).getSlotNumber().equalsIgnoreCase("01")) {

            key = AESEncryption.getInstance().encrypt(response.getResponse().getOwnerId1(), mLock.isEncrypted());
        }

        storeKeys(key);
    }

    private void doDateTimeRequest(String id, String key, int slotNumber, LockFlow mLockFlow,
                                   WifiLock wifiLock, LockFlow finalMLockFlow, int finalSlotNumber) {

        String dateString = DateTimeUtils.getCurrentGMTTime(DDMMYY_HHMMSS);

        Logger.d("### doDateTimeRequest dateString " + dateString);

        String[] dateAndTime = dateString.split(" ");

        Logger.d("### doDateTimeRequest date   " + dateAndTime[0]);
        Logger.d("### doDateTimeRequest time   " + dateAndTime[1]);

        wifiLock.setDate(dateAndTime[0]);
        wifiLock.setTime(dateAndTime[1]);

        ServiceManager.getInstance().post(ServiceUrl.WIFI_LOCK_DATE_TIME, wifiLock, new ResponseHandler() {
            @Override
            public void onSuccess(Object data) {

                WifiLockResponse mWifiLockResponse = new Gson().fromJson(data.toString(), WifiLockResponse.class);

                if (mWifiLockResponse.getStatus().equalsIgnoreCase(Constant.SUCCESS)) {

                    if (mLock.getLockVersion().equalsIgnoreCase(HW_VERSION_6_0)) {
                        doDisEngage(id, key, slotNumber, mLockFlow, wifiLock, finalMLockFlow, finalSlotNumber);
                    } else {
                        doBatteryRequest(id, key, slotNumber, mLockFlow, wifiLock, finalMLockFlow, finalSlotNumber);
                    }
                }

            }

            @Override
            public void onAuthError(String message) {

            }

            @Override
            public void onError(String message) {

                Loader.getInstance().hideLoader();
                AppDialog.showAlertDialog(getContext(), "Unable to connect the lock. Please try again later.");

            }
        });


    }

    private void doBatteryRequest(String id, String key, int slotNumber, LockFlow mLockFlow, WifiLock wifiLock,
                                  LockFlow finalMLockFlow, int finalSlotNumber) {

        ServiceManager.getInstance().post(ServiceUrl.WIFI_LOCK_BATTERY, wifiLock, new ResponseHandler() {
            @Override
            public void onSuccess(Object data) {

                WifiLockResponse mWifiLockResponse = new Gson().fromJson(data.toString(), WifiLockResponse.class);

                doDisEngage(id, key, slotNumber, mLockFlow, wifiLock, finalMLockFlow, finalSlotNumber);

                if (mWifiLockResponse != null && mWifiLockResponse.getResponse()
                        != null && mWifiLockResponse.getResponse().getBattery() != null) {
                    if (mLock != null) {
                        //In Wifi we get the battery value as string instead of hex value.
                        mLock.setBattery(mWifiLockResponse.getResponse().getBattery());
                    }
                    doServiceRequest();
                    updateBattery();
                }


            }

            @Override
            public void onAuthError(String message) {

            }

            @Override
            public void onError(String message) {

                doDisEngage(id, key, slotNumber, mLockFlow, wifiLock, finalMLockFlow, finalSlotNumber);

            }
        });
    }

    private void doDisEngage(String id, String key, int slotNumber, LockFlow mLockFlow, WifiLock mWifiLock,
                             LockFlow finalMLockFlow, int finalSlotNumber) {

        ServiceManager.getInstance().post(ServiceUrl.WIFI_LOCK_DISENGAGE, mWifiLock, new ResponseHandler() {
            @Override
            public void onSuccess(Object data) {
                Loader.getInstance().hideLoader();
                WifiLockResponse mWifiLockResponse = new Gson().fromJson(data.toString(), WifiLockResponse.class);
                if (getContext() != null) {
                    if (mWifiLockResponse != null) {
                        AppDialog.showAlertDialog(getActivity(), "Lock engaged successfully.");
                        //do Rewrite Slot Key

                        if (finalMLockFlow == LockFlow.TRANSFER_OWNER) {
                            BigInteger bigInt = BigInteger.valueOf(finalSlotNumber);

                            //doReWriteSlotKeysRequest(mWifiLock);
                            Logger.d("### if DISENGAGE Success doReadAllKeys");
                            doReadAllKeys(mWifiLock, bigInt);

                        } else {
                            //Get Activity History
                            //doReadAllKeys(mWifiLock); // Temp
                            Logger.d("### else DISENGAGE Success and make doAccessLogRequest");
                            doAccessLogRequest(mWifiLock);
                        }

                    } else {
                        AppDialog.showAlertDialog(getContext(), "Invalid response from Lock. Please try again later.");
                    }
                }
            }

            @Override
            public void onAuthError(String message) {
                Loader.getInstance().hideLoader();
                AppDialog.showAlertDialog(getContext(), "Invalid Lock Key. Please contact support.");
            }

            @Override
            public void onError(String message) {
                ENGAGE_ATTEMPT_COUNT++;
                if (message != null && message.contains("INSUF_AUTHORIZATION")) {
                    AppDialog.showAlertDialog(getContext(), "Unable to connect the lock. Please try again later.");
                } else {
                    if (ENGAGE_ATTEMPT_COUNT > 2 || (!WifiLockManager.getInstance().isWifiLockConnected(getContext()))) {
                        ENGAGE_ATTEMPT_COUNT = 1;
                        Loader.getInstance().hideLoader();
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
                                engageLockWithWifi(id, key, slotNumber, mLockFlow);
                            }
                        }, 1500);
                    }
                }
            }
        });
    }

    private void doServiceRequest(final boolean isGoBack) {

        if (ServiceManager.getInstance().isNetworkAvailable(getContext()) && mLock.getId() != null) {
            LockListService.getInstance().updateLock(mLock, new ResponseHandler() {
                @Override
                public void onSuccess(Object data) {
                    Loader.getInstance().hideLoader();
                    if (isGoBack && getActivity() != null) {
                        requireActivity().getOnBackPressedDispatcher().onBackPressed();
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
                    AppDialog.showAlertDialog(getContext(), message);
                }
            });
        } else {
            //Add to offline list for update.
            Loader.getInstance().hideLoader();
            //boolean isLockFound = false;
            /*Locks mLocks = StorageManager.getInstance().getOfflineLocks();
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
            LockDBClient.getInstance().save(mLock, getContext());
            if (getActivity() != null) {
                //SyncScheduler.getInstance().schedule(getActivity(), Constant.JOBS.ALL);
                if (isGoBack) {
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            }
        }
    }

    private void doHistoryUpdate() {

        if ((ServiceManager.getInstance().isNetworkAvailable(getActivity()) ||
                ServiceManager.getInstance().isMobileDataEnabled(getContext())) && mLock.getId() != null) {

            AccessLogList data = SecuredStorageManager.getInstance().getAccessLogs(mLock.getSerialNumber(), mLock.getId());

            HistoryService.getInstance().serviceRequest(mLock.getId(), data, new ResponseHandler() {
                @Override
                public void onSuccess(Object data) {
                    AccessLogResponse response = (AccessLogResponse) data;
                    if (response != null && response.getStatus().equalsIgnoreCase("success")) {
                        SecuredStorageManager.getInstance().removeSyncedLog(mLock.getId());
                    }
                }

                @Override
                public void onAuthError(String message) {

                }

                @Override
                public void onError(String message) {

                }
            });
        } /*else {
            SyncScheduler.getInstance().schedule(getContext(), Constant.JOBS.ALL);
        }*/
    }

    LockKeys mLockKeys = null;

    private void storeKeys(String key) {

        mLockKeys = new LockKeys();
        if (mLock.getUnAssignedKeys() != null && mLock.getUnAssignedKeys().size() > 0) {
            mLockKeys.setId(mLock.getUnAssignedKeys().get(0).getId());
            mLockKeys.setUserId("");
            mLockKeys.setStatus("0");
        }

        mLockKeys.setKey(key);

        if (ServiceManager.getInstance().isNetworkAvailable(getActivity()) && mLock.getId() != null) {

            LockKeyService.getInstance().transferKeys(mLockKeys, new ResponseHandler() {
                @Override
                public void onSuccess(Object data) {
                    try {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        //Update the Lock Status for Owner ID.
                                        for (int i = 0; i < mLock.getLockKeys().size(); i++) {
                                            if (mLock.getLockKeys().get(i).getUserType().equalsIgnoreCase(Constant.OWNER_ID)) {
                                                mLock.getLockKeys().get(i).setStatus(Constant.ACTIVE);
                                                break;
                                            }
                                        }
                                        updateUI();
                                    } catch (Exception e) {
                                        Logger.e(e);
                                    }
                                }
                            });
                        }
                    } catch (Exception e) {
                        Logger.e(e);
                    }

                }

                @Override
                public void onAuthError(String message) {
                    // Push new key to local storage
                    addOfflineKeys(mLockKeys);
                }

                @Override
                public void onError(String message) {
                    // Push new key to local storage
                    addOfflineKeys(mLockKeys);
                }
            }, mLockKeys.getKey());
        } else {
            // Push new key to local storage
            addOfflineKeys(mLockKeys);
        }
    }

    private void hideKeyboard() {
        try {
            if (getActivity() != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etDoorTitle.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
            }
        } catch (Exception e) {
            Logger.e(e);
        }
    }

    private boolean validateDoorName() {
        String name = etDoorTitle.getText().toString();
        if (name.isEmpty()) {
            doorTitleWrapper.setError("DoorName is mandatory");
            return false;
        }
        doorTitleWrapper.setErrorEnabled(false);
        return true;
    }

    private void setBleContainer(boolean isVisible) {
        if (isVisible) {
            rbWifi.setChecked(false);
            rbBle.setChecked(true);
        } else {
            rbWifi.setChecked(true);
            rbBle.setChecked(false);
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
                case R.id.et_door_title:
                    validateDoorName();
                    break;
            }
        }
    }

}
