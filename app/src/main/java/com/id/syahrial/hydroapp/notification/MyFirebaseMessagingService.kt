package com.id.syahrial.hydroapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.id.syahrial.hydroapp.R
import com.id.syahrial.hydroapp.home.MainActivity

const val channelID = "notification_channel"
const val channelName = "com.id.syahrial.hydroapp"

class MyFirebaseMessagingService : FirebaseMessagingService() {

    //generate a notification
    // attach  the notification created with custom layout
    //show notification
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(remotemessage: RemoteMessage) {
        if (remotemessage.notification != null) {
            generateNotification(
                remotemessage.notification!!.title!!,
                remotemessage.notification!!.body!!
            )
        }
    }

    fun getRemoteView(title: String, message: String): RemoteViews {
        val remoteView = RemoteViews("com.id.syahrial.hydroapp", R.layout.notification)
        remoteView.setTextViewText(R.id.title, title)
        remoteView.setTextViewText(R.id.message, message)
        remoteView.setImageViewResource(R.id.appLogo, R.drawable.ic_logo_app)
        return remoteView
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateNotification(title: String, message: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        var builder: NotificationCompat.Builder = NotificationCompat.Builder(
            applicationContext,
            channelID
        ).setSmallIcon(R.drawable.ic_logo_app)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000)).setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
        builder = builder.setContent(getRemoteView(title, message))
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR_0_1) {
            val notificationChannel =
                NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        notificationManager.notify(0, builder.build())
    }
}