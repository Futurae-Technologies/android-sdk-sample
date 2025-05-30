package com.futurae.sampleapp.settings.integrity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.futurae.sampleapp.utils.ILCEState
import com.futurae.sampleapp.R
import com.futurae.sampleapp.ui.TextWrapper
import com.futurae.sdk.public_api.operations.model.AppVerdict
import com.futurae.sdk.public_api.operations.model.DeviceVerdict
import com.futurae.sdk.public_api.operations.model.IntegrityResult
import com.futurae.sdk.public_api.operations.model.LicenseVerdict
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class IntegritySettingsViewModel(
    private val integrityCheckUseCase: IntegrityCheckUseCase,
) : ViewModel() {

    companion object {
        fun provideFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T = IntegritySettingsViewModel(integrityCheckUseCase = IntegrityCheckUseCase()) as T
        }
    }


    private val _integrityVerdict = MutableSharedFlow<ILCEState<List<IntegrityResultUIItem>>>()
    val integrityVerdict = _integrityVerdict.asSharedFlow()

    init {
        viewModelScope.launch {
            _integrityVerdict.emit(ILCEState.Loading)
            integrityCheckUseCase()
                .onSuccess {
                    _integrityVerdict.emit(ILCEState.Content(it.toUiState()))
                }
                .onFailure {
                    _integrityVerdict.emit(ILCEState.Error(it))
                }
        }
    }
}

private fun IntegrityResult.toUiState(): List<IntegrityResultUIItem> {
    val deviceVerdict = when(deviceVerdicts.first()) {
        DeviceVerdict.MEETS_NO_INTEGRITY -> IntegrityLevel.NONE
        DeviceVerdict.MEETS_DEVICE_INTEGRITY -> IntegrityLevel.BASIC
        DeviceVerdict.MEETS_BASIC_INTEGRITY -> IntegrityLevel.BASIC
        DeviceVerdict.MEETS_STRONG_INTEGRITY -> IntegrityLevel.STRONG
        DeviceVerdict.MEETS_VIRTUAL_INTEGRITY -> IntegrityLevel.BASIC
    }

    val (appVerdictLevel, appVerdictInformative, appVerdictExplanation) = when(appVerdict) {
        AppVerdict.PLAY_RECOGNIZED -> Triple(
            IntegrityLevel.STRONG,
            TextWrapper.Resource(R.string.app_verdict_strong_info),
            TextWrapper.Resource(R.string.app_verdict_strong_explanation)
        )
        AppVerdict.UNRECOGNIZED_VERSION -> Triple(
            IntegrityLevel.WEAK,
            TextWrapper.Resource(R.string.app_verdict_weak_info),
            TextWrapper.Resource(R.string.app_verdict_weak_explanation)
        )
        AppVerdict.UNEVALUATED -> Triple(
            IntegrityLevel.NONE,
            TextWrapper.Resource(R.string.app_verdict_none_info),
            TextWrapper.Resource(R.string.app_verdict_weak_explanation)
        )
    }

    val (licenseVerdictLevel, licenseVerdictInformative, licenseVerdictExplanation) =
        when(licenseVerdict) {
            LicenseVerdict.LICENSED -> Triple(
                IntegrityLevel.STRONG,
                TextWrapper.Resource(R.string.license_verdict_strong_info),
                TextWrapper.Resource(R.string.license_verdict_strong_explanation)
            )
            LicenseVerdict.UNLICENSED -> Triple(
                IntegrityLevel.WEAK,
                TextWrapper.Resource(R.string.license_verdict_weak_info),
                TextWrapper.Resource(R.string.license_verdict_weak_explanation)
            )
            LicenseVerdict.UNEVALUATED -> Triple(
                IntegrityLevel.NONE,
                TextWrapper.Resource(R.string.license_verdict_none_info),
                TextWrapper.Resource(R.string.license_verdict_weak_explanation)
            )
        }

    return listOf(
        IntegrityResultUIItem(
            title = TextWrapper.Resource(R.string.integrity_device),
            level = deviceVerdict,
            element = PresentationElement.BAR,
            informativeText = TextWrapper.Resource(deviceVerdict.toStringRes())
        ),
        IntegrityResultUIItem(
            title = TextWrapper.Resource(R.string.integrity_app),
            level = appVerdictLevel,
            element = PresentationElement.GRAPHIC,
            informativeText = appVerdictInformative,
            explanationText = appVerdictExplanation
        ),
        IntegrityResultUIItem(
            title = TextWrapper.Resource(R.string.integrity_license),
            level = licenseVerdictLevel,
            element = PresentationElement.GRAPHIC,
            informativeText = licenseVerdictInformative,
            explanationText = licenseVerdictExplanation
        )
    )
}

