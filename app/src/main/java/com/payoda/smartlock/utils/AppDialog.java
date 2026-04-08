package com.payoda.smartlock.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;

import com.payoda.smartlock.R;
import com.payoda.smartlock.plugins.storage.SecuredStorageManager;


/**
 * This class contains frequently used methods for our App Dialog.
 * <p>
 * Created by david
 */
public class AppDialog {

    public static void showAlertDialog(Context context, String message) {
        if (context != null)
            showAlertDialog(context, context.getResources().getString(R.string.app_name), message, null);
    }

    public static void showAlertDialog(Context context, String message, final DialogInterface.OnClickListener onClickListener) {
        if (context != null)
            showAlertDialog(context, context.getResources().getString(R.string.app_name), message, "OK", onClickListener, null, null);
    }

    public static void showAlertDialog(Context context, String title, String message) {
        showAlertDialog(context, title, message, null);
    }

    public static void showAlertDialog(Context context, String title, String message, final DialogInterface.OnClickListener onClickListener) {
        if (context != null)
            showAlertDialog(context, title, message, "OK", onClickListener, null, null);
    }

    public static void showAlertDialog(Context context, String title, String message, String positive,
                                       final DialogInterface.OnClickListener onClickListener) {
        if (context != null)
            showAlertDialog(context, title, message, positive, onClickListener, null, null);
    }

    public static void showAlertDialog(Context context, String title, String message, String positive,
                                       final DialogInterface.OnClickListener onPositiveClickListener,
                                       String negative, final DialogInterface.OnClickListener onNegativeClickListener) {
        if (context != null) {
            try {
                Loader.getInstance().hideLoader();
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        // write your code here
                        if(!((Activity) context).isFinishing()){
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle(title);
                            builder.setMessage(message);
                            builder.setPositiveButton(positive, onPositiveClickListener);
                            if (negative != null)
                                builder.setNegativeButton(negative, onNegativeClickListener);
                            builder.setCancelable(false);
                            builder.show();
                        }

                    }


                });

            } catch (Exception e) {
                Logger.e(e);
            }
        }
    }

    public static AlertDialog alertDialog;

/*    public static AlertDialog showAlertDialog(Context context, String title, String message, String positive,
                                              final DialogInterface.OnClickListener onPositiveClickListener,
                                              String negative, final DialogInterface.OnClickListener onNegativeClickListener,
                                              String neutral, final DialogInterface.OnClickListener onNeutralClickListener) {

        if (context != null) {

            try {

                Logger.d("$$$$$ Alert Dialog Show");
                Loader.getInstance().hideLoader();

                new Handler(Looper.getMainLooper()).post(() -> {

                    alertDialog = new AlertDialog.Builder(context)
                            .setTitle(title)
                            .setMessage(message)
                            .setPositiveButton(positive, onPositiveClickListener)
                            .setNegativeButton(negative, onNegativeClickListener)
                            .setNeutralButton(neutral, onNeutralClickListener)
                            .setCancelable(false)
                            .create();

                    alertDialog.show();

                });

            } catch (Exception e) {
                Logger.e(e);
            }

        }

        return null;
    }*/


    public static AlertDialog showAlertDialog(Context context, String title, String message, String positive,
                                              final DialogInterface.OnClickListener onPositiveClickListener,
                                              String negative, final DialogInterface.OnClickListener onNegativeClickListener,
                                              String neutral, final DialogInterface.OnClickListener onNeutralClickListener) {
        if (context != null) {
            try {
                Logger.d("$$$$$ Alert Dialog Show");
                Loader.getInstance().hideLoader();

                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        AlertDialog alertDialog = new AlertDialog.Builder(context)
                                .setTitle(title)
                                .setMessage(message)
                                .setPositiveButton(positive, onPositiveClickListener)
                                .setNegativeButton(negative, onNegativeClickListener)
                                .setNeutralButton(neutral, onNeutralClickListener)
                                .setCancelable(false)
                                .create();

                        alertDialog.setOnShowListener(dialog -> Logger.d("$$$$$ Alert Dialog shown successfully"));

                        alertDialog.setOnDismissListener(dialog -> {
                            Logger.d("### Alert dialog dismissed, remote data cleared");
                            SecuredStorageManager.getInstance().setRemoteInfo(null);
                        });

                        Logger.d("$$$$$ Attempting to show Alert Dialog");
                        alertDialog.show();
                    } catch (Exception e) {
                        Logger.d("$$$$$ Error showing dialog: "+e);
                    }
                });
            } catch (Exception e) {
                Logger.d("$$$$$ Error in Handler post: " +e);
            }
        } else {
            Logger.d("$$$$$ Context is null");
        }
        return null;
    }

    public static void showAlertDialog(final Activity activity, final String message) {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showAlertDialog(activity, activity.getResources().getString(R.string.app_name), message, null);
                }
            });
        }
    }

}

