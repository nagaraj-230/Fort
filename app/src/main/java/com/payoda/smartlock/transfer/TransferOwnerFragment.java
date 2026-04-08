package com.payoda.smartlock.transfer;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.payoda.countrydialcode.CountryCodePicker;
import com.payoda.smartlock.App;
import com.payoda.smartlock.R;
import com.payoda.smartlock.authentication.BaseFragment;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.locks.model.Lock;
import com.payoda.smartlock.locks.model.LockKeys;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.transfer.model.Transfer;
import com.payoda.smartlock.transfer.service.TransferService;
import com.payoda.smartlock.users.AssignUsersFragment;
import com.payoda.smartlock.users.model.AssignUser;
import com.payoda.smartlock.users.model.AssignUserRequest;
import com.payoda.smartlock.users.model.RequestUser;
import com.payoda.smartlock.users.service.AssignRequestUserService;
import com.payoda.smartlock.users.service.RequestsUserService;
import com.payoda.smartlock.utils.AppDialog;
import com.payoda.smartlock.utils.Loader;
import com.payoda.smartlock.utils.Logger;

import java.util.ArrayList;
import java.util.Timer;

import static android.app.Activity.RESULT_OK;

import io.michaelrocks.libphonenumber.android.NumberParseException;
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;
import io.michaelrocks.libphonenumber.android.Phonenumber;

/**
 * A simple {@link Fragment} subclass.
 */
public class TransferOwnerFragment extends BaseFragment implements ResponseHandler {

    public static final String TAG ="### TransferOwnerFragment";

    private static final int RESULT_PICK_CONTACT = 100;

    private TextView tvOwnerName, tvTrasferOwner, tvNewOwner;
    private Button btnAdd;
    Lock mLock;
    private boolean isTransferInitiated = false;
    private String requestToUserName = "";
    private String requestId=null;

    public TransferOwnerFragment() {
        // Required empty public constructor
    }

    public static TransferOwnerFragment getInstance() {
        return new TransferOwnerFragment();
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
        return initializeView(inflater.inflate(R.layout.fragment_transfer_owner, container, false));
    }

