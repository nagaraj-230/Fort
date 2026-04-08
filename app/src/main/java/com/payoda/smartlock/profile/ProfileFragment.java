package com.payoda.smartlock.profile;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.payoda.countrydialcode.CountryCodePicker;
import com.payoda.smartlock.App;
import com.payoda.smartlock.BuildConfig;
import com.payoda.smartlock.R;
import com.payoda.smartlock.authentication.model.Login;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.locks.model.LockKeys;
import com.payoda.smartlock.locks.model.LockUser;
import com.payoda.smartlock.locks.model.RequestDetail;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.storage.SecuredStorageManager;
import com.payoda.smartlock.plugins.storage.StorageManager;
import com.payoda.smartlock.profile.model.Profile;
import com.payoda.smartlock.profile.service.ProfileService;
import com.payoda.smartlock.utils.AppDialog;
import com.payoda.smartlock.utils.DateTimeUtils;
import com.payoda.smartlock.utils.Loader;
import com.payoda.smartlock.utils.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.payoda.smartlock.signup.SignUpFragment.MOBILE_PATTERN;

import io.michaelrocks.libphonenumber.android.NumberParseException;
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;
import io.michaelrocks.libphonenumber.android.Phonenumber;


public class ProfileFragment extends Fragment implements ResponseHandler {

    public static final String TAG = "### ProfileFragment";

    private TextInputLayout nameWrapper, addressWrapper,mobileWrapper;
    private EditText etName, etEmail, etMobileNumber, etAddress, etGrantedTime;
    private ImageView img_edit, img_save;
    private CountryCodePicker countryCodePickerEditProfile;

    private PhoneNumberUtil phoneUtil = null;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment getInstance() {
        return new ProfileFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Logger.d(TAG, TAG);
        return initializeView(inflater.inflate(R.layout.fragment_profile, container, false));
    }

