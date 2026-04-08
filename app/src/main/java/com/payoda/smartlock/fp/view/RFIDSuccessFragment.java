package com.payoda.smartlock.fp.view;

import android.app.Activity;
import android.content.Intent;
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

import com.payoda.smartlock.R;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.utils.Logger;

public class RFIDSuccessFragment extends Fragment {

    public static final String TAG = "### RFIDSuccessFragment";

    private ImageView ivBackBtn;
    private Button btnContinue;
    private TextView lblRFIDSuccess;
    private String screenName;

    public RFIDSuccessFragment() {
        // Required empty public constructor
    }

    public static RFIDSuccessFragment getInstance() {
        return new RFIDSuccessFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            screenName = bundle.getString(Constant.SCREEN_DATA);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Logger.d(TAG, TAG);
        return initializeView(inflater.inflate(R.layout.fragment_rfid_success, container, false));
    }

    /**
     * Use this method to initialize the view.
     *
     * @return View Return the initialized view;
     */
    private View initializeView(View view) {
        ((TextView) view.findViewById(R.id.tv_title)).setText(getString(R.string.rfid));
        ivBackBtn = view.findViewById(R.id.iv_back);
        ivBackBtn.setVisibility(View.INVISIBLE);
        btnContinue = view.findViewById(R.id.btn_continue);
        lblRFIDSuccess=view.findViewById(R.id.lblRFIDSuccess);
        if(screenName.equalsIgnoreCase(getString(R.string.rfid))){
            lblRFIDSuccess.setText(getString(R.string.rfid_success));
        }else {
            lblRFIDSuccess.setText(getString(R.string.finger_print_success));
            ((TextView) view.findViewById(R.id.tv_title)).setText(getString(R.string.finger_print));
        }

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent();
                intent.putExtra("state","back");
                getActivity().setResult(Activity.RESULT_OK,intent);
                requireActivity().getOnBackPressedDispatcher().onBackPressed();

            }
        });

        return view;
    }
}