    @SuppressLint("ClickableViewAccessibility")
    private View initializeView(final View view) {
        /*Toolbar Start*/
        ((TextView) view.findViewById(R.id.tv_title)).setText(getString(R.string.transfer_owner));
        view.findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });
        /*Toolbar Ends*/
        tvTrasferOwner = view.findViewById(R.id.tv_trasfer_owner);
        tvOwnerName = view.findViewById(R.id.tv_owner_name);
        tvNewOwner = view.findViewById(R.id.tv_new_owner);
        btnAdd = view.findViewById(R.id.btn_add_continue);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnAdd.getText().toString().equalsIgnoreCase(getString(R.string.add))) {
                    if(checkPermission(Manifest.permission.READ_CONTACTS)) {
                        pickContact();
                    }else {
                        getReadContactPermission(new BaseFragment.RequestPermissionAction() {
                            @Override
                            public void permissionDenied() {
                                AppDialog.showAlertDialog(getContext(), "Kindly accept the contact permission in the app settings. " +
                                        "In some device may not work giving access");
                            }

                            @Override
                            public void permissionGranted() {
                                pickContact();
                            }
                        });
                    }
                } else if (btnAdd.getText().toString().equalsIgnoreCase(getString(R.string.withdraw_text))) {
                    if (ServiceManager.getInstance().isNetworkAvailable(getContext())) {
                        Loader.getInstance().showLoader(getContext());
                        AssignUserRequest payload = new AssignUserRequest();
                        payload.setStatus("3");
                        RequestsUserService.getInstance().updateRequest(payload, requestId, new ResponseHandler() {
                            @Override
                            public void onSuccess(Object data) {
                                Loader.getInstance().hideLoader();
                                if (data != null) {
                                    if (getActivity() != null) {
                                        requireActivity().getOnBackPressedDispatcher().onBackPressed();
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

                } else if (btnAdd.getText().toString().equalsIgnoreCase(getString(R.string.btn_continue))) {
                    if (getActivity() != null)
                        App.getInstance().showDashboard(getActivity());
                } else {
                    if (getActivity() != null)
                        getActivity().finish();
                }
            }
        });
        doAssignUserRequest();
        return view;
    }

    /**
     * Method to choose contact and update the detail
     */
    private void pickContact() {

        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);

        startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {

        // check whether the result is ok
        if (resultCode == RESULT_OK) {

            switch (requestCode) {
                case RESULT_PICK_CONTACT:
                    //Show confirm dialog over here
                    AppDialog.showAlertDialog(getContext(), "Transfer Owner",
                            "Do you want to confirm to change the lock owner?", "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            populateContactData(data);
                        }
                    }, "CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    break;
            }
        }
    }

    private void populateContactData(Intent data) {
        if (getActivity() != null && data != null) {
            Cursor cursor;
            try {
                String phoneNo = null;
                String name = null;
                // getData() method will have the Content Uri of the selected contact
                Uri uri = data.getData();
                if (uri != null) {
                    //Query the content uri
                    cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        // column index of the contact name
                        int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                        phoneNo = cursor.getString(phoneIndex).replaceAll("[^\\d.]", "");
                        name = cursor.getString(nameIndex);
                        tvOwnerName.setText(name);
                        cursor.close();
                        if (mLock != null) {
                            String keyID = null;
                            for (LockKeys lockKeys : mLock.getUnAssignedKeys()) {
                                if (lockKeys.getUserType().equalsIgnoreCase(Constant.OWNER_ID)) {
                                    keyID = lockKeys.getId();
                                    break;
                                }
                            }
                            showContactEditDialog(keyID, phoneNo);
                        } else {
                            //Show some error message.
                            AppDialog.showAlertDialog(getContext(), getString(R.string.oops_error));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showContactEditDialog(String keyId, String selectedContactPhone){

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

                etMobileNumber.addTextChangedListener(new TransferOwnerFragment.MyTextWatcher(etMobileNumber, countryCodePicker, mMobileWrapper));

                builder.setPositiveButton("Confirm", null);
                builder.setNegativeButton("Cancel",null);

                builder.setView(dialogLayout);

                final AlertDialog mAlertDialog = builder.create();

                mAlertDialog.setOnShowListener(dialog -> {

                    Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

                    b.setOnClickListener(view -> {

                        if (validateMobile(mMobileWrapper, etMobileNumber, countryCodePicker)) {

                            mAlertDialog.dismiss();

                            doTransferOwner(keyId, countryCodePicker.getSelectedCountryNameCode(), etMobileNumber.getText().toString() );

                        }

                    });

                });

                mAlertDialog.setCancelable(false);
                mAlertDialog.show();

            }catch (Exception e) {

                Logger.e(e);

            }
        }
    }

    private void doTransferOwner(String keyID, String countryCode, String phoneNo) {
        Transfer transfer = new Transfer();
        transfer.setKeyId(keyID);
        transfer.setStatus("0");
        transfer.setCountryCode(countryCode);
        transfer.setMobile(phoneNo);
        Loader.getInstance().showLoader(getContext());
        TransferService.getInstance().serviceRequest(transfer, this);

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

    @Override
    public void onSuccess(Object data) {
        Loader.getInstance().hideLoader();
        try {
            tvTrasferOwner.setText(getString(R.string.tv_transfer_owner_success));
            tvOwnerName.setVisibility(View.VISIBLE);
            tvNewOwner.setVisibility(View.VISIBLE);
            btnAdd.setText(getString(R.string.btn_continue));
        } catch (Exception e) {
            AppDialog.showAlertDialog(getContext(), getString(R.string.oops_error));
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

    private void doAssignUserRequest() {

        if (mLock != null) {

            Loader.getInstance().showLoader(TransferOwnerFragment.this.getContext());

            AssignRequestUserService.getInstance().getAssignUserList(mLock.getId(), "1","", new ResponseHandler() {

                @Override
                public void onSuccess(Object data) {

                    Loader.getInstance().hideLoader();

                    if (data != null) {
                        AssignUser assignUser = (AssignUser) data;
                        ArrayList<LockKeys> lockKeysList = assignUser.getKeys();
                        prepopulateData(lockKeysList);
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
    }

    private void prepopulateData(ArrayList<LockKeys> lockKeysList) {
        if (lockKeysList != null && lockKeysList.size() > 0) {
            isTransferInitiated = true;
            for (int i = 0; i < lockKeysList.size(); i++) {
                LockKeys lockKeys = lockKeysList.get(i);
                if (lockKeys.getUserId() == null) {
                    //If both owner items in list not empty,then transfer initiated
                    isTransferInitiated = false;
                } else {
                    if (lockKeys.getStatus().equalsIgnoreCase("0") && lockKeys.getLockUser() != null) {
                        requestToUserName = lockKeys.getLockUser().getUsername();
                        requestId=lockKeys.getRequestDetail().getId();
                    }
                }
            }

            if (isTransferInitiated) {
                tvNewOwner.setVisibility(View.VISIBLE);
                tvOwnerName.setVisibility(View.VISIBLE);
                tvOwnerName.setText(requestToUserName);
                btnAdd.setVisibility(View.VISIBLE);
                btnAdd.setText(getString(R.string.withdraw_text));
                tvTrasferOwner.setText(getString(R.string.tv_transfer_owner_success));
                //btnAdd.setText(getString(R.string.withdraw_text));

            } else {
                tvNewOwner.setVisibility(View.INVISIBLE);
                tvOwnerName.setVisibility(View.INVISIBLE);
                tvOwnerName.setText("");
                btnAdd.setVisibility(View.VISIBLE);
            }

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
