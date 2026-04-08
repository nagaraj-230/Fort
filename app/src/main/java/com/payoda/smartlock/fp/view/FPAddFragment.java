package com.payoda.smartlock.fp.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.google.gson.Gson;
import com.payoda.smartlock.App;
import com.payoda.smartlock.R;
import com.payoda.smartlock.authentication.BaseFragment;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.fp.model.FPUser;
import com.payoda.smartlock.fp.presenter.FPPresenter;
import com.payoda.smartlock.history.model.AccessLogList;
import com.payoda.smartlock.history.model.AccessLogResponse;
import com.payoda.smartlock.history.service.HistoryService;
import com.payoda.smartlock.locks.model.Lock;
import com.payoda.smartlock.locks.model.LockKeys;
import com.payoda.smartlock.model.ScreenData;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.plugins.storage.SecuredStorageManager;
import com.payoda.smartlock.utils.Logger;
import com.payoda.smartlock.utils.SLViewBinder;

public class FPAddFragment extends BaseFragment implements SLViewBinder {

    public static final String TAG = "### FPAddFragment";


    private Button btnNext, btnPress;
    private EditText txtName;
    private CardView layoutStep2, layoutStep1;
    private TextView textFingerPrint;


    private LockKeys existingLockKeys;
    private Lock mLock;

    private FPPresenter presenter;
    private final int REQ_ADD_FP = 1;
    private FPUser fpUser;

    private final int REQ_ADD_SUCCESS_SCREEN = 1001;

    public FPAddFragment() {
        // Required empty public constructor
    }

    public static FPAddFragment getInstance() {
        return new FPAddFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            ScreenData screenData = new Gson().fromJson(bundle.getString(Constant.SCREEN_DATA), ScreenData.class);
            existingLockKeys = screenData.getLockKeys();
            mLock = screenData.getLock();
            fpUser = screenData.getFpUser();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Logger.d(TAG, TAG);
        return initializeView(inflater.inflate(R.layout.fragment_add_fp, container, false));
    }

    private View initializeView(final View view) {

        presenter = new FPPresenter(this);

        ((TextView) view.findViewById(R.id.tv_title)).setText(getString(R.string.finger_print_add));
        btnNext = view.findViewById(R.id.btnNext);
        btnPress = view.findViewById(R.id.btnPress);
        txtName = view.findViewById(R.id.txtName);
        textFingerPrint = view.findViewById(R.id.text_view_fp);

        layoutStep1 = view.findViewById(R.id.layoutStep1);
        layoutStep2 = view.findViewById(R.id.layoutStep2);

        if (!TextUtils.isEmpty(fpUser.getName())) {
            layoutStep1.setVisibility(View.GONE);
            layoutStep2.setVisibility(View.VISIBLE);
        }

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateName()) {

                    hideKeyboard();
                    layoutStep1.setVisibility(View.GONE);
                    layoutStep2.setVisibility(View.VISIBLE);
                    fpUser.setName(txtName.getText().toString());

                }
            }
        });

        btnPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getLocationPermission(new RequestPermissionAction() {
                    @Override
                    public void permissionDenied() {

                    }

                    @Override
                    public void permissionGranted() {
                        fpUser.setLockId(mLock.getId());
                        presenter.doAddFingerPrint(getContext(), REQ_ADD_FP, mLock, fpUser, FPPresenter.FP_ADD);
                    }
                });

            }
        });

        view.findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            }

        });

        textFingerPrint.setText(presenter.doUpdateUI(mLock));

        return view;

    }

    private boolean validateName() {

        String name = txtName.getText().toString();
        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Please enter name.", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    @Override
    public void onViewUpdate(int reqCode, Object response) {
        if (reqCode == REQ_ADD_FP) {
            if (response != null && response.toString().equalsIgnoreCase("success")) {
                App.getInstance().showFullScreenForResult(getActivity(), Constant.SCREEN.RFID_SUCCESS, getString(R.string.finger_print), REQ_ADD_SUCCESS_SCREEN);
            } else {
                if (getActivity() != null) {
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                    Logger.d("### 1 FP add back onViewUpdate");

                }
            }
        }
    }

    private void doHistoryUpdate() {
        if ((ServiceManager.getInstance().isNetworkAvailable(getActivity()) ||
                ServiceManager.getInstance().isMobileDataEnabled(getContext())) && mLock.getId() != null) {
            AccessLogList data = SecuredStorageManager.getInstance().getAccessLogs(mLock.getSerialNumber(), mLock.getId());
            HistoryService.getInstance().serviceRequest(mLock.getId(), data, new ResponseHandler() {
                @Override
                public void onSuccess(Object data) {
                    AccessLogResponse response = (AccessLogResponse) data;
                    if (response != null && response.getStatus().equalsIgnoreCase("success")) {
                        SecuredStorageManager.getInstance().removeSyncedLog(mLock.getId());
                    }
                }

                @Override
                public void onAuthError(String message) {

                }

                @Override
                public void onError(String message) {

                }
            });
        }
    }

    private void hideKeyboard() {
        try {
            if (getActivity() != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(txtName.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
            }
        } catch (Exception e) {
            Logger.e(e);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)  {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_ADD_SUCCESS_SCREEN && resultCode == Activity.RESULT_OK) {
            getActivity().setResult(Activity.RESULT_OK);
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        }
    }

}

