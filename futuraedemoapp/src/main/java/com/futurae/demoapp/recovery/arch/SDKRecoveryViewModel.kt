package com.futurae.demoapp.recovery.arch

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.futurae.demoapp.FuturaeDemoApplication
import com.futurae.demoapp.utils.ILCEState
import com.futurae.demoapp.utils.LocalStorage
import com.futurae.demoapp.arch.PinProviderViewModel
import com.futurae.demoapp.recovery.usecase.SDKRecoveryUseCase
import com.futurae.sdk.public_api.common.LockConfigurationType
import com.futurae.sdk.public_api.lock.model.UserPresenceVerificationFactor
import com.futurae.sdk.public_api.lock.model.UserPresenceVerificationMode
import com.futurae.sdk.public_api.lock.model.WithSDKPin
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SDKRecoveryViewModel(
    private val sdkRecoveryUseCase: SDKRecoveryUseCase,
    private val pinProviderViewModel: PinProviderViewModel,
    application: Application,
) : AndroidViewModel(application) {

    companion object {
        fun provideFactory(
            application: FuturaeDemoApplication,
            pinProviderViewModel: PinProviderViewModel
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>
                ): T = SDKRecoveryViewModel(
                    application = application,
                    sdkRecoveryUseCase = SDKRecoveryUseCase(),
                    pinProviderViewModel = pinProviderViewModel
                ) as T
            }
    }

    private val _state = MutableStateFlow<ILCEState<Unit>>(ILCEState.Idle)
    val state = _state.asStateFlow()

    private val _upvDependencyFlow = MutableSharedFlow<UserPresenceVerificationFactor>()
    val upvDependencyFlow = _upvDependencyFlow.asSharedFlow()

    init {
        viewModelScope.launch {
            pinProviderViewModel.pinFlow.collect {
                onPinProvided(it)
                pinProviderViewModel.reset()
            }
        }
    }

    fun requestSDKRecovery() {
        when (LocalStorage.persistedSDKConfig.lockConfigurationType) {
            LockConfigurationType.NONE -> {
                attemptSDKRecovery(null)
            }

            LockConfigurationType.BIOMETRICS_ONLY -> {
                viewModelScope.launch {
                    _upvDependencyFlow.emit(UserPresenceVerificationFactor.BIOMETRICS)
                }
            }

            LockConfigurationType.BIOMETRICS_OR_DEVICE_CREDENTIALS -> {
                viewModelScope.launch {
                    _upvDependencyFlow.emit(UserPresenceVerificationFactor.DEVICE_CREDENTIALS)
                }
            }

            LockConfigurationType.SDK_PIN_WITH_BIOMETRICS_OPTIONAL -> {
                viewModelScope.launch {
                    _upvDependencyFlow.emit(UserPresenceVerificationFactor.APP_PIN)
                }
            }
        }
    }

    private fun attemptSDKRecovery(userPresenceVerificationMode: UserPresenceVerificationMode?) {
        viewModelScope.launch {
            _state.emit(ILCEState.Loading)
            try {
                sdkRecoveryUseCase.invoke(getApplication(), userPresenceVerificationMode)
                _state.emit(ILCEState.Content(Unit))
            } catch (t: Throwable) {
                _state.emit(ILCEState.Error(t))
            }
        }
    }

    fun onPinProvided(sdkPin: CharArray?) {
        sdkPin?.let {
            attemptSDKRecovery(WithSDKPin(it))
        }
    }
}