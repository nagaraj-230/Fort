package com.payoda.smartlock.fp.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.payoda.smartlock.App;
import com.payoda.smartlock.R;
import com.payoda.smartlock.authentication.BaseFragment;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.fp.presenter.FPPresenter;
import com.payoda.smartlock.fp.presenter.RFIDPresenter;
import com.payoda.smartlock.fp.service.FPService;
import com.payoda.smartlock.locks.model.Lock;
import com.payoda.smartlock.locks.model.LockKeys;
import com.payoda.smartlock.model.ScreenData;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.pushnotification.RemoteDataEvent;
import com.payoda.smartlock.utils.AppDialog;
import com.payoda.smartlock.utils.Loader;
import com.payoda.smartlock.utils.Logger;
import com.payoda.smartlock.utils.SLViewBinder;

import java.util.ArrayList;

import static com.payoda.smartlock.constants.Constant.HW_VERSION_2_1;
import static com.payoda.smartlock.constants.Constant.HW_VERSION_4_0;
import static com.payoda.smartlock.constants.Constant.RFID_EMPTY;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class RFIDListFragment extends BaseFragment implements SLViewBinder {

    public static final String TAG = " ### RFIDListFragment";

    private LinearLayout llRFID;
    private TextView lblRFID1, lblRFID1Revoke, lblRFID2, lblRFID2Revoke;
    private TextView lblRFID3, lblRFID3Revoke;

    private Lock mLock;
    private ArrayList<LockKeys> rfidKeys;
    private FPPresenter fpPresenter;
    private RFIDPresenter rfidPresenter;

    private final int REQ_RFID = 1;
    private final int REQ_DELETE_RFID = 2;

    private final int REQ_ADD_RFID_SCREEN = 1001;

    public RFIDListFragment() {
        // Required empty public constructor
    }

    public static RFIDListFragment getInstance() {
        return new RFIDListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mLock = new Gson().fromJson(bundle.getString(Constant.SCREEN_DATA), Lock.class);
            Logger.d("### lock data RFID = " + new Gson().fromJson(bundle.getString(Constant.SCREEN_DATA), Lock.class));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Logger.d(TAG, TAG);
        return initializeView(inflater.inflate(R.layout.fragment_list_rfid, container, false));
    }

    private View initializeView(final View view) {

        fpPresenter = new FPPresenter(this);
        rfidPresenter = new RFIDPresenter(this);

        ((TextView) view.findViewById(R.id.tv_title)).setText(getString(R.string.rfid));
        llRFID = view.findViewById(R.id.ll_rfid);
        lblRFID1 = view.findViewById(R.id.lblRFID1);
        lblRFID1Revoke = view.findViewById(R.id.lblRFID1Revoke);
        lblRFID2 = view.findViewById(R.id.lblRFID2);
        lblRFID2Revoke = view.findViewById(R.id.lblRFID2Revoke);
        lblRFID3 = view.findViewById(R.id.lblRFID3);
        lblRFID3Revoke = view.findViewById(R.id.lblRFID3Revoke);


        lblRFID1Revoke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (lblRFID1Revoke.getText().toString().equalsIgnoreCase("Add")) {
                    System.out.println("#### ADD");
                    doAddRFID(0);
                } else {
                    System.out.println("#### DELETE");
                    doDeleteRFID(0);
                }
            }
        });
        lblRFID2Revoke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (lblRFID2Revoke.getText().toString().equalsIgnoreCase("Add")) {
                    doAddRFID(1);
                } else {
                    doDeleteRFID(1);
                }
            }
        });
        lblRFID3Revoke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (lblRFID3Revoke.getText().toString().equalsIgnoreCase("Add")) {
                    doAddRFID(2);
                } else {
                    doDeleteRFID(2);
                }
            }
        });
        view.findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();

            }
        });
        getRFIDList();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(RemoteDataEvent remoteDataEvent) {
        if(remoteDataEvent != null && remoteDataEvent.getStatus().equalsIgnoreCase(Constant.SUCCESS) && remoteDataEvent.getCommand().equalsIgnoreCase(Constant.RFID_DELETE_COMMAND)){
            Loader.getInstance().hideLoader();
            AppDialog.showAlertDialog(getContext(), remoteDataEvent.getTitle(), remoteDataEvent.getBody(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (getActivity() != null)
                        requireActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            });
        }else if(remoteDataEvent != null && remoteDataEvent.getStatus().equalsIgnoreCase(Constant.FAILURE) && remoteDataEvent.getCommand().equalsIgnoreCase(Constant.RFID_DELETE_COMMAND)){
            Loader.getInstance().hideLoader();
            AppDialog.showAlertDialog(getActivity(), remoteDataEvent.getBody());
        }
    }

    private void doDeleteRFID(int index) {

        if (mLock.getLockVersion().equalsIgnoreCase(HW_VERSION_4_0)) {

            Loader.getInstance().showLoader(getContext());
            LockKeys lockKeys = rfidKeys.get(index);
            int rfid = Integer.parseInt(lockKeys.getKey());


            FPService.getInstance().revokeRfidPrivilegeMqtt(mLock.getSerialNumber(), rfid, new ResponseHandler() {
                @Override
                public void onSuccess(Object data) {
                    Loader.getInstance().hideLoader();
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
            });
        }
        else {

            getLocationPermission(new RequestPermissionAction() {
                @Override
                public void permissionDenied() {

                }

                @Override
                public void permissionGranted() {
                    rfidPresenter.doDeleteRFID(getContext(), REQ_DELETE_RFID, mLock, rfidKeys.get(index));
                }
            });

        }
    }

    private void doAddRFID(int index) {

        getLocationPermission(new RequestPermissionAction() {
            @Override
            public void permissionDenied() {

            }

            @Override
            public void permissionGranted() {
                ScreenData screenData = new ScreenData();
                screenData.setLock(mLock);
                screenData.setLockKeys(rfidKeys.get(index));
                App.getInstance().showFullScreenForResult(getActivity(), Constant.SCREEN.RFID_ADD, new Gson().toJson(screenData),
                        REQ_ADD_RFID_SCREEN);
            }
        });

    }

    private void getRFIDList() {
        Loader.getInstance().showLoader(getContext());
        fpPresenter.getRFIDOrFPList(getActivity(), mLock.getId(), Constant.RFID, REQ_RFID);
    }

    private void populateUI() {
        if (rfidKeys != null && rfidKeys.size() >= 3) {
            llRFID.setVisibility(View.VISIBLE);
            if (rfidKeys.get(0).getKey().equalsIgnoreCase(RFID_EMPTY)) {
                lblRFID1.setText("Add RFID 1");
                lblRFID1Revoke.setText("Add");
            } else {
                lblRFID1.setText("RFID 1");
                lblRFID1Revoke.setText("Revoke");
            }
            if (rfidKeys.get(1).getKey().equalsIgnoreCase(RFID_EMPTY)) {
                lblRFID2.setText("Add RFID 2");
                lblRFID2Revoke.setText("Add");
            } else {
                lblRFID2.setText("RFID 2");
                lblRFID2Revoke.setText("Revoke");
            }
            if (rfidKeys.get(2).getKey().equalsIgnoreCase(RFID_EMPTY)) {
                lblRFID3.setText("Add RFID 3");
                lblRFID3Revoke.setText("Add");
            } else {
                lblRFID3.setText("RFID 3");
                lblRFID3Revoke.setText("Revoke");
            }
        }else {
            llRFID.setVisibility(View.GONE);
        }
    }

    @Override
    public void onViewUpdate(int reqCode, Object response) {
        if (REQ_RFID == reqCode) {
            Loader.getInstance().hideLoader();
            rfidKeys = (ArrayList<LockKeys>) response;
            populateUI();
        } else if (REQ_DELETE_RFID == reqCode) {
            if (getActivity() != null)
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_ADD_RFID_SCREEN && resultCode == getActivity().RESULT_OK) {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        }
    }

}
