package com.futurae.sampleapp.settings.debug

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
import com.futurae.sampleapp.ui.TextWrapper
import com.futurae.sampleapp.ui.shared.elements.snackbar.FuturaeSnackbarUIState
import com.futurae.sdk.FuturaeSDK
import com.futurae.sdk.debug.FuturaeDebugUtil
import com.futurae.sdk.public_api.lock.model.UnlockMethodType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SDKDebugUtilViewModel(
    application: FuturaeSampleApplication,
    private val deactivateBiometricsUseCase: DeactivateBiometricsUseCase
) :
    AndroidViewModel(application) {

    companion object {
        fun provideFactory(
            application: FuturaeSampleApplication
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T = SDKDebugUtilViewModel(
                application = application,
                deactivateBiometricsUseCase = DeactivateBiometricsUseCase()
            ) as T
        }
    }

    private val _debugUtilItems = MutableStateFlow(
        generateDebugUtilItems()
    )
    val debugUtilItems = _debugUtilItems.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<String>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _activateBiometricsRequest = MutableSharedFlow<Unit>()
    val activateBiometricsRequest = _activateBiometricsRequest.asSharedFlow()

    private val _snackbarUIState = MutableSharedFlow<FuturaeSnackbarUIState>()
    val snackbarUIState = _snackbarUIState.asSharedFlow()

    fun refreshItems() {
        viewModelScope.launch {
            _debugUtilItems.emit(generateDebugUtilItems())
        }
    }

    private fun generateDebugUtilItems(): List<SettingsListItem> {
        return listOfNotNull(
            SettingsItem(
                title = TextWrapper.Resource(R.string.sdk_reset),
                subtitle = TextWrapper.Resource(R.string.sdk_reset_subtitle),
                isItemWithWarning = true,
                actionCallback = {
                    viewModelScope.launch {
                        FuturaeSDK.reset(getApplication())
                        LocalStorage.reset()
                        _navigationEvent.emit(FuturaeSampleDestinations.SPLASH_ROUTE.route)
                    }
                }
            ),
            SettingsItem(
                title = TextWrapper.Resource(R.string.sdk_clear_enc_storage),
                subtitle = TextWrapper.Resource(R.string.sdk_clear_enc_storage_subtitle),
                isItemWithWarning = true,
                actionCallback = {
                    val snackbarUIState = try {
                        FuturaeDebugUtil.clearEncryptedTokens()
                        FuturaeSnackbarUIState.Success(TextWrapper.Primitive("Cleared Encryted Tokens"))
                    } catch (t : Throwable) {
                        FuturaeSnackbarUIState.Error(TextWrapper.Primitive(t.message ?: "Unknown Error"))
                    }
                    viewModelScope.launch {
                        _snackbarUIState.emit(snackbarUIState)
                    }
                }
            ),
            SettingsItem(
                title = TextWrapper.Resource(R.string.sdk_clear_keys),
                subtitle = TextWrapper.Resource(R.string.sdk_clear_keys_subtitle),
                isItemWithWarning = true,
                actionCallback = {
                    val snackbarUIState = try {
                        FuturaeDebugUtil.corruptV2Keys(getApplication())
                        FuturaeSnackbarUIState.Success(TextWrapper.Primitive("Corrupted SDK v2 Keys"))
                    } catch (t : Throwable) {
                        FuturaeSnackbarUIState.Error(TextWrapper.Primitive(t.message ?: "Unknown Error"))
                    }
                    viewModelScope.launch {
                        _snackbarUIState.emit(snackbarUIState)
                    }
                }
            ),
            SettingsItem(
                title = TextWrapper.Resource(R.string.sdk_corrut_db),
                subtitle = TextWrapper.Resource(R.string.sdk_corrut_db_subtitle),
                isItemWithWarning = true,
                actionCallback = {
                    val snackbarUIState = try {
                        FuturaeDebugUtil.corruptDBTokens(getApplication())
                        FuturaeSnackbarUIState.Success(TextWrapper.Primitive("Corrupted SDK DB tokens"))
                    } catch (t : Throwable) {
                        FuturaeSnackbarUIState.Error(TextWrapper.Primitive(t.message ?: "Unknown Error"))
                    }
                    viewModelScope.launch {
                        _snackbarUIState.emit(snackbarUIState)
                    }
                }
            ),
            activateBiometricsItem(),
            deactivateBiometricsItem(),
            SettingsItem(
                title = TextWrapper.Resource(R.string.debug_qr_code_utils),
                subtitle = TextWrapper.Resource(R.string.debug_qr_code_utils_subtitle),
                isItemWithWarning = true,
                actionCallback = {
                    viewModelScope.launch {
                        _navigationEvent.emit(FuturaeSampleDestinations.DEBUG_QR_CODE_UTILS_ROUTE.route)
                    }
                }
            ),
            SettingsItem(
                title = TextWrapper.Resource(R.string.debug_uri_utils),
                subtitle = TextWrapper.Resource(R.string.debug_uri_utils_subtitle),
                isItemWithWarning = true,
                actionCallback = {
                    viewModelScope.launch {
                        _navigationEvent.emit(FuturaeSampleDestinations.DEBUG_URI_UTILS_ROUTE.route)
                    }
                }
            )
        )
    }

    private fun activateBiometricsItem(): SettingsItem? =
        if (LocalStorage.isPinConfig && !LocalStorage.shouldSetupPin) {
            SettingsItem(
                title = TextWrapper.Resource(R.string.debug_utilities_activate_biometrics),
                subtitle = TextWrapper.Resource(R.string.activate_biometrics),
                isItemClickable = UnlockMethodType.BIOMETRICS !in FuturaeSDK.client.lockApi.getActiveUnlockMethods(),
                actionCallback = {
                    viewModelScope.launch {
                        _activateBiometricsRequest.emit(Unit)
                    }
                }
            )
        } else {
            null
        }

    private fun deactivateBiometricsItem(): SettingsItem? =
        if (LocalStorage.isPinConfig && !LocalStorage.shouldSetupPin) {
            SettingsItem(
                title = TextWrapper.Resource(R.string.debug_utilities_deactivate_biometrics),
                subtitle = TextWrapper.Resource(R.string.deactivate_biometrics),
                isItemClickable = UnlockMethodType.BIOMETRICS in FuturaeSDK.client.lockApi.getActiveUnlockMethods(),
                actionCallback = {
                    viewModelScope.launch {
                        try {
                            deactivateBiometricsUseCase()
                            val snachbarUIState = FuturaeSnackbarUIState.Success(TextWrapper.Resource(R.string.biometrics_deactivated))
                            _snackbarUIState.emit(snachbarUIState)
                            refreshItems()
                        } catch (e: Exception) {
                            val snachbarUIState = FuturaeSnackbarUIState.Error(TextWrapper.Primitive(e.message ?: "Unknown Error"))
                            _snackbarUIState.emit(snachbarUIState)
                        }
                    }
                }
            )
        } else {
            null
        }
}