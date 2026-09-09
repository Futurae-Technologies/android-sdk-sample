package com.futurae.sampleapp.settings.geofencing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futurae.sdk.FuturaeSDK
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GeofencingSettingsState(
    val isAuthGeofencingEnabled: Boolean,
    val isContinuousGeofencingEnabled: Boolean,
)

class GeofencingSettingsViewModel : ViewModel() {

    private val _state = MutableStateFlow(buildState())
    val state = _state.asStateFlow()

    private fun buildState() = GeofencingSettingsState(
        isAuthGeofencingEnabled = FuturaeSDK.client.geofencingApi.isLocationCollectionOnAuthEnabled,
        isContinuousGeofencingEnabled = FuturaeSDK.client.geofencingApi.isBackgroundLocationCollectionEnabled,
    )

    private fun refresh() {
        viewModelScope.launch { _state.emit(buildState()) }
    }

    fun enableAuthGeofencing() {
        FuturaeSDK.client.geofencingApi.enableLocationCollectionOnAuth()
        refresh()
    }

    fun disableAuthGeofencing() {
        FuturaeSDK.client.geofencingApi.disableLocationCollectionOnAuth()
        refresh()
    }

    fun enableContinuousGeofencing() {
        FuturaeSDK.client.geofencingApi.enableBackgroundLocationCollection()
        refresh()
    }

    fun disableContinuousGeofencing() {
        FuturaeSDK.client.geofencingApi.disableBackgroundLocationCollection()
        refresh()
    }
}
