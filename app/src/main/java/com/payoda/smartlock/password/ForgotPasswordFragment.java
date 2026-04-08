package com.payoda.smartlock.password;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.payoda.smartlock.App;
import com.payoda.smartlock.R;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.password.model.ForgotPassword;
import com.payoda.smartlock.password.service.ForgotPasswordService;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.utils.AppDialog;
import com.payoda.smartlock.utils.Loader;
import com.payoda.smartlock.utils.Logger;
import com.payoda.smartlock.utils.Validator;

/**
 * A simple {@link Fragment} subclass.
 */
public class ForgotPasswordFragment extends Fragment implements ResponseHandler {

    public static final String TAG = "### ForgotPasswordFragment";

    TextInputLayout emailWrapper;
    EditText etEmail;

    public ForgotPasswordFragment() {
        // Required empty public constructor
    }

    public static ForgotPasswordFragment getInstance() {
        return new ForgotPasswordFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Logger.d(TAG, TAG);
        return initializeView(inflater.inflate(R.layout.fragment_forgot_password, container, false));
    }

    /**
     * Use this method to initialize the view.
     *
     * @return View Return the initialized view;
     */
    @SuppressLint("ClickableViewAccessibility")
    private View initializeView(View view) {

        ((TextView) view.findViewById(R.id.tv_title)).setText(getString(R.string.fp_title));
        view.findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });

        etEmail = view.findViewById(R.id.et_email);
        emailWrapper = view.findViewById(R.id.emailWrapper);
        etEmail.addTextChangedListener(new MyTextWatcher(etEmail));
        emailWrapper.setHint("Enter your email address");
        view.findViewById(R.id.btn_send_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                if (isvalid()) {
                    doServiceRequest();
                }
            }
        });
        view.findViewById(R.id.acc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                App.getInstance().switchFragment(getActivity().getSupportFragmentManager(), R.id.full_screen_container, Constant.SCREEN.SIGNUP, null);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        emailWrapper.setErrorEnabled(false);
        etEmail.getText().clear();
    }

    @Override
    public void onPause() {
        super.onPause();
        emailWrapper.setErrorEnabled(false);
        etEmail.getText().clear();
    }

    private void doServiceRequest() {
        Loader.getInstance().showLoader(getContext());
        ForgotPassword mPassword = new ForgotPassword();
        mPassword.setEmail(etEmail.getText().toString());
        ForgotPasswordService.getInstance().serviceRequest(mPassword, this);
    }

    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            ((android.view.inputmethod.InputMethodManager) getActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE)).
                    hideSoftInputFromWindow(view.getWindowToken(), android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public boolean isvalid() {
        String email = emailWrapper.getEditText().getText().toString();
        if (email.isEmpty()) {
            emailWrapper.setError("Email is mandatory");
            return false;
        } else if (!Validator.IsEmail(email)) {
            emailWrapper.setError("Please enter a valid email address");
            return false;
        }
        emailWrapper.setErrorEnabled(false);
        return true;
    }

    @Override
    public void onSuccess(Object data) {
        Loader.getInstance().hideLoader();
        try {
            App.getInstance().switchFragment(getActivity().getSupportFragmentManager(), R.id.full_screen_container, Constant.SCREEN.FORGOT_PASSWORD_SUCCESS, etEmail.getText().toString());
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
                case R.id.et_email:
                    isvalid();
                    break;
            }
        }
    }

}
