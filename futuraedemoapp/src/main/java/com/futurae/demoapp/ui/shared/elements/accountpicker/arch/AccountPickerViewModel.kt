package com.futurae.demoapp.ui.shared.elements.accountpicker.arch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.futurae.demoapp.ui.shared.elements.accountpicker.AccountPickerUIState
import com.futurae.demoapp.ui.shared.elements.accountpicker.ActiveAccountItemUIState
import com.futurae.demoapp.ui.shared.elements.accountpicker.usecase.GetActiveAccountsUseCase
import com.futurae.sdk.public_api.common.model.FTAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AccountPickerViewModel(
    val getActiveAccountsUseCase: GetActiveAccountsUseCase
) : ViewModel() {

    companion object {
        fun provideFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T = AccountPickerViewModel(
                getActiveAccountsUseCase = GetActiveAccountsUseCase()
            ) as T
        }
    }

    private val viewModelState = MutableStateFlow(AccountPickerState())

    val uiState = viewModelState
        .map(AccountPickerState::toUIState)
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            viewModelState.value.toUIState()
        )

    init {
        viewModelScope.launch {
            viewModelState.emit(
                AccountPickerState(
                    availableAccounts = getActiveAccountsUseCase(),
                    selectedAccount = null
                )
            )
        }
    }

    fun onAccountSelected(account: FTAccount) {
        viewModelScope.launch {
            viewModelState.update {
                it.copy(selectedAccount = account)
            }
        }
    }

    data class AccountPickerState(
        val availableAccounts : List<FTAccount> = emptyList(),
        val selectedAccount : FTAccount? = null
    ) {

        fun toUIState() = AccountPickerUIState(
            items = availableAccounts.map {
                ActiveAccountItemUIState(account = it, isSelected = it == selectedAccount)
            },
            selectedAccount = selectedAccount
        )
    }
}