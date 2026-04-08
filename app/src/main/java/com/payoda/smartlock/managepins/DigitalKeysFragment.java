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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.payoda.smartlock.R;
import com.payoda.smartlock.authentication.BaseFragment;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.constants.ServiceUrl;
import com.payoda.smartlock.locks.LockManager;
import com.payoda.smartlock.locks.model.Lock;
import com.payoda.smartlock.locks.model.LockKeys;
import com.payoda.smartlock.locks.model.WifiLockResponse;
import com.payoda.smartlock.managepins.adapter.DigiPinAdapter;
import com.payoda.smartlock.managepins.adapter.DigiPinModel;
import com.payoda.smartlock.managepins.model.Pin;
import com.payoda.smartlock.managepins.model.PinRequest;
import com.payoda.smartlock.managepins.model.ReWritePin;
import com.payoda.smartlock.managepins.model.ReWritePinMqtt;
import com.payoda.smartlock.managepins.service.DigitalKeyService;
import com.payoda.smartlock.plugins.bluetooth.BleManager;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.plugins.pushnotification.RemoteDataEvent;
import com.payoda.smartlock.plugins.storage.SecuredStorageManager;
import com.payoda.smartlock.plugins.wifi.WifiLockManager;
import com.payoda.smartlock.plugins.wifi.WifiUtilManager;
import com.payoda.smartlock.service.AESEncryption;
import com.payoda.smartlock.service.SyncScheduler;
import com.payoda.smartlock.users.model.AssignUser;
import com.payoda.smartlock.users.service.AssignRequestUserService;
import com.payoda.smartlock.utils.AppDialog;
import com.payoda.smartlock.utils.Loader;
import com.payoda.smartlock.utils.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DigitalKeysFragment} factory method to
 * create an instance of this fragment.
 */
public class DigitalKeysFragment extends BaseFragment {

    private Lock mLock = new Lock();

    public static final String TAG = "### DigitalKeysFragment";

    private RecyclerView recyclerView;
    private DigiPinAdapter digiPinAdapter;
    public ArrayList<DigiPinModel> digiPinModelArrayList;

    private Button updatePinBtn;

    public DigitalKeysFragment() {
        // Required empty public constructor
    }

    public static DigitalKeysFragment getInstance() {
        return new DigitalKeysFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mLock = new Gson().fromJson(bundle.getString(Constant.SCREEN_DATA), Lock.class);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Logger.d(TAG, TAG);
        return initializeView(inflater.inflate(R.layout.fragment_digital_keys, container, false));
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
        if(remoteDataEvent != null && remoteDataEvent.getStatus().equalsIgnoreCase(Constant.SUCCESS)
                && (remoteDataEvent.getCommand().equalsIgnoreCase(Constant.PIN_REWRITE_COMMAND))){
            Loader.getInstance().hideLoader();
            AppDialog.showAlertDialog(getActivity(), remoteDataEvent.getTitle(), remoteDataEvent.getBody(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (getActivity() != null)
                        requireActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            });
        }
        else if(remoteDataEvent != null && remoteDataEvent.getStatus().equalsIgnoreCase(Constant.FAILURE)
                && (remoteDataEvent.getCommand().equalsIgnoreCase(Constant.PIN_REWRITE_COMMAND))){
            Loader.getInstance().hideLoader();
            AppDialog.showAlertDialog(getActivity(), remoteDataEvent.getBody());
        }
    }

