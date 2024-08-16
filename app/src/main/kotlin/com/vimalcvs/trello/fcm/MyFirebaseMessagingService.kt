package com.vimalcvs.trello.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.vimalcvs.trello.R
import com.vimalcvs.trello.activities.MainActivity
import com.vimalcvs.trello.activities.SignInActivity
import com.vimalcvs.trello.firebase.FireStoreClass
import com.vimalcvs.trello.utils.Constants

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        message.data.isNotEmpty().let {
            val title = message.data[Constants.FCM_KEY_TITLE]!!
            val description = message.data[Constants.FCM_KEY_MESSAGE]!!
            sendNotification(title, description)
        }
        message.notification?.let {

        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        sendRegistrationToServer()


    }

    private fun sendRegistrationToServer() {

    }

    private fun sendNotification(title: String, description: String) {
        val intent = if (FireStoreClass().getCurrentUserId().isNotEmpty()) Intent(
            this,
            MainActivity::class.java
        )
        else Intent(this, SignInActivity::class.java)

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val channelId = this.resources.getString(R.string.default_notification_channel_id)
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(
            this,
            channelId
        ).setSmallIcon(R.drawable.ic_group).setContentTitle(title)
            .setContentText(description)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        val notificationManager = getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel Trello Title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0, notificationBuilder.build())
    }
}