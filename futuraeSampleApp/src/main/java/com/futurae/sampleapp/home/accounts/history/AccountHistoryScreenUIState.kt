package com.futurae.sampleapp.home.accounts.history

import com.futurae.sampleapp.utils.ILCEState
import com.futurae.sampleapp.ui.TextWrapper
import com.futurae.sampleapp.ui.shared.elements.serviceinfosection.ServiceInfoSectionUIState

data class AccountHistoryScreenUIState(
    val serviceInfoSectionUIState: ServiceInfoSectionUIState,
    val details: ILCEState<List<AccountHistoryItemUIState>>
)

data class AccountHistoryItemUIState(
    val isSuccessful: Boolean,
    val description: Pair<TextWrapper, TextWrapper>,
    val date: String
)
