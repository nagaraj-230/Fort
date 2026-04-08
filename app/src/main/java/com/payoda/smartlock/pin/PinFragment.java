package com.payoda.smartlock.pin;


import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.payoda.smartlock.App;
import com.payoda.smartlock.R;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.plugins.storage.SecuredStorageManager;
import com.payoda.smartlock.plugins.storage.StorageManager;
import com.payoda.smartlock.utils.AppDialog;
import com.payoda.smartlock.utils.Logger;

import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class PinFragment extends DialogFragment implements View.OnClickListener {

    public static final String TAG = "### PinFragment";

    EditText etPin;
    String strPin;

    public PinFragment() {
        // Required empty public constructor
    }

    public static PinFragment getInstance() {
        PinFragment pinFragment=new PinFragment();
        return pinFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            strPin = bundle.getString(Constant.SCREEN_DATA);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        Logger.d(TAG, TAG);
        return initializeView(inflater.inflate(R.layout.fragment_pin, container, false));
    }

    /**
     * Use this method to initialize the view.
     *
     * @return View Return the initialized view;
     */
    @SuppressLint("ClickableViewAccessibility")
    private View initializeView(View view) {

        TextView tvHeader = view.findViewById(R.id.tv_title);
        TextView tvTitle = view.findViewById(R.id.tv_command);
        TextView tvCancel = view.findViewById(R.id.tv_cancel);
        ImageView ivBack = view.findViewById(R.id.iv_back);
        ivBack.setVisibility(View.INVISIBLE);
        tvHeader.setVisibility(View.VISIBLE);
        // null - Create PIN flow
        // VALIDATE_PIN_FLOW - Enter PIN flow
        // data - Confirm PIN flow

        if (TextUtils.isEmpty(strPin)) {
            if (SecuredStorageManager.getInstance().isResetPin()) {
                tvTitle.setText(getString(R.string.create_new_pin));
            } else {
                tvTitle.setText(getString(R.string.create_pin));
            }
        } else {

            if (strPin.equalsIgnoreCase(Constant.VALIDATE_PIN_FLOW)) {
                tvTitle.setText(getString(R.string.enter_pin));

            } else {
                if (SecuredStorageManager.getInstance().isResetPin()) {
                    tvTitle.setText(getString(R.string.confirm_new_pin));
                    ivBack.setVisibility(View.INVISIBLE);
                    tvCancel.setVisibility(View.VISIBLE);
                } else {
                    tvTitle.setText(getString(R.string.confirm_pin));
                    ivBack.setVisibility(View.INVISIBLE);
                }
            }
        }
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() != null) {
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
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
                    // null - Create PIN flow
                    // VALIDATE_PIN_FLOW - Enter PIN flow
                    // data - Confirm PIN flow

                    //Check if the user came from reset PIN
                    if (strPin != null && strPin.equalsIgnoreCase(Constant.VALIDATE_PIN_FLOW)) {
                        //Validate current Pin with stored pin
                        if (SecuredStorageManager.getInstance().getPin().equalsIgnoreCase(data)) {
                            SecuredStorageManager.getInstance().setResetPin();
                            App.getInstance().switchFragment(getActivity().getSupportFragmentManager(),
                                    R.id.full_screen_container, Constant.SCREEN.PIN, null);
                        } else {
                            AppDialog.showAlertDialog(getContext(), getString(R.string.validate_pin));
                        }
                    } else {

                        if (TextUtils.isEmpty(strPin)) {

                            App.getInstance().switchFragment(getActivity().getSupportFragmentManager(),
                                    R.id.full_screen_container, Constant.SCREEN.PIN, data);

                        } else {
                            //Check create pin is same as confirm PIN
                            if (strPin.equalsIgnoreCase(data)) {
                                //Push PIN set success Screen.
                                SecuredStorageManager.getInstance().setPin(data);
                                App.getInstance().switchFragment(getActivity().getSupportFragmentManager(),
                                        R.id.full_screen_container, Constant.SCREEN.PIN_SUCCESS, data);
                            } else {
                                AppDialog.showAlertDialog(getContext(), getString(R.string.error_pin));
                            }
                        }
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
        if (tvTitle.getText().toString().equalsIgnoreCase(getString(R.string.enter_pin)) ||
                tvTitle.getText().toString().equalsIgnoreCase(getString(R.string.create_new_pin))
                || tvTitle.getText().toString().equalsIgnoreCase(getString(R.string.confirm_new_pin)))
        {
            tvCancel.setVisibility(View.VISIBLE);
        }
        else {
            tvCancel.setVisibility(View.INVISIBLE);
        }
        tvCancel.setOnClickListener(this);
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
            case R.id.tv_cancel:
                try {
                    if (getActivity() != null) {
                        getActivity().finish();
                        /*if (strPin != null && strPin.equalsIgnoreCase(Constant.VALIDATE_PIN_FLOW)) {
                            getActivity().finish();
                        } else {
                            getActivity().onBackPressed();
                        }*/
                    }
                } catch (Exception e) {
                    Logger.e(e);
                }
                break;
            default:
                break;
        }
    }

    private void addText(int data) {
        etPin.setText(String.format("%s%s", etPin.getText().toString(), data));
    }





}
