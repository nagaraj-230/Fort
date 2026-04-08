package com.payoda.smartlock.signup;


import android.annotation.SuppressLint;
import android.content.Context;
import android.nfc.Tag;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputLayout;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.payoda.countrydialcode.CountryCodePicker;
import com.payoda.smartlock.App;
import com.payoda.smartlock.R;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.signup.service.SignUpService;
import com.payoda.smartlock.users.model.User;
import com.payoda.smartlock.utils.AppDialog;
import com.payoda.smartlock.utils.Loader;
import com.payoda.smartlock.utils.Logger;
import com.payoda.smartlock.utils.Validator;

import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.michaelrocks.libphonenumber.android.NumberParseException;
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;
import io.michaelrocks.libphonenumber.android.Phonenumber;

/**
 * A simple {@link Fragment} subclass.
 */
public class SignUpFragment extends Fragment implements ResponseHandler {


    private static final String TAG = SignUpFragment.class.getSimpleName();
    Button btn_signup;
    TextInputLayout nameWrapper, emailWrapper, passwordWrapper, conPasswordWrapper, mobileWrapper, addressWrapper;
    EditText etName, etEmail, etPassword, etConfirmPassword, etMobileNumber, etAddress;
    TextView lblTerms, lblPrivacy;

    CountryCodePicker countryCodePicker;
    private PhoneNumberUtil phoneUtil = null;

    public SignUpFragment() {
        // Required empty public constructor
    }

    public static SignUpFragment getInstance() {
        return new SignUpFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Logger.d(TAG, TAG);
        return initializeView(inflater.inflate(R.layout.fragment_signup, container, false));
    }

