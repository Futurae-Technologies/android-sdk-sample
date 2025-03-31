package com.futurae.demoapp.ui.shared.elements.resultinformativescreen

import com.futurae.demoapp.ui.TextWrapper
import com.futurae.demoapp.ui.shared.elements.serviceinfosection.ServiceInfoSectionUIState

data class ResultInformativeComposableUIState(
    val resultInformativeScreenUIState: ResultInformativeScreenUIState,
    val contentUIState: ResultInformativeScreenContentUIState
)

sealed class ResultInformativeScreenContentUIState {
    data class NewAccountEnrolled(
        val serviceInfoSectionUIState: ServiceInfoSectionUIState
    ): ResultInformativeScreenContentUIState()

    data class VerificationCodeReceived(val code: String) : ResultInformativeScreenContentUIState()

    data class Informative(
        val title: TextWrapper,
        val description: TextWrapper,
        val secondaryInfo: TextWrapper? = null
    ) : ResultInformativeScreenContentUIState()
}