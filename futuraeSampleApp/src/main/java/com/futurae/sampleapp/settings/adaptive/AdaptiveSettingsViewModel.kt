package com.futurae.sampleapp.settings.adaptive

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.futurae.sampleapp.FuturaeSampleApplication
import com.futurae.sampleapp.navigation.FuturaeSampleDestinations
import com.futurae.sampleapp.utils.LocalStorage
import com.futurae.sampleapp.R
import com.futurae.sampleapp.settings.SettingsListItem
import com.futurae.sampleapp.settings.SettingsNestedToggle
import com.futurae.sampleapp.settings.SettingsNestedToggleGroup
import com.futurae.sampleapp.ui.TextWrapper
import com.futurae.sdk.FuturaeSDK
import com.futurae.sdk.adaptive.AdaptiveSDK
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AdaptiveSettingsViewModel(application: FuturaeSampleApplication) :
    AndroidViewModel(application) {

    companion object {
        fun provideFactory(
            application: FuturaeSampleApplication
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T = AdaptiveSettingsViewModel(application) as T
        }
    }

    private val _state = MutableStateFlow(
        AdaptiveSettingsUIState(
            items = generateSettingsItems(),
            actions = generateActions(),
            thresholdUIState = ThresholdUIState(isVisible = false, value = 0)
        )
    )
    val state = _state.asStateFlow()

    private val _permissionsNeeded = MutableSharedFlow<Unit>()
    val permissionsNeededFlow = _permissionsNeeded.asSharedFlow()

    private val _navigationEvent = MutableSharedFlow<String>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    init {
        if (LocalStorage.isAdaptiveEnabled()) {
            viewModelScope.launch {
                _permissionsNeeded.emit(Unit)
            }
        }
    }

    fun onThresholdUpdate(value: Int) {
        AdaptiveSDK.setAdaptiveCollectionThreshold(value)
        onDismissThresholdDialog()
    }

    fun onDismissThresholdDialog() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    thresholdUIState = ThresholdUIState(
                        isVisible = false,
                        value = 0
                    )
                )
            }
        }
    }

    private fun reEvaluateState() {
        viewModelScope.launch {
            _state.emit(
                AdaptiveSettingsUIState(
                    items = generateSettingsItems(),
                    actions = generateActions(),
                    thresholdUIState = ThresholdUIState(isVisible = false, value = 0)
                )
            )
        }
    }

    private fun generateSettingsItems(): List<SettingsListItem> {
        return listOf(
            SettingsNestedToggleGroup(title = TextWrapper.Resource(R.string.adaptive),
                subtitle = TextWrapper.Resource(R.string.adaptive_subtitle),
                isToggled = LocalStorage.isAdaptiveEnabled(),
                onToggleChanged = {
                    LocalStorage.setAdaptiveEnabled(it)

                    if (it) {
                        FuturaeSDK.client.adaptiveApi.enableAdaptive(getApplication())
                        viewModelScope.launch {
                            _permissionsNeeded.emit(Unit)
                        }
                    } else {
                        FuturaeSDK.client.adaptiveApi.disableAdaptive()
                    }

                    reEvaluateState()
                },
                children = listOf(
                    SettingsNestedToggle(
                        title = TextWrapper.Resource(R.string.authentication),
                        isEnabled = LocalStorage.isAdaptiveEnabled(),
                        isToggled = LocalStorage.isAdaptiveAuthEnabled(),
                        onToggleChanged = {
                            LocalStorage.setAdaptiveAuthEnabled(it)
                            if (it) {
                                FuturaeSDK.client.adaptiveApi.enableAdaptiveSubmissionOnAuthentication()
                            } else {
                                FuturaeSDK.client.adaptiveApi.disableAdaptiveSubmissionOnAuthentication()
                            }

                            reEvaluateState()
                        }
                    ),
                    SettingsNestedToggle(
                        title = TextWrapper.Resource(R.string.migration),
                        isEnabled = LocalStorage.isAdaptiveEnabled(),
                        isToggled = LocalStorage.isAdaptiveMigrationEnabled(),
                        onToggleChanged = {
                            LocalStorage.setAdaptiveMigrationEnabled(it)
                            if (it) {
                                FuturaeSDK.client.adaptiveApi.enableAdaptiveSubmissionOnAccountMigration()
                            } else {
                                FuturaeSDK.client.adaptiveApi.disableAdaptiveSubmissionOnAccountMigration()
                            }

                            reEvaluateState()
                        }
                    )
                )
            )
        )
    }

    private fun generateActions(): List<SettingsAction> = if (LocalStorage.isAdaptiveEnabled()) {
        listOf(
            SettingsAction(
                cta = TextWrapper.Resource(R.string.adaptive_set_threshold_cta),
                onClick = {
                    viewModelScope.launch {
                        _state.update {
                            it.copy(
                                thresholdUIState = ThresholdUIState(
                                    isVisible = true,
                                    value = AdaptiveSDK.getAdaptiveCollectionThreshold()
                                )
                            )
                        }
                    }
                }
            ),
            SettingsAction(
                cta = TextWrapper.Resource(R.string.adaptive_view_collections_cta),
                onClick = {
                    viewModelScope.launch {
                        _navigationEvent.emit(FuturaeSampleDestinations.SETTINGS_ADAPTIVE_COLLECTIONS_ROUTE.route)
                    }
                }
            )
        )
    } else {
        emptyList()
    }
}