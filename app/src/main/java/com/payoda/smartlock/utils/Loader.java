package com.payoda.smartlock.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

public class Loader {

    private static Loader instance;
    private ProgressDialog progressDialog;

    private Loader() {
    }

    public static Loader getInstance() {
        if (instance == null) {
            instance = new Loader();
        }
        return instance;
    }

    public void showLoader(Context context) {

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.cancel();
        }
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Please wait...");
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void hideLoader(Activity activity) {
        try {
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideLoader();
                    }
                });
            }
        } catch (Exception e) {
            Logger.e(e);
        }
    }

    public boolean isShowing() {
        return progressDialog != null && progressDialog.isShowing();
    }

    public void hideLoader() {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.cancel();
            }
        } catch (Exception e) {
            Logger.e(e);
        }
    }

    public void dismissLoader() {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        } catch (Exception e) {
            Logger.e(e);
        }
    }
}
