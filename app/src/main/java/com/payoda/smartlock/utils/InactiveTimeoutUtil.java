package com.payoda.smartlock.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.os.AsyncTask;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

/**
 * Created by nivetha.m on 10/24/2018.
 */

public class InactiveTimeoutUtil {

    public interface TimeOutListener {
        void onInactiveTimeOut();
    }

    private static Timer timer;

    public static final int SESSION_TIMEOUT = 6 * 10; // IN Seconds
    //TODO change to 1 minute in live

     static int TIMER_MILLISECONDS = SESSION_TIMEOUT * 1000; //1 minutes

    //TODO delete testing
    //static int TIMER_MILLISECONDS = SESSION_TIMEOUT * 20000; //20 minutes for developing purpose

    private static boolean isForeground = false;

    public static synchronized void startTimer(Context context, TimeOutListener timeOutListener) {

        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timer == null) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    cancel();
                    timer = null;
                    try {
                        new ForegroundTask(context).run();
                        if (isForeground) {
                            //Invoke time out action
                            if (timeOutListener != null) {
                                timeOutListener.onInactiveTimeOut();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }, TIMER_MILLISECONDS);
        }
    }

    public static synchronized void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    static class ForegroundTask implements Runnable {
        private Context context;

        public ForegroundTask(Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            if (context != null) {
                isForeground = isAppOnForeground(context);
            }
        }

        private boolean isAppOnForeground(Context context) {

            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfos = activityManager.getRunningAppProcesses();
            if (runningAppProcessInfos == null) {
                return false;
            }
            String packageName = context.getPackageName();
            for (ActivityManager.RunningAppProcessInfo appProcessInfo : runningAppProcessInfos) {
                if (appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                        && appProcessInfo.processName.equalsIgnoreCase(packageName)) {
                    return true;
                }
            }

            return false;
        }
    }


}
