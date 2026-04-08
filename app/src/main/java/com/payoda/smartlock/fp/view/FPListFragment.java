package com.payoda.smartlock.fp.view;

import static com.payoda.smartlock.constants.Constant.HW_VERSION_4_0;
import static com.payoda.smartlock.constants.Constant.HW_VERSION_6_0;
import static com.payoda.smartlock.constants.Constant.WIFI_DELAY_TIME;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.payoda.smartlock.App;
import com.payoda.smartlock.R;
import com.payoda.smartlock.authentication.BaseFragment;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.constants.ServiceUrl;
import com.payoda.smartlock.fp.adapter.FPListAdapter;
import com.payoda.smartlock.fp.model.FPUser;
import com.payoda.smartlock.fp.model.ManageFpPrivilege;
import com.payoda.smartlock.fp.presenter.FPPresenter;
import com.payoda.smartlock.fp.service.FPService;
import com.payoda.smartlock.locks.LockDetailFragment;
import com.payoda.smartlock.locks.LockManager;
import com.payoda.smartlock.locks.model.Lock;
import com.payoda.smartlock.locks.model.LockKeys;
import com.payoda.smartlock.locks.model.WifiLockResponse;
import com.payoda.smartlock.managepins.model.AuthViaPinFp;
import com.payoda.smartlock.model.BaseResponse;
import com.payoda.smartlock.model.ScreenData;
import com.payoda.smartlock.plugins.bluetooth.BleManager;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.plugins.pushnotification.RemoteDataEvent;
import com.payoda.smartlock.plugins.storage.lock.LockDBClient;
import com.payoda.smartlock.plugins.wifi.WifiLockManager;
import com.payoda.smartlock.plugins.wifi.WifiUtilManager;
import com.payoda.smartlock.service.AESEncryption;
import com.payoda.smartlock.utils.AppDialog;
import com.payoda.smartlock.utils.Loader;
import com.payoda.smartlock.utils.Logger;
import com.payoda.smartlock.utils.SLViewBinder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

public class FPListFragment extends BaseFragment implements SLViewBinder {

    public static final String TAG = "### FPListFragment";

    public static final String AUTHVIA_FP_DISABLED = "AUTHVIA_FP_DISABLED";

    private Switch switchManagePrivilege;
    private FloatingActionButton fabFPAdd;
    private ImageView btnFPAdd;
    private RecyclerView listUser;
    private LinearLayout layoutFpNoAccess, layoutAdd;

    private TextView tvNoFpPrivilege;

    private Lock mLock;
    private FPPresenter presenter;
    private final int REQ_FP_USER_LIST = 1;
    private final int REQ_EDIT_NAME = 2;
    private final int REQ_REVOKE_FP = 3;

    private final int REQ_ADD_FP = 1001;
    int requestCode;

    private ArrayList<LockKeys> fpKeys;
    private LockKeys selectedUser;

    private boolean allowAPICall = true;

    private String userType = Constant.USER;

    ActivityResultLauncher<Intent> resultLauncher;

    public FPListFragment() {
        // Required empty public constructor
    }

