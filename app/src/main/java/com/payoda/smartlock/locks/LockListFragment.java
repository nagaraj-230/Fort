package com.payoda.smartlock.locks;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.payoda.smartlock.App;
import com.payoda.smartlock.BaseFragment;
import com.payoda.smartlock.R;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.locks.adapter.LockListAdapter;
import com.payoda.smartlock.locks.callback.ILockSelectCallback;
import com.payoda.smartlock.locks.model.Lock;
import com.payoda.smartlock.locks.model.Locks;
import com.payoda.smartlock.locks.model.ServerTime;
import com.payoda.smartlock.locks.service.LockListService;
import com.payoda.smartlock.locks.service.UtilService;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.plugins.storage.StorageManager;
import com.payoda.smartlock.plugins.storage.lock.LockDBClient;
import com.payoda.smartlock.utils.AppDialog;
import com.payoda.smartlock.utils.DateTimeUtils;
import com.payoda.smartlock.utils.Loader;
import com.payoda.smartlock.utils.Logger;
import com.payoda.smartlock.utils.PaginationScrollListener;

import java.util.ArrayList;

import static com.payoda.smartlock.constants.Constant.LIMIT;
import static com.payoda.smartlock.utils.DateTimeUtils.YYYYMMDD_HHMMSS;

/**
 * Created by nivetha.m on 6/11/2018.
 * A simple {@link Fragment} subclass.
 */
