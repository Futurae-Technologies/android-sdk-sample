package com.futurae.sampleapp.settings.geofencing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futurae.sampleapp.R
import com.futurae.sampleapp.settings.SettingsToggle
import com.futurae.sampleapp.ui.TextWrapper
import com.futurae.sdk.FuturaeSDK
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GeofencingSettingsViewModel : ViewModel() {

    private val _state = MutableStateFlow(generateSettingsItems())
    val state = _state.asStateFlow()

    private fun generateSettingsItems() = listOf(
        SettingsToggle(
            title = TextWrapper.Resource(R.string.geofencing),
            subtitle = TextWrapper.Resource(R.string.geofencing_subtitle),
            isEnabled = FuturaeSDK.client.geofencingApi.isLocationCollectionEnabled,
            onToggleChanged = {
                FuturaeSDK.client.geofencingApi.enableOrDisableLocationCollection(it)
                reEvaluateState()
            }
        )
    )

    private fun reEvaluateState() {
        viewModelScope.launch {
            _state.emit(
                generateSettingsItems()
            )
        }
    }
}