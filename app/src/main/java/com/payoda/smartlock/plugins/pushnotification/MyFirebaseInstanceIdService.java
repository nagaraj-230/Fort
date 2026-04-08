package com.payoda.smartlock.plugins.pushnotification;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.payoda.smartlock.plugins.storage.SecuredStorageManager;
import com.payoda.smartlock.plugins.storage.StorageManager;
import com.payoda.smartlock.utils.Logger;

public class MyFirebaseInstanceIdService extends FirebaseMessagingService {
    private static final String TAG = MyFirebaseInstanceIdService.class.getSimpleName();

    @Override
    public void onNewToken(@NonNull String refreshedToken) {
        super.onNewToken(refreshedToken);
        Logger.d(" ####Refreshed token: " + refreshedToken);

        SecuredStorageManager.getInstance().setDeviceToken(refreshedToken);
    }
}
