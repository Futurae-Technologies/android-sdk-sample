package com.futurae.demoapp.ui.shared.elements.topappbar

import androidx.annotation.StringRes
import com.futurae.demoapp.ui.TextWrapper
import com.futurae.demoapp.ui.shared.elements.resultinformativescreen.ResultState
import com.futurae.demoapp.ui.shared.elements.serviceinfosection.ServiceInfoSectionUIState

sealed class FuturaeTopAppBarUIState {
    data object FuturaeMore : FuturaeTopAppBarUIState()

    data object AccountPicker : FuturaeTopAppBarUIState()

    data class AccountHistory(
        val serviceInfoSectionUIState: ServiceInfoSectionUIState
    ) : FuturaeTopAppBarUIState()

    data class CommonTopBar(
        @StringRes val titleResId: Int,
        val hasBackNavigation: Boolean = false
    ) : FuturaeTopAppBarUIState()

    data class ResultTopBar(
        val state: ResultState,
        val label: TextWrapper
    ) : FuturaeTopAppBarUIState()

    data object None : FuturaeTopAppBarUIState()
}