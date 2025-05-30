package com.futurae.demoapp.home.accounts.history.arch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.futurae.demoapp.utils.ILCEState
import com.futurae.demoapp.R
import com.futurae.demoapp.home.accounts.history.AccountHistoryItemUIState
import com.futurae.demoapp.home.accounts.history.AccountHistoryScreenUIState
import com.futurae.demoapp.home.accounts.history.usecase.GetAccountForUserIdUseCase
import com.futurae.demoapp.home.accounts.history.usecase.GetAccountHistoryUseCase
import com.futurae.demoapp.ui.TextWrapper
import com.futurae.demoapp.ui.shared.elements.serviceinfosection.ServiceInfoSectionUIState
import com.futurae.demoapp.utils.fullTimestampFormat
import com.futurae.sdk.public_api.account.model.AccountHistoryItem
import com.futurae.sdk.public_api.common.model.FTAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AccountHistoryViewModel(
    private val userId: String,
    private val getAccountForUserIdUseCase: GetAccountForUserIdUseCase,
    private val getAccountHistoryUseCase: GetAccountHistoryUseCase
): ViewModel() {

    companion object {
        fun provideFactory(userId: String): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T = AccountHistoryViewModel(
                userId = userId,
                getAccountForUserIdUseCase = GetAccountForUserIdUseCase(),
                getAccountHistoryUseCase = GetAccountHistoryUseCase()
            ) as T
        }
    }

    private val _state = MutableStateFlow(
        AccountHistoryViewModelState(userId)
    )

    val uiState = _state
        .map(AccountHistoryViewModelState::toUIState)
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            _state.value.toUIState()
        )

    init {
        viewModelScope.launch {
            _state.update {
                it.copy(account = getAccountForUserIdUseCase(userId))
            }
        }

        getAccountHistory()
    }

    private fun getAccountHistory() {
        viewModelScope.launch {
            getAccountHistoryUseCase(userId)
                .onSuccess { historyItems ->
                    _state.update {
                        it.copy(history = ILCEState.Content(historyItems))
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(history = ILCEState.Error(error))
                    }
                }
        }
    }

    data class AccountHistoryViewModelState(
        val userId: String,
        val account: FTAccount? = null,
        val history: ILCEState<List<AccountHistoryItem>> = ILCEState.Loading
    ) {
        fun toUIState() = AccountHistoryScreenUIState(
            serviceInfoSectionUIState = ServiceInfoSectionUIState(
                serviceLogo = account?.serviceLogo ?: "",
                serviceName = account?.serviceName,
                username = account?.username
            ),
            details = when (history) {
                is ILCEState.Error -> ILCEState.Error(history.throwable)
                is ILCEState.Content -> {
                    ILCEState.Content(
                        history.data.map {
                            AccountHistoryItemUIState(
                                isSuccessful = AuthenticationResult
                                    .from(it.details.result) == AuthenticationResult.ALLOW,
                                description = it.description,
                                date = it.timestamp.fullTimestampFormat()
                            )
                        }
                    )
                }
                else -> ILCEState.Loading
            }
        )

        private val AccountHistoryItem.description: Pair<TextWrapper, TextWrapper>
            get() {
                val type = AuthenticationType.from(details.type)
                val result = AuthenticationResult.from(details.result)
                val firstPartStringResId = when (type) {
                    AuthenticationType.LOGIN -> R.string.login_type
                    AuthenticationType.TRANSACTION -> R.string.transaction_type
                    AuthenticationType.UNKNOWN -> R.string.empty
                }
                val secondPartStringResId = when (result) {
                    AuthenticationResult.ALLOW -> R.string.approved
                    AuthenticationResult.DENY -> R.string.denied
                    AuthenticationResult.UNKNOWN -> R.string.empty
                }

                return TextWrapper.Resource(firstPartStringResId) to
                        TextWrapper.Resource(secondPartStringResId)
            }

        private enum class AuthenticationType(val rawValue: String) {
            LOGIN("Login"),
            TRANSACTION("Transaction"),
            UNKNOWN("-");

            companion object {
                fun from(value: String?) = when (value) {
                    LOGIN.rawValue -> LOGIN
                    TRANSACTION.rawValue -> LOGIN
                    else -> UNKNOWN
                }
            }
        }

        private enum class AuthenticationResult(val rawValue: String) {
            ALLOW("allow"),
            DENY("deny"),
            UNKNOWN("-");

            companion object {
                fun from(value: String?) = when (value) {
                    ALLOW.rawValue -> ALLOW
                    DENY.rawValue -> DENY
                    else -> UNKNOWN
                }
            }
        }
    }
}