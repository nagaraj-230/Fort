package com.payoda.smartlock.fp.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.payoda.smartlock.App;
import com.payoda.smartlock.R;
import com.payoda.smartlock.authentication.BaseFragment;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.fp.adapter.UserListAdapter;
import com.payoda.smartlock.fp.model.FPUser;
import com.payoda.smartlock.fp.model.UserListResponse;
import com.payoda.smartlock.fp.presenter.FPPresenter;
import com.payoda.smartlock.locks.model.LockKeys;
import com.payoda.smartlock.model.ScreenData;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.users.model.User;
import com.payoda.smartlock.utils.AppDialog;
import com.payoda.smartlock.utils.Loader;
import com.payoda.smartlock.utils.Logger;
import com.payoda.smartlock.utils.SLViewBinder;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class UserListFragment extends BaseFragment implements SLViewBinder {

    public static final String TAG = "### UserListFragment";

    private TextView lblNoUser;
    private RecyclerView listUser;

    private ScreenData screenData;
    private FPPresenter presenter;

    private final int REQ_ADD_FP=1001;
    private ArrayList<LockKeys> fpExistingList;

    public UserListFragment() {
        // Required empty public constructor
    }

    public static UserListFragment getInstance() {
        return new UserListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            screenData = new Gson().fromJson(bundle.getString(Constant.SCREEN_DATA), ScreenData.class);
            fpExistingList=screenData.getFpExistingList();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Logger.d(TAG,TAG);
        return initializeView(inflater.inflate(R.layout.fragment_user_list, container, false));


    }

    private View initializeView(final View view) {
        presenter=new FPPresenter(this);
        ((TextView) view.findViewById(R.id.tv_title)).setText(getString(R.string.finger_print));
        lblNoUser=view.findViewById(R.id.lblNoUser);
        listUser=view.findViewById(R.id.listUser);
        listUser.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        listUser.setLayoutManager(linearLayoutManager);

        view.findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();

            }
        });
        getUserList();
        return view;
    }

    private void getUserList(){
        Loader.getInstance().showLoader(getContext());
        presenter.getUserList(getContext(),screenData.getLock(), new ResponseHandler() {
            @Override
            public void onSuccess(Object data) {
                UserListResponse response=(UserListResponse) data;
                ArrayList<User> users=response.getUsers();
                UserListAdapter adapter=new UserListAdapter(users) {
                    @Override
                    public void onUserSelect(User user) {
                        navigateToAdd(user);
                    }
                };
                listUser.setAdapter(adapter);
            }

            @Override
            public void onAuthError(String message) {

            }

            @Override
            public void onError(String message) {
                AppDialog.showAlertDialog(getContext(),message);
            }
        });
    }

    private void navigateToAdd(User user){
        FPUser fpUser=new FPUser();
        ArrayList<String> keys = new ArrayList<>();
        if(fpExistingList!=null && fpExistingList.size()>0){
            for(int i=0;i<fpExistingList.size();i++){
                if(fpExistingList.get(i).getLockUser()!=null){
                    int usedId=Integer.parseInt(fpExistingList.get(i).getLockUser().getId());
                    if(usedId==user.getId()){
                        fpUser.setId(fpExistingList.get(i).getId());
                        LockKeys selectedUser = fpExistingList.get(i);
                        if(selectedUser.getKey() != null && selectedUser.getKey().length() > 0) {
                            Gson gson = new Gson();
                            Type keyType = new TypeToken<ArrayList<String>>() {
                            }.getType();
                            keys = gson.fromJson(selectedUser.getKey(), keyType);
                            fpUser.setKeys(keys);
                        }
                    }
                }
            }

        }
        ScreenData payload=new ScreenData();
        payload.setLock(screenData.getLock());
        payload.setLockKeys(new LockKeys());

        fpUser.setName(user.getUsername());
        fpUser.setUserId(user.getId()+"");
        fpUser.setKeys(keys);
        payload.setFpUser(fpUser);
        App.getInstance().showFullScreenForResult(getActivity(), Constant.SCREEN.FINGER_PRINT_ADD, new Gson().toJson(payload),REQ_ADD_FP);
    }

    @Override
    public void onViewUpdate(int reqCode, Object response) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQ_ADD_FP && resultCode== Activity.RESULT_OK){
            getActivity().setResult(Activity.RESULT_OK);
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        }
    }
}
