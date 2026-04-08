package com.payoda.smartlock.locks;


import static com.payoda.smartlock.constants.Constant.HW_VERSION_6_0;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.payoda.smartlock.App;
import com.payoda.smartlock.BuildConfig;
import com.payoda.smartlock.R;
import com.payoda.smartlock.authentication.BaseFragment;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.constants.ServiceUrl;
import com.payoda.smartlock.locks.adapter.DeviceListAdapter;
import com.payoda.smartlock.locks.adapter.WifiListAdapter;
import com.payoda.smartlock.locks.callback.IKeyListener;
import com.payoda.smartlock.locks.model.Lock;
import com.payoda.smartlock.locks.model.LockAddResponse;
import com.payoda.smartlock.locks.model.LockKeys;
import com.payoda.smartlock.locks.model.WifiLock;
import com.payoda.smartlock.locks.model.WifiLockResponse;
import com.payoda.smartlock.locks.service.LockAddService;
import com.payoda.smartlock.plugins.bluetooth.BleDevice;
import com.payoda.smartlock.plugins.bluetooth.BleManager;
import com.payoda.smartlock.plugins.bluetooth.BleResponseHandler;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.plugins.storage.SecuredStorageManager;
import com.payoda.smartlock.plugins.storage.lock.LockDBClient;
import com.payoda.smartlock.plugins.wifi.WifiLockManager;
import com.payoda.smartlock.plugins.wifi.WifiUtilManager;
import com.payoda.smartlock.service.AESEncryption;
import com.payoda.smartlock.service.SyncScheduler;
import com.payoda.smartlock.splash.model.BrandInfoResponse;
import com.payoda.smartlock.utils.AX100Util;
import com.payoda.smartlock.utils.AppDialog;
import com.payoda.smartlock.utils.HexUtils;
import com.payoda.smartlock.utils.Loader;
import com.payoda.smartlock.utils.Logger;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 */
public class LockAddFragment extends BaseFragment implements View.OnClickListener {

    private EditText et_lockName, et_lockCode, etLockUuid;
    private TextInputLayout locknameWrapper, lockcodeWrapper, lockSerialNumberWrapper;
    private LinearLayout layoutWifiScanResult;
    private AppCompatSpinner spDeviceList, spWifiScanResult;
    private DeviceListAdapter mDeviceListAdapter;
    private WifiListAdapter wifiListAdapter;
    private RadioButton rbWifi, rbBle;
    private LinearLayout llBleContainer;
    private Lock mLock;

    private ArrayList<String> wifiScanResult = new ArrayList<>();
    private final int REQ_ADD = 2;

    private ArrayList<String> triedBLEList = new ArrayList<>();
    private ArrayList<String> triedWIFIList = new ArrayList<>();

    private BleDevice currentDevice = null;
    private String previousLockCode = "";
    private boolean isEncrypt = true;
    private ArrayList<Lock> offLineLockList = new ArrayList<>();
    private boolean isLockAddedFlow = false;
    private boolean isTimerEnd = false;
    private boolean isActive = false;

    int scratchCodeLen = BuildConfig.SCRATCH_CODE_LENGTH;
    String scratchCodePrefix = BuildConfig.SCRATCH_CODE_PREFIX;

    public LockAddFragment() {
        // Required empty public constructor
    }

    public static LockAddFragment getInstance() {
        return new LockAddFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        triedBLEList.clear();
        triedWIFIList.clear();
        Logger.d("### LockAddFragment");
        return initialiseView(inflater.inflate(R.layout.fragment_lock_add, container, false));

    }

    @Override
    public void onResume() {
        super.onResume();
        //Check WifiLock
        isActive = true;
        setBleContainer(!WifiLockManager.getInstance().isWifiLockConnected(getContext()));
        lockAddViaWifiSettings();
    }

    @Override
    public void onPause() {
        super.onPause();
        isActive = false;
    }

    private void lockAddViaWifiSettings() {
        if (!AX100Util.isNotEqualAndAboveQ() && isLockAddedFlow) {
            Loader.getInstance().showLoader(getContext());
            isLockAddedFlow = false;
            if (WifiLockManager.getInstance().isWifiLockConnected(getContext())) {
                setBleContainer(false);
                doAddLockViaWifi();
            } else if (isTimerEnd) {
                Loader.getInstance().hideLoader();
            }
        }
    }

