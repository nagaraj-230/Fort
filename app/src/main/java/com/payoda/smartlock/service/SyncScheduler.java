package com.payoda.smartlock.service;

import android.content.Context;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Data;
import androidx.work.BackoffPolicy;
import androidx.work.WorkManager;
import com.payoda.smartlock.constants.Constant;

import java.time.Clock;
import java.util.concurrent.TimeUnit;

public class SyncScheduler {

    private static SyncScheduler instance;

    public static SyncScheduler getInstance() {
        if (instance == null)
            instance = new SyncScheduler();
        return instance;
    }

    private SyncScheduler() {
    }

    public void schedule(Context context, Constant.JOBS jobs) {

        Data input = new Data.Builder()
                .putString("job-name", jobs.name())
                .build();

        Constraints constraints = new Constraints.Builder()
                // The Worker needs Network connectivity
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest workRequest =
                // Tell which work to execute
                new OneTimeWorkRequest.Builder(SyncWorkManager.class)
                        // Sets the input data for the ListenableWorker
                        .setInputData(input)
                        // If you want to delay the start of work by 0 seconds
                        .setInitialDelay(0, TimeUnit.SECONDS)
                        // Set a backoff criteria to be used when retry-ing
                        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30000, TimeUnit.MILLISECONDS)
                        // Set additional constraints
                        .setConstraints(constraints)
                        .build();


        String currentTimeMilliSecond = String.valueOf(System.currentTimeMillis());

        WorkManager.getInstance(context)
                // Use ExistingWorkPolicy.REPLACE to cancel and delete any existing pending
                // (uncompleted) work with the same unique name. Then, insert the newly-specified
                // work.
                .enqueueUniqueWork(jobs.name() + currentTimeMilliSecond, ExistingWorkPolicy.REPLACE, workRequest);


    }

}
