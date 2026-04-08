package com.payoda.smartlock.pin;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.payoda.smartlock.App;
import com.payoda.smartlock.R;
import com.payoda.smartlock.plugins.storage.SecuredStorageManager;
import com.payoda.smartlock.plugins.storage.StorageManager;
import com.payoda.smartlock.utils.Logger;

public class PinSuccessFragment extends Fragment {

    public static final String TAG = "### PinSuccessFragment";

    ImageView ivBackBtn;
    Button btnContinue;
    TextView tvPinSuccessMsg;

    public PinSuccessFragment() {
        // Required empty public constructor
    }

    public static PinSuccessFragment getInstance() {
        return new PinSuccessFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Logger.d(TAG, TAG);
        return initializeView(inflater.inflate(R.layout.fragment_pin_success, container, false));
    }

    /**
     * Use this method to initialize the view.
     *
     * @return View Return the initialized view;
     */
    private View initializeView(View view) {
        ((TextView) view.findViewById(R.id.tv_title)).setText(getString(R.string.security_pin));
        if (SecuredStorageManager.getInstance().isResetPin())
            ((TextView) view.findViewById(R.id.tv_pin_success)).setText(getString(R.string.update_pin_msg));
        else
            ((TextView) view.findViewById(R.id.tv_pin_success)).setText(getString(R.string.success_pin_msg));
        ivBackBtn = view.findViewById(R.id.iv_back);
        ivBackBtn.setVisibility(View.INVISIBLE);
        btnContinue = view.findViewById(R.id.btn_continue);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SecuredStorageManager.getInstance().setFreshLaunch(false);
                // If strPin contains value, then user came from Reset PIN flow. So finish the current activity.
                if (SecuredStorageManager.getInstance().isResetPin()) {
                    if (getActivity() != null) getActivity().finish();
                    SecuredStorageManager.getInstance().clearResetPin();
                } else {
                    SecuredStorageManager.getInstance().setTimeOutSeconds();
                    App.getInstance().showDashboard(getActivity());
                }
            }
        });
        return view;
    }
}
