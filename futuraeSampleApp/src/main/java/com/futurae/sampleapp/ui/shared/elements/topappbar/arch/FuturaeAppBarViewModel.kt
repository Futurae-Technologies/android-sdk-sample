package com.futurae.sampleapp.ui.shared.elements.topappbar.arch

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futurae.sampleapp.ui.TextWrapper
import com.futurae.sampleapp.ui.shared.elements.resultinformativescreen.ResultState
import com.futurae.sampleapp.ui.shared.elements.serviceinfosection.ServiceInfoSectionUIState
import com.futurae.sampleapp.ui.shared.elements.topappbar.FuturaeTopAppBarUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FuturaeAppBarViewModel : ViewModel() {

    private val _topAppBarState =
        MutableStateFlow<FuturaeTopAppBarUIState>(FuturaeTopAppBarUIState.None)
    val topAppBarUIState = _topAppBarState.asStateFlow()

    fun showCommonTopBar(
        @StringRes titleResId: Int,
        hasBackNavigation: Boolean
    ) {
        updateTopAppBarState(
            state = FuturaeTopAppBarUIState.CommonTopBar(
                titleResId = titleResId,
                hasBackNavigation = hasBackNavigation
            )
        )
    }

    fun showMoreTopBar() {
        updateTopAppBarState(state = FuturaeTopAppBarUIState.FuturaeMore)
    }

    fun showResultTopBar(state: ResultState, label: TextWrapper) {
        updateTopAppBarState(
            state = FuturaeTopAppBarUIState.ResultTopBar(
                state = state,
                label = label
            )
        )
    }

    fun showAccountPickerTopAppBar() {
        updateTopAppBarState(state = FuturaeTopAppBarUIState.AccountPicker)
    }

    fun hideTopAppBar() {
        updateTopAppBarState(state = FuturaeTopAppBarUIState.None)
    }

    private fun updateTopAppBarState(state: FuturaeTopAppBarUIState) {
        viewModelScope.launch {
            _topAppBarState.update { state }
        }
    }

    fun showAccountHistoryTopAppBar(serviceInfoSectionUIState: ServiceInfoSectionUIState) {
        updateTopAppBarState(
            state = FuturaeTopAppBarUIState.AccountHistory(
                serviceInfoSectionUIState = serviceInfoSectionUIState
            )
        )
    }
}