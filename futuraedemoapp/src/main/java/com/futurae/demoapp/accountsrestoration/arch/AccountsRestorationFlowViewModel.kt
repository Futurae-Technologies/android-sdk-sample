package com.futurae.demoapp.accountsrestoration.arch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.futurae.demoapp.ILCEState
import com.futurae.demoapp.LocalStorage
import com.futurae.demoapp.lock.LockScreenMode
import com.futurae.demoapp.migration.usecase.MigrateAccountsUseCase
import com.futurae.sdk.public_api.migration.model.MigrationUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AccountsRestorationFlowViewModel(
    private val isPinProtected: Boolean,
    private val migrateAccountsUseCase: MigrateAccountsUseCase
) : ViewModel() {

    companion object {
        fun provideFactory(
            isPinProtected: Boolean
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T = AccountsRestorationFlowViewModel(
                isPinProtected = isPinProtected,
                migrateAccountsUseCase = MigrateAccountsUseCase()
            ) as T
        }
    }

    private val _state = MutableStateFlow<ILCEState<Unit>>(ILCEState.Idle)
    val state = _state.asStateFlow()

    private val _pinRequested = MutableSharedFlow<LockScreenMode>()
    val pinRequestFlow = _pinRequested.asSharedFlow()

    fun attemptToRestoreAccounts() {
        val requiresPinProtection = isPinProtected || LocalStorage.isPinConfig
        if (requiresPinProtection) {
            requestSDKPin()
        } else {
            restoreAccounts(migrationUseCase = MigrationUseCase.AccountsNotSecuredWithPinCode)
        }
    }

    private fun requestSDKPin() {
        val lockScreenMode = if (isPinProtected) {
            LockScreenMode.GET_PIN
        } else {
            LockScreenMode.CREATE_PIN
        }

        viewModelScope.launch {
            _pinRequested.emit(lockScreenMode)
        }
    }

    private fun restoreAccounts(migrationUseCase: MigrationUseCase) {
        viewModelScope.launch {
            _state.emit(ILCEState.Loading)

            migrateAccountsUseCase(migrationUseCase, null)
                .onSuccess {
                    _state.emit(ILCEState.Content(Unit))
                }
                .onFailure {
                    _state.emit(ILCEState.Error(it))
                }
        }
    }

    fun onPinProvided(digits: CharArray?) {
        if (digits != null) {
            restoreAccounts(migrationUseCase = MigrationUseCase.AccountsSecuredWithPinCode(digits))
        }
    }
}