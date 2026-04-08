package com.payoda.smartlock.fp.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.payoda.smartlock.App;
import com.payoda.smartlock.R;
import com.payoda.smartlock.authentication.BaseFragment;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.fp.presenter.RFIDPresenter;
import com.payoda.smartlock.locks.model.Lock;
import com.payoda.smartlock.locks.model.LockKeys;
import com.payoda.smartlock.model.ScreenData;
import com.payoda.smartlock.utils.Logger;
import com.payoda.smartlock.utils.SLViewBinder;

public class RFIDAddFragment extends BaseFragment implements SLViewBinder {

    public static final String TAG = "### RFIDAddFragment";

    private Lock mLock;
    private LockKeys rfidKeys;
    private Button btnAddRFID;
    private RFIDPresenter presenter;

    private final int REQ_ADD_RFID=1;

    private final int REQ_ADD_SUCCESS_SCREEN=1001;

    public RFIDAddFragment() {
        // Required empty public constructor
    }

    public static RFIDAddFragment getInstance() {
        return new RFIDAddFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            ScreenData screenData = new Gson().fromJson(bundle.getString(Constant.SCREEN_DATA), ScreenData.class);
            mLock=screenData.getLock();
            rfidKeys=screenData.getLockKeys();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Logger.d(TAG, TAG);
        return initializeView(inflater.inflate(R.layout.fragment_rfid_add, container, false));
    }

    private View initializeView(final View view) {
        presenter=new RFIDPresenter(this);
        ((TextView) view.findViewById(R.id.tv_title)).setText("Add RFID");
        btnAddRFID=view.findViewById(R.id.btnAddRFID);
        btnAddRFID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.doAddRFID(getContext(),REQ_ADD_RFID,mLock,rfidKeys);
            }
        });
        view.findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();

            }
        });
        return view;
    }

    @Override
    public void onViewUpdate(int reqCode, Object response) {
        if(reqCode==REQ_ADD_RFID){
            if(response!=null && response.toString().equalsIgnoreCase("success")){
                App.getInstance().showFullScreenForResult(getActivity(), Constant.SCREEN.RFID_SUCCESS, getString(R.string.rfid),REQ_ADD_SUCCESS_SCREEN);
            }else{
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQ_ADD_SUCCESS_SCREEN && resultCode==getActivity().RESULT_OK){
            getActivity().setResult(Activity.RESULT_OK);
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        }
    }


}
