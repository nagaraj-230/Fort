package com.payoda.smartlock.notification;


import android.content.DialogInterface;
import android.os.Bundle;
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

import com.payoda.smartlock.App;
import com.payoda.smartlock.R;
import com.payoda.smartlock.notification.adapter.NotificationsAdapter;
import com.payoda.smartlock.notification.model.NotificationList;
import com.payoda.smartlock.notification.service.NotificationService;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.utils.AppDialog;
import com.payoda.smartlock.utils.Loader;
import com.payoda.smartlock.utils.Logger;
import com.payoda.smartlock.utils.PaginationScrollListener;

import static com.payoda.smartlock.constants.Constant.LIMIT;

/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = "### NotificationFragment";

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView tvNoNotification;
    private NotificationsAdapter mNotificationsAdapter;
    private RecyclerView mRecyclerView;
    private boolean loading = false;
    private boolean initialLoading = true, lastPage = false;
    private ProgressBar load_bottom_more_progress;
    private int offset = 0;

    public NotificationFragment() {
        // Required empty public constructor
    }

    public static NotificationFragment getInstance() {
        return new NotificationFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Logger.d(TAG,TAG
        );
        return initializeView(inflater.inflate(R.layout.fragment_notification, container, false));
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private View initializeView(View view) {
        tvNoNotification = view.findViewById(R.id.tv_no_notification);
        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        load_bottom_more_progress=view.findViewById(R.id.load_bottom_more_progress);
        mSwipeRefreshLayout.setOnRefreshListener(this);


        mRecyclerView = view.findViewById(R.id.notificationsDetailView);

        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        // Set Layout Manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(linearLayoutManager);
        // Limiting the size
        mRecyclerView.setHasFixedSize(true);
        mNotificationsAdapter = new NotificationsAdapter(null);
        mRecyclerView.setAdapter(mNotificationsAdapter);
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
        return view;
    }

    private void doRequestList(boolean isPullDown) {
        if (ServiceManager.getInstance().isNetworkAvailable(getContext())) {
            if(isPullDown) {
                showRefreshing();
            }else {
                load_bottom_more_progress.setVisibility(View.VISIBLE);
            }
            mRecyclerView.setVisibility(View.VISIBLE);
            NotificationService.getInstance().getResponse(String.valueOf(LIMIT),String.valueOf(offset),new ResponseHandler() {
                @Override
                public void onSuccess(Object data) {
                    try {
                        loading=false;
                        load_bottom_more_progress.setVisibility(View.GONE);
                        hideRefreshing();
                        NotificationList notificationList = (NotificationList) data;
                        prepopulateData(notificationList,isPullDown);
                    } catch (Exception e) {
                        e.printStackTrace();
                        AppDialog.showAlertDialog(getContext(), getString(R.string.oops_error));
                    }
                }

                @Override
                public void onAuthError(String message) {
                    hideRefreshing();
                    loading=false;
                    load_bottom_more_progress.setVisibility(View.GONE);
                    AppDialog.showAlertDialog(getContext(), message, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            App.getInstance().showLogin(getActivity());
                            dialog.dismiss();
                        }
                    });
                }

                @Override
                public void onError(String message) {
                    hideRefreshing();
                    loading=false;
                    load_bottom_more_progress.setVisibility(View.GONE);
                    Loader.getInstance().hideLoader();
                    prepopulateData(null,true);
                }
            });
        } else {
            hideRefreshing();
            mRecyclerView.setVisibility(View.GONE);
            tvNoNotification.setText(getString(R.string.no_internet));
            tvNoNotification.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRefresh() {

        if (ServiceManager.getInstance().isNetworkAvailable(NotificationFragment.this.getContext())) {
            lastPage = false;
            offset = 0;
            doRequestList(true);
        }else{
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

    private void prepopulateData(NotificationList notificationList,boolean isClear) {
        try {
            if (notificationList != null && notificationList.getNotifications() != null && notificationList.getNotifications().size() > 0) {
                if(isClear){
                    mNotificationsAdapter.clearList();
                }
                mNotificationsAdapter.setItemList(notificationList.getNotifications());
                tvNoNotification.setVisibility(View.GONE);
                if (notificationList.getNotifications().size() < 10) {
                    lastPage = true;
                }
            } else {
                lastPage=true;
                if(mNotificationsAdapter.getItemCount()==0) {
                    mNotificationsAdapter.setItemList(null);
                    tvNoNotification.setText(getString(R.string.no_notification));
                    tvNoNotification.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            Logger.e(e);
        }
    }
}
