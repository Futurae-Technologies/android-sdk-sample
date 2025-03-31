package com.futurae.demoapp.ui.shared.elements.alertdialog

import com.futurae.demoapp.ui.TextWrapper

data class FuturaeAlertDialogUIState(
    val title: TextWrapper,
    val text: TextWrapper,
    val confirmButtonCta: TextWrapper,
    val dismissButtonCta: TextWrapper? = null
)