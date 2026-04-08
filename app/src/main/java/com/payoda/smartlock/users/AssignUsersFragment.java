package com.payoda.smartlock.users;

import static android.app.Activity.RESULT_OK;
import static com.payoda.smartlock.constants.Constant.FP;
import static com.payoda.smartlock.constants.Constant.HW_VERSION_4_0;
import static com.payoda.smartlock.constants.Constant.HW_VERSION_6_0;
import static com.payoda.smartlock.constants.Constant.RFID;
import static com.payoda.smartlock.constants.Constant.WIFI_DELAY_TIME;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.payoda.countrydialcode.CountryCodePicker;
import com.payoda.smartlock.App;
import com.payoda.smartlock.BuildConfig;
import com.payoda.smartlock.R;
import com.payoda.smartlock.authentication.BaseFragment;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.constants.ServiceUrl;
import com.payoda.smartlock.fp.model.FPUser;
import com.payoda.smartlock.fp.presenter.FPPresenter;
import com.payoda.smartlock.locks.LockManager;
import com.payoda.smartlock.locks.callback.IKeyListener;
import com.payoda.smartlock.locks.model.Lock;
import com.payoda.smartlock.locks.model.LockAddResponse;
import com.payoda.smartlock.locks.model.LockKeys;
import com.payoda.smartlock.locks.model.LockUser;
import com.payoda.smartlock.locks.model.WifiLock;
import com.payoda.smartlock.locks.model.WifiLockResponse;
import com.payoda.smartlock.plugins.bluetooth.BleManager;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.plugins.pushnotification.RemoteDataEvent;
import com.payoda.smartlock.plugins.storage.SecuredStorageManager;
import com.payoda.smartlock.plugins.storage.lock.LockDBClient;
import com.payoda.smartlock.plugins.wifi.WifiLockManager;
import com.payoda.smartlock.plugins.wifi.WifiUtilManager;
import com.payoda.smartlock.service.AESEncryption;
import com.payoda.smartlock.service.SyncScheduler;
import com.payoda.smartlock.users.adapter.MasterUserListAdapter;
import com.payoda.smartlock.users.model.AssignUser;
import com.payoda.smartlock.users.model.AssignUserRequest;
import com.payoda.smartlock.users.model.AssignUserResponse;
import com.payoda.smartlock.users.model.RequestUser;
import com.payoda.smartlock.users.model.ScheduleUserKeys;
import com.payoda.smartlock.users.service.AssignRequestUserService;
import com.payoda.smartlock.users.service.RequestsUserService;
import com.payoda.smartlock.utils.AppDialog;
import com.payoda.smartlock.utils.HexUtils;
import com.payoda.smartlock.utils.Loader;
import com.payoda.smartlock.utils.Logger;
import com.payoda.smartlock.utils.SLViewBinder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;

import io.michaelrocks.libphonenumber.android.NumberParseException;
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;
import io.michaelrocks.libphonenumber.android.Phonenumber;

/**
 * Created by david on 6/15/2018.
 */

