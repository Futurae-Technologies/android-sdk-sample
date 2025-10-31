package com.futurae.sampleapp.configuration.arch

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.futurae.sampleapp.FuturaeSampleApplication
import com.futurae.sampleapp.R
import com.futurae.sampleapp.utils.SdkWrapper
import com.futurae.sampleapp.configuration.usecase.SwitchSDKConfigurationUseCase
import com.futurae.sampleapp.navigation.RootNavigationEvent
import com.futurae.sampleapp.ui.TextWrapper
import com.futurae.sampleapp.ui.shared.elements.configuration.ConfigurationItem
import com.futurae.sampleapp.ui.shared.elements.configuration.LockConfigurationItem
import com.futurae.sampleapp.ui.shared.elements.configuration.LockDurationItem
import com.futurae.sampleapp.ui.shared.elements.configuration.OptionConfigurationsItem
import com.futurae.sampleapp.ui.shared.elements.configuration.SdkConfigOptionalFlag
import com.futurae.sampleapp.utils.LocalStorage
import com.futurae.sdk.FuturaeSDK
import com.futurae.sdk.public_api.common.LockConfigurationType
import com.futurae.sdk.public_api.common.SDKConfiguration
import com.futurae.sdk.public_api.common.model.PresentationConfigurationForBiometricsPrompt
import com.futurae.sdk.public_api.common.model.PresentationConfigurationForDeviceCredentialsPrompt
import com.futurae.sdk.public_api.lock.model.SwitchTargetLockConfiguration
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConfigurationViewModel(
    private val sdkSwitchSDKConfigurationUseCase: SwitchSDKConfigurationUseCase,
    application: FuturaeSampleApplication
) :
    AndroidViewModel(application) {

    companion object {
        fun provideFactory(
            application: FuturaeSampleApplication
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T = ConfigurationViewModel(
                sdkSwitchSDKConfigurationUseCase = SwitchSDKConfigurationUseCase(),
                application = application
            ) as T
        }
    }

    private val _configurationStateFlow = MutableStateFlow<ConfigurationUIState>(
        ConfigurationUIState.Unselected
    )
    val configurationStateFlow = _configurationStateFlow.asStateFlow()

    private val _operationStatusFlow = MutableSharedFlow<Result<Unit>>()
    val operationStatusFlow = _operationStatusFlow.asSharedFlow()

    private val _navigateToRecovery = MutableSharedFlow<Unit>()
    val navigateToRecovery = _navigateToRecovery.asSharedFlow()

    private val _configurationItems = MutableStateFlow<List<ConfigurationItem>>(
        generateConfigurationItems()
    )
    val configurationItems = _configurationItems.asStateFlow()

    private val _onUnlockRequired = MutableSharedFlow<LockConfigurationType>()
    val onUnlockRequired = _onUnlockRequired.asSharedFlow()

    fun submitConfiguration(isConfigurationChange: Boolean) {
        viewModelScope.launch {
            try {
                val configurationItems = _configurationItems.value
                val sdkConfig = generateSDKConfigFromOptions(configurationItems)
                if (isConfigurationChange) {
                    val lockConfigurationType =
                        configurationItems.firstOrNull { it is LockConfigurationItem }?.let {
                            (it as LockConfigurationItem).selectedChoice
                        }
                            ?: return@launch _operationStatusFlow.emit(
                                Result.failure(
                                    IllegalArgumentException(
                                        "Please provide a Lock Configuration Type"
                                    )
                                )
                            )
                    _onUnlockRequired.emit(lockConfigurationType)
                } else {
                    handleSdkInitialLaunch(sdkConfig)
                }
            } catch (t: Throwable) {
                _operationStatusFlow.emit(Result.failure(t))
            }
        }
    }

    private suspend fun handleSdkInitialLaunch(sdkConfiguration: SDKConfiguration) {
        val rootNavigationEvent = SdkWrapper.attemptToLaunchSdkWithErrorHandling(
            application = getApplication(),
            sdkConfiguration = sdkConfiguration
        )

        when (rootNavigationEvent) {
            is RootNavigationEvent.Error -> {
                _operationStatusFlow.emit(Result.failure(Throwable(rootNavigationEvent.message)))
            }

            RootNavigationEvent.Recovery -> {
                if (sdkConfiguration.lockConfigurationType == LockConfigurationType.NONE) {
                    LocalStorage.persistSDKConfiguration(sdkConfiguration)
                    _navigateToRecovery.emit(Unit)
                } else {
                    _operationStatusFlow.emit(Result.failure(Throwable("Cannot migrate from old version with other than NONE config")))
                }
            }

            else -> {
                _operationStatusFlow.emit(Result.success(Unit))
            }
        }
    }

    private fun generateSDKConfigFromOptions(configurationItems: List<ConfigurationItem>): SDKConfiguration {
        val sdkConfig = SDKConfiguration.Builder()
        configurationItems.forEach {
            when (it) {
                is LockConfigurationItem -> {
                    if (it.selectedChoice != null) {
                        sdkConfig.setLockConfigurationType(it.selectedChoice)
                    }
                }

                is LockDurationItem -> {
                    if (it.selectedChoice != null) {
                        sdkConfig.setUnlockDuration(it.selectedChoice)
                    }
                }

                is OptionConfigurationsItem -> {
                    it.items.entries.filter { (_, v) -> v }.forEach { entry ->
                        when (entry.key) {
                            SdkConfigOptionalFlag.SKIP_HW_SECURITY -> sdkConfig.setSkipHardwareSecurity(
                                true
                            )

                            SdkConfigOptionalFlag.REQUIRE_UNLOCKED_DEVICE -> sdkConfig.setUnlockedDeviceRequired(
                                true
                            )

                            SdkConfigOptionalFlag.BIOMETRIC_INVALIDATION -> sdkConfig.setInvalidatedByBiometricChange(
                                true
                            )

                            SdkConfigOptionalFlag.CHANGE_PIN_WITH_BIOMETRICS -> sdkConfig.setAllowChangePinCodeWithBiometricUnlock(
                                true
                            )
                        }
                    }
                }
            }
        }
        return sdkConfig.build()
    }

    fun onItemsUpdated(index: Int, item: ConfigurationItem) {
        viewModelScope.launch {
            val currentList = _configurationItems.value.toMutableList()
            currentList[index] = item
            _configurationItems.emit(currentList)
            (item as? LockConfigurationItem)?.takeIf { it.selectedChoice != null }?.let {
                _configurationStateFlow.emit(ConfigurationUIState.InProgress)
            }
        }
    }


    private fun generateConfigurationItems(): SnapshotStateList<ConfigurationItem> {
        return mutableStateListOf(
            LockConfigurationItem(
                title = TextWrapper.Resource(R.string.lock_mechanism),
                selectedChoice = null,
                subtitle = TextWrapper.Resource(R.string.lock_mechanism_subtitle),
            ),
            LockDurationItem(
                title = TextWrapper.Resource(R.string.unlock_duration),
                selectedChoice = null,
                subtitle = TextWrapper.Resource(R.string.unlock_duration_subtitle),
            ),
            OptionConfigurationsItem(
                title = TextWrapper.Resource(R.string.optional_configurations),
                items = SdkConfigOptionalFlag.entries.associateWith { false },
                subtitle = TextWrapper.Resource(R.string.optional_configurations_subtitle),
            ),
        )
    }

    fun switchToSdkConfigurationNone() {
        viewModelScope.launch {
            try {
                val configuration = generateSDKConfigFromOptions(_configurationItems.value)
                sdkSwitchSDKConfigurationUseCase(
                    getApplication(),
                    SwitchTargetLockConfiguration.None(
                        configuration
                    )
                )
                _operationStatusFlow.emit(Result.success(Unit))
            } catch (t: Throwable) {
                _operationStatusFlow.emit(Result.failure(t))
            }
        }
    }

    fun switchToSDKConfigurationBiometrics(presentationConfig: PresentationConfigurationForBiometricsPrompt) {
        viewModelScope.launch {
            try {
                val configuration = generateSDKConfigFromOptions(_configurationItems.value)
                sdkSwitchSDKConfigurationUseCase(
                    getApplication(),
                    SwitchTargetLockConfiguration.Biometrics(
                        configuration,
                        presentationConfig
                    )
                )
                _operationStatusFlow.emit(Result.success(Unit))
            } catch (t: Throwable) {
                _operationStatusFlow.emit(Result.failure(t))
            }
        }
    }

    fun switchToSDKConfigurationBiometricsOrDeviceCreds(presentationConfig: PresentationConfigurationForDeviceCredentialsPrompt) {
        viewModelScope.launch {
            try {
                val configuration = generateSDKConfigFromOptions(_configurationItems.value)
                sdkSwitchSDKConfigurationUseCase(
                    getApplication(),
                    SwitchTargetLockConfiguration.BiometricsOrCredentials(
                        configuration,
                        presentationConfig
                    )
                )
                _operationStatusFlow.emit(Result.success(Unit))
            } catch (t: Throwable) {
                _operationStatusFlow.emit(Result.failure(t))
            }
        }
    }

    private fun switchToSDKConfigurationSDKPin(sdkPin: CharArray) {
        viewModelScope.launch {
            try {
                val configuration = generateSDKConfigFromOptions(_configurationItems.value)
                sdkSwitchSDKConfigurationUseCase(
                    getApplication(),
                    SwitchTargetLockConfiguration.PinWithBiometricsOptional(
                        configuration,
                        sdkPin
                    )
                )
                _operationStatusFlow.emit(Result.success(Unit))
            } catch (t: Throwable) {
                _operationStatusFlow.emit(Result.failure(t))
            }
        }
    }

    fun onPinProvided(sdkPin: CharArray?) {
        if(sdkPin != null) {
            switchToSDKConfigurationSDKPin(sdkPin)
        } else {
            viewModelScope.launch {
                _operationStatusFlow.emit(Result.failure(IllegalArgumentException("A pin must be provided to switch to a PIN configuration")))
            }
        }
    }
}

sealed class ConfigurationUIState {
    data object Unselected : ConfigurationUIState()
    data object InProgress : ConfigurationUIState()
}