    private View initialiseView(View view) {
        /*ToolBar Start*/
        ((TextView) view.findViewById(R.id.tv_title)).setText(getString(R.string.new_lock));
        view.findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();

            }
        });
        /*ToolBar End*/
        locknameWrapper = view.findViewById(R.id.locknameWrapper);
        lockcodeWrapper = view.findViewById(R.id.lockcodeWrapper);
        lockSerialNumberWrapper = view.findViewById(R.id.lockSerialNumberWrapper);

        et_lockName = view.findViewById(R.id.et_lockName);
        et_lockCode = view.findViewById(R.id.et_lockCode);

        // Retrieve branding information
        BrandInfoResponse.BrandInfo brandInfo = SecuredStorageManager.getInstance().getBrandInfo();
        if (brandInfo != null) {
            scratchCodeLen = brandInfo.getScratchCodeLength();
            scratchCodePrefix = brandInfo.getScratchCodePrefix();
        }
        et_lockCode.setFilters(new InputFilter[]{new InputFilter.LengthFilter(scratchCodeLen)});

        etLockUuid = view.findViewById(R.id.et_lock_uuid);
        spDeviceList = view.findViewById(R.id.sp_device_list);
        spWifiScanResult = view.findViewById(R.id.spWifiScanResult);
        layoutWifiScanResult = view.findViewById(R.id.layoutWifiScanResult);
        if (getContext() != null) {
            mDeviceListAdapter = new DeviceListAdapter(getContext(), null);
            wifiScanResult.add(getString(R.string.please_select_wifi));
            wifiListAdapter = new WifiListAdapter(getContext(), wifiScanResult);

        }
        spDeviceList.setAdapter(mDeviceListAdapter);
        spWifiScanResult.setAdapter(wifiListAdapter);
        spWifiScanResult.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        et_lockName.addTextChangedListener(new MyTextWatcher(et_lockName));
        et_lockCode.addTextChangedListener(new MyTextWatcher(et_lockCode));

        rbWifi = view.findViewById(R.id.rb_wifi);
        rbWifi.setOnClickListener(this);
        rbBle = view.findViewById(R.id.rb_ble);
        rbBle.setOnClickListener(this);
        llBleContainer = view.findViewById(R.id.ll_ble_container);

        Button btnScan = view.findViewById(R.id.btnScan);
        btnScan.setOnClickListener(this);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(this);
        Button btnAdd = view.findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(this);
        loadLocal();
        clearText();
        return view;
    }

    private void loadLocal() {

        LockDBClient.getInstance().getAll(getContext(), new LockDBClient.DBCallback() {
            @Override
            public void onLockList(ArrayList<Lock> lockList) {
                if (lockList != null) {
                    offLineLockList = lockList;
                }
            }

            @Override
            public void onSuccess(String msg) {

            }
        });
    }

    private boolean validateLockName() {
        String name = et_lockName.getText().toString();
        if (name.isEmpty()) {
            locknameWrapper.setError("Please set a lock name.");
            return false;
        } else if (name.length() > 60) {
            locknameWrapper.setError("Lock name should not be more than 60 characters.");
            return false;
        }
        locknameWrapper.setErrorEnabled(false);
        return true;
    }

    private boolean validateLockCode() {
        String lock = et_lockCode.getText().toString();
        if (lock.isEmpty()) {
            lockcodeWrapper.setError("Lock code is mandatory");
            return false;
        } else if (scratchCodeLen == 10 && (lock.length() < 10)) {
            lockcodeWrapper.setError("Please set a 10 digit lock code");
            return false;
        } else if (scratchCodeLen == 10 && (lock.length() == 10)) {
            String lockPrefix = lock.substring(0, 2);
            if (!scratchCodePrefix.equalsIgnoreCase(lockPrefix)) {
                lockcodeWrapper.setError("Invalid Lock code. Please enter a valid lock code.");
                return false;
            }
        } else if (scratchCodeLen == 9 && lock.length() < 8) {
            lockcodeWrapper.setError("Please set a 8 or 9 digit lock code");
            return false;
        }
        lockcodeWrapper.setErrorEnabled(false);
        return true;
    }

    private boolean validateSerialNumber() {
        String lock = etLockUuid.getText().toString();
        if (lock.isEmpty()) {
            lockSerialNumberWrapper.setError(getString(R.string.please_select_lock));
            return false;
        }
        lockSerialNumberWrapper.setErrorEnabled(false);
        return true;
    }

    private long getWifiScanTime() {

        long WIFI_SCAN_TIME = 13000;
        String activationCode = et_lockCode.getText().toString();

        if (activationCode.length() == 9 || activationCode.length() == 10) {
            WIFI_SCAN_TIME = 20000;
        }

        return WIFI_SCAN_TIME;
    }

    private String getActivationCode() {
        String activationCode = et_lockCode.getText().toString();
        if (scratchCodeLen == 10 && activationCode.length() == 10) {
            activationCode = activationCode.substring(2);
        } else if (scratchCodeLen == 9 && activationCode.length() == 9) {
            activationCode = activationCode.substring(1);
        }
        return activationCode;
    }

    public void disableError() {
        locknameWrapper.setErrorEnabled(false);
        lockcodeWrapper.setErrorEnabled(false);
        lockSerialNumberWrapper.setErrorEnabled(false);
    }

    private void resetDevice() {
        ArrayList<BleDevice> deviceList = new ArrayList<>();
        deviceList.add(new BleDevice(-1, getString(R.string.please_select_lock)));
        mDeviceListAdapter.setItemList(deviceList);
        spDeviceList.setAdapter(mDeviceListAdapter);
    }

    public void clearText() {
        //llBleContainer.setVisibility(View.GONE);
        et_lockName.getText().clear();
        et_lockCode.getText().clear();
        etLockUuid.getText().clear();
        ArrayList<BleDevice> deviceList = new ArrayList<>();
        deviceList.add(new BleDevice(-1, getString(R.string.please_select_lock)));
        mDeviceListAdapter.setItemList(deviceList);
        disableError();
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

    private void doAddLockViaBLE() {
        if (validateSerialNumber()) {
            //Communicate with lock and get the details
            if (mLock == null) {
                //String activationCode=HexUtils.stringToHex(et_lockCode.getText().toString());
                String activationCode = getActivationCode();
                String ssid = BleManager.MANUFACTURER_CODE + mLock.getSerialNumber();

                LockManager.getInstance().activateDevice(getActivity(), etLockUuid.getText().toString(), ssid, activationCode, new IKeyListener() {

                    @Override
                    public void onLockIds(ArrayList<String> alIds) {

                        if (mLock == null)
                            mLock = new Lock();
                        if (alIds != null && alIds.size() == LockManager.TOTAL_IDS) {
                            ArrayList<LockKeys> alLockIds = new ArrayList<>();
                            LockKeys mKeys;
                            for (int i = 0; i < alIds.size(); i++) {
                                mKeys = new LockKeys();
                                mKeys.setKey(AESEncryption.getInstance().encrypt(alIds.get(i), isEncrypt));
                                BigInteger bigInt = BigInteger.valueOf(i);
                                mKeys.setSlotNumber(String.valueOf(bigInt));
                                alLockIds.add(mKeys);
                            }
                            mLock.setLockIds(alLockIds);
                        }
                        mLock.setLockVersion(Constant.HW_VERSION_1);
                    }

                    @Override
                    public void onLockKeys(ArrayList<String> alKeys) {
                        if (mLock == null) mLock = new Lock();
                        if (alKeys != null && alKeys.size() == LockManager.TOTAL_KEYS) {
                            ArrayList<LockKeys> alLockKeys = new ArrayList<>();
                            LockKeys mKeys;
                            for (int i = 0; i < alKeys.size(); i++) {
                                mKeys = new LockKeys();
                                mKeys.setKey(AESEncryption.getInstance().encrypt(alKeys.get(i), isEncrypt));
                                BigInteger bigInt = BigInteger.valueOf(i);
                                mKeys.setSlotNumber(String.valueOf(bigInt));
                                alLockKeys.add(mKeys);
                            }
                            mLock.setLockKeys(alLockKeys);
                        }
                    }

                    @Override
                    public void onBatteryUpdate(String hwVersion) {
                        if (mLock == null)
                            mLock = new Lock();
                        String lockVersion = HexUtils.hexToString(hwVersion).trim();
                        if (lockVersion != null) {
                            if (lockVersion.equalsIgnoreCase(Constant.HW_VERSION_2)
                                    || (lockVersion.equalsIgnoreCase(Constant.HW_VERSION_2_1)
                                    || lockVersion.equalsIgnoreCase(Constant.HW_VERSION_3)
                                    || (lockVersion.equalsIgnoreCase(Constant.HW_VERSION_3_1)
                                    || (lockVersion.equalsIgnoreCase(Constant.HW_VERSION_3_2)
                                    || (lockVersion.equalsIgnoreCase(Constant.HW_VERSION_4_0))
                                    || (lockVersion.equalsIgnoreCase(Constant.HW_VERSION_6_0))

                            )))) {
                                mLock.setLockVersion(lockVersion);
                                // RFID's
                                ArrayList<LockKeys> rfids = new ArrayList<>();
                                rfids.add(new LockKeys("RFID 1", "0", "0"));
                                rfids.add(new LockKeys("RFID 2", "1", "1"));
                                rfids.add(new LockKeys("RFID 3", "2", "2"));
                                mLock.setRfids(rfids);
                            } else {
                                mLock.setLockVersion(lockVersion);
                            }
                        }
                    }

                    @Override
                    public void onAccessLog(ArrayList<String> alLogs) {

                    }

                    @Override
                    public void onLockActivated() {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Loader.getInstance().hideLoader();
                                mLock.setName(et_lockName.getText().toString());
                                String activationCode = getActivationCode();
                                mLock.setScratchCode(activationCode);
                                mLock.setUuid(currentDevice.getAddress());
                                mLock.setSsid(currentDevice.getName());
                                mLock.setSerialNumber(currentDevice.getName());
                                mLock.setIsEncrypted("1");
                                mLock.setOffline(true);
                                addLock(mLock);
                                clearText();
                                disableError();

                            }
                        });

                    }

                    @Override
                    public void onDeviceNotConnected() {
                        triedBLEList.add(currentDevice.getName());
                        Loader.getInstance().hideLoader();
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (ServiceManager.getInstance().isMobileDataEnabled(getContext())) {
                                        AppDialog.showAlertDialog(getContext(), "Please switch-off your mobile data and try again");
                                    } else {
                                        AppDialog.showAlertDialog(getContext(), getString(R.string.lock_failure));
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onMacAddressUpdate(String ssid, String macAddress) {
                    }
                });
            } else {

                Loader.getInstance().hideLoader();

                mLock.setName(et_lockName.getText().toString());
                String activationCode = getActivationCode();
                mLock.setScratchCode(activationCode);
                mLock.setUuid(etLockUuid.getText().toString());
                mLock.setSerialNumber(currentDevice.getName());
                mLock.setSsid(mLock.getSerialNumber());
                mLock.setIsEncrypted("1");
                mLock.setOffline(true);
                addLock(mLock);
                clearText();
            }
        } else {
            Loader.getInstance().hideLoader();
            AppDialog.showAlertDialog(getContext(), getString(R.string.please_select_lock));
        }
    }

    private boolean isLockAddingViaWifi = false;

    private void doAddLockViaWifi() {
        String activationCode = getActivationCode();
        activationCode = HexUtils.stringToHex(activationCode);

        if (!WifiLockManager.getInstance().isWifiEnabled(getContext())) {
            AppDialog.showAlertDialog(getContext(), getString(R.string.turn_on_wifi_connect_lock));
        } else {
            isLockAddingViaWifi = true;
            final WifiLock mWifiLock = new WifiLock();
            mWifiLock.setActivationCode(activationCode);
            ServiceManager.getInstance().post(ServiceUrl.WIFI_LOCK_ACTIVATE, mWifiLock, new ResponseHandler() {
                @Override
                public void onSuccess(Object data) {
                    Logger.d("Add Lock data -->", data.toString());
                    WifiLockResponse mWifiLockResponse = new Gson().fromJson(data.toString(), WifiLockResponse.class);
                    if (mWifiLockResponse != null && getContext() != null && mWifiLockResponse.getStatus() != null
                            && (!mWifiLockResponse.getStatus().equalsIgnoreCase("error"))) {
                        Loader.getInstance().hideLoader();
                        mWifiLockResponse.setSsid(WifiLockManager.getInstance().getSsid(getContext()));
                        Lock lock = parseWifiResponse(mWifiLockResponse);
                        //Deactivate lock
                        ServiceManager.getInstance().post(ServiceUrl.WIFI_LOCK_DEACTIVATE, mWifiLock, new ResponseHandler() {
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

                        forgetWIFINetwork(getContext(), lock);


                    } else {
                        AppDialog.showAlertDialog(getContext(), "Invalid response from Lock. Please ensure that is lock already added by someone.");
                    }
                }

                @Override
                public void onAuthError(String message) {
                    triedWIFIList.add(wifiSerialNumber);
                    Loader.getInstance().hideLoader();
                    AppDialog.showAlertDialog(getContext(), message);
                }

                @Override
                public void onError(String message) {
                    ENGAGE_ATTEMPT_COUNT++;
                    Loader.getInstance().hideLoader();
                    if (ENGAGE_ATTEMPT_COUNT > 2 || (!WifiLockManager.getInstance().isWifiLockConnected(getContext()))) {
                        triedWIFIList.add(wifiSerialNumber);
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
                                doAddLockViaWifi();

                            }
                        }, 1500);
                    }

                }
            });
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

    private int ENGAGE_ATTEMPT_COUNT = 1;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAdd:
                //doAddLock();
                doAddLockNewFlow();
                break;
            case R.id.btnScan:

                break;
            case R.id.btnCancel:
                BleManager.getInstance().stopScan();
                clearText();
                disableError();
                if (getActivity() != null)
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                break;
            case R.id.rb_wifi:
                setBleContainer(false);
                break;
            case R.id.rb_ble:
                setBleContainer(true);
                break;
        }
    }

    private void registerNetworkCallBack() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkRequest.Builder builder = new NetworkRequest.Builder();

        connectivityManager.registerNetworkCallback(builder.build(),
                new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(Network network) {
                        //Do your work here or restart your activity
                    }

                    @Override
                    public void onLost(Network network) {
                        //internet lost
                    }
                });
    }

    private void doAddLock() {

        if (!validateLockName()) {
            return;
        }
        if (!validateLockCode()) {
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

        AppDialog.showAlertDialog(getContext(), "Activate Lock", msg, "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getLocationPermission(new BaseFragment.RequestPermissionAction() {
                            @Override
                            public void permissionDenied() {

                            }

                            @Override
                            public void permissionGranted() {
                                addLockFlow();
                            }
                        });

                    }
                }, "CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
    }

    private void doAddLockNewFlow() {
        if (!validateLockName()) {
            return;
        }
        if (!validateLockCode()) {
            return;
        }

        String activationCode = getActivationCode();
        String lockName = et_lockName.getText().toString();
        Lock lock = new Lock();
        lock.setName(lockName);
        lock.setScratchCode(activationCode);

        Loader.getInstance().showLoader(getContext());

        LockAddService.getInstance().addNewLock(lock, new ResponseHandler() {
            @Override
            public void onSuccess(Object value) {
                Loader.getInstance().hideLoader();
                LockAddResponse lockAddResponse = (LockAddResponse) value;
                if (lockAddResponse != null && lockAddResponse.getStatus() != null &&
                        lockAddResponse.getStatus().equalsIgnoreCase("success")) {
                    Lock lockData = lockAddResponse.getData();
                    AppDialog.showAlertDialog(getContext(), "Lock added Successfully.", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (getActivity() != null) {
                                requireActivity().getOnBackPressedDispatcher().onBackPressed();
                                if (lockAddResponse.getData().getLockVersion().equalsIgnoreCase(Constant.HW_VERSION_4_0)) {
                                    App.getInstance().showFullScreen(getActivity(), Constant.SCREEN.WIFI_MQTT_CONFIGURATION,
                                            new Gson().toJson(lockData));
                                }
                            }
                        }
                    });
                }
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
    }

    private void addLockFlow() {
        isLockAddingViaWifi = false;
        if (WifiLockManager.getInstance().isWifiLockConnected(getContext())) {
            setBleContainer(false);
            doAddLockViaWifi();
        } else {
            long WIFI_SCAN_TIME = getWifiScanTime();
            if (AX100Util.isNotEqualAndAboveQ()) {
                Loader.getInstance().showLoader(getContext());
                WifiUtilManager wifiUtilManager = new WifiUtilManager(getContext(), new WifiUtilManager.WifiListener() {
                    @Override
                    public void connectionSuccess() {
                        registerNetworkCallBack();
                        doAddLockViaWifi();
                    }

                    @Override
                    public void connectionTimeOut() {
                        doScanBLE();
                    }
                });
                wifiUtilManager.startSearching(WIFI_SCAN_TIME, getActivationCode());
            } else {
                isTimerEnd = false;
                Logger.d("addLockFlow wifi time --> ", String.valueOf(WIFI_SCAN_TIME));
                new CountDownTimer(WIFI_SCAN_TIME, 1000) {
                    public void onTick(long millisUntilFinished) {
                        Logger.d("onTick");
                    }

                    public void onFinish() {
                        Logger.d("WIFI Scan Time End");
                        if (isActive) {
                            doScanBLE();
                        } else {
                            isTimerEnd = true;
                            Loader.getInstance().hideLoader();
                        }
                    }

                }.start();
                Loader.getInstance().showLoader(getContext());
                AppDialog.showAlertDialog(getContext(), "Activate Lock", getString(R.string.lock_add_security),
                        "OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                isLockAddedFlow = true;
                                Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
                                startActivity(panelIntent);

                            }
                        }, "CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Loader.getInstance().showLoader(getContext());
                            }
                        });

            }
        }

    }

    private Lock parseWifiResponse(WifiLockResponse response) {
        AESEncryption aesEncryption = AESEncryption.getInstance();
        Lock mLock = new Lock();
        //Owner IDS
        ArrayList<LockKeys> alLockIds = new ArrayList<>();
        //Owner ID 0
        alLockIds.add(new LockKeys(aesEncryption.encrypt(response.getResponse().getOwnerId0(), isEncrypt), "00"));
        //Owner ID 1
        alLockIds.add(new LockKeys(aesEncryption.encrypt(response.getResponse().getOwnerId1(), isEncrypt), "01"));
        mLock.setLockIds(alLockIds);

        //Keys IDS
        ArrayList<LockKeys> alLockKeys = new ArrayList<>();

        alLockKeys.add(new LockKeys(aesEncryption.encrypt(response.getResponse().getSlotKey0(), isEncrypt), "00"));
        alLockKeys.add(new LockKeys(aesEncryption.encrypt(response.getResponse().getSlotKey1(), isEncrypt), "01"));
        alLockKeys.add(new LockKeys(aesEncryption.encrypt(response.getResponse().getSlotKey2(), isEncrypt), "02"));
        alLockKeys.add(new LockKeys(aesEncryption.encrypt(response.getResponse().getSlotKey3(), isEncrypt), "03"));
        alLockKeys.add(new LockKeys(aesEncryption.encrypt(response.getResponse().getSlotKey4(), isEncrypt), "04"));
        alLockKeys.add(new LockKeys(aesEncryption.encrypt(response.getResponse().getSlotKey5(), isEncrypt), "05"));
        alLockKeys.add(new LockKeys(aesEncryption.encrypt(response.getResponse().getSlotKey6(), isEncrypt), "06"));
        alLockKeys.add(new LockKeys(aesEncryption.encrypt(response.getResponse().getSlotKey7(), isEncrypt), "07"));
        alLockKeys.add(new LockKeys(aesEncryption.encrypt(response.getResponse().getSlotKey8(), isEncrypt), "08"));
        alLockKeys.add(new LockKeys(aesEncryption.encrypt(response.getResponse().getSlotKey9(), isEncrypt), "09"));
        alLockKeys.add(new LockKeys(aesEncryption.encrypt(response.getResponse().getSlotKey10(), isEncrypt), "10"));
        alLockKeys.add(new LockKeys(aesEncryption.encrypt(response.getResponse().getSlotKey11(), isEncrypt), "11"));
        alLockKeys.add(new LockKeys(aesEncryption.encrypt(response.getResponse().getSlotKey12(), isEncrypt), "12"));
        alLockKeys.add(new LockKeys(aesEncryption.encrypt(response.getResponse().getSlotKey13(), isEncrypt), "13"));
        alLockKeys.add(new LockKeys(aesEncryption.encrypt(response.getResponse().getSlotKey14(), isEncrypt), "14"));
        alLockKeys.add(new LockKeys(aesEncryption.encrypt(response.getResponse().getSlotKey15(), isEncrypt), "15"));
        alLockKeys.add(new LockKeys(aesEncryption.encrypt(response.getResponse().getSlotKey16(), isEncrypt), "16"));
        alLockKeys.add(new LockKeys(aesEncryption.encrypt(response.getResponse().getSlotKey17(), isEncrypt), "17"));
        alLockKeys.add(new LockKeys(aesEncryption.encrypt(response.getResponse().getSlotKey18(), isEncrypt), "18"));
        alLockKeys.add(new LockKeys(aesEncryption.encrypt(response.getResponse().getSlotKey19(), isEncrypt), "19"));
        alLockKeys.add(new LockKeys(aesEncryption.encrypt(response.getResponse().getSlotKey20(), isEncrypt), "20"));
        alLockKeys.add(new LockKeys(aesEncryption.encrypt(response.getResponse().getSlotKey21(), isEncrypt), "21"));
        alLockKeys.add(new LockKeys(aesEncryption.encrypt(response.getResponse().getSlotKey22(), isEncrypt), "22"));
        alLockKeys.add(new LockKeys(aesEncryption.encrypt(response.getResponse().getSlotKey23(), isEncrypt), "23"));
        alLockKeys.add(new LockKeys(aesEncryption.encrypt(response.getResponse().getSlotKey24(), isEncrypt), "24"));

        mLock.setLockKeys(alLockKeys);

        if (response.getResponse().getHardwareVersion() != null &&
                (response.getResponse().getHardwareVersion().equalsIgnoreCase(Constant.HW_VERSION_2)
                        || (response.getResponse().getHardwareVersion().equalsIgnoreCase(Constant.HW_VERSION_2_1)
                        || response.getResponse().getHardwareVersion().equalsIgnoreCase(Constant.HW_VERSION_3)
                        || (response.getResponse().getHardwareVersion().equalsIgnoreCase(Constant.HW_VERSION_3_1)
                        || response.getResponse().getHardwareVersion().equalsIgnoreCase(Constant.HW_VERSION_3_2)
                        || (response.getResponse().getHardwareVersion().equalsIgnoreCase(Constant.HW_VERSION_4_0))
                        || (response.getResponse().getHardwareVersion().equalsIgnoreCase(Constant.HW_VERSION_6_0))
                )))) {
            mLock.setLockVersion(response.getResponse().getHardwareVersion());
            // RFID's
            ArrayList<LockKeys> rfids = new ArrayList<>();
            rfids.add(new LockKeys("RFID 1", "0", "0"));
            rfids.add(new LockKeys("RFID 2", "1", "1"));
            rfids.add(new LockKeys("RFID 3", "2", "2"));

            mLock.setRfids(rfids);

        } else {
            mLock.setLockVersion(Constant.HW_VERSION_1);
        }

        mLock.setName(et_lockName.getText().toString());
        String activationCode = getActivationCode();
        mLock.setScratchCode(activationCode);
        mLock.setUuid(response.getResponse().getMacAddr());
        String replaceString = BuildConfig.MANUFACTURER_CODE;
        BrandInfoResponse.BrandInfo brandInfo = SecuredStorageManager.getInstance().getBrandInfo();
        if (brandInfo != null && !brandInfo.getManufacturerCode().isEmpty()) {
            replaceString = brandInfo.getManufacturerCode();
        }
        mLock.setSsid(response.getSsid().replace(replaceString, "").replaceAll("^\"|\"$", ""));
        mLock.setSerialNumber(response.getSsid().replace(replaceString, "").replaceAll("^\"|\"$", ""));
        mLock.setStatus(Constant.ACTIVE);
        mLock.setIsEncrypted("1");
        mLock.setOffline(true);
        addLock(mLock);
        clearText();
        return mLock;
    }

    private void addLock(final Lock data) {
        boolean isLockAlreadyExist = false;
        if (data == null) return;

        //In debug mode, we can add duplicate lock.

        for (Lock lock : offLineLockList) {
            if (lock.getSerialNumber().equalsIgnoreCase(data.getSerialNumber())) {
                //throw error
                isLockAlreadyExist = true;
                break;
            }
        }

        if (!isLockAlreadyExist) {

            if (ServiceManager.getInstance().isNetworkAvailable(getActivity())) {
                Loader.getInstance().showLoader(getActivity());
                //Call web service to add new lock
                LockAddService.getInstance().addLock(data, new ResponseHandler() {
                    @Override
                    public void onSuccess(Object value) {
                        Loader.getInstance().hideLoader();
                        LockAddResponse lockAddResponse = (LockAddResponse) value;
                        if (lockAddResponse != null && lockAddResponse.getStatus() != null &&
                                lockAddResponse.getStatus().equalsIgnoreCase("success")) {
                            //Toast.makeText(getContext(), "Id " + lockAddResponse.getData().getId(), Toast.LENGTH_SHORT).show();
                            AppDialog.showAlertDialog(getContext(), "Lock added Successfully.", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    doOfflineAddLock(data, false);
                                }
                            });
                        }

                    }

                    @Override
                    public void onAuthError(String message) {
                        Loader.getInstance().hideLoader();
                        doOfflineAddLock(data, true);
                    }

                    @Override
                    public void onError(String message) {
                        Loader.getInstance().hideLoader();
                        if (message.contains("exists")) {
                            AppDialog.showAlertDialog(getContext(), message);
                        } else {
                            doOfflineAddLock(data, true);
                        }
                    }
                });
            } else {
                Loader.getInstance().hideLoader();
                doOfflineAddLock(data, true);
            }
        } else {
            Loader.getInstance().hideLoader();
            AppDialog.showAlertDialog(getContext(), "Lock already added. Please add other lock.");
        }
    }

    private void doOfflineAddLock(final Lock lock, boolean sync) {
        if (lock != null) {
            lock.setOffline(true);
        }
        lock.setId(Calendar.getInstance().getTimeInMillis() + Constant.OFFLINE_LOCK_ID);
        lock.setSync(false);
        LockDBClient.getInstance().save(lock, getContext());
        if (getActivity() != null) {
            if (sync) {
                SyncScheduler.getInstance().schedule(getActivity(), Constant.JOBS.ALL);
            }
            // Don't close the page for version 4.0
            if (!lock.getLockVersion().equalsIgnoreCase(Constant.HW_VERSION_4_0)) {
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        }
    }

    private boolean isScannedResultAvailable = false;
    private String wifiSerialNumber = "";

    private void scanBleDevices() {
        String status = LockManager.getInstance().preCheckBLE(getActivity());
        if (!status.equalsIgnoreCase("success")) {
            if (status.equalsIgnoreCase("Location permission not enabled.")) {
                BleManager.getInstance().checkLocationPermission(getActivity());
                return;
            } else {
                AppDialog.showAlertDialog(getActivity(), status);
            }
            return;
        }
        isScannedResultAvailable = false;
        BleManager.getInstance().startScan(getContext(), new BleResponseHandler() {
            @Override
            public void onScanResult(final ArrayList<BleDevice> devices) {
                isScannedResultAvailable = true;
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            boolean isNotValidLock = true;
                            if (devices.size() > 0) {
                                for (BleDevice cDevice : devices) {
                                    if (!triedBLEList.contains(cDevice.getAddress()) && isNotValidLock) {
                                        currentDevice = cDevice;
                                        isNotValidLock = false;
                                        etLockUuid.setText(cDevice.getAddress());
                                        doAddLockViaBLE();
                                    }
                                }
                            }
                            if (isNotValidLock) {
                                Loader.getInstance().hideLoader();
                                AppDialog.showAlertDialog(getActivity(), getString(R.string.lock_failure));
                            }
                        }
                    });
                }
            }

            @Override
            public void onTimeOut() {
                if (!isScannedResultAvailable) {
                    Loader.getInstance().hideLoader();
                    AppDialog.showAlertDialog(getActivity(), getString(R.string.lock_failure));
                }
                Logger.d("Timeout");

            }

            @Override
            public void onError(String error) {
                Loader.getInstance().hideLoader();
                AppDialog.showAlertDialog(getContext(), error);
            }

            @Override
            public void onLocationPermissionError(String error) {
                Loader.getInstance().hideLoader();
                AppDialog.showAlertDialog(getContext(), "Please enable location to scan Bluetooth devices.");
            }

            @Override
            public void onPermissionError(String error) {
                Loader.getInstance().hideLoader();
                BleManager.getInstance().checkLocationPermission(getActivity());
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void doScanBLE() {
        if (validateLockCode()) {
            if (!previousLockCode.equalsIgnoreCase(et_lockCode.getText().toString())) {
                triedWIFIList.clear();
                triedBLEList.clear();
                previousLockCode = et_lockCode.getText().toString();
            }
            if (!WifiLockManager.getInstance().isWifiLockConnected(getContext())) {
                //Toast.makeText(getContext(),"SCAN BLE",Toast.LENGTH_LONG).show();
                resetDevice();

                scanBleDevices();
            } else {
                doAddLockViaWifi();
            }
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
                case R.id.et_lockName:
                    validateLockName();
                    break;
                case R.id.et_lockCode:
                    validateLockCode();
                    break;
            }
        }
    }

}