    private View initializeView(final View view) {

        nameWrapper = view.findViewById(R.id.nameWrapper);
        addressWrapper = view.findViewById(R.id.addressWrapper);
        mobileWrapper=view.findViewById(R.id.mobileWrapper);
        etName = view.findViewById(R.id.profile_name);
        etEmail = view.findViewById(R.id.profile_email);
        countryCodePickerEditProfile = view.findViewById(R.id.countryCodePickerEditProfile);
        etMobileNumber = view.findViewById(R.id.profile_mobile);
        etAddress = view.findViewById(R.id.profile_address);
        etGrantedTime = view.findViewById(R.id.granted_time);
        img_edit = view.findViewById(R.id.iv_edit);
        img_edit.setVisibility(View.VISIBLE);
        img_save = view.findViewById(R.id.iv_save);
        ((TextView) view.findViewById(R.id.tv_title)).setText(getString(R.string.profile));

        if (phoneUtil == null) {
            phoneUtil = PhoneNumberUtil.createInstance(getContext().getApplicationContext());
        }

        view.findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });
        img_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TextView) view.findViewById(R.id.tv_title)).setText(getString(R.string.edit_profile));
                img_edit.setVisibility(View.INVISIBLE);
                img_save.setVisibility(View.VISIBLE);

                etName.setEnabled(true);
                etName.setFocusable(true);
                etName.requestFocus();
                etAddress.setEnabled(true);
                etEmail.setEnabled(false);
                etMobileNumber.setEnabled(true);

                countryCodePickerEditProfile.setCcpClickable(true);
                countryCodePickerEditProfile.setContentColor(Color.BLACK);
            }
        });
        img_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidInput()) {
                    if(etMobileNumber.getText().toString().equalsIgnoreCase(etMobileNumber.getTag().toString())){
                        saveProfile();
                    }else {
                        AppDialog.showAlertDialog(getContext(), "AX100", "Please confirm if you wanted to update the mobile number from "+ etMobileNumber.getTag().toString()+" to "+etMobileNumber.getText().toString()+"?", "YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                saveProfile();
                            }
                        }, "NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                    }

                }
            }
        });

        initVersion(view);

        Bundle bundle = getArguments();
        if (bundle != null) {
            LockKeys selectedLockKeys = new Gson().fromJson(bundle.getString(Constant.SCREEN_DATA), LockKeys.class);
            updateUserDetail(selectedLockKeys);
        } else {
            doServiceRequest();
        }
        return view;
    }

    private void initVersion(View view){
        try {
            if(BuildConfig.APP_VERSION_VISIBILITY){
                TextView lblVersion = view.findViewById(R.id.lblVersion);
                lblVersion.setVisibility(View.VISIBLE);
                lblVersion.setText( "App Version: " + BuildConfig.VERSION_NAME + "#" + BuildConfig.VERSION_CODE + "(" + BuildConfig.BUILD_VARIANT  + ")");
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void updateUserDetail(LockKeys selectedLockKeys) {
        LockUser user = selectedLockKeys.getLockUser();
        etName.setText(selectedLockKeys.getName());
        etEmail.setText(user.getEmail());
        countryCodePickerEditProfile.setCountryForNameCode(user.getCountryCode());
        etMobileNumber.setText(user.getMobile());
        etMobileNumber.setTag(user.getMobile());
        etAddress.setText(user.getAddress());
        RequestDetail detail = selectedLockKeys.getRequestDetail();
        if (detail != null && detail.getModifiedDate() != null) {
            etGrantedTime.setText(DateTimeUtils.getLocalDateFromGMT(detail.getModifiedDate()));
            etGrantedTime.setVisibility(View.VISIBLE);
        }
        img_edit.setVisibility(View.GONE);
    }

    private boolean isValidInput() {
        if (!validateName()) {
            return false;
        }
        if (!validateAddress()) {
            return false;
        }
        if(!validateMobile()){
            return false;
        }
        return true;
    }

    private boolean validateMobile() {
        String mobile = etMobileNumber.getText().toString();
        if (mobile.isEmpty()) {
            mobileWrapper.setError("Mobile Number is mandatory");
            return false;
        } else {
            try {
                final Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(mobile, countryCodePickerEditProfile.getSelectedCountryNameCode());
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

        /*else if (!validateMobileRegex(mobile)) {
            mobileWrapper.setError("Please enter a valid 10 digit mobile number");
            return false;
        }*/
        mobileWrapper.setErrorEnabled(false);
        return true;
    }

    public static boolean validateMobileRegex(String mobileStr) {
        Matcher matcher = MOBILE_PATTERN.matcher(mobileStr);
        return matcher.matches();
    }


    public static final Pattern Name_PATTERN =
            Pattern.compile("^[a-zA-Z\\s]+$", Pattern.CASE_INSENSITIVE);

    public static boolean validateNameRegex(String nameStr) {
        Matcher matcher = Name_PATTERN.matcher(nameStr);
        return matcher.matches();
    }

    private boolean validateName() {
        String name = etName.getText().toString();
        if (name.isEmpty()) {
            nameWrapper.setError("Name is mandatory");
            return false;
        } else if (!validateNameRegex(name)) {
            nameWrapper.setError("Name Shouldn't contain Special char or number");
            return false;
        }
        nameWrapper.setErrorEnabled(false);
        return true;
    }

    private boolean validateAddress() {
        String address = etAddress.getText().toString();
        if (address.isEmpty()) {
            addressWrapper.setError("Address is mandatory");
            return false;
        }
        //addressWrapper.setError("Enter a combination of letters, numbers and special chars (&/-_'()#,)");
        addressWrapper.setErrorEnabled(false);
        return true;
    }


    private void doServiceRequest() {
        Loader.getInstance().showLoader(getContext());
        ProfileService.getInstance().serviceRequest(this);
    }

    private void saveProfile() {
        Loader.getInstance().showLoader(getContext());
        Profile mProfile = new Profile();
        mProfile.setUsername(etName.getText().toString());
        mProfile.setEmail(etEmail.getText().toString());
        mProfile.setCountryCode(countryCodePickerEditProfile.getSelectedCountryNameCode());
        mProfile.setMobile(etMobileNumber.getText().toString());
        mProfile.setAddress(etAddress.getText().toString());
        Login mLogin = SecuredStorageManager.getInstance().getUserData();
        mLogin.setName(etName.getText().toString());
        SecuredStorageManager.getInstance().setUserData(mLogin);
        ProfileService.getInstance().saveRequest(mProfile, new ResponseHandler() {
            @Override
            public void onSuccess(Object data) {
                Loader.getInstance().hideLoader();
                Profile mProfile = (Profile) data;
                etEmail.setEnabled(true);
                etMobileNumber.setEnabled(true);
                AppDialog.showAlertDialog(getContext(), mProfile.getMessage(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (getActivity() != null)
                            requireActivity().getOnBackPressedDispatcher().onBackPressed();
                    }
                });
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

    @Override
    public void onSuccess(Object data) {
        Loader.getInstance().hideLoader();
        try {
            Profile mProfile = (Profile) data;
            etName.setText(mProfile.getUsername());
            etEmail.setText(mProfile.getEmail());
            countryCodePickerEditProfile.setCountryForNameCode(mProfile.getCountryCode());
            etMobileNumber.setText(mProfile.getMobile());
            etMobileNumber.setTag(mProfile.getMobile());
            etAddress.setText(mProfile.getAddress());
        } catch (Exception e) {
            AppDialog.showAlertDialog(getContext(), getString(R.string.oops_error));
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
}
