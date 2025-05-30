package com.futurae.demoapp.settings.adaptive

import com.futurae.demoapp.settings.SettingsListItem
import com.futurae.demoapp.ui.TextWrapper

data class AdaptiveSettingsUIState(
    val items: List<SettingsListItem>,
    val actions: List<SettingsAction>,
    val thresholdUIState: ThresholdUIState
)

data class SettingsAction(
    val cta: TextWrapper,
    val onClick: () -> Unit
)