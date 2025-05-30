package com.futurae.sampleapp.ui.shared.elements.decisionmodal

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class FuturaeFullScreenDecisionModalUIState(
    @DrawableRes val drawableResId: Int,
    @StringRes val titleResId: Int,
    @StringRes val descriptionResId: Int,
    @StringRes val noticeResId: Int,
    @StringRes val primaryActionResId: Int,
    @StringRes val secondaryAction: Int,
    val isDismissible: Boolean
)