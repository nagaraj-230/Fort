package com.payoda.smartlock.password;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.payoda.smartlock.App;
import com.payoda.smartlock.R;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.utils.Logger;

public class SuccessfullyPasswordResetFragment extends Fragment {

    public static final String TAG = "### SuccessfullyPasswordResetFragment";

    public SuccessfullyPasswordResetFragment() {
        // Required empty public constructor
    }

    public static SuccessfullyPasswordResetFragment getInstance() {
        return new SuccessfullyPasswordResetFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Logger.d(TAG, TAG);
        return initializeView(inflater.inflate(R.layout.fragment_password_rest, container, false));
    }

    /**
     * Use this method to initialize the view.
     *
     * @return View Return the initialized view;
     */
    private View initializeView(View view) {

        Bundle bundle = getArguments();
        if (bundle != null) {
            ((TextView) view.findViewById(R.id.tv_email)).setText(String.format(getString(R.string.forgotpassword_success_message), bundle.getString(Constant.SCREEN_DATA)));
        }
        ((TextView) view.findViewById(R.id.tv_title)).setText(getString(R.string.fp_title));
        view.findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });
        view.findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                App.getInstance().showLogin(getActivity());
            }
        });
        return view;
    }
}