public class LockListFragment extends Fragment implements ILockSelectCallback, SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = "### LockListFragment";

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LockListAdapter mLockListAdapter;
    private FloatingActionButton fb_add;
    private TextView tv_no_lock_error;

    private boolean loading = false;
    private boolean initialLoading = true, lastPage = false;
    private ProgressBar load_bottom_more_progress;
    private int offset = 0;
    private ArrayList<Lock> offLineLockList = new ArrayList<>();

    public LockListFragment() {
        // Required empty public constructor
    }

    public static LockListFragment getInstance() {
        return new LockListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Logger.d(TAG, TAG);
        return initialiseView(inflater.inflate(R.layout.fragment_lock_list, container, false));
    }

    @Override
    public void onResume() {
        super.onResume();
        loadLocal();

    }

    private void loadLocal(){

        LockDBClient.getInstance().getAll(getContext(), new LockDBClient.DBCallback() {
            @Override
            public void onLockList(ArrayList<Lock> lockList) {
                if(lockList!=null){
                    offLineLockList=lockList;
                }
                doLockListRequest(true);
            }

            @Override
            public void onSuccess(String msg) {

            }
        });
    }

    private View initialiseView(View rootView) {

        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        load_bottom_more_progress = rootView.findViewById(R.id.load_bottom_more_progress);
        swipeRefreshLayout.setOnRefreshListener(this);

        mRecyclerView = rootView.findViewById(R.id.lockListDetailView);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        // Set Layout Manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(LockListFragment.this.getContext());
        mRecyclerView.setLayoutManager(linearLayoutManager);
        // Limiting the size
        mRecyclerView.setHasFixedSize(true);
        mLockListAdapter = new LockListAdapter(getContext(),null, this);
        mRecyclerView.setAdapter(mLockListAdapter);


        fb_add = rootView.findViewById(R.id.fb_add);
        fb_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                App.getInstance().showFullScreen(getActivity(), Constant.SCREEN.LOCK_ADD, null);
            }
        });
        tv_no_lock_error = rootView.findViewById(R.id.tv_no_lock_error);

        ignoreBatteryOptimization();

        mRecyclerView.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                loading = true;
                offset++;
                doLockListRequest(false);
            }

            @Override
            public int getTotalPageCount() {
                return LIMIT;
            }

            @Override
            public boolean isLastPage() {
                return lastPage;
            }

            @Override
            public boolean isLoading() {
                return loading;
            }
        });

        return rootView;
    }

    private void ignoreBatteryOptimization() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // Do something for lollipop and above versions
            Intent intent = new Intent();
            String packageName = getContext().getPackageName();
            PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
                            /*if (pm.isIgnoringBatteryOptimizations(packageName))
                                intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                            else {*/
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + packageName));
            //}
            getContext().startActivity(intent);

        }
    }

    private void doLockListRequest(final boolean isPullDown) {

        prepopulateData(offLineLockList, isPullDown);

        if(ServiceManager.getInstance().isNetworkAvailable(getContext())) {
            if (isPullDown) {
                showRefreshing();
            } else {
                load_bottom_more_progress.setVisibility(View.VISIBLE);
            }
            LockListService.getInstance().getLock(50, offset, new ResponseHandler() {
                @Override
                public void onSuccess(Object data) {
                    hideRefreshing();
                    loading = false;
                    load_bottom_more_progress.setVisibility(View.GONE);
                    if (data != null) {
                        Locks mLocks = (Locks) data;
                        prepopulateData(mLocks.getLocks(), isPullDown);
                    }
                }

                @Override
                public void onAuthError(String message) {
                    hideRefreshing();
                    loading = false;
                    load_bottom_more_progress.setVisibility(View.GONE);
                    if (getActivity() != null) {
                        AppDialog.showAlertDialog(getActivity(), message, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                App.getInstance().showLogin(getActivity());
                            }
                        });
                    }
                }

                @Override
                public void onError(String message) {
                    loading = false;
                    load_bottom_more_progress.setVisibility(View.GONE);
                    hideRefreshing();
                }
            });
        }
    }

    private void showRefreshing() {
        if (swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(true);
    }

    private void hideRefreshing() {
        if (swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Method to load data to adapter
     *
     * @param onlineLocks
     */
    private void prepopulateData(ArrayList<Lock> onlineLocks, boolean isClear) {

        try {
            if (!isClear) {
                appendLock(onlineLocks);
                return;
            } else {
                mLockListAdapter.clearList();
            }

            /*ArrayList<Lock> offLineLockList = new ArrayList<>();
            Locks offlineLocks = StorageManager.getInstance().getOfflineLocks();
            if (offlineLocks != null && offlineLocks.getLocks() != null && offlineLocks.getLocks().size() > 0) {
                offLineLockList = offlineLocks.getLocks();
            }*/

            ArrayList<Lock> entireLockList = new ArrayList<>();

            ArrayList<Lock> filteredLocalLocks = new ArrayList<>();

            if (offLineLockList.size() > 0) {

                if (onlineLocks != null && onlineLocks.size() > 0) {

                    for (Lock offlineLock : offLineLockList) {

                        boolean isFound = false;

                        for (Lock onlineLock : onlineLocks) {

                            if (onlineLock.getSerialNumber().equalsIgnoreCase(offlineLock.getSerialNumber())) {
                                // For lock version 4.0 and 6.0, backend will get the battery value from the hardware directly via MQTT.
                                // So, no need to over write the online battery status with offline value.
                                if(!offlineLock.getLockVersion().equalsIgnoreCase(Constant.HW_VERSION_4_0) &&
                                        !offlineLock.getLockVersion().equalsIgnoreCase(Constant.HW_VERSION_6_0) ){

                                    onlineLock.setBattery(offlineLock.getBattery());
                                }
                                isFound = true;
                            }


                        }


                        if (!isFound && offlineLock.isOffline() && offlineLock.getStatus()!=null &&
                                offlineLock.getStatus()!=Constant.FACTORY_RESET) {
                            filteredLocalLocks.add(offlineLock);
                        }
                    }
                    entireLockList.addAll(filteredLocalLocks);
                    entireLockList.addAll(onlineLocks);
                } else {
                    // Add all offline locks if no online locks found
                    entireLockList.addAll(offLineLockList);
                }
            } else {
                if (onlineLocks != null &&
                        onlineLocks.size() > 0) {
                    // Add all online locks if no offline locks found
                    entireLockList.addAll(onlineLocks);
                }
            }

            ArrayList<Lock> tempLockList = new ArrayList<>();
            tempLockList.addAll(entireLockList);
            //Remove the deleted lock
            for (int i = 0; i < tempLockList.size(); i++) {
                for (Lock offlineLock : offLineLockList) {
                    if (tempLockList.get(i).getSerialNumber().equalsIgnoreCase(offlineLock.getSerialNumber()) &&
                            offlineLock.getStatus() != null && offlineLock.getStatus().equalsIgnoreCase(Constant.FACTORY_RESET)) {
                        entireLockList.remove(i);
                    }
                }
            }

            //StorageManager.getInstance().setLocks(onlineLocks);
            if(ServiceManager.getInstance().isNetworkAvailable(getContext())) {
                if(onlineLocks != null){
                    LockDBClient.getInstance().deleteAllLock(getContext(), new LockDBClient.DBCallback(){
                        @Override
                        public void onLockList(ArrayList<Lock> lockList) {
                        }
                        @Override
                        public void onSuccess(String msg) {
                            LockDBClient.getInstance().saveAll(onlineLocks, getContext());
                        }
                    });
                }
            }

            ArrayList<Lock> filterScheduleLock= new ArrayList<>();
            for(Lock scheduleLock:entireLockList){
                if(scheduleLock.getLockKeys()!=null && scheduleLock.getLockKeys().size() == 0 &&
                        scheduleLock.getLockKeys().get(1).getIs_schedule_access()!=null &&
                        scheduleLock.getLockKeys().get(1).getIs_schedule_access().equalsIgnoreCase("1")){
                    String startDate=scheduleLock.getLockKeys().get(1).getSchedule_date_from();
                    String startTime=scheduleLock.getLockKeys().get(1).getSchedule_time_from();

                    String endDate=scheduleLock.getLockKeys().get(1).getSchedule_date_to();
                    String endTime=scheduleLock.getLockKeys().get(1).getSchedule_time_to();

                    String startDateAndTime= DateTimeUtils.getLocalDateFromGMT(startDate,startTime,YYYYMMDD_HHMMSS);
                    String endDateAndTime= DateTimeUtils.getLocalDateFromGMT(endDate,endTime,YYYYMMDD_HHMMSS);

                    String startDateAlone=startDateAndTime.split(" ")[0];
                    String endDateAlone=endDateAndTime.split(" ")[0];

                    if(DateTimeUtils.isBetweenDate(startDateAlone,endDateAlone)){
                        filterScheduleLock.add(scheduleLock);
                    }
                }else{
                    filterScheduleLock.add(scheduleLock);
                }
            }
            entireLockList.clear();
            entireLockList.addAll(filterScheduleLock);

            mLockListAdapter.setItemList(entireLockList);
            changeViewVisibility();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void appendLock(ArrayList<Lock> onlineLocks) {
        if (onlineLocks != null && !onlineLocks.isEmpty()) {
            mLockListAdapter.setItemList(onlineLocks);
            if (onlineLocks.size() < LIMIT) {
                lastPage = true;
            }
        } else {
            lastPage = true;
        }
    }

    private void prepopulateLocalData(Locks locks) {
        try {
            if (locks != null) {
                mLockListAdapter.setItemList(locks.getLocks());
            }
            changeViewVisibility();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void changeViewVisibility() {
        if (mLockListAdapter != null && mLockListAdapter.getItemCount() > 0) {
            //Show List
            swipeRefreshLayout.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.VISIBLE);
            tv_no_lock_error.setVisibility(View.GONE);
        } else {
            //Hide List
            swipeRefreshLayout.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
            tv_no_lock_error.setVisibility(View.VISIBLE);
        }
    }

 /*   @Override
    public void onLockItemSelect(Lock lock) {

        Logger.d("####### lock.getLockKeys().size() > 1 " + (lock.getLockKeys().size() >= 1));
        Logger.d("####### lock.getLockKeys().size()  " + (lock.getLockKeys().size()));

        if (lock != null && lock.getLockKeys()!=null && lock.getLockKeys().size() >= 1) {
            if(lock.getLockKeys().get(1).getIs_schedule_access()!=null
                    && lock.getLockKeys().get(1).getIs_schedule_access().equalsIgnoreCase("1")){

                if(ServiceManager.getInstance().isNetworkAvailable(getContext())) {
                    doValidateServerTime(lock);
                }else{
                    AppDialog.showAlertDialog(getContext(), getString(R.string.lock_schedule_alert));
                }

            }else {
                Logger.d("### lock detail = " + lock);
                App.getInstance().showFullScreen(getActivity(), Constant.SCREEN.LOCK_DETAILS, new Gson().toJson(lock));
            }

        } else {
            AppDialog.showAlertDialog(getContext(), getString(R.string.lock_detail_error));
        }

    }*/

    @Override
    public void onLockItemSelect(Lock lock) {

        if (lock != null && lock.getLockKeys() != null && lock.getLockKeys().size() > 1) {

            Logger.d("####### lock.getLockKeys().size() > 1 " + (lock.getLockKeys().size() > 1));
            Logger.d("####### lock.getLockKeys().size()  " + (lock.getLockKeys().size()));

            if (lock.getLockKeys().get(1).getIs_schedule_access() != null &&
                    lock.getLockKeys().get(1).getIs_schedule_access().equalsIgnoreCase("1")) {
                if (ServiceManager.getInstance().isNetworkAvailable(getContext())) {
                    doValidateServerTime(lock);
                } else {
                    AppDialog.showAlertDialog(getContext(), getString(R.string.lock_schedule_alert));
                }
            }
            else {
                Logger.d("### lock detail = " + lock);
                App.getInstance().showFullScreen(getActivity(), Constant.SCREEN.LOCK_DETAILS, new Gson().toJson(lock));
            }

        } else if (lock != null && lock.getLockKeys() != null && lock.getLockKeys().size() == 1) {

            Logger.d("####### lock.getLockKeys().size() == 1 " + (lock.getLockKeys().size() == 1));
            Logger.d("####### lock.getLockKeys().size()  " + (lock.getLockKeys().size()));

            if (lock.getLockKeys().get(0).getIs_schedule_access() != null &&
                    lock.getLockKeys().get(0).getIs_schedule_access().equalsIgnoreCase("1")) {

                if (ServiceManager.getInstance().isNetworkAvailable(getContext())) {
                    doValidateServerTime(lock);
                } else {
                    AppDialog.showAlertDialog(getContext(), getString(R.string.lock_schedule_alert));
                }

            } else {
                Logger.d("### lock detail = " + lock);
                App.getInstance().showFullScreen(getActivity(), Constant.SCREEN.LOCK_DETAILS, new Gson().toJson(lock));
            }

        } else {
            AppDialog.showAlertDialog(getContext(), getString(R.string.lock_detail_error));
        }
    }


    private void doValidateServerTime(final Lock lock) {
        Loader.getInstance().showLoader(getContext());

        UtilService.getInstance().getServerTime(new ResponseHandler() {
            @Override
            public void onSuccess(Object data) {
                Loader.getInstance().hideLoader();
                ServerTime objServerTime = (ServerTime) data;

                String serverTime = objServerTime.getData().getServer_time();
                
                String startDate= lock.getLockKeys().get(1).getSchedule_date_from();
                String startTime= lock.getLockKeys().get(1).getSchedule_time_from();

                String endDate= lock.getLockKeys().get(1).getSchedule_date_to();
                String endTime=lock.getLockKeys().get(1).getSchedule_time_to();

                Logger.d("#### startDate " + startDate);
                Logger.d("#### startTime " + startTime);
                Logger.d("#### endDate " + endDate);
                Logger.d("#### endTime " + endTime);
                Logger.d("#### serverTime " + serverTime);


                /*String startDateAndTime= DateTimeUtils.getLocalDateFromGMT(startDate,startTime,YYYYMMDD_HHMMSS);
                String endDateAndTime= DateTimeUtils.getLocalDateFromGMT(endDate,endTime,YYYYMMDD_HHMMSS);

                String startDateAlone=startDateAndTime.split(" ")[0];
                String endDateAlone=endDateAndTime.split(" ")[0];

                String startTimeAlone=startDateAndTime.split(" ")[1];
                String endTimeAlone=endDateAndTime.split(" ")[1];*/

                boolean isDateValid=DateTimeUtils.isBetweenDate(startDate,endDate,serverTime);
                boolean isTimeValid=DateTimeUtils.isBetweenTime(startTime,endTime,serverTime);

                if(isDateValid && isTimeValid){
                    App.getInstance().showFullScreen(getActivity(), Constant.SCREEN.LOCK_DETAILS, new Gson().toJson(lock));
                }else{
                    AppDialog.showAlertDialog(getContext(), "You don't have permission to access now.");
                }
            }

            @Override
            public void onAuthError(String message) {
                Loader.getInstance().hideLoader();
                if (getActivity() != null) {
                    AppDialog.showAlertDialog(getActivity(), message, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            App.getInstance().showLogin(getActivity());
                        }
                    });
                }
            }

            @Override
            public void onError(String message) {
                Loader.getInstance().hideLoader();
            }
        });
    }

    @Override
    public void onRefresh() {
        if (ServiceManager.getInstance().isNetworkAvailable(getContext())) {
            offset = 0;
            lastPage = false;
            //doLockListRequest(true);
            loadLocal();
        } else {
            hideRefreshing();
            AppDialog.showAlertDialog(getContext(), getString(R.string.no_internet));
        }
    }

}