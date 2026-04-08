package com.payoda.smartlock.utils;

import android.util.Log;

import com.payoda.smartlock.BuildConfig;

import java.util.Arrays;

public class Logger {

    private static String TAG = "SmartLock";

    private static boolean enabled = BuildConfig.DEBUG;

    private Logger() {

    }

    public static void d(String tag, String message) {
        if (enabled) {
            Log.d(TAG, tag + " " + message);
        }
    }

    public static void d(String message) {
        if (enabled) {
            Log.d(TAG, message);
        }
    }

    public static void i(String tag, String message) {
        if (enabled) {
            Log.d(TAG, tag + " " + message);
        }
    }

    public static void i(String message) {
        if (enabled) {
            if (message != null)
                Log.d(TAG, message);
        }
    }

    public static void e(Exception e) {
        if (enabled) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    public static void e(Throwable th) {
        if (enabled) {
            Log.e(TAG, th.getClass() + "->" + th.getMessage() + "->" + Arrays.toString(th.getStackTrace()));
        }
    }

    public static void setEnabled(boolean enabled) {
        Logger.enabled = enabled;
    }
}
