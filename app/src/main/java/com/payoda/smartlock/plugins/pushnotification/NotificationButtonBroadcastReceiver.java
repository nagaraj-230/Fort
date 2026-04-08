package com.payoda.smartlock.plugins.pushnotification;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.payoda.smartlock.FullscreenActivity;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.model.RemoteAccessModel;
import com.payoda.smartlock.plugins.network.ResponseHandler;
import com.payoda.smartlock.plugins.network.ResponseModel;
import com.payoda.smartlock.plugins.storage.SecuredStorageManager;
import com.payoda.smartlock.request.model.Request;
import com.payoda.smartlock.request.model.RequestAccept;
import com.payoda.smartlock.request.service.RequestService;
import com.payoda.smartlock.utils.AppDialog;
import com.payoda.smartlock.utils.Loader;
import com.payoda.smartlock.utils.Logger;

public class NotificationButtonBroadcastReceiver extends BroadcastReceiver {

    // extends BroadcastReceiver
    private static final String TAG = "### NotificationButtonBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null) {

            switch (intent.getAction()) {

                case MyFirebaseMessagingButtonsService.ACTION_ACCEPT -> {

                    String notificationId = intent.getStringExtra("notificationId");
                    String lock = intent.getStringExtra("lockDetails");
                    notificationId = notificationId == null ? "0.0" : notificationId;

                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel((int) Long.parseLong(notificationId));

                    if (AppDialog.alertDialog != null && AppDialog.alertDialog.isShowing()) {
                        AppDialog.alertDialog.dismiss();
                        Logger.d("### Alert showing dismissed");
                    }

                    if (lock != null){

                        String[] parts = lock.split(",");

                        if (parts.length == 2) {
                            String requestId = parts[0];
                            String lockSerialNo = parts[1];
                            doAcceptOrRejectRequest(Constant.ACCEPT, lockSerialNo, requestId,context);
                        }

                    }

                }

                case MyFirebaseMessagingButtonsService.ACTION_REJECT -> {

                    String notificationId = intent.getStringExtra("notificationId");
                    String lock = intent.getStringExtra("lockDetails");
                    notificationId = notificationId == null ? "0.0" : notificationId;

                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel((int) Long.parseLong(notificationId));

                    if (AppDialog.alertDialog != null && AppDialog.alertDialog.isShowing()) {
                        AppDialog.alertDialog.dismiss();
                        Logger.d("### Alert showing dismissed");
                    }

                    if (lock != null){

                        String[] parts = lock.split(",");

                        if (parts.length == 2) {
                            String requestId = parts[0];
                            String lockSerialNo = parts[1];
                            doAcceptOrRejectRequest(Constant.REJECT, lockSerialNo, requestId,context);
                        }

                    }

                    //do api call, if rejects

                }

            }
        }
    }

    // Remote Access for V6.0
    private void doAcceptOrRejectRequest(String status, String lockSerialNo, String requestId,Context context) {

        try {

            ResponseHandler handler = new ResponseHandler() {
                @Override
                public void onSuccess(Object data) {
                    if (data != null) {
                        ResponseModel responseModel = (ResponseModel) data;

                        if (responseModel.getStatus().equalsIgnoreCase("success")) {

                            Logger.d(TAG, "success");
                            Toast.makeText(context,responseModel.getMessage(),Toast.LENGTH_SHORT).show();

                        } else {
                            Logger.d(TAG, "Failed");

                        }
                    }
                }

                @Override
                public void onAuthError(String message) {
                    Logger.d(TAG, "onAuthError");

                }

                @Override
                public void onError(String message) {
                    Logger.d(TAG, "onError");

                }

            };

            RemoteAccessModel mData = new RemoteAccessModel();

            if (status.equalsIgnoreCase(Constant.ACCEPT)) { // accept
                mData.setLockStatus(Constant.ACCEPT);
            } else { // reject
                mData.setLockStatus(Constant.REJECT);
            }

            mData.setLockSerialNo(lockSerialNo);
            mData.setRequestId(requestId);
            RequestService.getInstance().remoteAccess(mData, handler);

        } catch (Exception e) {
            Logger.e(e);
        }

    }

}
