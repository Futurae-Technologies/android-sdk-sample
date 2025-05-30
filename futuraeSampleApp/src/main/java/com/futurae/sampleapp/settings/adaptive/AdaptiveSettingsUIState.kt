package com.futurae.sampleapp.settings.adaptive

import com.futurae.sampleapp.settings.SettingsListItem
import com.futurae.sampleapp.ui.TextWrapper

data class AdaptiveSettingsUIState(
    val items: List<SettingsListItem>,
    val actions: List<SettingsAction>,
    val thresholdUIState: ThresholdUIState
)

data class SettingsAction(
    val cta: TextWrapper,
    val onClick: () -> Unit
)