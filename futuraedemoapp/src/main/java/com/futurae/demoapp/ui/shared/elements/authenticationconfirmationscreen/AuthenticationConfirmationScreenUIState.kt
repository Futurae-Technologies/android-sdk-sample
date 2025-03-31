package com.futurae.demoapp.ui.shared.elements.authenticationconfirmationscreen

import com.futurae.demoapp.ui.TextWrapper
import com.futurae.demoapp.ui.shared.elements.serviceinfosection.ServiceInfoSectionUIState

sealed class AuthenticationConfirmationComposableScreenUIState {

    data object Loading: AuthenticationConfirmationComposableScreenUIState()

    data class AuthenticationConfirmationScreenUIState(
        val serviceInfoSectionUIState: ServiceInfoSectionUIState,
        val authenticationType: TextWrapper,
        val content: AuthenticationScreenContent,
        val timeoutInSeconds: Int
    ): AuthenticationConfirmationComposableScreenUIState()
}

sealed class AuthenticationScreenContent {
    data class Details(val details: List<InfoItemUIState>): AuthenticationScreenContent()
    data class MultiNumberedChallenge(val options: List<Int>): AuthenticationScreenContent()
}


data class InfoItemUIState(
    val label: TextWrapper,
    val value: TextWrapper
)