package com.payoda.smartlock.plugins.pushnotification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.payoda.smartlock.R;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.plugins.storage.SecuredStorageManager;
import com.payoda.smartlock.splash.SplashActivity;

import org.greenrobot.eventbus.EventBus;

public class MyFirebaseMessagingButtonsService extends FirebaseMessagingService {

    // in manifest

    private static final String TAG = MyFirebaseMessagingButtonsService.class.getSimpleName();

    public static final String ACTION_ACCEPT = "accept";
    public static final String ACTION_REJECT = "reject";

    @Override
    public void onNewToken(@NonNull String refreshedToken) {
        super.onNewToken(refreshedToken);
        SecuredStorageManager.getInstance().setDeviceToken(refreshedToken);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {

            Log.d(TAG, "### Message data payload: " + remoteMessage.getData());
            Log.d(TAG, "### Message data payload: " + remoteMessage);

            String status = remoteMessage.getData().get(Constant.STATUS);
            String title = remoteMessage.getData().get(Constant.TITLE);
            String body = remoteMessage.getData().get(Constant.BODY);
            String command = remoteMessage.getData().get(Constant.COMMAND);

            RemoteDataEvent remoteDataEvent = new RemoteDataEvent(status, title, body, command);
            sendNotification(getApplicationContext(), title, body, command, status);

            EventBus.getDefault().post(remoteDataEvent);

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {

            Log.d(TAG, "### Message Notification Body: " + remoteMessage.getNotification().getBody());
            sendNotification(getApplicationContext(), remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody(), "", "");

        }

    }

    //This method is only generating push notification
    //It is same as we did in earlier posts
    private void sendNotification(Context context, String title, String messageBody,
                                  String command, String status) {

        Intent intent = new Intent(this, SplashActivity.class);

        // for new version 6.0
        if (command.equalsIgnoreCase(Constant.REMOTE_ACCESS_COMMAND)) {
            RemoteDataEvent remoteDataEvent = new RemoteDataEvent(status, title, messageBody, command);
            intent.putExtra(Constant.REMOTE_ACCESS_DATA, new Gson().toJson(remoteDataEvent));
        } else {
            intent.putExtra(Constant.REMOTE_ACCESS_DATA, "");
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent;

        long notificationId = 1;
        int uptimeMillis = (int) SystemClock.uptimeMillis();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity
                    (this, uptimeMillis, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            pendingIntent = PendingIntent.getActivity
                    (this, uptimeMillis, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        }

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("Notification",
                    "Smart Lock",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription(messageBody);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "Notification")
                .setSmallIcon(R.mipmap.ic_launcher_nova)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setSound(defaultSoundUri)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        // for new version 6.0 under certain condition
        if (command.equalsIgnoreCase(Constant.REMOTE_ACCESS_COMMAND)) {

            int drw_accept = R.drawable.accept_button_selector;
            ;
            int drw_reject = R.drawable.reject_button_selector;

            // Dismiss after given seconds
            //notificationBuilder.setTimeoutAfter(Constant.DIALOG_DISMISS_SECS);
            // notificationBuilder.setOngoing(true); //Keep the notification ongoing

            notificationBuilder.addAction(drw_accept, "ACCEPT", getPendingIntentAccept(notificationId, status));
            notificationBuilder.addAction(drw_reject, "REJECT", getPendingIntentReject(notificationId, status));

        }

        notificationManager.notify((int) notificationId, notificationBuilder.build());

    }

    protected PendingIntent getPendingIntentAccept(long notificationId, String status) {

        PendingIntent pendingIntentAccept;
        Intent intentAccept = new Intent(getApplicationContext(), NotificationButtonBroadcastReceiver.class);
        intentAccept.setAction(ACTION_ACCEPT);
        intentAccept.putExtra("notificationId", String.valueOf(notificationId));
        intentAccept.putExtra("lockDetails", status);
        pendingIntentAccept = PendingIntent.getBroadcast(getApplicationContext(), 0, intentAccept, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        return pendingIntentAccept;

    }

    protected PendingIntent getPendingIntentReject(long notificationId, String status) {

        PendingIntent pendingIntentReject;

        Intent intentReject = new Intent(getApplicationContext(), NotificationButtonBroadcastReceiver.class);
        intentReject.setAction(ACTION_REJECT);
        intentReject.putExtra("notificationId", String.valueOf(notificationId));
        intentReject.putExtra("lockDetails", status);
        pendingIntentReject = PendingIntent.getBroadcast(getApplicationContext(), 0, intentReject, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        return pendingIntentReject;
    }

}


