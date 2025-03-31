package com.futurae.demoapp.home.accounts.history

import com.futurae.demoapp.ILCEState
import com.futurae.demoapp.ui.TextWrapper
import com.futurae.demoapp.ui.shared.elements.serviceinfosection.ServiceInfoSectionUIState

data class AccountHistoryScreenUIState(
    val serviceInfoSectionUIState: ServiceInfoSectionUIState,
    val details: ILCEState<List<AccountHistoryItemUIState>>
)

data class AccountHistoryItemUIState(
    val isSuccessful: Boolean,
    val description: Pair<TextWrapper, TextWrapper>,
    val date: String
)
