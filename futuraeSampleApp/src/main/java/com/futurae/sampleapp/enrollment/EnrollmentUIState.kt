package com.futurae.sampleapp.enrollment

import com.futurae.sampleapp.ui.shared.elements.resultinformativescreen.ResultInformativeComposableUIState

sealed class EnrollmentUIState {
    data object Idle : EnrollmentUIState()
    data class FlowBindingTokenInput(val token: String) : EnrollmentUIState()
    data class Result(
        val uiState: ResultInformativeComposableUIState,
        val shouldPromptUserToEnableBiometrics: Boolean = false
    ) : EnrollmentUIState()
}