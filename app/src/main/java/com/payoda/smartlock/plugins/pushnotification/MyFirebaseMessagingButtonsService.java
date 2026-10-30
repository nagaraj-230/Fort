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

            // Extract OTP if present in data payload
            String otp = remoteMessage.getData().get("otp");
            if (otp == null || otp.isEmpty()) {
                otp = remoteMessage.getData().get("OTP");
            }
            if (otp != null && !otp.isEmpty()) {
                if (title == null || title.isEmpty()) {
                    title = "OTP Notification";
                }
                if (body == null || body.isEmpty()) {
                    body = "Your OTP is: " + otp;
                }
                if (command == null || command.isEmpty()) {
                    command = "OTP_NOTIFICATION";
                }
                if (status == null || status.isEmpty()) {
                    status = Constant.SUCCESS;
                }
            }

            // Fallback to notification payload if present
            if (remoteMessage.getNotification() != null) {
                if (title == null || title.isEmpty()) {
                    title = remoteMessage.getNotification().getTitle();
                }
                if (body == null || body.isEmpty()) {
                    body = remoteMessage.getNotification().getBody();
                }
            }

            if (title == null || title.isEmpty()) {
                title = getString(R.string.app_name);
            }
            if (body == null) {
                body = "";
            }
            if (command == null) {
                command = "";
            }
            if (status == null) {
                status = "";
            }

            RemoteDataEvent remoteDataEvent = new RemoteDataEvent(status, title, body, command);
            sendNotification(getApplicationContext(), title, body, command, status);

            EventBus.getDefault().post(remoteDataEvent);

        } else if (remoteMessage.getNotification() != null) {

            Log.d(TAG, "### Message Notification Body: " + remoteMessage.getNotification().getBody());
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            if (title == null || title.isEmpty()) {
                title = getString(R.string.app_name);
            }
            if (body == null) {
                body = "";
            }
            sendNotification(getApplicationContext(), title, body, "", "");

            String command = body.toLowerCase().contains("otp") ? "OTP_NOTIFICATION" : "";
            RemoteDataEvent remoteDataEvent = new RemoteDataEvent(Constant.SUCCESS, title, body, command);
            EventBus.getDefault().post(remoteDataEvent);

        }

    }

    //This method is only generating push notification
    //It is same as we did in earlier posts
    private void sendNotification(Context context, String title, String messageBody,
                                  String command, String status) {

        Intent intent = new Intent(this, SplashActivity.class);

        // for new version 6.0
        if (Constant.REMOTE_ACCESS_COMMAND.equalsIgnoreCase(command)) {
            RemoteDataEvent remoteDataEvent = new RemoteDataEvent(status, title, messageBody, command);
            intent.putExtra(Constant.REMOTE_ACCESS_DATA, new Gson().toJson(remoteDataEvent));
        } else {
            intent.putExtra(Constant.REMOTE_ACCESS_DATA, "");
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent;

        long notificationId = System.currentTimeMillis();
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
                .setSmallIcon(R.drawable.fort_notification_logo)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setSound(defaultSoundUri)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        // for new version 6.0 under certain condition
        if (Constant.REMOTE_ACCESS_COMMAND.equalsIgnoreCase(command)) {

            int drw_accept = R.drawable.accept_button_selector;
            int drw_reject = R.drawable.reject_button_selector;

            notificationBuilder.addAction(drw_accept, "ACCEPT", getPendingIntentAccept(notificationId, status));
            notificationBuilder.addAction(drw_reject, "REJECT", getPendingIntentReject(notificationId, status));

        }

        notificationManager.notify((int) (notificationId % Integer.MAX_VALUE), notificationBuilder.build());

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