    public static FPListFragment getInstance() {
        return new FPListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mLock = new Gson().fromJson(bundle.getString(Constant.SCREEN_DATA), Lock.class);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstance) {
        Logger.d(TAG, TAG);
        return initializeView(inflater.inflate(R.layout.fragment_list_fp, container, false));
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

    private int revokeCount = 0;
    private int numberOfFP = 0;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(RemoteDataEvent remoteDataEvent) {

        if (remoteDataEvent != null && remoteDataEvent.getStatus().equalsIgnoreCase(Constant.SUCCESS)
                && (remoteDataEvent.getCommand().equalsIgnoreCase(Constant.FP_DELETE_COMMAND))) {
            Loader.getInstance().hideLoader();
            revokeCount++;
            getFingerprintList();
            if (numberOfFP == revokeCount) {
                revokeCount = 0;
                numberOfFP = 0;
                FPUser fpUser = new FPUser();
                fpUser.setId(selectedUser.getId());
                fpUser.setUserId(selectedUser.getUserId());
                presenter.doRevokeFingerPrint(getContext(), fpUser, REQ_REVOKE_FP, true);
                AppDialog.showAlertDialog(getContext(), remoteDataEvent.getTitle(), remoteDataEvent.getBody(), (dialogInterface, i) -> {
                    if (getActivity() != null)
                        requireActivity().getOnBackPressedDispatcher().onBackPressed();
                });
            }
        } else if (remoteDataEvent != null && remoteDataEvent.getStatus().equalsIgnoreCase(Constant.SUCCESS)
                && (remoteDataEvent.getCommand().equalsIgnoreCase(Constant.FP_ON_COMMAND)
                || remoteDataEvent.getCommand().equalsIgnoreCase(Constant.FP_OFF_COMMAND))) {

            Loader.getInstance().hideLoader();
            String fpStatus = remoteDataEvent.getCommand().equalsIgnoreCase(Constant.FP_ON_COMMAND) ? "1" : "0";
            LockDetailFragment.mLock.setEnableFp(fpStatus);

            AppDialog.showAlertDialog(getContext(), remoteDataEvent.getTitle(), remoteDataEvent.getBody(), (dialogInterface, i) -> {
                if (getActivity() != null)
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
            });

        } else if (remoteDataEvent != null && remoteDataEvent.getStatus().equalsIgnoreCase(Constant.FAILURE)
                && (remoteDataEvent.getCommand().equalsIgnoreCase(Constant.FP_DELETE_COMMAND)
                || (remoteDataEvent.getCommand().equalsIgnoreCase(Constant.FP_ON_COMMAND)
                || remoteDataEvent.getCommand().equalsIgnoreCase(Constant.FP_OFF_COMMAND)))) {
            Loader.getInstance().hideLoader();
            AppDialog.showAlertDialog(getActivity(), remoteDataEvent.getBody());
        }

    }

    private View initializeView(final View view) {
        presenter = new FPPresenter(this);
        ((TextView) view.findViewById(R.id.tv_title)).setText(getString(R.string.finger_print));
        switchManagePrivilege = view.findViewById(R.id.manageFpSimpleSwitch);
        tvNoFpPrivilege = view.findViewById(R.id.tv_no_fp_access);
        btnFPAdd = view.findViewById(R.id.btnFPAdd);
        fabFPAdd = view.findViewById(R.id.fabFPAdd);
        listUser = view.findViewById(R.id.listUser);
        layoutAdd = view.findViewById(R.id.layoutAdd);
        layoutFpNoAccess = view.findViewById(R.id.layoutNoFpAccess);
        listUser.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        listUser.setLayoutManager(linearLayoutManager);

        startActivityForResult();

        btnFPAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doAddFingerPrint();
            }
        });

        fabFPAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doAddFingerPrint();
            }
        });

        view.findViewById(R.id.iv_back).setOnClickListener(v -> {

            Logger.d("### 1 FPList back");
            requireActivity().getOnBackPressedDispatcher().onBackPressed();

        });

        switchManagePrivilege.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (allowAPICall) {
                if (mLock.getLockVersion().equalsIgnoreCase(HW_VERSION_4_0)) {
                    ManageFpPrivilege manageFpPrivilege = new ManageFpPrivilege();
                    manageFpPrivilege.setEnableFp(isChecked ? "1" : "0");

                    Loader.getInstance().showLoader(getContext());
                    ServiceManager.getInstance().patch(String.format(ServiceUrl.MANAGE_FP_MQTT, mLock.getSerialNumber()), manageFpPrivilege, new ResponseHandler() {
                        @Override
                        public void onSuccess(Object data) {
                            Loader.getInstance().hideLoader();
                        }

                        @Override
                        public void onAuthError(String message) {
                            Loader.getInstance().hideLoader();
                            allowAPICall = false;
                            switchManagePrivilege.setChecked(!isChecked);
                            AppDialog.showAlertDialog(getContext(), message);
                        }

                        @Override
                        public void onError(String message) {
                            Loader.getInstance().hideLoader();
                            allowAPICall = false;
                            switchManagePrivilege.setChecked(!isChecked);
                            AppDialog.showAlertDialog(getContext(), message);
                        }
                    });
                } else {
                    doUpdateFpPrivilege(isChecked);
                }
            } else {
                allowAPICall = true;
            }
        });

        // Need to check this
        showManagePrivilegeSwitch();


        //getFingerprintList();
        return view;

    }

    private void showManagePrivilegeSwitch() {
        if (mLock != null && mLock.getLockKeys() != null && !mLock.getLockKeys().isEmpty()) {

            if (mLock.getLockVersion() != null && (mLock.getLockVersion().equalsIgnoreCase(Constant.HW_VERSION_3)
                    || mLock.getLockVersion().equalsIgnoreCase(Constant.HW_VERSION_3_1)
                    || mLock.getLockVersion().equalsIgnoreCase(Constant.HW_VERSION_3_2)
                    || mLock.getLockVersion().equalsIgnoreCase(Constant.HW_VERSION_4_0)
                    || mLock.getLockVersion().equalsIgnoreCase(Constant.HW_VERSION_6_0)
            )) {
                //String mLockUserType = Constant.USER;
                for (LockKeys lockKeys : mLock.getLockKeys()) {
                    if (!lockKeys.getUserType().equalsIgnoreCase(Constant.OWNER_ID)) {
                        userType = lockKeys.getUserType();
                        break;
                    }
                }
                //checkEnableFpAccessFromLocal(userType);
                boolean fpAccess = mLock.getEnableFp() != null && mLock.getEnableFp().equalsIgnoreCase("1");
                if (userType.equalsIgnoreCase(Constant.OWNER)) {
                    switchManagePrivilege.setVisibility(View.VISIBLE);
                    if (!fpAccess) {
                        allowAPICall = true;
                        switchManagePrivilege.setChecked(false);
                        showAndHideFpPrivilegeControls(Constant.OWNER);
                    } else {
                        allowAPICall = false;
                        switchManagePrivilege.setChecked(true);
                        getFingerprintList();
                    }
                } else if (userType.equalsIgnoreCase(Constant.MASTER)) {
                    if (!fpAccess) {
                        showAndHideFpPrivilegeControls(Constant.MASTER);
                    } else {
                        getFingerprintList();
                    }
                } else {
                    switchManagePrivilege.setVisibility(View.GONE);
                    getFingerprintList();
                }

            } else {
                switchManagePrivilege.setVisibility(View.GONE);
                getFingerprintList();
            }

        } else {
            getFingerprintList();
        }

    }

    private void showAndHideFpPrivilegeControls(String userType) {

        layoutAdd.setVisibility(View.GONE);
        listUser.setVisibility(View.GONE);
        fabFPAdd.setVisibility(View.GONE);

        layoutFpNoAccess.setVisibility(View.VISIBLE);
        tvNoFpPrivilege.setVisibility(View.VISIBLE);
        if (userType != null && userType.equalsIgnoreCase(Constant.OWNER)) {
            tvNoFpPrivilege.setText(getContext().getString(R.string.no_fp_access_msg_owner));
        } else if (userType != null && userType.equalsIgnoreCase(Constant.MASTER)) {
            tvNoFpPrivilege.setText(getContext().getString(R.string.no_fp_access_msg_master));
        }
    }

    private void doUpdateFpPrivilege(boolean isChecked) {

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
                        doAuthViaFp(isChecked);
                    }
                }, "CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        allowAPICall = false;
                        switchManagePrivilege.setChecked(!isChecked);
                    }
                });
    }

    private void doAuthViaFp(boolean isChecked) {
        Loader.getInstance().showLoader(getContext());
        String SSID = BleManager.MANUFACTURER_CODE + mLock.getSerialNumber();
        Logger.d("### FP SSID = " + SSID);
        String password = mLock.getScratchCode();
        String lockVersion = mLock.getLockVersion();
        WifiUtilManager wifiUtilManager = new WifiUtilManager(getContext(), new WifiUtilManager.WifiListener() {
            @Override
            public void connectionSuccess() {
                Logger.d("Connection Success");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doAuthViaFpRequest(getContext(), isChecked);
                    }
                }, WIFI_DELAY_TIME);
            }

            @Override
            public void connectionTimeOut() {
                allowAPICall = false;
                switchManagePrivilege.setChecked(!isChecked);
                Loader.getInstance().hideLoader();
                Toast.makeText(getContext(), "Please try again later", Toast.LENGTH_LONG).show();
            }
        });
        wifiUtilManager.startScanning(lockVersion, SSID, password);
    }

    private void doAuthViaFpRequest(Context context, boolean isChecked) {

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

            ServiceManager.getInstance().post(ServiceUrl.WIFI_LOCK_AUTH_VIA_FP, authViaPinFp, new ResponseHandler() {
                @Override
                public void onSuccess(Object data) {
                    Loader.getInstance().hideLoader();
                    if (data != null) {
                        WifiLockResponse authViaFpResponse = new Gson().fromJson(data.toString(), WifiLockResponse.class);
                        if (authViaFpResponse != null && authViaFpResponse.getStatus() != null
                                && authViaFpResponse.getStatus().equalsIgnoreCase("success")) {

                            if (isChecked) {
                                hideFpNoAccessControls();
                            } else {
                                showFpNoAccessControls();
                            }

                            mLock.setEnableFp(isChecked ? "1" : "0");
                            LockDetailFragment.mLock.setEnableFp(isChecked ? "1" : "0");
                            String msg = isChecked ? "enabled" : "disabled";
                            forgetWIFINetwork(context, mLock);
                            AppDialog.showAlertDialog(context, "Success", "Fingerprint access " + msg + " successfully",
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
                    switchManagePrivilege.setChecked(!isChecked);
                    Loader.getInstance().hideLoader();
                    Logger.d(message);
                    AppDialog.showAlertDialog(context, "Invalid Lock Key. Please contact support.");
                }

                @Override
                public void onError(String message) {
                    allowAPICall = false;
                    switchManagePrivilege.setChecked(!isChecked);
                    Loader.getInstance().hideLoader();
                    try {
                        if (message != null && message.equals("No Internet. Please Check your network connection.")) {
                            AppDialog.showAlertDialog(context, message);
                        } else {
                            WifiLockResponse mWifiLockResponse = new Gson().fromJson(message, WifiLockResponse.class);
                            String errorMsg = "";
                            if (mWifiLockResponse != null && mWifiLockResponse.getErrorMessage() != null && mWifiLockResponse.getErrorMessage().equalsIgnoreCase(FPListFragment.AUTHVIA_FP_DISABLED)) {
                                forgetWIFINetwork(context, mLock);
                                if (userType != null && userType.equalsIgnoreCase(Constant.OWNER)) {
                                    errorMsg = getContext().getString(R.string.no_fp_access_msg_owner);
                                } else if (userType != null && userType.equalsIgnoreCase(Constant.MASTER)) {
                                    errorMsg = getContext().getString(R.string.no_fp_access_msg_master);
                                }
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

    private void hideFpNoAccessControls() {
        layoutFpNoAccess.setVisibility(View.GONE);
        tvNoFpPrivilege.setVisibility(View.GONE);
        tvNoFpPrivilege.setText(getContext().getString(R.string.no_fp_access_msg_owner));
    }

    private void showFpNoAccessControls() {
        layoutAdd.setVisibility(View.GONE);
        listUser.setVisibility(View.GONE);
        fabFPAdd.setVisibility(View.GONE);

        layoutFpNoAccess.setVisibility(View.VISIBLE);
        tvNoFpPrivilege.setVisibility(View.VISIBLE);
        tvNoFpPrivilege.setText(getContext().getString(R.string.no_fp_access_msg_owner));
    }

    private void doAddFingerPrint() {
        AppDialog.showAlertDialog(getContext(), getContext().getString(R.string.app_name), getContext().getString(R.string.alert_select_user_type),
                "New User", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        ScreenData screenData = new ScreenData();
                        screenData.setLock(mLock);
                        screenData.setLockKeys(new LockKeys());
                        FPUser fpUser = new FPUser();
                        ArrayList<String> keys = new ArrayList<>();
                        fpUser.setKeys(keys);
                        screenData.setFpUser(fpUser);
                        requestCode = REQ_ADD_FP;
                        App.getInstance().showFullScreenForResult(getActivity(), Constant.SCREEN.FINGER_PRINT_ADD,
                                new Gson().toJson(screenData), REQ_ADD_FP, resultLauncher);

                    }

                },
                "Existing User", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        ScreenData screenData = new ScreenData();
                        screenData.setLock(mLock);
                        screenData.setLockKeys(new LockKeys());
                        screenData.setFpExistingList(fpKeys);

                        App.getInstance().showFullScreenForResult(getActivity(),
                                Constant.SCREEN.USER_LIST, new Gson().toJson(screenData), REQ_ADD_FP, resultLauncher);

                    }
                });

    }

    private void startActivityForResult() {
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {

                    Logger.d("### FP LIST REQ CODE + " + requestCode);
                    if (requestCode == REQ_ADD_FP && result.getResultCode() == Activity.RESULT_OK) {
                        getActivity().setResult(Activity.RESULT_OK);
                        requireActivity().getOnBackPressedDispatcher().onBackPressed();
                    }
                });
    }

    private void getFingerprintList() {
        presenter.getRFIDOrFPList(getActivity(), mLock.getId(), Constant.FP, REQ_FP_USER_LIST);
    }

    @Override
    public void onViewUpdate(int reqCode, Object response) {
        if (REQ_FP_USER_LIST == reqCode) {
            fpKeys = (ArrayList<LockKeys>) response;
            populateUI();
        } else if (REQ_EDIT_NAME == reqCode) {
            BaseResponse baseResponse = (BaseResponse) response;
            if (baseResponse.getStatus().equalsIgnoreCase("success")) {
                Toast.makeText(getContext(), "Name Updated Successfully", Toast.LENGTH_LONG).show();
                fpListAdapter.notifyDataSetChanged();
            }
        } else if (REQ_REVOKE_FP == reqCode) {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        }
    }

    private FPListAdapter fpListAdapter;

    private void populateUI() {
        if (fpKeys != null && fpKeys.size() > 0) {
            layoutAdd.setVisibility(View.GONE);
            fabFPAdd.setVisibility(View.VISIBLE);
            listUser.setVisibility(View.VISIBLE);
            HashMap<String, ArrayList<LockKeys>> fpUsers = new HashMap<>();
            ArrayList<String> ids = new ArrayList<>();
            for (LockKeys lockKeys : fpKeys) {
                if (fpUsers.containsKey(lockKeys.getGuestId())) {
                    fpUsers.get(lockKeys.getGuestId()).add(lockKeys);
                } else {
                    ids.add(lockKeys.getGuestId());
                    ArrayList<LockKeys> newFPuser = new ArrayList<>();
                    Type type = new TypeToken<ArrayList<String>>() {
                    }.getType();
                    ArrayList<String> fpIds = new Gson().fromJson(lockKeys.getKey(), type);
                    lockKeys.setOriginalFPIds(fpIds);
                    newFPuser.add(lockKeys);
                    fpUsers.put(lockKeys.getGuestId(), newFPuser);
                }
            }
            setAdapter(fpUsers, ids);

        }
    }

    private void setAdapter(HashMap<String, ArrayList<LockKeys>> fpUsers, ArrayList<String> ids) {
        fpListAdapter = new FPListAdapter(fpUsers, ids) {
            @Override
            public void onUserAdd(ArrayList<LockKeys> users) {
                selectedUser = users.get(0);
                ScreenData screenData = new ScreenData();
                screenData.setLock(mLock);
                screenData.setLockKeys(users.get(0));
                FPUser fpUser = new FPUser();
                Type keyType = new TypeToken<ArrayList<String>>() {
                }.getType();
                Gson gson = new Gson();
                ArrayList<String> keys = gson.fromJson(selectedUser.getKey(), keyType);
                fpUser.setKeys(keys);
                fpUser.setId(selectedUser.getId());
                fpUser.setLockId(selectedUser.getLockId());
                fpUser.setUserId(selectedUser.getUserId());
                if (selectedUser.getLockUser() != null) {
                    fpUser.setName(selectedUser.getLockUser().getUsername());
                } else {
                    fpUser.setName(selectedUser.getRegistrationDetails().getName());
                }
                screenData.setFpUser(fpUser);
                App.getInstance().showFullScreenForResult(getActivity(), Constant.SCREEN.FINGER_PRINT_ADD, new Gson().toJson(screenData), REQ_ADD_FP, resultLauncher);
            }

            @Override
            public void onUserRevoke(ArrayList<LockKeys> users) {
                //selectedUser=user;
                selectedUser = users.get(0);
                FPUser fpUser = new FPUser();
                Type keyType = new TypeToken<ArrayList<String>>() {
                }.getType();
                Gson gson = new Gson();
                ArrayList<String> keys = gson.fromJson(selectedUser.getKey(), keyType);
                fpUser.setId(selectedUser.getId());
                fpUser.setKeys(keys);
                fpUser.setLockId(selectedUser.getLockId());
                fpUser.setUserId(selectedUser.getUserId());
                if (mLock.getLockVersion().equalsIgnoreCase(HW_VERSION_4_0)) {
                    numberOfFP = fpUser.getKeys().size();
                    for (String fpId : fpUser.getKeys()) {
                        Loader.getInstance().showLoader(getContext());
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                FPService.getInstance().revokeFpIdPrivilegeMqtt(mLock.getSerialNumber(), selectedUser.getId(), fpId, new ResponseHandler() {
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
                            }
                        }, 1000);
                    }
                } else {
                    presenter.doAddFingerPrint(getContext(), REQ_REVOKE_FP, mLock, fpUser, FPPresenter.FP_REVOKE);
                }
            }

            @Override
            public void onUserEdit(ArrayList<LockKeys> users) {
                selectedUser = users.get(0);
                doEditName();
            }
        };
        listUser.setAdapter(fpListAdapter);
    }

    private void doEditName() {
        // create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getContext().getString(R.string.app_name));
        // set the custom layout
        final View customLayout = getLayoutInflater().inflate(R.layout.layout_custom_dialog, null);
        builder.setView(customLayout);
        // add a button
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // send data from the AlertDialog to the Activity
                EditText txtName = customLayout.findViewById(R.id.txtName);
                if (!TextUtils.isEmpty(txtName.getText().toString())) {
                    LockKeys payload = new LockKeys();
                    payload.setId(selectedUser.getGuestId());
                    payload.setName(txtName.getText().toString());
                    selectedUser.getRegistrationDetails().setName(txtName.getText().toString());
                    presenter.updateGuestName(getContext(), payload, REQ_EDIT_NAME);
                } else {
                    Toast.makeText(getContext(), "Please Enter Name", Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
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

}
