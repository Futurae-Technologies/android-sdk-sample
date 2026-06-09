package com.futurae.sampleapp

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.futurae.sampleapp.arch.NotificationType
import com.futurae.sampleapp.arch.NotificationUI
import com.futurae.sampleapp.ui.TextWrapper
import com.futurae.sampleapp.ui.shared.elements.alertdialog.FuturaeAlertDialogUIState
import com.futurae.sampleapp.utils.LocalStorage
import com.futurae.sampleapp.utils.NotificationHelper
import com.futurae.sdk.FuturaeSDK
import com.futurae.sdk.messaging.FTRNotificationEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

class FuturaeSampleApplication : Application() {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        LocalStorage.init(applicationContext)
        coroutineScope.launch {
            FuturaeSDK.notificationsFlow
                .onEach { result ->
                    result
                        .onSuccess { event ->
                            if (!isAppInForeground()) {
                                showSystemNotificationForEvent(event)
                            }
                        }
                        .onFailure {
                            Timber.e("Notification Flow failure: $it")
                        }
                }
                .collect()
        }
    }

    private fun showSystemNotificationForEvent(event: FTRNotificationEvent) {
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PERMISSION_GRANTED) return
        val notificationUI = event.toNotificationUI() ?: return
        NotificationHelper.showNotification(applicationContext, notificationUI)
    }

    private fun FTRNotificationEvent.toNotificationUI(): NotificationUI? = when (this) {
        is FTRNotificationEvent.Authentication -> NotificationUI(
            type = NotificationType.AUTH,
            dialogState = FuturaeAlertDialogUIState(
                title = TextWrapper.Resource(R.string.sdk_notification_auth_title),
                text = TextWrapper.Resource(R.string.sdk_notification_auth_body),
                confirmButtonCta = TextWrapper.Resource(R.string.ok),
            ),
            timeoutEpochMs = session.timeout * 1000L
        )
        is FTRNotificationEvent.AccountUnenrollment -> NotificationUI(
            type = NotificationType.UNENROLL,
            dialogState = FuturaeAlertDialogUIState(
                title = TextWrapper.Resource(R.string.sdk_notification_unenroll_title),
                text = TextWrapper.Resource(
                    R.string.sdk_notification_unenroll_body,
                    listOf(userId)
                ),
                confirmButtonCta = TextWrapper.Resource(R.string.ok),
            )
        )
        is FTRNotificationEvent.QRCodeScanRequest -> NotificationUI(
            type = NotificationType.QR_SCAN,
            dialogState = FuturaeAlertDialogUIState(
                title = TextWrapper.Resource(R.string.sdk_qr_scan_notification_title),
                text = TextWrapper.Resource(
                    R.string.sdk_qr_scan_notification_body,
                    listOf(userId)
                ),
                confirmButtonCta = TextWrapper.Resource(R.string.ok),
            )
        )
        is FTRNotificationEvent.CustomInAppMessaging -> null
    }

    private fun isAppInForeground(): Boolean {
        return ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
    }
}