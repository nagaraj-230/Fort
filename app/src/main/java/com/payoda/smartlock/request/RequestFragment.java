package com.payoda.smartlock.request;

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
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
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.locks.model.LockKeys;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ResponseModel;
import com.payoda.smartlock.plugins.network.ServiceManager;
import com.payoda.smartlock.plugins.pushnotification.RemoteDataEvent;
import com.payoda.smartlock.request.adapter.RequestAdapter;
import com.payoda.smartlock.request.callback.IRequestCallback;
import com.payoda.smartlock.request.model.Request;
import com.payoda.smartlock.request.model.RequestAccept;
import com.payoda.smartlock.request.model.RequestList;
import com.payoda.smartlock.request.service.RequestService;
import com.payoda.smartlock.utils.AppDialog;
import com.payoda.smartlock.utils.Loader;
import com.payoda.smartlock.utils.Logger;
import com.payoda.smartlock.utils.PaginationScrollListener;

import static com.payoda.smartlock.constants.Constant.LIMIT;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by david on 6/15/2018.
 */

public class RequestFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, IRequestCallback {

    public static final String TAG = "### RequestFragment";

    private SwipeRefreshLayout swipeRefreshLayout;
    private RequestAdapter mRequestAdapter;
    private TextView tv_network_check;

    private boolean loading = false;
    private boolean initialLoading = true, lastPage = false;
    private ProgressBar load_bottom_more_progress;
    private int offset = 0;


    public RequestFragment() {
        // Required empty public constructor
    }

