package com.futurae.demoapp.settings.sdksettings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.futurae.demoapp.FuturaeDemoApplication
import com.futurae.demoapp.FuturaeDemoDestinations
import com.futurae.demoapp.LocalStorage
import com.futurae.demoapp.R
import com.futurae.demoapp.settings.SettingsItem
import com.futurae.demoapp.settings.SettingsListItem
import com.futurae.demoapp.settings.SettingsSpacer
import com.futurae.demoapp.settings.SettingsToggle
import com.futurae.demoapp.ui.TextWrapper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SDKSettingsViewModel(application: FuturaeDemoApplication) :
    AndroidViewModel(application) {

    companion object {
        fun provideFactory(
            application: FuturaeDemoApplication
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T = SDKSettingsViewModel(application = application) as T
        }
    }

    private val _settingsItems = MutableStateFlow(
        generateSettingsItems()
    )
    val settingsItems = _settingsItems.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<String>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private fun generateSettingsItems(): List<SettingsListItem> {
        return listOf(
            SettingsItem(title = TextWrapper.Resource(R.string.adaptive_overview),
                subtitle = TextWrapper.Resource(R.string.adaptive_overview_subtitle),
                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                actionCallback = {
                    viewModelScope.launch {
                        _navigationEvent.emit(FuturaeDemoDestinations.SETTINGS_ADAPTIVE_ROUTE.route)
                    }
                }),
            SettingsItem(title = TextWrapper.Resource(R.string.integrity_check),
                subtitle = TextWrapper.Resource(R.string.integrity_check_subtitle),
                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                actionCallback = {
                    viewModelScope.launch {
                        _navigationEvent.emit(FuturaeDemoDestinations.SETTINGS_INTEGRITY_ROUTE.route)
                    }
                }),
            SettingsItem(title = TextWrapper.Resource(R.string.sdk_configuration),
                subtitle = TextWrapper.Resource(R.string.sdk_switch_config_title),
                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                actionCallback = {
                    viewModelScope.launch {
                        _navigationEvent.emit(FuturaeDemoDestinations.SETTINGS_SDK_CONFIGURATION_ROUTE.route)
                    }
                }),
            SettingsToggle(title = TextWrapper.Resource(R.string.flow_binding),
                subtitle = TextWrapper.Resource(R.string.flow_binding_subtitle),
                isEnabled = LocalStorage.isFlowBindingEnabled(),
                onToggleChanged = {
                    LocalStorage.setFlowBindingEnabled(
                        !LocalStorage.isFlowBindingEnabled()
                    )
                    viewModelScope.launch {
                        _settingsItems.emit(generateSettingsItems())
                    }
                }),
            SettingsToggle(title = TextWrapper.Resource(R.string.session_info_without_unlock),
                subtitle = TextWrapper.Resource(R.string.session_info_without_unlock_subtitle),
                isEnabled = LocalStorage.isSessionInfoWithoutUnlockEnabled,
                onToggleChanged = {
                    LocalStorage.setSessionInfoWithoutUnlockEnabled(
                        !LocalStorage.isSessionInfoWithoutUnlockEnabled
                    )
                    viewModelScope.launch {
                        _settingsItems.emit(generateSettingsItems())
                    }
                }),
            SettingsSpacer,
            SettingsItem(title = TextWrapper.Resource(R.string.debug_utilities),
                subtitle = TextWrapper.Resource(R.string.debug_utilities_subtitle),
                isItemWithWarning = true,
                actionCallback = {
                    viewModelScope.launch {
                        _navigationEvent.emit(FuturaeDemoDestinations.SETTINGS_SDK_DEBUG_ROUTE.route)
                    }
                }),
        )
    }


}