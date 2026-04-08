package com.payoda.smartlock.managepins;

import static com.payoda.smartlock.constants.Constant.HW_VERSION_6_0;
import static com.payoda.smartlock.constants.Constant.WIFI_DELAY_TIME;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.payoda.smartlock.BuildConfig;
import com.payoda.smartlock.R;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.constants.ServiceUrl;
import com.payoda.smartlock.locks.LockManager;
import com.payoda.smartlock.locks.model.Lock;
import com.payoda.smartlock.locks.model.LockKeys;
import com.payoda.smartlock.locks.model.WifiLockResponse;
import com.payoda.smartlock.managepins.adapter.OtpAdapter;
import com.payoda.smartlock.managepins.adapter.OtpModel;
import com.payoda.smartlock.managepins.model.Otp;
import com.payoda.smartlock.managepins.model.OtpRequest;
import com.payoda.smartlock.managepins.model.OtpResponse;
import com.payoda.smartlock.managepins.model.ReWriteOtp;
import com.payoda.smartlock.managepins.model.ReWriteOtpMqtt;
import com.payoda.smartlock.managepins.service.OtpAddService;
import com.payoda.smartlock.managepins.service.OtpKeyService;
import com.payoda.smartlock.plugins.bluetooth.BleManager;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.plugins.pushnotification.RemoteDataEvent;
import com.payoda.smartlock.plugins.storage.SecuredStorageManager;
import com.payoda.smartlock.plugins.wifi.WifiLockManager;
import com.payoda.smartlock.plugins.wifi.WifiUtilManager;
import com.payoda.smartlock.service.AESEncryption;
import com.payoda.smartlock.service.SyncScheduler;
import com.payoda.smartlock.utils.AppDialog;
import com.payoda.smartlock.utils.Loader;
import com.payoda.smartlock.utils.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OtpKeysFragment} factory method to
 * create an instance of this fragment.
 */
public class OtpKeysFragment extends Fragment {

    public static final String TAG = "### OtpKeysFragment";

    private Lock mLock = new Lock();
    private Button btnReWriteOtp, btnGenerateOtp;
    private TextView tvReWriteOtp;
    protected LinearLayout llNoOtpView;
    protected RecyclerView recyclerView;
    private OtpAdapter otpAdapter;
    public ArrayList<OtpModel> otpModelArrayList;

    protected ArrayList<AppCompatEditText> otpEditTextViews = new ArrayList<AppCompatEditText>();

    public OtpKeysFragment() {
        // Required empty public constructor
    }

    public static OtpKeysFragment getInstance() {
        return new OtpKeysFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mLock = new Gson().fromJson(bundle.getString(Constant.SCREEN_DATA), Lock.class);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Logger.d(TAG, TAG);
        return initializeView(inflater.inflate(R.layout.fragment_otp_keys, container, false));
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
        if (remoteDataEvent != null && remoteDataEvent.getStatus().equalsIgnoreCase(Constant.SUCCESS)
                && (remoteDataEvent.getCommand().equalsIgnoreCase(Constant.OTP_REWRITE_COMMAND))) {
            Loader.getInstance().hideLoader();
            AppDialog.showAlertDialog(getActivity(), remoteDataEvent.getTitle(), remoteDataEvent.getBody(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (getActivity() != null)
                        requireActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            });
        } else if (remoteDataEvent != null && remoteDataEvent.getStatus().equalsIgnoreCase(Constant.FAILURE)
                && (remoteDataEvent.getCommand().equalsIgnoreCase(Constant.OTP_REWRITE_COMMAND))) {
            Loader.getInstance().hideLoader();
            AppDialog.showAlertDialog(getActivity(), remoteDataEvent.getBody());
        }
    }

