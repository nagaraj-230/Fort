package com.payoda.smartlock.history;

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.payoda.smartlock.R;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.history.adapter.LockNotificationAdapter;
import com.payoda.smartlock.history.model.LockNotificationList;
import com.payoda.smartlock.history.service.MasterLogService;
import com.payoda.smartlock.locks.model.Lock;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.utils.AppDialog;
import com.payoda.smartlock.utils.Loader;
import com.payoda.smartlock.utils.Logger;
import com.payoda.smartlock.utils.PaginationScrollListener;

import static com.payoda.smartlock.constants.Constant.LIMIT;

/**
 * Created by david on 10/30/2018.
 */

public class LockNotificationFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = "### LockNotificationFragment";

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView tvNoHistory;
    private LockNotificationAdapter mHistoryAdapter;
    private Lock mLock = new Lock();
    private LinearLayout header;
    private boolean loading = false;
    private boolean initialLoading = true, lastPage = false;
    private ProgressBar load_bottom_more_progress;
    private int offset = 0;


    public LockNotificationFragment() {
        // Required empty public constructor
    }

    public static LockNotificationFragment getInstance() {
        return new LockNotificationFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mLock = new Gson().fromJson(bundle.getString(Constant.SCREEN_DATA), Lock.class);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Logger.d(TAG, TAG);
        return initializeView(inflater.inflate(R.layout.fragment_lock_notification, container, false));

    }

    @Override
    public void onResume() {
        super.onResume();
        //
    }

    private View initializeView(View rootView) {
        tvNoHistory = rootView.findViewById(R.id.tv_no_notification);
        mSwipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        load_bottom_more_progress=rootView.findViewById(R.id.load_bottom_more_progress);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        RecyclerView mRecyclerView = rootView.findViewById(R.id.notificationsDetailView);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        // Set Layout Manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(linearLayoutManager);
        // Limiting the size
        mRecyclerView.setHasFixedSize(true);

        mHistoryAdapter = new LockNotificationAdapter(null);
        mRecyclerView.setAdapter(mHistoryAdapter);

        mRecyclerView.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                if (!initialLoading) {
                    if (ServiceManager.getInstance().isNetworkAvailable(getContext())) {
                        loading = true;
                        offset++;
                        doRequestList(false);
                    }else{
                        AppDialog.showAlertDialog(getContext(), getString(R.string.no_internet));
                    }

                } else {
                    initialLoading = false;
                }
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

        doRequestList(true);
        return rootView;
    }

    private void doRequestList(boolean isPullDown) {
        if (ServiceManager.getInstance().isNetworkAvailable(getContext())) {
            if(mLock.getId()!=null) {
                if(isPullDown) {
                    showRefreshing();
                }else {
                    load_bottom_more_progress.setVisibility(View.VISIBLE);
                }
                MasterLogService.getInstance().serviceNotificationRequest(mLock.getId(), LIMIT, offset, new ResponseHandler() {
                    @Override
                    public void onSuccess(Object data) {
                        try {
                            loading=false;
                            load_bottom_more_progress.setVisibility(View.GONE);
                            hideRefreshing();
                            prepopulateData((LockNotificationList) data,isPullDown);
                        } catch (Exception e) {
                            Logger.e(e);
                            AppDialog.showAlertDialog(getContext(), getString(R.string.oops_error));
                        }
                    }

                    @Override
                    public void onAuthError(String message) {
                        loading=false;
                        load_bottom_more_progress.setVisibility(View.GONE);
                        hideRefreshing();
                        if (getActivity() != null) {
                            AppDialog.showAlertDialog(getActivity(), message, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onError(String message) {
                        loading=false;
                        load_bottom_more_progress.setVisibility(View.GONE);
                        hideRefreshing();
                        Loader.getInstance().hideLoader();
                        prepopulateData(null,true);
                    }
                });
            }
        } else {
            hideRefreshing();
            tvNoHistory.setText(getString(R.string.no_internet));
            tvNoHistory.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRefresh() {
        if (ServiceManager.getInstance().isNetworkAvailable(getContext())) {
            lastPage = false;
            offset = 0;
            doRequestList(true);
        } else {
            hideRefreshing();
            AppDialog.showAlertDialog(getContext(), getString(R.string.no_internet));
        }
    }

    private void showRefreshing() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);
    }

    private void hideRefreshing() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(false);
    }


    private void prepopulateData(LockNotificationList notificationList,boolean isClear) {
        try {
            if (notificationList != null && notificationList.getLockNotifications() != null && notificationList.getLockNotifications().size() > 0) {
                if(isClear){
                    mHistoryAdapter.clearList();
                }
                mHistoryAdapter.setItemList(notificationList.getLockNotifications());
                tvNoHistory.setVisibility(View.GONE);
            } else {
                lastPage=true;
                if(mHistoryAdapter.getItemCount()==0) {
                    mHistoryAdapter.setItemList(null);
                    tvNoHistory.setText(getString(R.string.no_history));
                    tvNoHistory.setVisibility(View.VISIBLE);
                }

            }
        } catch (Exception e) {
            Logger.e(e);
        }
    }

}
