package com.futurae.demoapp.ui.shared.elements.snackbar

import com.futurae.demoapp.ui.TextWrapper

sealed class FuturaeSnackbarUIState(open val message: TextWrapper) {
    data class Success(override val message: TextWrapper) : FuturaeSnackbarUIState(message)
    data class Error(override val message: TextWrapper) : FuturaeSnackbarUIState(message)
}