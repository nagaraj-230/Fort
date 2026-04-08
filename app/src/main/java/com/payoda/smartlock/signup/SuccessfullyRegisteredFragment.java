package com.payoda.smartlock.signup;

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

public class SuccessfullyRegisteredFragment  extends Fragment {

    public static final String TAG ="### SuccessfullyRegisteredFragment";

    public SuccessfullyRegisteredFragment() {
        // Required empty public constructor
    }

    public static SuccessfullyRegisteredFragment getInstance() {
        return new SuccessfullyRegisteredFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Logger.d(TAG, TAG);
        return initializeView(inflater.inflate(R.layout.fragment_successfully_registered, container, false));
    }

    /**
     * Use this method to initialize the view.
     *
     * @return View Return the initialized view;
     */
    private View initializeView(View view) {

        Bundle bundle = getArguments();
        if (bundle != null) {
            ((TextView) view.findViewById(R.id.tv_email)).setText(String.format(getString(R.string.successfully_registered_message), bundle.getString(Constant.SCREEN_DATA)));
        }

        view.findViewById(R.id.btn_gotologin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                App.getInstance().showLogin(getActivity());
            }
        });
        return view;
    }
}