    public static RequestFragment getInstance() {
        return new RequestFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Logger.d(TAG, TAG);
        return initializeView(inflater.inflate(R.layout.fragment_requests, container, false));
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
        if(remoteDataEvent != null && remoteDataEvent.getStatus().equalsIgnoreCase(Constant.SUCCESS)
                && (remoteDataEvent.getCommand().equalsIgnoreCase(Constant.OWNER_TRANSFER))){
            Loader.getInstance().hideLoader();
            AppDialog.showAlertDialog(getActivity(), remoteDataEvent.getTitle(), remoteDataEvent.getBody(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    doServiceRequest(true);
                }
            });
        }
    }

    private View initializeView(View rootView) {
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        load_bottom_more_progress = rootView.findViewById(R.id.load_bottom_more_progress);
        RecyclerView mRecyclerView = rootView.findViewById(R.id.requestDetailView);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        // Set Layout Manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(RequestFragment.this.getContext());
        mRecyclerView.setLayoutManager(linearLayoutManager);
        // Limiting the size
        mRecyclerView.setHasFixedSize(true);

        mRequestAdapter = new RequestAdapter(getContext(), null, this);
        mRecyclerView.setAdapter(mRequestAdapter);

        tv_network_check = rootView.findViewById(R.id.tv_network_check);
        mRecyclerView.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                if (!initialLoading) {
                    if (ServiceManager.getInstance().isNetworkAvailable(getContext())) {
                        loading = true;
                        offset++;
                        doServiceRequest(false);
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
        doServiceRequest(true);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void doServiceRequest(boolean isPullDown) {
        if (ServiceManager.getInstance().isNetworkAvailable(RequestFragment.this.getContext())) {
            swipeRefreshLayout.setVisibility(View.VISIBLE);
            tv_network_check.setVisibility(View.GONE);
            doRequestList(isPullDown);
        } else {
            swipeRefreshLayout.setVisibility(View.GONE);
            tv_network_check.setVisibility(View.VISIBLE);
        }
    }

    private void doRequestList(boolean isPullDown) {
        if (isPullDown) {
            showRefreshing();
        } else {
            load_bottom_more_progress.setVisibility(View.VISIBLE);
        }
        RequestService.getInstance().getRequest(String.valueOf(LIMIT), String.valueOf(offset), new ResponseHandler() {
            @Override
            public void onSuccess(Object data) {
                loading = false;
                load_bottom_more_progress.setVisibility(View.GONE);
                try {
                    hideRefreshing();
                    if (data != null) {
                        prepopulateData((RequestList) data, isPullDown);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
                            App.getInstance().showLogin(getActivity());
                        }
                    });
                }
            }

            @Override
            public void onError(String message) {
                hideRefreshing();
                loading = false;
                load_bottom_more_progress.setVisibility(View.GONE);
                AppDialog.showAlertDialog(getContext(), message);
            }
        });
    }


    @Override
    public void onRefresh() {
        if (ServiceManager.getInstance().isNetworkAvailable(RequestFragment.this.getContext())) {
            lastPage = false;
            offset = 0;
            doServiceRequest(true);
        } else {
            AppDialog.showAlertDialog(getContext(), getString(R.string.no_internet));
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


    private void prepopulateData(RequestList request, boolean isClear) {
        try {
            if (request != null && request.getRequest() != null && request.getRequest().size() > 0) {
                if (isClear) {
                    mRequestAdapter.clearList();
                }
                mRequestAdapter.setItemList(request.getRequest());
                if (request.getRequest().size() < LIMIT) {
                    lastPage = true;
                }
            } else {
                lastPage = true;
                if (mRequestAdapter.getItemCount() == 0) {
                    swipeRefreshLayout.setVisibility(View.GONE);
                    tv_network_check.setVisibility(View.VISIBLE);
                    tv_network_check.setText(getString(R.string.no_request));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Request currentRequest=null;
    @Override
    public void onRequestAction(Request req, boolean isAccepted) {
        currentRequest=req;
        LockKeys keys = currentRequest.getKey();

        if (keys != null && keys.getUserType().equalsIgnoreCase(Constant.OWNER_ID) && isAccepted) {

            //Type Owner & Owner "Accept"
            String lockSerialNumber = "";
            boolean isIoT = false;
            if(currentRequest.lock.getLockVersion() != null && (currentRequest.lock.getLockVersion().equalsIgnoreCase(Constant.HW_VERSION_4_0))){
                isIoT = true;
            }
            if(currentRequest.lock.getSerialNumber() != null && (currentRequest.lock.getLockVersion().equalsIgnoreCase(Constant.HW_VERSION_4_0))){
                lockSerialNumber = currentRequest.lock.getSerialNumber();
            }

            boolean finalIsIoT = isIoT;
            String finalLockSerialNumber = lockSerialNumber;
            AppDialog.showAlertDialog(getContext(), getString(R.string.app_name), getString(isIoT ? R.string.accept_confirm_msg_v4 : R.string.accept_confirm_msg),
                    "YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        RequestAccept requestAccept = new RequestAccept();
                        requestAccept.setRequestId(currentRequest.getId());
                        Loader.getInstance().showLoader(getContext());
                        if(finalIsIoT){
                            RequestService.getInstance().acceptRequestViaMqtt(finalLockSerialNumber, currentRequest.getId(), new ResponseHandler() {
                                @Override
                                public void onSuccess(Object data) {
                                    Loader.getInstance().hideLoader();
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
                                    AppDialog.showAlertDialog(getContext(), message);
                                }
                            });
                        }else {
                            RequestService.getInstance().acceptRequest(currentRequest.getKeyId(), requestAccept, new ResponseHandler() {
                                @Override
                                public void onSuccess(Object data) {
                                    Loader.getInstance().hideLoader();
                                    if (data != null) {
                                        ResponseModel responseModel = (ResponseModel) data;
                                        if (responseModel.getStatus().equalsIgnoreCase("success")) {
                                            doRequestList(true);
                                        } else {
                                            AppDialog.showAlertDialog(getContext(), responseModel.getMessage());
                                        }

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
                                    AppDialog.showAlertDialog(getContext(), message);
                                }
                            });
                        }

                    } catch (Exception e) {
                        Logger.e(e);
                    }
                }
            }, "NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

        } else {
            //Type Master/User & Owner "Reject"
            if (isAccepted) {
                callUpdateRequest("1", currentRequest);
            } else {
                callUpdateRequest("2", currentRequest);
            }
        }

    }

    private void callUpdateRequest(String status, Request request) {
        try {
            ResponseModel mData = new ResponseModel();
            mData.setStatus(status);
            Loader.getInstance().showLoader(getContext());
            RequestService.getInstance().rejectRequest(request.getId(), mData, new ResponseHandler() {
                @Override
                public void onSuccess(Object data) {
                    Loader.getInstance().hideLoader();
                    if (data != null) {
                        ResponseModel responseModel = (ResponseModel) data;
                        if (responseModel.getStatus().equalsIgnoreCase("success")) {
                            doRequestList(true);
                        } else {
                            AppDialog.showAlertDialog(getContext(), responseModel.getMessage());
                        }
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
                    AppDialog.showAlertDialog(getContext(), message);
                }
            });

        } catch (Exception e) {
            Logger.e(e);
        }

    }


}
