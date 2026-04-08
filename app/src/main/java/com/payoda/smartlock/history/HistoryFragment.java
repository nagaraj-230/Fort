package com.payoda.smartlock.history;


import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.payoda.smartlock.R;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.history.adapter.HistoryAdapter;
import com.payoda.smartlock.history.model.HistoryList;
import com.payoda.smartlock.history.service.HistoryService;
import com.payoda.smartlock.locks.model.Lock;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.utils.AppDialog;
import com.payoda.smartlock.utils.EndlessScrollListener;
import com.payoda.smartlock.utils.Logger;
import com.payoda.smartlock.utils.PaginationScrollListener;

import static com.payoda.smartlock.constants.Constant.LIMIT;

/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView tvNoHistory;
    private HistoryAdapter mHistoryAdapter;
    private Lock mLock = new Lock();
    private LinearLayout header;
    private int offset = 0;
    private EndlessScrollListener endlessScrollListener;
    public static final String TAG = "### HistoryFragment";

    public HistoryFragment() {
        // Required empty public constructor
    }

    public static HistoryFragment getInstance() {
        return new HistoryFragment();
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
        return initializeView(inflater.inflate(R.layout.fragment_history, container, false));

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private boolean loading = false;
    private boolean initialLoading = true, lastPage = false;
    private ProgressBar load_bottom_more_progress;

    private View initializeView(View rootView) {
        header = rootView.findViewById(R.id.header);
        ((TextView) rootView.findViewById(R.id.tv_title)).setText(getString(R.string.history));
        rootView.findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });
        //header.setVisibility(View.GONE);

        tvNoHistory = rootView.findViewById(R.id.tv_no_history);
        mSwipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        load_bottom_more_progress = rootView.findViewById(R.id.load_bottom_more_progress);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        RecyclerView mRecyclerView = rootView.findViewById(R.id.historyDetailView);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        // Set Layout Manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(linearLayoutManager);
        // Limiting the size
        mRecyclerView.setHasFixedSize(true);
        mHistoryAdapter = new HistoryAdapter(null);
        mRecyclerView.setAdapter(mHistoryAdapter);
        mRecyclerView.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                if (!initialLoading) {
                    if (ServiceManager.getInstance().isNetworkAvailable(getContext())) {
                        loading = true;
                        offset++;
                        doRequestList(false);
                    } else {
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
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                doRequestList(true);
            }
        }, 500);
        return rootView;
    }


    private void doRequestList(final boolean isPullDown) {

        if (ServiceManager.getInstance().isNetworkAvailable(getContext())) {
            if (mLock.getId() != null) {
                if (isPullDown) {
                    showRefreshing();
                } else {
                    load_bottom_more_progress.setVisibility(View.VISIBLE);
                }

                HistoryService.getInstance().serviceRequest(mLock.getId(), LIMIT, offset, new ResponseHandler() {
                    @Override
                    public void onSuccess(Object data) {
                        try {
                            hideRefreshing();
                            load_bottom_more_progress.setVisibility(View.GONE);
                            loading = false;
                            prepopulateData((HistoryList) data, isPullDown);
                        } catch (Exception e) {
                            Logger.e(e);
                            AppDialog.showAlertDialog(getContext(), getString(R.string.oops_error));
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

                                }
                            });
                        }
                    }

                    @Override
                    public void onError(String message) {
                        hideRefreshing();
                        loading = false;
                        load_bottom_more_progress.setVisibility(View.GONE);
                        prepopulateData(null, false);
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
            offset = 0;
            lastPage = false;
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

    private void prepopulateData(HistoryList history, boolean isClear) {
        try {
            if (history != null && history.getHistory() != null && history.getHistory().size() > 0) {
                if (isClear) {
                    mHistoryAdapter.clearList();
                }

                Logger.d("############### history.getHistory() : "+history.getHistory());
                mHistoryAdapter.setItemList(history.getHistory());
                tvNoHistory.setVisibility(View.GONE);

                if (history.getHistory().size() < LIMIT) {
                    lastPage = true;
                }
            } else {
                lastPage = true;
                if (mHistoryAdapter.getItemCount() == 0) {
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
