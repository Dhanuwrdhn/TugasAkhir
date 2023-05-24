package com.id.syahrial.hydroapp.notification

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.id.syahrial.hydroapp.R

const val notificationID = 1
const val titleExtra = "titleExtra"
const val messageExtra = "messageExtra"

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val notification: Notification? =
            context?.let {
                NotificationCompat.Builder(it, channelID)
                    .setSmallIcon(R.drawable.ic_logo_app)
                    .setContentTitle(intent?.getStringExtra(titleExtra))
                    .setContentText(intent?.getStringExtra(messageExtra))
                    .build()
            }
        val manager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationID,notification)
    }
}