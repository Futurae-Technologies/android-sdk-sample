package com.futurae.demoapp.arch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class PinProviderViewModel : ViewModel() {
    private val _pin = MutableSharedFlow<CharArray?>(replay = 1)
    val pinFlow = _pin.filterNotNull()

    fun setResult(pin: CharArray?) {
        viewModelScope.launch {
            _pin.emit(pin)
        }
    }

    fun reset() {
        setResult(null)
    }
}

