package com.futurae.demoapp.ui.shared.elements.alertdialog

import androidx.annotation.DrawableRes
import com.futurae.demoapp.ui.TextWrapper
import com.futurae.demoapp.ui.shared.elements.buttons.ActionButtonType

data class FuturaeAlertDialogWithFilledButtonsUIState(
    @DrawableRes val drawableRes: Int,
    val title: TextWrapper,
    val text: TextWrapper,
    val confirmButtonCta: TextWrapper,
    val dismissButtonCta: TextWrapper? = null,
    val confirmButtonType: ActionButtonType
)