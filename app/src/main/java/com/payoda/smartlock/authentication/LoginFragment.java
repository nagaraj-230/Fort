package com.payoda.smartlock.authentication;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;

import com.google.android.material.textfield.TextInputLayout;

import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.AppCompatEditText;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.payoda.smartlock.App;
import com.payoda.smartlock.R;
import com.payoda.smartlock.authentication.model.Login;
import com.payoda.smartlock.authentication.service.LoginService;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.storage.SecuredStorageManager;
import com.payoda.smartlock.plugins.storage.StorageManager;
import com.payoda.smartlock.utils.AppDialog;
import com.payoda.smartlock.utils.Loader;
import com.payoda.smartlock.utils.Logger;
import com.payoda.smartlock.utils.Validator;

import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 */

public class LoginFragment extends BaseFragment implements ResponseHandler {

    private AppCompatEditText etUsername, etPassword;
    TextInputLayout usernameWrapper, passwordWrapper;

    public static final String TAG = "### LoginFragment";

    public LoginFragment() {
        // Required empty public constructor
    }

    public static LoginFragment getInstance() {
        return new LoginFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Logger.d(TAG, TAG);
        return initializeView(inflater.inflate(R.layout.fragment_login, container, false));

    }

    /**
     * Use this method to initialize the view.
     *
     * @return View Return the initialized view;
     */
    @SuppressLint("ClickableViewAccessibility")
    private View initializeView(View view) {

        etUsername = view.findViewById(R.id.et_username);
        etPassword = view.findViewById(R.id.et_password);
        usernameWrapper = view.findViewById(R.id.il_usernameWrapper);
        passwordWrapper = view.findViewById(R.id.il_passwordWrapper);
        etUsername.addTextChangedListener(new LoginFragment.MyTextWatcher(etUsername));
        etPassword.addTextChangedListener(new LoginFragment.MyTextWatcher(etPassword));

        view.findViewById(R.id.btn_signin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getBatteryOptPermission(new RequestPermissionAction() {
                    @Override
                    public void permissionDenied() {

                    }

                    @Override
                    public void permissionGranted() {

                        doLogin();
                    }
                });

            }
        });

        view.findViewById(R.id.tv_forgot_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    App.getInstance().navigateDetail(getActivity(), Constant.SCREEN.FORGOT_PASSWORD);
            }
        });

        view.findViewById(R.id.tv_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    App.getInstance().navigateDetail(getActivity(), Constant.SCREEN.SIGNUP);
            }
        });

        ignoreBatteryOptimization();
        notificationPermission();

        return view;
    }

    private void notificationPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getPostNotificationPermission(new RequestPermissionAction() {
                @Override
                public void permissionDenied() {

                }

                @Override
                public void permissionGranted() {

                }
            });
        }
    }

    private void ignoreBatteryOptimization() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // Do something for lollipop and above versions
            Intent intent = new Intent();
            String packageName = getContext().getPackageName();
            PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);

            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + packageName));
            //}
            getContext().startActivity(intent);

        }

    }

    public void disableError() {
        usernameWrapper.setErrorEnabled(false);
        passwordWrapper.setErrorEnabled(false);
    }

    public void clearText() {
        etUsername.getText().clear();
        etPassword.getText().clear();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void doLogin() {

        try {
            hideKeyboard();
            String email = etUsername.getText().toString();
            String password = etPassword.getText().toString();
            // Do all validation over here
            if (isValidInput()) {
                Loader.getInstance().showLoader(getContext());
                String uuidAsString = UUID.randomUUID().toString();
                LoginService.getInstance().serviceRequest(new Login(email, password, uuidAsString, SecuredStorageManager.getInstance().getDeviceToken()), this);
            }
        } catch (Exception e) {
            Logger.e(e);
        }
    }

    private boolean isValidInput() {
        if (!validateEmail()) {
            return false;
        } else if (!validPassword()) {
            return false;
        }
        return true;
    }

    public boolean validateEmail() {
        String email = etUsername.getText().toString();
        if (email.isEmpty()) {
            usernameWrapper.setError("Email is mandatory");
            return false;
        } else if (!Validator.IsEmail(email)) {
            usernameWrapper.setError("Please enter a valid email address");
            return false;
        }
        usernameWrapper.setErrorEnabled(false);
        return true;
    }

    public boolean validPassword() {
        String password = passwordWrapper.getEditText().getText().toString();
        if (password.isEmpty()) {
            passwordWrapper.setError("Password is mandatory");
            return false;
        }
        passwordWrapper.setErrorEnabled(false);
        return true;
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etPassword.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
        } catch (Exception e) {
            Logger.e(e);
        }
    }

    @Override
    public void onSuccess(Object data) {
        Loader.getInstance().hideLoader();
        try {
            Login login = (Login) data;
            SecuredStorageManager.getInstance().setToken(login.getToken());
            SecuredStorageManager.getInstance().setUserData(login);
            App.getInstance().switchFragment(getActivity().getSupportFragmentManager(), R.id.full_screen_container, Constant.SCREEN.PIN, null);
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
                case R.id.et_username:
                    validateEmail();
                    break;
                case R.id.et_password:
                    validPassword();
                    break;
            }
        }
    }

}
