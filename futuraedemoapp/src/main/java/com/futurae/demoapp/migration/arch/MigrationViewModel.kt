package com.futurae.demoapp.migration.arch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.futurae.demoapp.ILCEState
import com.futurae.demoapp.migration.usecase.GetMigratableAccountsUseCase
import com.futurae.sdk.public_api.migration.model.MigratableAccounts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MigrationViewModel(
    private val getMigratableUseCase: GetMigratableAccountsUseCase
) : ViewModel() {

    companion object {
        fun provideFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T = MigrationViewModel(getMigratableUseCase = GetMigratableAccountsUseCase()) as T
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