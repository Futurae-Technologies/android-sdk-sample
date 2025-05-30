package com.futurae.sampleapp.arch

import com.futurae.sampleapp.ui.shared.elements.alertdialog.FuturaeAlertDialogUIState

enum class NotificationType {
    INFO,
    QR_SCAN,
    AUTH,
    UNENROLL,
    GENERIC,
}

data class NotificationUI(
    val type : NotificationType,
    val dialogState: FuturaeAlertDialogUIState
)