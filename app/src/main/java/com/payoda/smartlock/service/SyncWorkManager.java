package com.payoda.smartlock.service;

import android.content.Context;
import android.text.TextUtils;


import androidx.annotation.NonNull;


import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;


import com.google.common.util.concurrent.ListenableFuture;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.plugins.network.SyncAll;
import com.payoda.smartlock.utils.Logger;


public class SyncWorkManager extends ListenableWorker {

    private Context context;

    public SyncWorkManager(@NonNull Context appContext, @NonNull WorkerParameters params) {
        super(appContext, params);
        context = appContext;
    }

    @Override
    public ListenableFuture<Result> startWork() {

        Logger.d("### SyncWorkManager startWork()");

        return CallbackToFutureAdapter.getFuture(completer -> {

            Data job = getInputData();

            if (job != null) {

                String jobType = job.getString("job-name");
                Logger.d("### SyncWorkManager jobType"+jobType);

                if (!TextUtils.isEmpty(jobType)) {
                    if (jobType.equalsIgnoreCase(Constant.JOBS.ADD_LOCK.name())) {

                        SyncAll.getInstance().pushLocks(context);
                    }
                    else if (jobType.equalsIgnoreCase(Constant.JOBS.UPDATE_KEYS.name())) {

                        SyncAll.getInstance().pushKeys();
                    } else if (jobType.equalsIgnoreCase(Constant.JOBS.ACTIVITY_HISTORY.name())) {

                        SyncAll.getInstance().pushActivityLog();
                    }else if (jobType.equalsIgnoreCase(Constant.JOBS.WIFI_MQTT_CONFIG.name())) {

                        SyncAll.getInstance().pushWifiConfig();
                    } else {
                        SyncAll.getInstance().pushAll(getApplicationContext());
                    }
                }
            } else {
                SyncAll.getInstance().pushAll(getApplicationContext());
            }

            return completer.set(Result.success());
        });

    }

    @Override
    public void onStopped() {
        // Cleanup because you are being stopped.
        Logger.d("### SyncWorkManager onStopped");
    }

}