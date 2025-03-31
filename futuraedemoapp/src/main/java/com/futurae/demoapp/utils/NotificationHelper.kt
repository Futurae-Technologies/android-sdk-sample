package com.futurae.demoapp.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.futurae.demoapp.MainActivity
import com.futurae.demoapp.R
import com.futurae.demoapp.arch.NotificationType
import com.futurae.demoapp.arch.NotificationUI

object NotificationHelper {

    const val EXTRA_AUTH = "EXTRA_AUTH"
    const val EXTRA_UNENROLL = "EXTRA_UNENROLL"
    const val EXTRA_QR = "EXTRA_QR"
    const val CHANNEL_ID = "futurae"
    const val CHANNEL_NAME = "Futurae"
    const val CHANNEL_DESC = "Futurae Push Notifications"

    fun showNotification(context: Context, notificationUI: NotificationUI) {
        val notificationId = 1


        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
            description = CHANNEL_DESC
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        when (notificationUI.type) {
            NotificationType.GENERIC,
            NotificationType.INFO -> {
                // no-op
            }
            NotificationType.QR_SCAN -> intent.putExtra(EXTRA_QR, true)
            NotificationType.AUTH -> intent.putExtra(EXTRA_AUTH, true)
            NotificationType.UNENROLL -> intent.putExtra(EXTRA_UNENROLL, true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )
        val notificationBuilder = Notification.Builder(context, CHANNEL_ID)
            .setContentTitle(notificationUI.dialogState.title.value(context))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentText(notificationUI.dialogState.text.value(context))
            .setPriority(Notification.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)

        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}