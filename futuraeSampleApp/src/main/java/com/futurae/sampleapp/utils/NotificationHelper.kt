package com.futurae.sampleapp.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.futurae.sampleapp.MainActivity
import com.futurae.sampleapp.R
import com.futurae.sampleapp.arch.NotificationType
import com.futurae.sampleapp.arch.NotificationUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

object NotificationHelper {

    const val EXTRA_AUTH = "EXTRA_AUTH"
    const val EXTRA_UNENROLL = "EXTRA_UNENROLL"
    const val EXTRA_QR = "EXTRA_QR"
    private const val CHANNEL_ID = "futurae"
    private const val CHANNEL_NAME = "Futurae"
    private const val CHANNEL_DESC = "Futurae Push Notifications"
    private const val NOTIFICATION_ID = 1

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun showNotification(context: Context, notificationUI: NotificationUI) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = CHANNEL_DESC
        }
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
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = Notification.Builder(context, CHANNEL_ID)
            .setContentTitle(notificationUI.dialogState.title.value(context))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentText(notificationUI.dialogState.text.value(context))
            .setPriority(Notification.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)

        notificationUI.timeoutEpochMs?.let { timeoutEpochMs ->
            scope.launch {
                val delayMs = timeoutEpochMs - System.currentTimeMillis()
                if (delayMs > 0) delay(delayMs.milliseconds)
                cancelNotification(context.applicationContext, NOTIFICATION_ID)
            }
        }
    }

    fun cancelNotification(context: Context, notificationId: Int) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }
}