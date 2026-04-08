package com.payoda.smartlock.plugins.pushnotification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import android.os.Build;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.payoda.smartlock.R;
import com.payoda.smartlock.constants.Constant;
import com.payoda.smartlock.splash.SplashActivity;
import com.payoda.smartlock.plugins.storage.SecuredStorageManager;
import com.payoda.smartlock.utils.Logger;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    @Override
    public void onNewToken(@NonNull String refreshedToken) {
        super.onNewToken(refreshedToken);
        Logger.d(" ####Refreshed token: " + refreshedToken);

        SecuredStorageManager.getInstance().setDeviceToken(refreshedToken);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "### From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "### Message data payload: " + remoteMessage.getData());
            String status = remoteMessage.getData().get(Constant.STATUS);
            String title = remoteMessage.getData().get(Constant.TITLE);
            String body = remoteMessage.getData().get(Constant.BODY);
            String command = remoteMessage.getData().get(Constant.COMMAND);
            RemoteDataEvent remoteDataEvent = new RemoteDataEvent(status,title,body, command);

            sendNotification(getApplicationContext(), title, body);
            EventBus.getDefault().post(remoteDataEvent);
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "### Message Notification Body: " + remoteMessage.getNotification().getBody());
            sendNotification(getApplicationContext(), remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        }

    }

    //This method is only generating push notification
    //It is same as we did in earlier posts
    private void sendNotification(Context context, String title, String messageBody) {

        Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity
                    (this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        }
        else {
            pendingIntent = PendingIntent.getActivity
                    (this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
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

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                this,getString(R.string.notification_channel_id))
                .setSmallIcon(R.mipmap.ic_launcher_nova)
              //  .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher)) // This icon will appear in the expanded notification view.
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);
        notificationManager.notify((int)Calendar.getInstance().getTimeInMillis(), notificationBuilder.build());

    }


}
