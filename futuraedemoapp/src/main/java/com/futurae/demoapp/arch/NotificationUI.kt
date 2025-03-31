package com.futurae.demoapp.arch

import com.futurae.demoapp.ui.shared.elements.alertdialog.FuturaeAlertDialogUIState

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