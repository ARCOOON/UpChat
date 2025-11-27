package com.devusercode.upchat

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MessagingService : FirebaseMessagingService() {
    @Suppress("ktlint:standard:property-naming")
    private val TAG = "FCM"

    @Suppress("ktlint:standard:property-naming")
    private val CHANNEL_ID = "personal_notifications"

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(TAG, "Message Payload: ${message.data}")
        Log.d(TAG, "Message Notification: ${message.notification}")
        Log.d(TAG, "Message From: ${message.from}")

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val notificationTitle: String? = message.notification?.title
        val notificationMessage: String? = message.notification?.body
        val clickAction: String? = message.notification?.clickAction
        val uid: String? = message.data["uid"]

        Log.d(TAG, "Message Uid: $uid")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(
                    CHANNEL_ID,
                    "My Notifications",
                    NotificationManager.IMPORTANCE_HIGH,
                )

            val mBuilder: NotificationCompat.Builder =
                NotificationCompat
                    .Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.app_icon)
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationMessage)

            notificationManager.createNotificationChannel(notificationChannel)

            val intent = Intent(clickAction)
            intent.putExtra("uid", uid)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            val pendingIntent =
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            mBuilder.setContentIntent(pendingIntent)

            val notificationId = System.currentTimeMillis().toInt()
            notificationManager.notify(notificationId, mBuilder.build())
        } else {
            Log.e(TAG, "Android version not compatible with FCM Push-Notification")
        }
    }
}
