package com.futurae.sampleapp.lock.arch

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.futurae.sampleapp.FuturaeSampleApplication
import com.futurae.sampleapp.utils.LocalStorage
import com.futurae.sampleapp.R
import com.futurae.sampleapp.lock.usecase.ActivateBiometricsUseCase
import com.futurae.sampleapp.lock.usecase.UnlockUseCase
import com.futurae.sampleapp.ui.TextWrapper
import com.futurae.sdk.FuturaeSDK
import com.futurae.sdk.public_api.common.LockConfigurationType
import com.futurae.sdk.public_api.lock.model.UnlockMethodType
import com.futurae.sdk.public_api.lock.model.UserPresenceVerificationMode
import com.futurae.sdk.public_api.lock.model.WithBiometrics
import com.futurae.sdk.public_api.lock.model.WithSDKPin
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LockScreenViewModel(
    private val configuration: LockScreenConfiguration,
    private val unlockUseCase: UnlockUseCase,
    private val activateBiometricsUseCase: ActivateBiometricsUseCase,
    application: Application
) : AndroidViewModel(application) {

    private val digitsEnteredRepeat: MutableList<Int> = mutableListOf()
    private val digitsEntered: MutableList<Int> = mutableListOf()

    private val _unlockEventFlow = MutableSharedFlow<UnlockRequired>(replay = 1)
    val unlockEventFlow = _unlockEventFlow.asSharedFlow()

    private val _activateBiometricsFlow = MutableSharedFlow<Unit>(replay = 1)
    val activateBiometricsFlow = _activateBiometricsFlow.asSharedFlow()

    private val _uiStateFlow = MutableStateFlow(initializeLockScreenState())
    val uiState = _uiStateFlow.asStateFlow()

    private val _exitScreenFlow = MutableSharedFlow<CharArray?>()
    val exitScreenFlow = _exitScreenFlow.asSharedFlow()

    private val isSecondInput: Boolean
        get() = digitsEntered.size == configuration.maxDigitsAllowed

    private fun initializeLockScreenState(): LockScreenUIState {
        val sdkConfig = LocalStorage.persistedSDKConfig
        return when (configuration.lockMode) {
            LockScreenMode.UNLOCK -> {
                when (sdkConfig.lockConfigurationType) {
                    LockConfigurationType.NONE -> {
                        throw IllegalStateException("LockScreen shown for LockConfigurationType.NONE")
                    }

                    LockConfigurationType.BIOMETRICS_ONLY -> {
                        viewModelScope.launch {
                            _unlockEventFlow.emit(UnlockRequired.BIOMETRICS)
                        }
                        LockScreenUIState.BioCredsScreen(
                            title = TextWrapper.Resource(R.string.unlock_with_biometrics),
                        )
                    }

                    LockConfigurationType.BIOMETRICS_OR_DEVICE_CREDENTIALS -> {
                        viewModelScope.launch {
                            _unlockEventFlow.emit(UnlockRequired.BIOMETRICS_OR_CREDS)
                        }
                        LockScreenUIState.BioCredsScreen(
                            title = TextWrapper.Resource(R.string.unlock_with_biometrics_or_creds)
                        )
                    }

                    LockConfigurationType.SDK_PIN_WITH_BIOMETRICS_OPTIONAL -> {
                        val supportsAlternativeAuth = FuturaeSDK.client.lockApi
                            .getActiveUnlockMethods()
                            .contains(UnlockMethodType.BIOMETRICS)
                        LockScreenUIState.PinScreen(
                            digitsEntered = digitsEntered.toTypedArray(),
                            supportAlternativeAuthText = if (supportsAlternativeAuth) {
                                TextWrapper.Resource(R.string.unlock_with_biometrics_alternative)
                            } else {
                                null
                            },
                            title = TextWrapper.Resource(R.string.unlock_with_pin)
                        )
                    }
                }
            }

            LockScreenMode.GET_PIN -> {
                val supportsAlternativeAuth = FuturaeSDK.client.lockApi
                    .getActiveUnlockMethods()
                    .contains(UnlockMethodType.BIOMETRICS)
                LockScreenUIState.PinScreen(
                    digitsEntered = digitsEntered.toTypedArray(),
                    supportAlternativeAuthText = if (supportsAlternativeAuth) {
                        TextWrapper.Resource(R.string.unlock_with_biometrics_alternative)
                    } else {
                        null
                    },
                    title = TextWrapper.Resource(R.string.unlock_with_pin)
                )
            }

            LockScreenMode.CREATE_PIN,
            LockScreenMode.CHANGE_PIN -> {
                LockScreenUIState.PinScreen(
                    digitsEntered = if (isSecondInput) {
                        digitsEnteredRepeat.toTypedArray()
                    } else {
                        digitsEntered.toTypedArray()
                    },
                    supportAlternativeAuthText = null,
                    title = if (isSecondInput) {
                        TextWrapper.Resource(R.string.repeat_new_pin)
                    } else {
                        TextWrapper.Resource(R.string.create_new_pin)
                    }
                )
            }

            LockScreenMode.ACTIVATE_BIO -> {
                viewModelScope.launch {
                    _activateBiometricsFlow.emit(Unit)
                }
                LockScreenUIState.BioCredsScreen(
                    title = TextWrapper.Resource(R.string.activate_biometrics)
                )
            }
        }
    }

    fun onDigitEntered(pinDigit: Int) {
        if (isSecondInput) {
            digitsEnteredRepeat.add(pinDigit)
            viewModelScope.launch {
                val current = (_uiStateFlow.value as LockScreenUIState.PinScreen)
                _uiStateFlow.emit(
                    current.copy(digitsEntered = digitsEnteredRepeat.toTypedArray())
                )
            }
            if (digitsEnteredRepeat.size == configuration.maxDigitsAllowed) {
                onPinEntered()
            }
        } else {
            digitsEntered.add(pinDigit)
            viewModelScope.launch {
                val current = (_uiStateFlow.value as LockScreenUIState.PinScreen)
                _uiStateFlow.emit(
                    current.copy(digitsEntered = digitsEntered.toTypedArray())
                )
            }
            if (digitsEntered.size == configuration.maxDigitsAllowed) {
                onPinEntered()
            }
        }

    }

    fun onDeleteDigitPressed() {
        if (digitsEntered.isNotEmpty()) {
            digitsEntered.removeAt(digitsEntered.size - 1)
        }
        viewModelScope.launch {
            _uiStateFlow.emit(
                (_uiStateFlow.value as LockScreenUIState.PinScreen).copy(digitsEntered = digitsEntered.toTypedArray())
            )
        }
    }


    // This is only available for unlocking. Not creating a new PIN
    fun onAlternativeAuthRequest() {
        when (val lockConfigurationType = LocalStorage.persistedSDKConfig.lockConfigurationType) {
            LockConfigurationType.NONE,
            LockConfigurationType.BIOMETRICS_ONLY,
            LockConfigurationType.BIOMETRICS_OR_DEVICE_CREDENTIALS -> throw IllegalStateException("Alternative Auth request for $lockConfigurationType")

            LockConfigurationType.SDK_PIN_WITH_BIOMETRICS_OPTIONAL -> {
                if (FuturaeSDK.client.lockApi.getActiveUnlockMethods()
                        .contains(UnlockMethodType.BIOMETRICS)
                ) {
                    if (uiState.value is LockScreenUIState.PinScreen) {
                        viewModelScope.launch {
                            _uiStateFlow.emit(
                                LockScreenUIState.BioCredsScreen(
                                    supportAlternativeAuthText = TextWrapper.Resource(R.string.unlock_with_pin_alternative),
                                    title = TextWrapper.Resource(R.string.unlock_with_biometrics),
                                )
                            )
                            _unlockEventFlow.emit(UnlockRequired.BIOMETRICS)
                        }
                    } else {
                        digitsEntered.clear()
                        digitsEnteredRepeat.clear()
                        val supportsAlternativeAuth = FuturaeSDK.client.lockApi
                            .getActiveUnlockMethods()
                            .contains(UnlockMethodType.BIOMETRICS)
                        viewModelScope.launch {
                            _uiStateFlow.emit(
                                LockScreenUIState.PinScreen(
                                    digitsEntered = digitsEntered.toTypedArray(),
                                    if (supportsAlternativeAuth) {
                                        TextWrapper.Resource(R.string.unlock_with_biometrics_alternative)
                                    } else {
                                        null
                                    },
                                    title = TextWrapper.Resource(R.string.unlock_with_pin)
                                )
                            )
                        }
                    }
                } else {
                    throw IllegalStateException("Alternative Auth request for ${lockConfigurationType}, without activated Biometrics")
                }
            }
        }
    }

    fun onPinCanceled() {
        viewModelScope.launch {
            _exitScreenFlow.emit(null)
        }
    }

    private fun onPinEntered() {
        when (configuration.lockMode) {
            LockScreenMode.GET_PIN -> {
                viewModelScope.launch {
                    _exitScreenFlow.emit(digitsEntered.toCharArray())
                }
            }

            LockScreenMode.CREATE_PIN,
            LockScreenMode.CHANGE_PIN -> {
                if (isSecondInput) {
                    if (digitsEnteredRepeat == digitsEntered) {
                        viewModelScope.launch {
                            _exitScreenFlow.emit(digitsEntered.toCharArray())
                        }
                    } else if (digitsEntered.size == digitsEnteredRepeat.size) {
                        digitsEntered.clear()
                        digitsEnteredRepeat.clear()
                        viewModelScope.launch {
                            _uiStateFlow.emit(
                                LockScreenUIState.PinScreen(
                                    digitsEntered = digitsEntered.toTypedArray(),
                                    supportAlternativeAuthText = null,
                                    title = TextWrapper.Resource(R.string.create_new_pin),
                                    error = TextWrapper.Resource(R.string.create_pin_missmatch),
                                )
                            )
                        }
                    } else {
                        viewModelScope.launch {
                            _uiStateFlow.emit(
                                LockScreenUIState.PinScreen(
                                    digitsEntered = digitsEnteredRepeat.toTypedArray(),
                                    supportAlternativeAuthText = null,
                                    title = TextWrapper.Resource(R.string.repeat_new_pin)
                                )
                            )
                        }
                    }
                } else {
                    viewModelScope.launch {
                        _uiStateFlow.emit(
                            LockScreenUIState.PinScreen(
                                digitsEntered = digitsEnteredRepeat.toTypedArray(),
                                supportAlternativeAuthText = null,
                                title = TextWrapper.Resource(R.string.repeat_new_pin)
                            )
                        )
                    }
                }
            }

            else -> {
                unlock(WithSDKPin(digitsEntered.toCharArray()))
            }
        }
    }

    fun unlock(upv: UserPresenceVerificationMode) {
        viewModelScope.launch {
            try {
                unlockUseCase.invoke(upv)
                _exitScreenFlow.emit(null)
            } catch (t: Throwable) {
                when (val state = _uiStateFlow.value) {
                    is LockScreenUIState.BioCredsScreen -> {
                        _uiStateFlow.emit(
                            state.copy(error = TextWrapper.Primitive(t.message ?: "Unknown Error"))
                        )
                    }

                    is LockScreenUIState.PinScreen -> {
                        digitsEntered.clear()
                        _uiStateFlow.emit(
                            state.copy(
                                digitsEntered = digitsEntered.toTypedArray(),
                                error = TextWrapper.Primitive(t.message ?: "Unknown Error")
                            )
                        )
                    }
                }
            }
        }
    }

    fun onSystemAuthRequested() {
        initializeLockScreenState()
    }

    fun activateBiometrics(withBiometrics: WithBiometrics) {
        viewModelScope.launch {
            try {
                activateBiometricsUseCase(withBiometrics)
                _exitScreenFlow.emit(null)
            } catch (t: Throwable) {
                when (val state = _uiStateFlow.value) {
                    is LockScreenUIState.BioCredsScreen -> {
                        _uiStateFlow.emit(
                            state.copy(error = TextWrapper.Primitive(t.message ?: "Unknown Error"))
                        )
                    }

                    else -> {
                        // should never occur in this flow
                    }
                }
            }
        }
    }

    private fun List<Int>.toCharArray() : CharArray {
        return this.joinToString("") { it.toString() }.toCharArray()
    }

    companion object {
        fun provideFactory(
            application: FuturaeSampleApplication,
            configuration: LockScreenConfiguration,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T = LockScreenViewModel(
                configuration = configuration,
                unlockUseCase = UnlockUseCase(),
                activateBiometricsUseCase = ActivateBiometricsUseCase(),
                application = application,
            ) as T
        }
    }

}