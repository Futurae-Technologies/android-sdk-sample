package com.futurae.sampleapp.settings.sdksettings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.futurae.sampleapp.FuturaeSampleApplication
import com.futurae.sampleapp.navigation.FuturaeSampleDestinations
import com.futurae.sampleapp.utils.LocalStorage
import com.futurae.sampleapp.R
import com.futurae.sampleapp.settings.SettingsItem
import com.futurae.sampleapp.settings.SettingsListItem
import com.futurae.sampleapp.settings.SettingsSpacer
import com.futurae.sampleapp.settings.SettingsToggle
import com.futurae.sampleapp.ui.TextWrapper
import com.futurae.sampleapp.ui.shared.elements.snackbar.FuturaeSnackbarUIState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SDKSettingsViewModel(
    application: FuturaeSampleApplication,
    val changeSDKPinUseCase: ChangeSDKPinUseCase
) : AndroidViewModel(application) {

    companion object {
        fun provideFactory(
            application: FuturaeSampleApplication
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T = SDKSettingsViewModel(
                application = application,
                changeSDKPinUseCase = ChangeSDKPinUseCase()
            ) as T
        }
    }

    private val _settingsItems = MutableStateFlow(
        generateSettingsItems()
    )
    val settingsItems = _settingsItems.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<String>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _requestSDKPinChange = MutableSharedFlow<Unit>()
    val requestSDKPinChange = _requestSDKPinChange.asSharedFlow()

    private val _notifyUser = MutableSharedFlow<FuturaeSnackbarUIState>()
    val notifyUser = _notifyUser.asSharedFlow()

    private fun generateSettingsItems(): List<SettingsListItem> {
        return listOfNotNull(
            SettingsItem(
                title = TextWrapper.Resource(R.string.adaptive_overview),
                subtitle = TextWrapper.Resource(R.string.adaptive_overview_subtitle),
                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                actionCallback = {
                    viewModelScope.launch {
                        _navigationEvent.emit(FuturaeSampleDestinations.SETTINGS_ADAPTIVE_ROUTE.route)
                    }
                }
            ),
            SettingsItem(
                title = TextWrapper.Resource(R.string.integrity_check),
                subtitle = TextWrapper.Resource(R.string.integrity_check_subtitle),
                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                actionCallback = {
                    viewModelScope.launch {
                        _navigationEvent.emit(FuturaeSampleDestinations.SETTINGS_INTEGRITY_ROUTE.route)
                    }
                }
            ),
            SettingsItem(
                title = TextWrapper.Resource(R.string.sdk_configuration),
                subtitle = TextWrapper.Resource(R.string.sdk_switch_config_title),
                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                actionCallback = {
                    viewModelScope.launch {
                        _navigationEvent.emit(FuturaeSampleDestinations.SETTINGS_SDK_CONFIGURATION_ROUTE.route)
                    }
                }
            ),
            changeSDKPinSettingsItemIffApplicable(),
            SettingsToggle(
                title = TextWrapper.Resource(R.string.flow_binding),
                subtitle = TextWrapper.Resource(R.string.flow_binding_subtitle),
                isEnabled = LocalStorage.isFlowBindingEnabled(),
                onToggleChanged = {
                    LocalStorage.setFlowBindingEnabled(
                        !LocalStorage.isFlowBindingEnabled()
                    )
                    viewModelScope.launch {
                        _settingsItems.emit(generateSettingsItems())
                    }
                }
            ),
            SettingsToggle(
                title = TextWrapper.Resource(R.string.session_info_without_unlock),
                subtitle = TextWrapper.Resource(R.string.session_info_without_unlock_subtitle),
                isEnabled = LocalStorage.isSessionInfoWithoutUnlockEnabled,
                onToggleChanged = {
                    LocalStorage.setSessionInfoWithoutUnlockEnabled(
                        !LocalStorage.isSessionInfoWithoutUnlockEnabled
                    )
                    viewModelScope.launch {
                        _settingsItems.emit(generateSettingsItems())
                    }
                }
            ),
            SettingsSpacer,
            SettingsItem(
                title = TextWrapper.Resource(R.string.debug_utilities),
                subtitle = TextWrapper.Resource(R.string.debug_utilities_subtitle),
                isItemWithWarning = true,
                actionCallback = {
                    viewModelScope.launch {
                        _navigationEvent.emit(FuturaeSampleDestinations.SETTINGS_SDK_DEBUG_ROUTE.route)
                    }
                }
            ),
        )
    }

    private fun changeSDKPinSettingsItemIffApplicable(): SettingsItem? =
        if (LocalStorage.isPinConfig && LocalStorage.isDeviceEnrolled()) {
            SettingsItem(
                title = TextWrapper.Resource(R.string.sdk_pin),
                subtitle = TextWrapper.Resource(R.string.sdk_change_pin),
                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                actionCallback = {
                    viewModelScope.launch {
                        _requestSDKPinChange.emit(Unit)
                    }
                }
            )
        } else {
            null
        }

    fun onPinProvided(newSDKPin: CharArray) {
        viewModelScope.launch {
            changeSDKPinUseCase(newSDKPin)
                .onSuccess {
                    _notifyUser.emit(
                        FuturaeSnackbarUIState.Success(
                            TextWrapper.Resource(R.string.sdk_change_pin_success)
                        )
                    )
                }
                .onFailure {
                    _notifyUser.emit(
                        FuturaeSnackbarUIState.Error(
                            TextWrapper.Resource(
                                R.string.sdk_change_pin_error,
                                listOf(it.localizedMessage ?: "-")
                            )
                        )
                    )
                }

        }
    }
}