public class AssignUsersFragment extends BaseFragment implements View.OnClickListener,
        SwipeRefreshLayout.OnRefreshListener, SLViewBinder {

    private static final int RESULT_PICK_CONTACT = 100;
    //private ScrollView swipeRefreshLayout;
    private ExpandableListView listMaster;
    private ArrayList<LockKeys> masterKeyList = new ArrayList<>();
    private HashMap<String, ArrayList<LockKeys>> masterUserKeyList = new HashMap<>();
    private MasterUserListAdapter masterUserListAdapter = null;
    private Lock mLock;
    private final int REQ_ADD_PRIVILEGE=1;
    private AssignUser masterUserList;

    private LockKeys selectedLockKeys;
    private RequestUser selectedRequestUser;
    private String userType = null;
    private TextView lblTitleMaster;
    private final int LIST_USER = 1;
    private final int ADD_USER = 2;
    private final int WITHDRAW_USER = 3;
    private final int REVOKE_USER = 4;
    private final int REVOKE_FP = 5;
    private int initialLoad = 0;

    public AssignUsersFragment() {
        // Required empty public constructor
    }

    public static AssignUsersFragment getInstance() {
        return new AssignUsersFragment();
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

        Logger.d("### AssignUsersFragment");
        // Inflate the layout for this fragment
        return initializeView(inflater.inflate(R.layout.fragment_assign_users, container, false));
    }

    private View initializeView(View view) {

        /*Toolbar Start*/
        ((TextView) view.findViewById(R.id.tv_title)).setText(getString(R.string.assign_users));
        lblTitleMaster = view.findViewById(R.id.lblTitleMaster);
        view.findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();

            }
        });
        /*Toolbar Ends*/
        //swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        //swipeRefreshLayout.setOnRefreshListener(this);
        listMaster = view.findViewById(R.id.listMaster);

        doAssignUserRequest(LIST_USER, "");
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //doAssignUserRequest(LIST_USER, "");
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
        if((remoteDataEvent != null && remoteDataEvent.getStatus().equalsIgnoreCase(Constant.SUCCESS))
                && (remoteDataEvent.getCommand().equalsIgnoreCase(Constant.MASTER_REVOKE_COMMAND)
                || remoteDataEvent.getCommand().equalsIgnoreCase(Constant.USER_REVOKE_COMMAND))){
            Loader.getInstance().hideLoader();
            AppDialog.showAlertDialog(getContext(), remoteDataEvent.getTitle(),
                    remoteDataEvent.getBody(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    doAssignUserRequest(LIST_USER, "");
                }
            });
        }else if((remoteDataEvent != null && remoteDataEvent.getStatus().equalsIgnoreCase(Constant.FAILURE))
                && (remoteDataEvent.getCommand().equalsIgnoreCase(Constant.MASTER_REVOKE_COMMAND)
                || remoteDataEvent.getCommand().equalsIgnoreCase(Constant.USER_REVOKE_COMMAND))){
            Loader.getInstance().hideLoader();
            AppDialog.showAlertDialog(getActivity(), remoteDataEvent.getBody());
        }
    }

    private void updateUI() {
        initialLoad++;
        if (mLock != null && mLock.getLockKeys() != null && !mLock.getLockKeys().isEmpty()) {
            String mLockUserType = Constant.USER;
            for (LockKeys lockKeys : mLock.getLockKeys()) {
                if (!lockKeys.getUserType().equalsIgnoreCase(Constant.OWNER_ID)) {
                    mLockUserType = lockKeys.getUserType();
                    break;
                }
            }
            userType = mLockUserType;
            if (mLockUserType.equalsIgnoreCase(Constant.OWNER) || mLockUserType.equalsIgnoreCase(Constant.MASTER)) {
                //linMasterList.setVisibility(View.VISIBLE);
                //linGeneralUsersList.setVisibility(View.VISIBLE);


                if (masterUserListAdapter == null) {



                    masterUserListAdapter = new MasterUserListAdapter(getContext(), masterKeyList,
                            masterUserKeyList, mLock.getPrivilege(), mLock.getLockVersion()) {



                        @Override
                        public void onInfoIconClick(LockKeys lockKeys) {
                            App.getInstance().showFullScreen(getActivity(), Constant.SCREEN.PROFILE, new Gson().toJson(lockKeys));
                        }

                        @Override
                        public void onActionButtonClick(String action, LockKeys lockKeys, RequestUser requestUser) {
                            selectedLockKeys = lockKeys;
                            selectedRequestUser = requestUser;

                            Logger.d("@@@@@@ requestUser : "+requestUser);
                            Logger.d("@@@@@@ lockKeys : "+lockKeys);
                            Logger.d("@@@@@@ selectedLockKeys : "+selectedLockKeys);
                            Logger.d("@@@@@@ selectedRequestUser : "+selectedRequestUser);


                            if (action.equalsIgnoreCase(getString(R.string.add_text))) {

                                if(checkPermission(Manifest.permission.READ_CONTACTS)) {
                                    pickContact();
                                }
                                else {
                                    getReadContactPermission(new BaseFragment.RequestPermissionAction() {
                                        @Override
                                        public void permissionDenied() {
                                            AppDialog.showAlertDialog(getContext(), "Kindly accept the contact permission in the app settings. In some device may not work giving access");
                                        }

                                        @Override
                                        public void permissionGranted() {
                                            pickContact();
                                        }
                                    });
                                }
                            }
                            else {
                                doUpdateRequestUser(action);
                            }
                        }

                        @Override
                        public void onScheduleIconClick(LockKeys lockKeys) {
                            ScheduleUserKeys scheduleUserKeys = new ScheduleUserKeys();
                            scheduleUserKeys.setLockKeys(lockKeys);
                            scheduleUserKeys.setEditable(true);
                            if (mLock.getLockKeys().get(1).getUserType().equalsIgnoreCase(Constant.OWNER)) {
                                int slotId = Integer.parseInt(lockKeys.getSlotNumber());
                                if (slotId >= 1 && slotId <= 8) {
                                    scheduleUserKeys.setEditable(true);
                                } else {
                                    scheduleUserKeys.setEditable(false);
                                }
                            }
                            App.getInstance().showFullScreen(getActivity(), Constant.SCREEN.SCHEDULE, new Gson().toJson(scheduleUserKeys));
                        }

                        @Override
                        public void onFPAccessButtonClick(LockKeys lockKeys) {
                            selectedLockKeys=lockKeys;
                            FPPresenter fpPresenter=new FPPresenter(AssignUsersFragment.this);
                            JsonObject jsonObject=new JsonObject();
                            jsonObject.addProperty("lock_id",mLock.getId());

                            if(mLock.getPrivilege()!=null && mLock.getPrivilege().equalsIgnoreCase(lockKeys.getLockUser().getId())){
                                jsonObject.addProperty("user_id", "");
                            }else {
                                jsonObject.addProperty("user_id", lockKeys.getLockUser().getId());
                            }
                            fpPresenter.addOrRevokePrivilege(getContext(),jsonObject,REQ_ADD_PRIVILEGE);


                        }
                    };
                    listMaster.setAdapter(masterUserListAdapter);
                } else {
                    masterUserListAdapter.setValue(masterKeyList, masterUserKeyList);
                }

                listMaster.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                    @Override
                    public boolean onGroupClick(ExpandableListView expandableListView, View view, int position, long l) {
                        if (position == masterKeyList.size() - 1) {
                            return true;
                        }
                        return false;
                    }
                });

                if (initialLoad == 2) {
                    for (int i = 0; i < masterKeyList.size(); i++) {
                        if (i == masterKeyList.size() - 1) {
                            listMaster.expandGroup(i);
                        }
                    }
                }
            }

        }
    }

    private void doAssignUserRequest(final int requestCode, final String message) {
        String userType = Constant.USER;
        if (mLock != null && mLock.getLockKeys() != null && !mLock.getLockKeys().isEmpty()) {
            String mLockUserType = Constant.USER;
            for (LockKeys lockKeys : mLock.getLockKeys()) {
                if (!lockKeys.getUserType().equalsIgnoreCase(Constant.OWNER_ID)) {
                    mLockUserType = lockKeys.getUserType();
                    break;
                }
            }
            userType = mLockUserType;
        }
        String type = "";
        if(userType.equalsIgnoreCase(Constant.OWNER)){
            type = Constant.MASTER + "," + Constant.USER;
        }else if(userType.equalsIgnoreCase(Constant.MASTER)){
            type = Constant.USER;
        }
        if (mLock != null) {
            Loader.getInstance().showLoader(getContext());
            // Owner = 0 - To get key list
            // Owner - 1 - To get Owner ID and Owner Key
            AssignRequestUserService.getInstance().getAssignUserList(mLock.getId(), "0", type, new ResponseHandler() {
                @Override
                public void onSuccess(Object data) {
                    hideRefreshing();
                    Loader.getInstance().hideLoader();
                    if (data != null) {
                        updateUI();
                        masterUserList = (AssignUser) data;
                        prepopulateData(masterUserList.getKeys());
                    }
                    if (ADD_USER == requestCode) {
                        String msg = message != null ? message : "Request sent successfully";
                        AppDialog.showAlertDialog(getActivity(), msg);
                    } else if (WITHDRAW_USER == requestCode) {
                        String msg = message != null ? message : "Request withdrawn successfully";
                        AppDialog.showAlertDialog(getActivity(), msg);
                    } else if (REVOKE_USER == requestCode) {
                        String msg = message != null ? message : "Access revoked successfully";
                        AppDialog.showAlertDialog(getActivity(), msg);
                    }
                }

                @Override
                public void onAuthError(String message) {
                    hideRefreshing();
                    Loader.getInstance().hideLoader();
                    hideList();
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
                    hideRefreshing();
                    Loader.getInstance().hideLoader();
                    hideList();
                    AppDialog.showAlertDialog(getContext(), message);
                }
            });
        }
    }

    HashMap<String, ArrayList<LockKeys>> fpUsers = new HashMap<>();

    private void splitFPUsers(ArrayList<LockKeys> fpKeys){
        for(LockKeys lockKeys:fpKeys){
            if(fpUsers.containsKey(lockKeys.getGuestId())){
                fpUsers.get(lockKeys.getGuestId()).add(lockKeys);
            }else{
                ArrayList<LockKeys> newFPuser=new ArrayList<>();
                newFPuser.add(lockKeys);
                fpUsers.put(lockKeys.getGuestId(),newFPuser);
            }
        }
    }

    private void prepopulateData(ArrayList<LockKeys> lockKeysList) {
        masterKeyList.clear();
        masterUserKeyList.clear();
        splitFPUsers(lockKeysList);

        if (lockKeysList != null && lockKeysList.size() > 0) {
            String name = "";
            String master1Id = "Master", master2Id = "Master", master3Id = "Master";
            for (int i = 0; i < lockKeysList.size(); i++) {
                LockKeys lockKeys = lockKeysList.get(i);
                if (lockKeys.getUserType().equalsIgnoreCase(RFID) ||
                        lockKeys.getUserType().equalsIgnoreCase(FP)) {
                    continue;
                }
                name = lockKeys.getName();
                if (lockKeys.getLockUser() != null && !TextUtils.isEmpty(lockKeys.getLockUser().getUsername())) {
                    name = lockKeys.getLockUser().getUsername();
                }
                lockKeys.setName(name);
                if(lockKeys.getLockUser()!=null && lockKeys.getLockUser().getId()!=null){
                    if(fpUsers.containsKey(lockKeys.getLockUser().getId())){
                        lockKeys.setFpUsers(fpUsers.get(lockKeys.getLockUser().getId()));
                    }
                }
                RequestUser requestUser = new RequestUser();
                requestUser.setKeyId(lockKeys.getId());
                requestUser.setSlotNumber(lockKeys.getSlotNumber());
                requestUser.setKey(lockKeys.getKey());
                requestUser.setUserId(lockKeys.getUserId());
                if (getTextLabel(lockKeys.getStatus(), lockKeys.getLockUser()).equalsIgnoreCase("withdraw")) {
                    if (lockKeys.getRequestDetail() != null) {
                        requestUser.setRequestId(lockKeys.getRequestDetail().getId());
                    }
                } else {
                    requestUser.setRequestId(lockKeys.getId());
                }
                switch (lockKeys.getSlotNumber()) {
                    case "01" -> {
                        master1Id = lockKeys.getId();
                        masterKeyList.add(lockKeys);
                    }
                    case "02" -> {
                        master2Id = lockKeys.getId();
                        masterKeyList.add(lockKeys);
                    }
                    case "03" -> {
                        master3Id = lockKeys.getId();
                        masterKeyList.add(lockKeys);
                    }
                    case "04" -> addUserUnderMaster(lockKeys, "OWNER");
                    case "05" -> addUserUnderMaster(lockKeys, "OWNER");
                    case "06" -> addUserUnderMaster(lockKeys, "OWNER");
                    case "07" -> addUserUnderMaster(lockKeys, "OWNER");
                    case "08" -> addUserUnderMaster(lockKeys, "OWNER");
                    case "09" -> addUserUnderMaster(lockKeys, master1Id);
                    case "10" -> addUserUnderMaster(lockKeys, master1Id);
                    case "11" -> addUserUnderMaster(lockKeys, master1Id);
                    case "12" -> addUserUnderMaster(lockKeys, master1Id);
                    case "13" -> addUserUnderMaster(lockKeys, master1Id);
                    case "14" -> addUserUnderMaster(lockKeys, master2Id);
                    case "15" -> addUserUnderMaster(lockKeys, master2Id);
                    case "16" -> addUserUnderMaster(lockKeys, master2Id);
                    case "17" -> addUserUnderMaster(lockKeys, master2Id);
                    case "18" -> addUserUnderMaster(lockKeys, master2Id);
                    case "19" -> addUserUnderMaster(lockKeys, master3Id);
                    case "20" -> addUserUnderMaster(lockKeys, master3Id);
                    case "21" -> addUserUnderMaster(lockKeys, master3Id);
                    case "22" -> addUserUnderMaster(lockKeys, master3Id);
                    case "23" -> addUserUnderMaster(lockKeys, master3Id);
                }
            }
            if (userType.equalsIgnoreCase(Constant.OWNER)) {
                LockKeys owner = new LockKeys();
                owner.setId("OWNER");
                owner.setName("GENRAL USER(S)");
                masterKeyList.add(owner);
            } else if (userType.equalsIgnoreCase(Constant.MASTER)) {
                lblTitleMaster.setVisibility(View.GONE);
                LockKeys owner = new LockKeys();
                owner.setId("Master");
                owner.setName("Master USER(S)");
                masterKeyList.add(owner);
            }
            updateUI();
        }

    }

    private void addUserUnderMaster(LockKeys lockKeys, String masterId) {
        ArrayList<LockKeys> extList = null;
        if (masterUserKeyList.containsKey(masterId)) {
            masterUserKeyList.get(masterId).add(lockKeys);
        } else {
            extList = new ArrayList<>();
            extList.add(lockKeys);
            masterUserKeyList.put(masterId, extList);
        }
    }

    private String getTextLabel(String status, LockUser lockUser) {
        String label = "Add";
        switch (status) {
            case Constant.INACTIVE:
                if (lockUser == null) {
                    label = getResources().getString(R.string.add_text);
                } else {
                    label = getResources().getString(R.string.withdraw_text);
                }
                return label;
            case Constant.ACTIVE:
                if (lockUser != null) {
                    label = getResources().getString(R.string.revoke_text);
                }
                return label;
            default:
                break;
        }
        return label;
    }

    @Override
    public void onClick(View view) {

    }

    private void pickContact() {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT);
        /*Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT);*/
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // check whether the result is ok
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RESULT_PICK_CONTACT:
                    populateContactData(data);
                    break;
            }
        } else {
            Log.e("Activity", "Please ensure you are near the lock before accepting the request. Please engage with the lock immediately after accepting the request and connect to internet for security reasons.");
        }
    }

    private void populateContactData(Intent data) {
        if (data != null) {
            Cursor cursor = null;
            try {
                String phoneNo = null;
                String name = null;
                // getData() method will have the Content Uri of the selected contact
                Uri uri = data.getData();
                //Query the content uri

                cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
                cursor.moveToFirst();
                // column index of the phone number
                int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                // column index of the contact name
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                phoneNo = cursor.getString(phoneIndex).replaceAll("[^\\d.]", "");
                name = cursor.getString(nameIndex);
                // Set the value to the textview
//                lblContactUpdate.setText(name);
                //Update the text
                if (selectedRequestUser != null) {
                    showContactEditDialog(phoneNo, name);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showContactEditDialog(String selectedContactPhone, String selectedContactName){
        if (getContext() != null) {
            try {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                LayoutInflater inflater = getLayoutInflater();
                View dialogLayout = inflater.inflate(R.layout.contact_edit_dialog, null);
                TextView tvTitle = dialogLayout.findViewById(R.id.dlgSelectedContactTextView);
                CountryCodePicker countryCodePicker = dialogLayout.findViewById(R.id.dlgContactCountryCodePicker);
                TextInputLayout mMobileWrapper = dialogLayout.findViewById(R.id.dlgContactMobileWrapper);
                EditText etMobileNumber = dialogLayout.findViewById(R.id.dlgContactMobileEditText);

                tvTitle.setText("Please confirm this mobile number: " + selectedContactPhone);
                etMobileNumber.setText(selectedContactPhone);

                etMobileNumber.addTextChangedListener(new AssignUsersFragment.MyTextWatcher(etMobileNumber, countryCodePicker, mMobileWrapper));

                builder.setPositiveButton("Confirm", null);
                builder.setNegativeButton("Cancel",null);

                builder.setView(dialogLayout);

                final AlertDialog mAlertDialog = builder.create();
                mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

                    @Override
                    public void onShow(DialogInterface dialog) {
                        Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

                        b.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (validateMobile(mMobileWrapper, etMobileNumber, countryCodePicker)) {
                                    mAlertDialog.dismiss();

                                    RequestUser requestUser = selectedRequestUser;
                                    requestUser.setCountryCode(countryCodePicker.getSelectedCountryNameCode());
                                    requestUser.setMobile(etMobileNumber.getText().toString());
                                    //Add
                                    requestUser.setStatus("0");
                                    doCreateRequestUser(selectedContactName, requestUser);
                                }
                            }
                        });
                    }
                });

                mAlertDialog.setCancelable(false);
                mAlertDialog.show();
            }catch (Exception e) {
                Logger.e(e);
            }
        }
    }

    private boolean validateMobile(TextInputLayout mobileWrapper, EditText etMobileNumber, CountryCodePicker countryCodePicker) {
        String mobile = etMobileNumber.getText().toString();
        PhoneNumberUtil phoneUtil = null;
        if (mobile.isEmpty()) {
            mobileWrapper.setError("Mobile Number is mandatory");
            return false;
        } else {
            try {
                if (phoneUtil == null) {
                    phoneUtil = PhoneNumberUtil.createInstance(getContext().getApplicationContext());
                }
                final Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(mobile, countryCodePicker.getSelectedCountryNameCode());
                boolean isValid = phoneUtil.isValidNumber(phoneNumber);
                if(!isValid){
                    mobileWrapper.setError("Please enter a valid mobile number");
                    return false;
                }
            } catch (NumberParseException e) {
                e.printStackTrace();
                mobileWrapper.setError("Please enter a valid mobile number");
                return false;
            }
        }
        mobileWrapper.setErrorEnabled(false);
        return true;
    }

    private void doCreateRequestUser(final String username, final RequestUser requestUser) {
        Loader.getInstance().showLoader(getContext());
        RequestsUserService.getInstance().createRequest(requestUser, new ResponseHandler() {
            @Override
            public void onSuccess(Object data) {
                Loader.getInstance().hideLoader();
                if (data != null) {
                    AssignUserResponse assignUserResponse = (AssignUserResponse) data;
                    if (assignUserResponse.getStatus().equalsIgnoreCase("success")) {
                        //Update Withdraw functionality
                        String message = assignUserResponse.getMessage();
                        //Assign Users List
                        doAssignUserRequest(ADD_USER, message);
                    }
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
    }

    private void showList() {
        //linMasterList.setVisibility(View.VISIBLE);
        //linGeneralUsersList.setVisibility(View.VISIBLE);
    }

    private void hideList() {
        //linMasterList.setVisibility(View.GONE);
        //linGeneralUsersList.setVisibility(View.GONE);
    }

    private void doUpdateRequestUser(String action) {

        RequestUser requestUser = null;
        AssignUserRequest payload = new AssignUserRequest();

        if (selectedRequestUser != null) {
            requestUser = selectedRequestUser;
            try {
                if (action.equalsIgnoreCase(getString(R.string.withdraw_text))) {
                    Loader.getInstance().showLoader(getContext());
                    payload.setStatus("3");
                    RequestsUserService.getInstance().updateRequest(payload, requestUser.getRequestId(), new ResponseHandler() {
                        @Override
                        public void onSuccess(Object data) {
                            Loader.getInstance().hideLoader();
                            if (data != null) {
                                AssignUserResponse assignUserResponse = (AssignUserResponse) data;
                                if (assignUserResponse.getStatus().equalsIgnoreCase("success")) {
                                    //Assign Users List
                                    String msg = assignUserResponse.getMessage();
                                    doAssignUserRequest(WITHDRAW_USER, msg);
                                }
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
                }
                else {
                    if (mLock.getLockVersion().equalsIgnoreCase(HW_VERSION_4_0)) {
                        try {
                            Loader.getInstance().showLoader(getContext());
                            AssignRequestUserService.getInstance().revokeAccessMqtt(mLock.getSerialNumber(),
                                    selectedLockKeys.getUserType(),
                                    selectedRequestUser.getUserId(),
                                    new ResponseHandler() {
                                @Override
                                public void onSuccess(Object data) {
                                    Logger.d("revokeUserMqtt", data.toString());
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

                        }catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                    else {
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
                                getLocationPermission(new BaseFragment.RequestPermissionAction() {
                                    @Override
                                    public void permissionDenied() {

                                    }

                                    @Override
                                    public void permissionGranted() {
                                        doLockRevokeRequest();
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    String key;
    String slotId;
    String id = null;
    String tempKey = null;
    int rewriteSlotNumber;
    private LockKeys fpKeys=null;

    private void doLockRevokeRequest() {
        // Revoke User
        try {

            Logger.d("@@@@@@ selectedLockKeys 123 : "+selectedLockKeys);
            Logger.d("@@@@@@ selectedRequestUser 123  : "+selectedRequestUser);

            RequestUser requestUser = selectedRequestUser;
            Logger.d("@@@@@ rewriteSlotNumber a : "+ rewriteSlotNumber);
            rewriteSlotNumber = Integer.parseInt(selectedRequestUser.getSlotNumber());
            Logger.d("@@@@@ rewriteSlotNumber b : "+ rewriteSlotNumber);


            slotId = rewriteSlotNumber + "";

            Logger.d("@@@@@ slotId b : "+ slotId);

            for (LockKeys lockKeys : mLock.getLockKeys()) {
                if (lockKeys.getUserType().equalsIgnoreCase(Constant.OWNER_ID)) {
                    id = AESEncryption.getInstance().decrypt(lockKeys.getKey(),mLock.isEncrypted());
                } else {
                    tempKey = AESEncryption.getInstance().decrypt(lockKeys.getKey(),mLock.isEncrypted());
                }
            }
            key = id + tempKey;
            slotId = slotId.length() == 1 ? "0" + slotId : slotId;

            Logger.d("@@@@@ slotId a : "+ slotId);


            fpKeys=null;
            if(masterUserList!=null && masterUserList.getKeys()!=null) {
                for (int i = 0; i < masterUserList.getKeys().size(); i++) {
                    LockKeys lockKeys = masterUserList.getKeys().get(i);
                    if (lockKeys.getUserType() != null && lockKeys.getUserType().equalsIgnoreCase("Fingerprint")) {
                        if (lockKeys.getLockUser()!=null &&lockKeys.getLockUser().getId()!=null
                                && lockKeys.getLockUser().getId().equalsIgnoreCase(requestUser.getUserId())) {
                            fpKeys = lockKeys;
                            break;
                        }
                    }
                }
            }
            Loader.getInstance().showLoader(getContext());
            if (WifiLockManager.getInstance().isWifiLockConnected(getContext())) {
                if(fpKeys==null) {
                    doRewriteSlotViaWifi();
                }else{
                    doRevokeFP();
                }
            }

            else {
                //connectDeviceWithWifi(REQ_REVOKE);
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
                                if(fpKeys==null) {
                                    doRewriteSlotViaWifi();
                                }else{
                                    doRevokeFP();

                                }
                            }
                        }, WIFI_DELAY_TIME);
                    }

                    @Override
                    public void connectionTimeOut() {
                        Logger.d("Connection Timeout");
                        if (!(mLock.getLockVersion() != null &&
                                (mLock.getLockVersion().equalsIgnoreCase(Constant.HW_VERSION_2) || mLock.getLockVersion().equalsIgnoreCase(Constant.HW_VERSION_2_1)
                                        || mLock.getLockVersion().equalsIgnoreCase(Constant.HW_VERSION_3)
                                        || mLock.getLockVersion().equalsIgnoreCase(Constant.HW_VERSION_3_1)
                                        || mLock.getLockVersion().equalsIgnoreCase(Constant.HW_VERSION_3_2)
                                        || mLock.getLockVersion().equalsIgnoreCase(HW_VERSION_4_0)
                                        || mLock.getLockVersion().equalsIgnoreCase(HW_VERSION_6_0)

                                ))) {

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                getBluetoothScanConnectPermission(new RequestPermissionAction() {
                                    @Override
                                    public void permissionDenied() {
                                        BleManager.getInstance().checkBluetoothScanPermission(getActivity());
                                    }

                                    @Override
                                    public void permissionGranted() {
                                        doRewriteSlotViaBLE();
                                    }
                                });
                            }else {
                                doRewriteSlotViaBLE();
                            }
                        }else{
                            AppDialog.showAlertDialog(getActivity(), getString(R.string.lock_failure));
                        }
                    }
                });
                wifiUtilManager.startScanning(lockVersion, SSID, password);
            }
        } catch (Exception e) {
            Logger.e(e);
        }
    }

    private void doRevokeFP(){
        FPUser fpUser=new FPUser();
        Type keyType = new TypeToken<ArrayList<String>>(){}.getType();
        Gson gson = new Gson();
        ArrayList<String> keys=gson.fromJson(fpKeys.getKey(),keyType);
        fpUser.setId(fpKeys.getId());
        fpUser.setKeys(keys);
        fpUser.setLockId(fpKeys.getLockId());
        fpUser.setUserId(fpKeys.getUserId());
        FPPresenter fpPresenter=new FPPresenter(this);
        fpPresenter.doRevokePrintRequest(getContext(),REVOKE_FP,mLock,fpUser);
    }

    private void doRewriteSlotViaBLE() {

        String ssid = BleManager.MANUFACTURER_CODE + mLock.getSerialNumber();
        LockManager.getInstance().reWriteSlot(getActivity(), mLock.getUuid(),ssid, key, rewriteSlotNumber, new IKeyListener() {
            @Override
            public void onLockIds(ArrayList<String> alIds) {

            }

            @Override
            public void onLockKeys(ArrayList<String> alKeys) {
                Loader.getInstance().hideLoader();
                if (alKeys != null && alKeys.size() > 0) {
                    doRevokeRequestUser(AESEncryption.getInstance().encrypt(alKeys.get(0),mLock.isEncrypted()));
                } else {
                    AppDialog.showAlertDialog(getActivity(), "Invalid slot number.");
                }
            }

            @Override
            public void onBatteryUpdate(String battery) {

            }

            @Override
            public void onAccessLog(ArrayList<String> alLogs) {

            }

            @Override
            public void onLockActivated() {

            }

            @Override
            public void onDeviceNotConnected() {
                Loader.getInstance().hideLoader();
                if (ServiceManager.getInstance().isMobileDataEnabled(getContext())) {
                    AppDialog.showAlertDialog(getContext(), "Please switch-off your mobile data and try again");
                } else {
                    AppDialog.showAlertDialog(getActivity(), getString(R.string.lock_failure));
                }
            }

            @Override
            public void onMacAddressUpdate(String ssid, String macAddress) {
                if(mLock != null){
                    mLock.setUuid(macAddress);
                    mLock.setSync(false);
                    LockDBClient.getInstance().save(mLock,getContext());
                }
            }

        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    private void doRewriteSlotViaWifi() {
        if (!WifiLockManager.getInstance().isWifiEnabled(getContext())) {
            AppDialog.showAlertDialog(getContext(), getString(R.string.turn_on_wifi_connect_lock));
        } else {
            Logger.d("@@@@@ slotId revoke : "+ slotId);

            WifiLock mWifiLock = new WifiLock();
            mWifiLock.setOwnerId(id);
            mWifiLock.setSlotKey(tempKey);
            //mWifiLock.setSlotId(HexUtils.intToHex(Integer.parseInt(slotId)));
            mWifiLock.setSlotId(slotId);


            ServiceManager.getInstance().post(ServiceUrl.WIFI_LOCK_REWRITE_SLOT, mWifiLock, new ResponseHandler() {
                @Override
                public void onSuccess(Object data) {

                    Loader.getInstance().hideLoader();

                    WifiLockResponse mWifiLockResponse = new Gson().fromJson(data.toString(), WifiLockResponse.class);

                    if (mWifiLockResponse != null && mWifiLockResponse.getResponse() != null
                            && mWifiLockResponse.getResponse().getSlotKey() != null) {
                        doRevokeRequestUser(AESEncryption.getInstance().encrypt(mWifiLockResponse.getResponse()
                                .getSlotKey(),mLock.isEncrypted()));
                    }
                    else {
                        AppDialog.showAlertDialog(getActivity(), "Invalid slot number.");
                    }

                    forgetWIFINetwork();
                }

                @Override
                public void onAuthError(String message) {
                    Loader.getInstance().hideLoader();
                }

                @Override
                public void onError(String message) {
                    Loader.getInstance().hideLoader();
                    if (ServiceManager.getInstance().isMobileDataEnabled(getContext())) {
                        AppDialog.showAlertDialog(getContext(), "Please switch-off your mobile data and try again");
                    } else {
                        AppDialog.showAlertDialog(getContext(), getContext().getString(R.string.lock_failure_alert));
                    }
                }
            });
        }
    }

    private void forgetWIFINetwork() {

        String SSID = BleManager.MANUFACTURER_CODE + mLock.getSerialNumber();
        WifiUtilManager.forgetMyNetwork(getContext(),SSID);


    }

    private void doRevokeRequestUser(String key) {
        RequestUser requestUser = null;
        if (selectedRequestUser != null) {
            requestUser = selectedRequestUser;
            try {
                requestUser.setUserId(requestUser.getUserId());
                requestUser.setKey(key);
                requestUser.setStatus("0");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        final RequestUser payload = requestUser;
        if (ServiceManager.getInstance().isNetworkAvailable(getActivity())) {
            final RequestUser finalRequestUser = requestUser;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Loader.getInstance().showLoader(getContext());
                    RequestsUserService.getInstance().revokeRequest(payload,
                            finalRequestUser.getRequestId(), new ResponseHandler() {
                        @Override
                        public void onSuccess(Object data) {
                            Loader.getInstance().hideLoader();
                            if (data != null) {
                                AssignUserResponse assignUserResponse = (AssignUserResponse) data;
                                if (assignUserResponse.getStatus().equalsIgnoreCase("success")) {
                                    //Assign Users List
                                    String msg = assignUserResponse.getMessage();
                                    doAssignUserRequest(REVOKE_USER, msg);
                                }
                            }
                        }

                        @Override
                        public void onAuthError(String message) {
                            Loader.getInstance().hideLoader();
                            storeOffline(finalRequestUser);
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
                            storeOffline(finalRequestUser);
                            AppDialog.showAlertDialog(getContext(), message);
                        }
                    });
                }
            });
        } else {
            storeOffline(payload);
        }

    }

    private void storeOffline(RequestUser requestUser) {
        SecuredStorageManager.getInstance().saveRevokeUserList(requestUser.getRequestId(), requestUser);
        SyncScheduler.getInstance().schedule(getActivity(), Constant.JOBS.ALL);
        Toast.makeText(getContext(), "Access revoked successfully", Toast.LENGTH_LONG).show();
        if (getActivity() != null) {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        }
    }

    @Override
    public void onRefresh() {
        doAssignUserRequest(LIST_USER, "");
    }

    private void showRefreshing() {
        /*if (swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(true);*/
    }

    private void hideRefreshing() {
        /*if (swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(false);*/
    }

    @Override
    public void onViewUpdate(int reqCode, Object response) {
        if(reqCode==REQ_ADD_PRIVILEGE){
            LockAddResponse baseResponse=new Gson().fromJson(response.toString(), LockAddResponse.class);
            if(baseResponse.getStatus().equalsIgnoreCase(Constant.SUCCESS)) {
                if(mLock.getPrivilege()!=null && mLock.getPrivilege().equalsIgnoreCase(selectedLockKeys.getLockUser().getId())){
                    masterUserListAdapter.setPrivilege("");
                    mLock.setPrivilege("");
                }else {
                    masterUserListAdapter.setPrivilege(selectedLockKeys.getLockUser().getId());
                    mLock.setPrivilege(selectedLockKeys.getLockUser().getId());
                }

            }
        }else if(reqCode==REVOKE_FP){
            doRewriteSlotViaWifi();
        }
    }

    private class MyTextWatcher implements TextWatcher {
        private Timer mTimer = new Timer();
        private final int DELAY = 1000;
        private View view;
        private CountryCodePicker countryCodePicker;
        private TextInputLayout mMobileWrapper;

        private MyTextWatcher(View view, CountryCodePicker countryCodePicker, TextInputLayout mobileWrapper) {
            this.view = view;
            this.countryCodePicker = countryCodePicker;
            this.mMobileWrapper = mobileWrapper;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.dlgContactMobileEditText:
                    validateMobile(mMobileWrapper, (EditText)view,countryCodePicker);
                    break;
            }
        }
    }
}
