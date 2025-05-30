package com.futurae.sampleapp.ui.shared.elements.alertdialog

import com.futurae.sampleapp.ui.TextWrapper

data class FuturaeAlertDialogUIState(
    val title: TextWrapper,
    val text: TextWrapper,
    val confirmButtonCta: TextWrapper,
    val dismissButtonCta: TextWrapper? = null
)