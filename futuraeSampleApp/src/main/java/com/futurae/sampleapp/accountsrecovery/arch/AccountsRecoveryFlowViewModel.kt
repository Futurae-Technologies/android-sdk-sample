package com.futurae.sampleapp.accountsrecovery.arch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.futurae.sampleapp.utils.ILCEState
import com.futurae.sampleapp.utils.LocalStorage
import com.futurae.sampleapp.lock.arch.LockScreenMode
import com.futurae.sampleapp.accountsrecovery.usecase.MigrateAccountsUseCase
import com.futurae.sdk.FuturaeSDK
import com.futurae.sdk.public_api.migration.model.MigrationUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AccountsRecoveryFlowViewModel(
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
            ): T = AccountsRecoveryFlowViewModel(
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
                    LocalStorage.setDeviceEnrolled()
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