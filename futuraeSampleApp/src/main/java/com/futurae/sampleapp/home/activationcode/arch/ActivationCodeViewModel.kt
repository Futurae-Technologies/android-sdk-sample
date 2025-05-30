package com.futurae.sampleapp.home.activationcode.arch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.futurae.sampleapp.enrollment.EnrollmentCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ActivationCodeViewModel : ViewModel() {

    companion object {
        fun provideFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T = ActivationCodeViewModel() as T
        }
    }

    private val _activationCode = MutableStateFlow("")
    val activationCode = _activationCode.asStateFlow()

    private val _onEnrollmentFlowRequested = MutableSharedFlow<EnrollmentCase.ManualEntry>()
    val onEnrollmentFlowRequest: SharedFlow<EnrollmentCase.ManualEntry> = _onEnrollmentFlowRequested

    fun submitCode() {
        viewModelScope.launch {
            _onEnrollmentFlowRequested.emit(
                EnrollmentCase.ManualEntry(_activationCode.value.replace(" ", ""))
            )
        }
    }

    fun onCodeChange(code: String) {
        viewModelScope.launch {
            _activationCode.emit(code)
        }
    }
}