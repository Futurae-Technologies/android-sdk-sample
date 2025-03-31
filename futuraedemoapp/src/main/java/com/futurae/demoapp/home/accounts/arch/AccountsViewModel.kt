package com.futurae.demoapp.home.accounts.arch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.futurae.demoapp.ILCEState
import com.futurae.demoapp.LocalStorage
import com.futurae.demoapp.home.accounts.AccountRowUIState
import com.futurae.demoapp.home.accounts.AccountsScreenUIState
import com.futurae.demoapp.home.accounts.restoreaccountsbanner.RestoreAccountsBannerUIState
import com.futurae.demoapp.home.usecase.GetTOTPUseCase
import com.futurae.demoapp.ui.shared.elements.timeoutIndicator.startCountdown
import com.futurae.sdk.FuturaeSDK
import com.futurae.sdk.public_api.auth.model.SDKAuthMode
import com.futurae.sdk.public_api.auth.model.TOTP
import com.futurae.sdk.public_api.common.FuturaeSDKStatus
import com.futurae.sdk.public_api.common.model.FTAccount
import com.futurae.sdk.public_api.exception.FTAccountMigrationNoMigrationInfoException
import com.futurae.sdk.public_api.migration.model.MigratableAccounts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AccountsViewModel(val getTOTPUseCase: GetTOTPUseCase) : ViewModel() {

    companion object {
        fun provideFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T = AccountsViewModel(getTOTPUseCase = GetTOTPUseCase()) as T
        }
    }

    private var countdownJob: Job? = null

    private val viewModelState = MutableStateFlow(
        HomeViewModelState(
            accounts = emptyList(),
            codes = emptyMap()
        )
    )

    val uiState = viewModelState
        .map(HomeViewModelState::toUIState)
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            viewModelState.value.toUIState()
        )

    private var _timeoutCountdownProgress = MutableStateFlow(0f)
    val timeoutCountdownProgress = _timeoutCountdownProgress.asStateFlow()

    private val _onHOTPGenerated = MutableSharedFlow<String>()
    val onHOTPGenerated = _onHOTPGenerated.asSharedFlow()

    private val _restorationBannerUIState = MutableStateFlow<RestoreAccountsBannerUIState>(RestoreAccountsBannerUIState.None)
    val restorationBannerUIState = _restorationBannerUIState.asStateFlow()

    init {
        viewModelScope.launch {
            FuturaeSDK.client.accountApi.activeAccountsFlow.collect { newAccounts ->
                viewModelState.update {
                    it.copy(accounts = newAccounts)
                }

                if (newAccounts.isEmpty()) return@collect

                getTotps()
            }
        }

        viewModelScope.launch {
            FuturaeSDK.sdkState()
                .distinctUntilChangedBy { it.status }
                .collect {
                    when (it.status) {
                        is FuturaeSDKStatus.Unlocked -> {
                            getTotps()
                        }
                        FuturaeSDKStatus.Locked -> {
                            countdownJob?.cancel()
                        }
                        else -> {
                            // do nothing
                        }
                    }
                }
        }
    }

    fun getTotps() {
        viewModelScope.launch {
            val totps = mutableMapOf<String, TOTP>()
            viewModelState.value.accounts
                .filter { !it.lockedOut }
                .forEach {
                    getTOTPUseCase(
                        it.userId,
                        SDKAuthMode.Unlock
                    ).onSuccess { totp ->
                        totps[it.userId] = totp
                    }
                }

            viewModelState.update {
                it.copy(
                    codes = totps.mapValues { totp -> totp.value.passcode }
                )
            }

            if (totps.isEmpty()) {
                countdownJob?.cancel()
                _timeoutCountdownProgress.value = 1f
                return@launch
            }

            countdownJob?.cancel()
            countdownJob = startCountdown(
                timeoutInSeconds = totps.minOf { totp -> totp.value.remainingSeconds },
                onProgressUpdate = { _timeoutCountdownProgress.value = it },
                onTimeout = { getTotps() }
            )
        }
    }

    fun getHOTP(userId: String) {
        val hotp = FuturaeSDK.client.authApi.getNextSynchronousAuthToken(userId)
        viewModelScope.launch {
            _onHOTPGenerated.emit(hotp)
        }
    }

    fun deleteAccount(userId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                FuturaeSDK.client.accountApi.logoutAccount(userId).await()
            }
        }
    }

    private fun ILCEState<MigratableAccounts>.toRestorationBannerUIState(): RestoreAccountsBannerUIState {
        if (FuturaeSDK.client.accountApi.getActiveAccounts().isNotEmpty() ||
            LocalStorage.hasUserBeenInformedAboutAccountRestoration ||
            LocalStorage.isDeviceEnrolled()
        ) {
            return RestoreAccountsBannerUIState.None
        }

        return when {
            this is ILCEState.Content && data.migratableAccountInfos.isNotEmpty() -> {
                RestoreAccountsBannerUIState.SuccessfulCheck(isPinProtected = data.pinProtected)
            }
            this is ILCEState.Error && throwable !is FTAccountMigrationNoMigrationInfoException -> {
                RestoreAccountsBannerUIState.FailedCheck
            }
            else -> {
                RestoreAccountsBannerUIState.None
            }
        }
    }

    fun onMigrationInfoChanges(state: ILCEState<MigratableAccounts>) {
        viewModelScope.launch {
            _restorationBannerUIState.emit(state.toRestorationBannerUIState())
        }
    }

    fun userHasBeenInformed() {
        LocalStorage.setUserHasBeenInformedAboutAccountRestoration()
        viewModelScope.launch {
            _restorationBannerUIState.emit(RestoreAccountsBannerUIState.None)
        }
    }

    fun accountsRestorationBannerDismissed() {
        viewModelScope.launch {
            _restorationBannerUIState.emit(RestoreAccountsBannerUIState.InformativeForSettingsEntryPoint)
         }

        viewModelScope.launch {
            delay(5000)
            userHasBeenInformed()
        }
    }

    private data class HomeViewModelState(
        val accounts: List<FTAccount>,
        val codes: Map<String, String>
    ) {
        fun toUIState() = AccountsScreenUIState(
            accountRowUIStates = accounts.map {
                AccountRowUIState(
                    account = it,
                    code = codes[it.userId] ?: "-"
                )
            }
        )
    }
}