    private View initializeView(View rootView) {

        llNoOtpView = rootView.findViewById(R.id.ll_rewrite_otp_container);
        tvReWriteOtp = rootView.findViewById(R.id.tv_rewrite_otp);

        recyclerView = rootView.findViewById(R.id.otp_recycler_view);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), LinearLayout.VERTICAL) {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            }
        });

        // Set Layout Manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        // Limiting the size
        recyclerView.setHasFixedSize(true);

        btnGenerateOtp = rootView.findViewById(R.id.btnGenerateOtp);
        btnReWriteOtp = rootView.findViewById(R.id.btnReWriteOtp);

        btnReWriteOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doReWriteOtp();
            }
        });

        btnGenerateOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doGetOtp(getContext(), mLock.getId(), "1", false);
            }
        });

        if (mLock != null && mLock.getEnablePin() != null && mLock.getEnablePin().equalsIgnoreCase("1")) {
            doGetOtp(getContext(), mLock.getId(), "0", true);
        }

        newFlowVersion6(true);

        return rootView;

    }

    public void newFlowVersion6(boolean allowAPI) {

        if (mLock.getLockVersion().equalsIgnoreCase(Constant.HW_VERSION_6_0)) {

            btnReWriteOtp.setVisibility(View.GONE);
            btnGenerateOtp.setVisibility(View.VISIBLE);

            if (mLock != null && mLock.getEnablePin() != null && mLock.getEnablePin().equalsIgnoreCase("1")) {


                if (allowAPI){
                    Logger.d("########### doGetOtp  : "+"456");
                    doGetOtp(getContext(), mLock.getId(), "0", true);
                }

            }
        }

    }

    private void doReWriteOtp() {
        ArrayList<String> otpList = prepareReWriteOtpList();
        if (otpList.size() != 5) {
            return;
        }

        if (mLock.getLockVersion().equalsIgnoreCase(Constant.HW_VERSION_4_0)) {
            final ReWriteOtpMqtt reWriteOtpMqtt = new ReWriteOtpMqtt();
            AESEncryption aesEncryption = AESEncryption.getInstance();
            reWriteOtpMqtt.setOtp1(aesEncryption.encrypt(otpList.get(0), true));
            reWriteOtpMqtt.setOtp2(aesEncryption.encrypt(otpList.get(1), true));
            reWriteOtpMqtt.setOtp3(aesEncryption.encrypt(otpList.get(2), true));
            reWriteOtpMqtt.setOtp4(aesEncryption.encrypt(otpList.get(3), true));
            reWriteOtpMqtt.setOtp5(aesEncryption.encrypt(otpList.get(4), true));

            Loader.getInstance().showLoader(getContext());

            OtpAddService.getInstance().addOtpMqtt(mLock.getSerialNumber(), reWriteOtpMqtt, new ResponseHandler() {
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
        else {
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

            AppDialog.showAlertDialog(getActivity(), "Activate Lock", msg, "OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            performReWriteOtp(getContext(), mLock, otpList);
                        }
                    }, "CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
        }
    }

    private void performReWriteOtp(Context context, Lock mLock, ArrayList<String> otpList) {
        Loader.getInstance().showLoader(context);
        String SSID = BleManager.MANUFACTURER_CODE + mLock.getSerialNumber();
        String password = mLock.getScratchCode();
        String lockVersion = mLock.getLockVersion();
        WifiUtilManager wifiUtilManager = new WifiUtilManager(context, new WifiUtilManager.WifiListener() {
            @Override
            public void connectionSuccess() {
                Logger.d("Connection Success");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doReWriteOtpRequest(context, mLock, otpList);
                    }
                }, WIFI_DELAY_TIME);
            }

            @Override
            public void connectionTimeOut() {
                Loader.getInstance().hideLoader();
                Toast.makeText(context, "Please try again later", Toast.LENGTH_LONG).show();
            }
        });
        wifiUtilManager.startScanning(lockVersion, SSID, password);
    }

    private void doReWriteOtpRequest(Context context, Lock mLock, ArrayList<String> otpList) {

        Logger.d("### mLock.getSsid()  " + mLock.getSsid());

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

            final ReWriteOtp reWriteOtp = new ReWriteOtp();
            reWriteOtp.setOwnerId(AESEncryption.getInstance().decrypt(id, mLock.isEncrypted()));
            reWriteOtp.setSlotKey(AESEncryption.getInstance().decrypt(key, mLock.isEncrypted()));

            reWriteOtp.setOtp1(otpList.get(0));
            reWriteOtp.setOtp2(otpList.get(1));
            reWriteOtp.setOtp3(otpList.get(2));
            reWriteOtp.setOtp4(otpList.get(3));
            reWriteOtp.setOtp5(otpList.get(4));

            ServiceManager.getInstance().post(ServiceUrl.WIFI_LOCK_RE_WRITE_OTP, reWriteOtp, new ResponseHandler() {
                @Override
                public void onSuccess(Object data) {
                    Loader.getInstance().hideLoader();
                    if (data != null) {
                        WifiLockResponse reWriteOtpResponse = new Gson().fromJson(data.toString(), WifiLockResponse.class);
                        if (reWriteOtpResponse != null && reWriteOtpResponse.getStatus() != null
                                && reWriteOtpResponse.getStatus().equalsIgnoreCase("success")) {
                            forgetWIFINetwork(context, mLock);
                            AppDialog.showAlertDialog(context, "Success", "OTP Updated Successfully",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            doAddOtp(context, mLock.getId(), reWriteOtp);
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
                    Loader.getInstance().hideLoader();
                    Logger.d(message);
                    AppDialog.showAlertDialog(context, "Invalid Lock Key. Please contact support.");
                }

                @Override
                public void onError(String message) {
                    Loader.getInstance().hideLoader();
                    try {
                        if (message != null && message.equals("No Internet. Please Check your network connection.")) {
                            AppDialog.showAlertDialog(context, message);
                        } else {
                            WifiLockResponse mWifiLockResponse = new Gson().fromJson(message, WifiLockResponse.class);
                            String errorMsg = "";
                            if (mWifiLockResponse != null && mWifiLockResponse.getErrorMessage() != null) {
                                forgetWIFINetwork(context, mLock);
                                switch (mWifiLockResponse.getErrorMessage()) {
                                    case ManagePinConstant.TP_PIN_OTP_INVALID_FORMAT:
                                        errorMsg = "Invalid OTP format";
                                        break;
                                    case ManagePinConstant.TP_PIN_OTP_INVALID_LEN:
                                        errorMsg = "Invalid OTP length";
                                        break;
                                    case ManagePinConstant.TP_PIN_OTP_ALREADY_EXISTS:
                                        errorMsg = "OTP already exists";
                                        break;
                                    case ManagePinConstant.AUTHVIA_TP_DISABLED:
                                        errorMsg = context.getString(R.string.no_pin_access_msg);
                                        break;
                                    default:
                                        break;
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

    private void doAddOtp(Context context, String lockId, ReWriteOtp reWriteOtp) {
        ArrayList<Otp> otpArrayList = formOtpList(reWriteOtp);

        final OtpRequest payload = new OtpRequest();
        payload.setLockId(lockId);
        payload.setLockOtps(otpArrayList);

        storeOffline(payload);
    }

    private void storeOffline(OtpRequest otpRequest) {
        SecuredStorageManager.getInstance().saveOfflineOtp(otpRequest);
        SyncScheduler.getInstance().schedule(getActivity(), Constant.JOBS.ALL);
        if (getActivity() != null) {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        }
    }

    private void doGetOtp(Context context, String lockId, String next, boolean isOnPageLoad) {

        Loader.getInstance().showLoader(context);

        OtpKeyService.getInstance().getOtp(lockId, next, new ResponseHandler() {
            @Override
            public void onSuccess(Object data) {
                Loader.getInstance().hideLoader();
                if (data != null) {
                    OtpResponse otpResponse = new Gson().fromJson(data.toString(), OtpResponse.class);
                    parseOtpResponse(otpResponse, isOnPageLoad);
                }
            }

            @Override
            public void onAuthError(String message) {
                Loader.getInstance().hideLoader();
                if (context != null) {
                    AppDialog.showAlertDialog(context, message, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

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

    private void parseOtpResponse(OtpResponse otpResponse, boolean isOnPageLoad) {

        if (!otpResponse.getStatus().equals("failure")) {

            AESEncryption aesEncryption = AESEncryption.getInstance();

            ArrayList<OtpModel> list = new ArrayList<>();

            ArrayList<LockKeys> lockKeys = otpResponse.getOtpData().getLockKeyList();

            if (lockKeys != null && lockKeys.size() > 0) {

                for (int i = 0; i < lockKeys.size(); i++) {


                    OtpModel otpModel = new OtpModel();
                    otpModel.setOtp(aesEncryption.decrypt(lockKeys.get(i).getKey(), true));
                    otpModel.setStatus(lockKeys.get(i).getStatus());
                    otpModel.setRequestTime(lockKeys.get(i).getAssigned_datetime());
                    list.add(otpModel);

                }
            }

            otpModelArrayList = list;
            otpAdapter = new OtpAdapter(getActivity(), getContext(), otpModelArrayList);
            recyclerView.setAdapter(otpAdapter);

            if (list.size() > 0) {
                recyclerView.setVisibility(View.VISIBLE);
                llNoOtpView.setVisibility(View.GONE);
                tvReWriteOtp.setVisibility(View.GONE);
            }
            else {
                recyclerView.setVisibility(View.GONE);
                llNoOtpView.setVisibility(View.VISIBLE);
                tvReWriteOtp.setVisibility(View.VISIBLE);

                if (otpResponse.getOtpData().isGetOtp()) {
                    tvReWriteOtp.setText(getContext().getString(R.string.no_generated_otp_message));
                } else {
                    tvReWriteOtp.setText(getContext().getString(R.string.rewrite_otp_message));
                }
            }

            Logger.d("### is get otp = " + otpResponse.getOtpData().isGetOtp());

            if (otpResponse.getOtpData().isGetOtp()) {
                btnGenerateOtp.setVisibility(View.VISIBLE);
            }
            else {
                btnGenerateOtp.setVisibility(View.GONE);
            }

            newFlowVersion6(false);

        }
        else {

            recyclerView.setVisibility(View.GONE);
            llNoOtpView.setVisibility(View.VISIBLE);
            tvReWriteOtp.setVisibility(View.VISIBLE);
            if (otpResponse.getOtpData().isGetOtp()) {
                tvReWriteOtp.setText(getContext().getString(R.string.no_generated_otp_message));
                btnGenerateOtp.setVisibility(View.VISIBLE);
            } else {
                if (!mLock.getLockVersion().equalsIgnoreCase(Constant.HW_VERSION_6_0)) {
                    tvReWriteOtp.setText(getContext().getString(R.string.rewrite_otp_message));
                    btnGenerateOtp.setVisibility(View.GONE);
                } else {
                    if (getContext() != null)
                        tvReWriteOtp.setText(getContext().getString(R.string.no_generated_otp_message));
                    else
                        tvReWriteOtp.setText("Please generate OTP to proceed");
                }
            }

            newFlowVersion6(false);

        }
    }

    private ArrayList<Otp> formOtpList(ReWriteOtp reWriteOtp) {
        AESEncryption aesEncryption = AESEncryption.getInstance();

        ArrayList<Otp> otpArrayList = new ArrayList<Otp>();

        Otp otp1 = new Otp();
        otp1.setPin(aesEncryption.encrypt(reWriteOtp.getOtp1(), true));
        otp1.setSlotNumber("1");
        otpArrayList.add(otp1);

        Otp otp2 = new Otp();
        otp2.setPin(aesEncryption.encrypt(reWriteOtp.getOtp2(), true));
        otp2.setSlotNumber("2");
        otpArrayList.add(otp2);

        Otp otp3 = new Otp();
        otp3.setPin(aesEncryption.encrypt(reWriteOtp.getOtp3(), true));
        otp3.setSlotNumber("3");
        otpArrayList.add(otp3);

        Otp otp4 = new Otp();
        otp4.setPin(aesEncryption.encrypt(reWriteOtp.getOtp4(), true));
        otp4.setSlotNumber("4");
        otpArrayList.add(otp4);

        Otp otp5 = new Otp();
        otp5.setPin(aesEncryption.encrypt(reWriteOtp.getOtp5(), true));
        otp5.setSlotNumber("5");
        otpArrayList.add(otp5);

        return otpArrayList;
    }

    private ArrayList<String> prepareReWriteOtpList() {
        ArrayList<String> otpList = new ArrayList<>();
        boolean isNotSatisfied = true;
        while (isNotSatisfied) {
            String randomNum = genRandomNum();
            if (!otpList.contains(randomNum)) {
                otpList.add(randomNum);
            }
            if (otpList.size() == 5) {
                isNotSatisfied = false;
            }
        }
        return otpList;
    }

    private String genRandomNum() {
        Random random = new Random();
        int num = random.nextInt(100000);
        return String.format("%05d", num);
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