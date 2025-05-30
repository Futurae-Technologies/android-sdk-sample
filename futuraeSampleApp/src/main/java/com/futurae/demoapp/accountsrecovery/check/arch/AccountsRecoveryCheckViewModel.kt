package com.futurae.demoapp.accountsrecovery.check.arch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.futurae.demoapp.utils.ILCEState
import com.futurae.demoapp.accountsrecovery.check.usecase.GetMigratableAccountsUseCase
import com.futurae.sdk.public_api.migration.model.MigratableAccounts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AccountsRecoveryCheckViewModel(
    private val getMigratableUseCase: GetMigratableAccountsUseCase
) : ViewModel() {

    companion object {
        fun provideFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T = AccountsRecoveryCheckViewModel(getMigratableUseCase = GetMigratableAccountsUseCase()) as T
        }
    }

    private val _migrationInfo = MutableStateFlow<ILCEState<MigratableAccounts>>(ILCEState.Idle)
    val migrationInfo = _migrationInfo.asStateFlow()

    fun checkForMigratableAccounts() {
        viewModelScope.launch {
            _migrationInfo.emit(ILCEState.Loading)

            getMigratableUseCase()
                .onSuccess { _migrationInfo.emit(ILCEState.Content(it)) }
                .onFailure { _migrationInfo.emit(ILCEState.Error(it)) }
        }
    }
}