    /**
     * Use this method to initialize the view.
     *
     * @return View Return the initialized view;
     */
    @SuppressLint("ClickableViewAccessibility")
    private View initializeView(View view) {
        /*Header Start*/
        ((TextView) view.findViewById(R.id.tv_title)).setText(getString(R.string.sign_up));
        view.findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });
        /*Header End*/
        nameWrapper = view.findViewById(R.id.nameWrapper);
        emailWrapper = view.findViewById(R.id.emailWrapper);
        passwordWrapper = view.findViewById(R.id.passwordWrapper);
        conPasswordWrapper = view.findViewById(R.id.conPasswordWrapper);
        countryCodePicker = view.findViewById(R.id.countryCodePicker);
        mobileWrapper = view.findViewById(R.id.mobileWrapper);
        addressWrapper = view.findViewById(R.id.addressWrapper);
        lblTerms = view.findViewById(R.id.lblTerms);
        lblTerms.setText(Html.fromHtml("<u>Terms and Conditions</u>"));
        lblTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                App.getInstance().showFullScreen(getActivity(), Constant.SCREEN.TERMS, null);
            }
        });

        lblPrivacy = view.findViewById(R.id.lblPrivacy);
        lblPrivacy.setText(Html.fromHtml("<u>Privacy Policy</u>"));
        lblPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                App.getInstance().showFullScreen(getActivity(), Constant.SCREEN.PRIVACY_POLICY, null);
            }
        });

        etName = view.findViewById(R.id.signup_name);
        etEmail = view.findViewById(R.id.signup_email);
        etPassword = view.findViewById(R.id.signup_password);
        etConfirmPassword = view.findViewById(R.id.signup_con_password);
        etMobileNumber = view.findViewById(R.id.signup_mobile);
        etAddress = view.findViewById(R.id.signup_address);

        etName.addTextChangedListener(new MyTextWatcher(etName));
        etEmail.addTextChangedListener(new MyTextWatcher(etEmail));
        etPassword.addTextChangedListener(new MyTextWatcher(etPassword));
        etConfirmPassword.addTextChangedListener(new MyTextWatcher(etConfirmPassword));
        etMobileNumber.addTextChangedListener(new MyTextWatcher(etMobileNumber));
        etAddress.addTextChangedListener(new MyTextWatcher(etAddress));

        if (phoneUtil == null) {
            phoneUtil = PhoneNumberUtil.createInstance(getContext().getApplicationContext());
        }

        btn_signup = view.findViewById(R.id.btn_signup);
        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                if (isValidInput()) {
                    doServiceRequest();
                }
            }
        });
        return view;
    }

    private void doServiceRequest() {
        Loader.getInstance().showLoader(getContext());
        User mUser = new User();
        mUser.setName(etName.getText().toString());
        mUser.setEmail(etEmail.getText().toString());
        mUser.setPassword(etPassword.getText().toString());
        mUser.setCountryCode(countryCodePicker.getSelectedCountryNameCode());
        mUser.setMobile(etMobileNumber.getText().toString());
        mUser.setAddress(etAddress.getText().toString());
        SignUpService.getInstance().serviceRequest(mUser, this);
    }

    private boolean isValidInput() {
        if (!validateName()) {
            return false;
        } else if (!validateEmail()) {
            return false;
        } else if (!validatePassword()) {
            return false;
        } else if (!validateConPassword()) {
            return false;
        } else if (!validateMobile()) {
            return false;
        } else if (!validateAddress()) {
            return false;
        }
        return true;
    }

    public void disableError() {
        nameWrapper.setErrorEnabled(false);
        passwordWrapper.setErrorEnabled(false);
        conPasswordWrapper.setErrorEnabled(false);
        mobileWrapper.setErrorEnabled(false);
        addressWrapper.setErrorEnabled(false);
    }

    public void clearText() {
        etName.getText().clear();
        etEmail.getText().clear();
        etPassword.getText().clear();
        etConfirmPassword.getText().clear();
        etMobileNumber.getText().clear();
        etAddress.getText().clear();
    }

    @Override
    public void onResume() {
        super.onResume();
        //disableError();
        //clearText();
    }

    @Override
    public void onPause() {
        super.onPause();
        //disableError();
        //clearText();
    }


    private void hideKeyboard() {
        try {
            View view = getActivity().getCurrentFocus();
            if (view != null) {
                ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).
                        hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } catch (Exception e) {
            Log.e(TAG, "hideKeyboard: " + e.getMessage());
        }
    }

    public static final Pattern Name_PATTERN =
            Pattern.compile("^[a-zA-Z\\s]+$", Pattern.CASE_INSENSITIVE);

    public static boolean validateNameRegex(String nameStr) {
        Matcher matcher = Name_PATTERN.matcher(nameStr);
        return matcher.matches();
    }

    public static final Pattern MOBILE_PATTERN =
            Pattern.compile("^[0-9]{10}$", Pattern.CASE_INSENSITIVE);

    public static boolean validateMobileRegex(String mobileStr) {
        Matcher matcher = MOBILE_PATTERN.matcher(mobileStr);
        return matcher.matches();
    }

    private boolean validateName() {
        String name = etName.getText().toString();
        if (name.isEmpty()) {
            nameWrapper.setError("Name is mandatory");
            return false;
        } else if (!validateNameRegex(name)) {
            nameWrapper.setError("Name should not contain special characters or Number");
            return false;
        }
        nameWrapper.setErrorEnabled(false);
        return true;
    }

    private boolean validateEmail() {
        String email = etEmail.getText().toString();
        if (email.isEmpty()) {
            emailWrapper.setError("Email ID is mandatory");
            return false;
        } else if (!Validator.IsEmail(email)) {
            emailWrapper.setError("Please  enter a valid email id to create an account");
            return false;
        }
        emailWrapper.setErrorEnabled(false);
        return true;
    }

    private boolean validatePassword() {
        String password = etPassword.getText().toString();
        String conpassword = etConfirmPassword.getText().toString();
        if (password.isEmpty()) {
            passwordWrapper.setError("Password is mandatory");
            return false;
        } else if (!Validator.IsPassword(password)) {
            passwordWrapper.setError("Enter a combination of atleast 8 letters, numbers and special chars (!@#$%&*().,)");
            return false;
        } else if (password.equals(conpassword)) {
            conPasswordWrapper.setErrorEnabled(false);
        }
        passwordWrapper.setErrorEnabled(false);
        return true;
    }

    private boolean validateConPassword() {
        String password = etPassword.getText().toString();
        String conpassword = etConfirmPassword.getText().toString();
        if (conpassword.isEmpty()) {
            conPasswordWrapper.setError("Confirm Password is mandatory");
            return false;
        } else if (password.equals(conpassword)) {
            conPasswordWrapper.setErrorEnabled(false);
        } else {
            conPasswordWrapper.setError("Password and Confirm password does not match");
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
                final Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(mobile, countryCodePicker.getSelectedCountryNameCode());
                boolean isValid = phoneUtil.isValidNumber(phoneNumber);
                if (!isValid) {
                    mobileWrapper.setError("Please enter a valid mobile number");
                    return false;
                }
            } catch (NumberParseException e) {
                e.printStackTrace();
                mobileWrapper.setError("Please enter a valid mobile number");
                return false;
            }
        }/*else if (!validateMobileRegex(mobile)) {
            mobileWrapper.setError("Please enter a valid 10 digit mobile number");
            return false;
        }*/
        mobileWrapper.setErrorEnabled(false);
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

    @Override
    public void onSuccess(Object data) {
        Loader.getInstance().hideLoader();
        try {
            //ResponseModel mResponseModel = (ResponseModel) data;
            if (getActivity() != null)
                App.getInstance().switchFragment(getActivity().getSupportFragmentManager(), R.id.full_screen_container,
                        Constant.SCREEN.SUCCESSFULLY_REGISTERED, etEmail.getText().toString());
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

    private class MyTextWatcher implements TextWatcher {
        private Timer mTimer = new Timer();
        private final int DELAY = 1000;
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
                case R.id.signup_name:
                    validateName();
                    break;
                case R.id.signup_email:
                    validateEmail();
                    break;
                case R.id.signup_password:
                    validatePassword();
                    String password = etPassword.getText().toString();
                    String conpassword = etConfirmPassword.getText().toString();
                    if (conpassword.length() > 0 && !(password.equals(conpassword))) {
                        passwordWrapper.setError("Password and Confirm password does not match");
                    }
                    break;
                case R.id.signup_con_password:
                    validateConPassword();
                    String password1 = etPassword.getText().toString();
                    String conpassword1 = etConfirmPassword.getText().toString();
                    if (password1.length() > 0 && conpassword1.equals(password1)) {
                        passwordWrapper.setErrorEnabled(false);
                    }
                    break;
                case R.id.signup_mobile:
                    validateMobile();
                    break;
                case R.id.signup_address:
                    validateAddress();
                    break;
            }
            /*if (mTimer != null)
                mTimer.cancel();
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // call ur function
                            }
                        });
                    }
                }
            }, DELAY);*/
        }
    }
}
