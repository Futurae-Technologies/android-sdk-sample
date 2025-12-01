package com.futurae.sampleapp.settings

import androidx.compose.ui.graphics.vector.ImageVector
import com.futurae.sampleapp.ui.TextWrapper

sealed class SettingsListItem
data class SettingsItem(
    val title: TextWrapper,
    val subtitle: TextWrapper,
    val icon: ImageVector? = null,
    val isItemWithWarning: Boolean = false,
    val isItemClickable: Boolean = true,
    val actionCallback: () -> Unit,
) : SettingsListItem()

data class SettingsToggle(
    val title: TextWrapper,
    val subtitle: TextWrapper,
    val testTag: String,
    val isEnabled: Boolean,
    val onToggleChanged: (Boolean) -> Unit,
) : SettingsListItem()

data class SettingsNestedToggleGroup(
    val title: TextWrapper,
    val subtitle: TextWrapper,
    val isToggled: Boolean,
    val onToggleChanged: (Boolean) -> Unit,
    val children: List<SettingsNestedToggle> = emptyList()
) : SettingsListItem()

data object SettingsSpacer : SettingsListItem()

data class SettingsNestedToggle(
    val title: TextWrapper,
    val isEnabled: Boolean,
    val isToggled: Boolean,
    val onToggleChanged: (Boolean) -> Unit
)