    private View initializeView(View rootView) {

        updatePinBtn = rootView.findViewById(R.id.update_pin_button);
        updatePinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doUpdatePin();
            }
        });

        recyclerView = rootView.findViewById(R.id.digi_pin_recycler_view);
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

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == 1) {
                    Logger.d("onScrollStateChanged", String.valueOf(newState));
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(recyclerView.getWindowToken(), 0);
                }
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        if(mLock != null && mLock.getEnablePin() != null && mLock.getEnablePin().equalsIgnoreCase("1")){
            doReadPin(getContext(), mLock.getId(), "PIN");
        }

        return rootView;
    }

    private void doReadPin(Context context, String lockId, String type) {
        Loader.getInstance().showLoader(context);
        AssignRequestUserService.getInstance().getAssignUserList(lockId, "0", type, new ResponseHandler() {
            @Override
            public void onSuccess(Object data) {
                if (data != null) {
                    AssignUser pinResponse = (AssignUser) data;
                    parseReadPinResponse(pinResponse.getKeys());


                }else{
                    Loader.getInstance().hideLoader();
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

    private void parseReadPinResponse(ArrayList<LockKeys> pinList) {

        ArrayList<DigiPinModel> list = new ArrayList<DigiPinModel>(9);

        AESEncryption aesEncryption = AESEncryption.getInstance();

        if (pinList != null && pinList.size() > 0) {

            for (LockKeys lockKeys : pinList) {

                String name = (lockKeys.getRegistrationDetails() != null) ? lockKeys.getRegistrationDetails().getName() : "";
                String pin = lockKeys.getKey();
                pin = (pin != null) ? (pin.equalsIgnoreCase("0")) ? "" : aesEncryption.decrypt(pin, true) : "";
                int i = Integer.parseInt(lockKeys.getSlotNumber());

                Logger.d("### pin decrypt = " + pin);
                Logger.d("### pin getSlotNumber = " + lockKeys.getSlotNumber());

                DigiPinModel editModel = new DigiPinModel();
                editModel.setEditTextValue((name != null) ? name : "");

                char textArray[] = pin.toCharArray();
                LinkedTreeMap<Integer, String> pinHash = new LinkedTreeMap<>();
                for (int index = 0; index < textArray.length; index++) {
                    pinHash.put(index + 1, String.valueOf(textArray[index]));
                }

                editModel.setPinValue(pinHash);
                editModel.setIndexValue(String.valueOf(i));

                list.add((i - 1), editModel);

            }

            for (int i = pinList.size(); i < 9; i++) {
                LinkedTreeMap<Integer, String> pinHash = new LinkedTreeMap<>();
                DigiPinModel editModel = new DigiPinModel();
                editModel.setEditTextValue("");
                editModel.setPinValue(pinHash);
                editModel.setIndexValue(String.valueOf(i + 1));
                list.add((i), editModel);
            }

        } else {
            for (int i = 0; i < 9; i++) {
                LinkedTreeMap<Integer, String> pinHash = new LinkedTreeMap<>();
                DigiPinModel editModel = new DigiPinModel();
                editModel.setEditTextValue("");
                editModel.setPinValue(pinHash);
                editModel.setIndexValue(String.valueOf(i + 1));
                list.add((i), editModel);
            }
        }

        Logger.d("### list = "+ list);
        digiPinModelArrayList = list;
        digiPinAdapter = new DigiPinAdapter(getActivity(), getContext(), digiPinModelArrayList, true);
        recyclerView.setAdapter(digiPinAdapter);

        Loader.getInstance().hideLoader();
    }

    private void doUpdatePin() {
        if (!validateNameAndPin()) {
            return;
        }

        if (!validateDuplicatePinName()) {
            return;
        }

        if (!validateDuplicatePin()) {
            return;
        }

        if(mLock.getLockVersion().equalsIgnoreCase(Constant.HW_VERSION_4_0)){
            AESEncryption aesEncryption = AESEncryption.getInstance();
            final ReWritePinMqtt reWritePinMqtt= new ReWritePinMqtt();

            String pin1 = convertHashMapToString(digiPinModelArrayList.get(0).getPinValue());
            reWritePinMqtt.setPin1((pin1 != null && !pin1.isEmpty()) ? aesEncryption.encrypt(pin1, true) : "0000");
            String name1 = digiPinModelArrayList.get(0).getEditTextValue();
            reWritePinMqtt.setName1((name1 != null && !name1.isEmpty()) ? name1 : "");

            String pin2 = convertHashMapToString(digiPinModelArrayList.get(1).getPinValue());
            reWritePinMqtt.setPin2((pin2 != null && !pin2.isEmpty()) ? aesEncryption.encrypt(pin2, true) : "0000");
            String name2 = digiPinModelArrayList.get(1).getEditTextValue();
            reWritePinMqtt.setName2((name2 != null && !name2.isEmpty()) ? name2 : "");

            String pin3 = convertHashMapToString(digiPinModelArrayList.get(2).getPinValue());
            reWritePinMqtt.setPin3((pin3 != null && !pin3.isEmpty()) ? aesEncryption.encrypt(pin3, true) : "0000");
            String name3 = digiPinModelArrayList.get(2).getEditTextValue();
            reWritePinMqtt.setName3((name3 != null && !name3.isEmpty()) ? name3 : "");

            String pin4 = convertHashMapToString(digiPinModelArrayList.get(3).getPinValue());
            reWritePinMqtt.setPin4((pin4 != null && !pin4.isEmpty()) ? aesEncryption.encrypt(pin4, true) : "0000");
            String name4 = digiPinModelArrayList.get(3).getEditTextValue();
            reWritePinMqtt.setName4((name4 != null && !name4.isEmpty()) ? name4 : "");

            String pin5 = convertHashMapToString(digiPinModelArrayList.get(4).getPinValue());
            reWritePinMqtt.setPin5((pin5 != null && !pin5.isEmpty()) ? aesEncryption.encrypt(pin5, true) : "0000");
            String name5 = digiPinModelArrayList.get(4).getEditTextValue();
            reWritePinMqtt.setName5((name5 != null && !name5.isEmpty()) ? name5 : "");

            String pin6 = convertHashMapToString(digiPinModelArrayList.get(5).getPinValue());
            reWritePinMqtt.setPin6((pin6 != null && !pin6.isEmpty()) ? aesEncryption.encrypt(pin6, true) : "0000");
            String name6 = digiPinModelArrayList.get(5).getEditTextValue();
            reWritePinMqtt.setName6((name6 != null && !name6.isEmpty()) ? name6 : "");

            String pin7 = convertHashMapToString(digiPinModelArrayList.get(6).getPinValue());
            reWritePinMqtt.setPin7((pin7 != null && !pin7.isEmpty()) ? aesEncryption.encrypt(pin7, true) : "0000");
            String name7 = digiPinModelArrayList.get(6).getEditTextValue();
            reWritePinMqtt.setName7((name7 != null && !name7.isEmpty()) ? name7 : "");

            String pin8 = convertHashMapToString(digiPinModelArrayList.get(7).getPinValue());
            reWritePinMqtt.setPin8((pin1 != null && !pin8.isEmpty()) ? aesEncryption.encrypt(pin8, true) : "0000");
            String name8 = digiPinModelArrayList.get(7).getEditTextValue();
            reWritePinMqtt.setName8((name8 != null && !name8.isEmpty()) ? name8 : "");

            String pin9 = convertHashMapToString(digiPinModelArrayList.get(8).getPinValue());
            reWritePinMqtt.setPin9((pin9 != null && !pin9.isEmpty()) ? aesEncryption.encrypt(pin9, true) : "0000");
            String name9 = digiPinModelArrayList.get(8).getEditTextValue();
            reWritePinMqtt.setName9((name9 != null && !name9.isEmpty()) ? name9 : "");

            Loader.getInstance().showLoader(getContext());
            DigitalKeyService.getInstance().addUpdateDigiPinMqtt(mLock.getSerialNumber(), reWritePinMqtt, new ResponseHandler() {
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
        }else {

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

            AppDialog.showAlertDialog(getActivity(), "Activate Lock", msg,
                    "OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    getLocationPermission(new RequestPermissionAction() {
                                              @Override
                                              public void permissionDenied() {

                                              }

                                              @Override
                                              public void permissionGranted() {
                                                  doReWritePin(getContext(), mLock);
                                              }
                                          });

                }
            }, "CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        }


    }

    private void doReWritePin(Context context, Lock mLock) {

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
                        doReWritePinRequest(context,mLock);
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

    private void doReWritePinRequest(Context context, Lock mLock) {
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
        final ReWritePin reWritePin = new ReWritePin();
        reWritePin.setOwnerId(AESEncryption.getInstance().decrypt(id, mLock.isEncrypted()));
        reWritePin.setSlotKey(AESEncryption.getInstance().decrypt(key, mLock.isEncrypted()));

        String pin1 = convertHashMapToString(digiPinModelArrayList.get(0).getPinValue());
        reWritePin.setPin1((pin1 != null && !pin1.isEmpty()) ? pin1 : "0000");

        String pin2 = convertHashMapToString(digiPinModelArrayList.get(1).getPinValue());
        reWritePin.setPin2((pin2 != null && !pin2.isEmpty()) ? pin2 : "0000");

        String pin3 = convertHashMapToString(digiPinModelArrayList.get(2).getPinValue());
        reWritePin.setPin3((pin3 != null && !pin3.isEmpty()) ? pin3 : "0000");

        String pin4 = convertHashMapToString(digiPinModelArrayList.get(3).getPinValue());
        reWritePin.setPin4((pin4 != null && !pin4.isEmpty()) ? pin4 : "0000");

        String pin5 = convertHashMapToString(digiPinModelArrayList.get(4).getPinValue());
        reWritePin.setPin5((pin5 != null && !pin5.isEmpty()) ? pin5 : "0000");

        String pin6 = convertHashMapToString(digiPinModelArrayList.get(5).getPinValue());
        reWritePin.setPin6((pin6 != null && !pin6.isEmpty()) ? pin6 : "0000");

        String pin7 = convertHashMapToString(digiPinModelArrayList.get(6).getPinValue());
        reWritePin.setPin7((pin7 != null && !pin7.isEmpty()) ? pin7 : "0000");

        String pin8 = convertHashMapToString(digiPinModelArrayList.get(7).getPinValue());
        reWritePin.setPin8((pin1 != null && !pin8.isEmpty()) ? pin8 : "0000");

        String pin9 = convertHashMapToString(digiPinModelArrayList.get(8).getPinValue());
        reWritePin.setPin9((pin9 != null && !pin9.isEmpty()) ? pin9 : "0000");

        ServiceManager.getInstance().post(ServiceUrl.WIFI_LOCK_RE_WRITE_PIN, reWritePin, new ResponseHandler() {
            @Override
            public void onSuccess(Object data) {
                Loader.getInstance().hideLoader();
                if (data != null) {
                    WifiLockResponse reWritePinResponse = new Gson().fromJson(data.toString(), WifiLockResponse.class);
                    if (reWritePinResponse != null && reWritePinResponse.getStatus() != null
                            && reWritePinResponse.getStatus().equalsIgnoreCase("success")) {
                        forgetWIFINetwork(context, mLock);
                        AppDialog.showAlertDialog(context, "Success", "PIN Updated Successfully", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                doAddUpdatePin(context, mLock.getId());
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
                    if(message != null && message.equals("No Internet. Please Check your network connection.")){
                        AppDialog.showAlertDialog(context,message);
                    }else {
                        WifiLockResponse mWifiLockResponse = new Gson().fromJson(message, WifiLockResponse.class);
                        String errorMsg = "";
                        if (mWifiLockResponse != null && mWifiLockResponse.getErrorMessage() != null) {
                            forgetWIFINetwork(context, mLock);
                            switch (mWifiLockResponse.getErrorMessage()) {
                                case ManagePinConstant.TP_PIN_OTP_INVALID_FORMAT:
                                    errorMsg = "Invalid PIN format";
                                    break;
                                case ManagePinConstant.TP_PIN_OTP_INVALID_LEN:
                                    errorMsg = "Invalid PIN length";
                                    break;
                                case ManagePinConstant.TP_PIN_OTP_ALREADY_EXISTS:
                                    errorMsg = "PIN already exists";
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

    private String convertHashMapToString(LinkedTreeMap<Integer, String> pinValue){
        String str = "";

        for (LinkedTreeMap.Entry<Integer,String> entry : pinValue.entrySet()){
            System.out.println("Key = " + entry.getKey() +
                    ", Value = " + entry.getValue());
            str += entry.getValue();
        }

        return str;
    }

    private void doAddUpdatePin(Context context, String lockId) {
        AESEncryption aesEncryption = AESEncryption.getInstance();
        ArrayList<Pin> pinArrayList = new ArrayList<Pin>();

        for (int i = 0; i < digiPinModelArrayList.size(); i++) {

            DigiPinModel digiPinModel = digiPinModelArrayList.get(i);
            Pin pin = new Pin();
            String pinValue = digiPinModel.getPinValue() != null ? convertHashMapToString(digiPinModel.getPinValue()) : "";
            pin.setName(digiPinModel.getEditTextValue() != null ? digiPinModel.getEditTextValue() : "");
            pin.setPin(!pinValue.isEmpty() ? aesEncryption.encrypt(pinValue, true) : "");
            pin.setSlotNumber(digiPinModel.getIndexValue());
            pinArrayList.add(pin);

            Logger.d("### pin Name = " + digiPinModel.getEditTextValue());
            Logger.d("### pin slot = " + digiPinModel.getIndexValue());
            Logger.d("### pin = " + pinValue);
            Logger.d("### pin Encrypt = " + pin.getPin());


        }

        final PinRequest payload = new PinRequest();
        payload.setLockId(lockId);
        payload.setLockPins(pinArrayList);

        storeOffline(payload);
    }

    private void storeOffline(PinRequest pinRequest) {
        SecuredStorageManager.getInstance().saveOfflinePin(pinRequest);
        SyncScheduler.getInstance().schedule(getActivity(), Constant.JOBS.ALL);
       // Toast.makeText(getContext(), "PIN updated successfully", Toast.LENGTH_LONG).show();
        if (getActivity() != null) {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        }
    }

    private boolean validateIsEmpty() {
        int count = 0;
        for (int i = 0; i < digiPinModelArrayList.size(); i++) {
            DigiPinModel editModel = digiPinModelArrayList.get(i);
            String pinName = editModel.getEditTextValue();
            String pinValue = convertHashMapToString(editModel.getPinValue());
            if ((pinName == null || pinName.isEmpty()) && (pinValue == null || pinValue.isEmpty())) {
                count++;
            }
        }
        if (count == 9)
            AppDialog.showAlertDialog(getContext(), "Please enter one Name and PIN");
        return (count == 9) ? true : false;
    }

    private boolean validateDuplicatePinName() {
        boolean returnValue = true;
        for (int i = 0; i < digiPinModelArrayList.size(); i++) {
            DigiPinModel editModel = digiPinModelArrayList.get(i);
            String pinName = editModel.getEditTextValue();
            if (pinName != null && !pinName.isEmpty()) {
                for (int j = 0; j < digiPinModelArrayList.size(); j++) {
                    if (i != j) {
                        DigiPinModel editModelCompare = digiPinModelArrayList.get(j);
                        String pinNameCompare = editModelCompare.getEditTextValue();
                        if (pinNameCompare != null && pinName.equals(pinNameCompare)) {
                            AppDialog.showAlertDialog(getContext(), "Name(" + (i + 1) + ") & Name(" + (j + 1) + ") are same");
                            returnValue = false;
                            break;
                        }
                    }
                }
                if (!returnValue)
                    break;
            }
        }
        return returnValue;
    }

    private boolean validateDuplicatePin() {
        boolean returnValue = true;
        for (int i = 0; i < digiPinModelArrayList.size(); i++) {
            DigiPinModel editModel = digiPinModelArrayList.get(i);
            String pinValue = convertHashMapToString(editModel.getPinValue());
            if (pinValue != null && !pinValue.isEmpty()) {
                for (int j = 0; j < digiPinModelArrayList.size(); j++) {
                    if (i != j) {
                        DigiPinModel editModelCompare = digiPinModelArrayList.get(j);
                        String pinValueCompare = convertHashMapToString(editModelCompare.getPinValue());
                        if (pinValueCompare != null && pinValue.equals(pinValueCompare)) {
                            AppDialog.showAlertDialog(getContext(), "PIN(" + (i + 1) + ") & PIN(" + (j + 1) + ") are same");
                            returnValue = false;
                            break;
                        }
                    }
                }
                if (!returnValue)
                    break;
            }
        }
        return returnValue;
    }

    private boolean validateNameAndPin() {
        boolean returnValue = true;
        for (int i = 0; i < digiPinModelArrayList.size(); i++) {
            DigiPinModel editModel = digiPinModelArrayList.get(i);
            String pinName = editModel.getEditTextValue();
            String pinValue = convertHashMapToString(editModel.getPinValue());

            if (pinName != null && !pinName.isEmpty()) {
                if (pinValue == null || pinValue.isEmpty()) {
                    AppDialog.showAlertDialog(getContext(), "PIN(" + (i + 1) + ") can't be empty");
                    returnValue = false;
                    break;
                }
            }

            if (pinName != null && !pinName.isEmpty()) {
                if (pinValue != null && pinValue.length() < 4) {
                    AppDialog.showAlertDialog(getContext(), "PIN(" + (i + 1) + ") is invalid, Please enter four digit PIN");
                    returnValue = false;
                    break;
                }
            }

            if (pinName != null && !pinName.isEmpty()) {
                if (pinValue != null && pinValue.equalsIgnoreCase("0000")) {
                    AppDialog.showAlertDialog(getContext(), "PIN(" + (i + 1) + ") can't be \'0000\'");
                    returnValue = false;
                    break;
                }
            }

            if (pinValue != null && !pinValue.isEmpty()) {
                if (pinName == null || pinName.isEmpty()) {
                    AppDialog.showAlertDialog(getContext(), "Name(" + (i + 1) + ") can't be empty");
                    returnValue = false;
                    break;
                }
            }
        }
        return returnValue;
    }

    @Override
    public void onResume() {
        super.onResume();
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

   /* *//**
     * Check Read SMS Permission is enabled
     *
     * @param onPermissionCallBack
     *//*

    private RequestPermissionAction onPermissionCallBack;
    private final int REQUEST_LOCATION_PERMISSION=1;
    private final int REQUEST_BATTERY_OPT_PERMISSION=2;
    private final int REQUEST_READ_CONTACT_PERMISSION=3;
    private final int REQUEST_BLUETOOTH_SCAN_CONNECT_PERMISSION=4;

    public void getLocationPermission(RequestPermissionAction onPermissionCallBack) {
        this.onPermissionCallBack = onPermissionCallBack;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
                return;
            }
        }
        if (onPermissionCallBack != null)
            onPermissionCallBack.permissionGranted();
    }

    public boolean checkPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public interface RequestPermissionAction {
        void permissionDenied();

        void permissionGranted();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (REQUEST_LOCATION_PERMISSION == requestCode) {
                Logger.d( "REQUEST_LOCATION_PERMISSION Permission Granted");
            } else if(REQUEST_BATTERY_OPT_PERMISSION==requestCode){
                Logger.d( "REQUEST_BATTERY_OPT_PERMISSION Permission Granted");
            }else if(REQUEST_BLUETOOTH_SCAN_CONNECT_PERMISSION==requestCode){
                Logger.d( "REQUEST_BLUETOOTH_SCAN_PERMISSION Permission Granted");
            }
            if (onPermissionCallBack != null)
                onPermissionCallBack.permissionGranted();

        } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            if (REQUEST_LOCATION_PERMISSION == requestCode) {
                Logger.d( "REQUEST_LOCATION_PERMISSION Permission Denied");
            }else if(REQUEST_BLUETOOTH_SCAN_CONNECT_PERMISSION==requestCode){
                Logger.d( "REQUEST_BLUETOOTH_SCAN_PERMISSION Permission Denied");
            }
            if (onPermissionCallBack != null)
                onPermissionCallBack.permissionDenied();
        }
    }

*/
}