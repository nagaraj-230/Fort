package com.payoda.smartlock.timeoutpin;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.payoda.smartlock.App;
import com.payoda.smartlock.R;
import com.payoda.smartlock.plugins.storage.SecuredStorageManager;
import com.payoda.smartlock.plugins.storage.StorageManager;
import com.payoda.smartlock.utils.AppDialog;
import com.payoda.smartlock.utils.Logger;

import java.util.Timer;
import java.util.TimerTask;

public class TimeOutPinFragment extends DialogFragment implements View.OnClickListener {

    public static final String TAG ="### TimeOutPinFragment";

    EditText etPin;

    public TimeOutPinFragment() {
        // Required empty public constructor
    }

    public static TimeOutPinFragment getInstance() {
        TimeOutPinFragment timeOutPinFragment = new TimeOutPinFragment();
        timeOutPinFragment.setCancelable(false);
        return timeOutPinFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Logger.d(TAG, TAG);
        return initializeView(inflater.inflate(R.layout.fragment_timeout_pin, container, false));
    }

    /**
     * Use this method to initialize the view.
     *
     * @return View Return the initialized view;
     */
    @SuppressLint("ClickableViewAccessibility")
    private View initializeView(View view) {

        TextView tvTitle = view.findViewById(R.id.tv_title);
        tvTitle.setText(getString(R.string.timeout_pin));

        etPin = view.findViewById(R.id.et_pin);
        etPin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String data = etPin.getText().toString();
                if (!TextUtils.isEmpty(data) && data.length() == 4 && getActivity() != null) {
                    //Validate current Pin with stored pin
                    if (SecuredStorageManager.getInstance().getPin().equalsIgnoreCase(data)) {
                        SecuredStorageManager.getInstance().setTimeOutSeconds();
                        SecuredStorageManager.getInstance().setFreshLaunch(false);

                        App.getInstance().showDashboard(getActivity());

                    } else {
                        AppDialog.showAlertDialog(getContext(), getString(R.string.validate_pin));
                    }

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (etPin != null)
                                            etPin.getText().clear();
                                    }
                                });
                            }
                        }
                    }, 1000);


                }
            }
        });
        view.findViewById(R.id.iv_delete).setOnClickListener(this);
        view.findViewById(R.id.button0).setOnClickListener(this);
        view.findViewById(R.id.button1).setOnClickListener(this);
        view.findViewById(R.id.button2).setOnClickListener(this);
        view.findViewById(R.id.button3).setOnClickListener(this);
        view.findViewById(R.id.button4).setOnClickListener(this);
        view.findViewById(R.id.button5).setOnClickListener(this);
        view.findViewById(R.id.button6).setOnClickListener(this);
        view.findViewById(R.id.button7).setOnClickListener(this);
        view.findViewById(R.id.button8).setOnClickListener(this);
        view.findViewById(R.id.button9).setOnClickListener(this);


        return view;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button0:
                addText(0);
                break;
            case R.id.button1:
                addText(1);
                break;
            case R.id.button2:
                addText(2);
                break;
            case R.id.button3:
                addText(3);
                break;
            case R.id.button4:
                addText(4);
                break;
            case R.id.button5:
                addText(5);
                break;
            case R.id.button6:
                addText(6);
                break;
            case R.id.button7:
                addText(7);
                break;
            case R.id.button8:
                addText(8);
                break;
            case R.id.button9:
                addText(9);
                break;
            case R.id.iv_delete:
                String data = etPin.getText().toString();
                if (!TextUtils.isEmpty(data))
                    etPin.setText(data.substring(0, data.length() - 1));
                break;
            default:
                break;
        }
    }

    private void addText(int data) {
        etPin.setText(String.format("%s%s", etPin.getText().toString(), data));
    }

}
