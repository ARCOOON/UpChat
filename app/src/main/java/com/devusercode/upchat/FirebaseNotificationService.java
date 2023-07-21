package com.devusercode.upchat;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.devusercode.upchat.utils.UserUtils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseNotificationService extends FirebaseMessagingService {
    private static final String TAG = "FirebaseNotificationService";

    @SuppressLint("MissingPermission")
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Handle the incoming FCM message
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "remote message: " + remoteMessage);

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            String id = "my_channel_01";
            CharSequence name = "myname";
            String description = "notifications";

            int importance = NotificationManager.IMPORTANCE_LOW;

            NotificationChannel mChannel = new NotificationChannel(id, name, importance);

            mChannel.setDescription(description);
            mChannel.enableLights(true);

            mNotificationManager.createNotificationChannel(mChannel);

            // Extract the notification message data
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();

            Log.d(TAG, "from: " + title);
            Log.d(TAG, "message: " + body);

            // Customize the notification content and appearance
            // You can use a notification builder to create and display the notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, id)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setOngoing(true)
                    .setSmallIcon(R.drawable.app_icon)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            // Display the notification
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(1, builder.build());
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "new token: " + token);
        UserUtils.update(Key.User.DEVICE_TOKEN, token);
        super.onNewToken(token);
    }
}