package com.futurae.sampleapp.home.accounts.arch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.futurae.sampleapp.utils.ILCEState
import com.futurae.sampleapp.utils.LocalStorage
import com.futurae.sampleapp.home.accounts.AccountRowUIState
import com.futurae.sampleapp.home.accounts.AccountsScreenUIState
import com.futurae.sampleapp.home.accounts.restoreaccountsbanner.RestoreAccountsBannerUIState
import com.futurae.sampleapp.home.accounts.usecase.GetEnrollmentExchangeTokensUseCase
import com.futurae.sampleapp.home.accounts.usecase.GetTOTPUseCase
import com.futurae.sampleapp.home.accounts.usecase.InitiateApp2AppEnrollmentUseCase
import com.futurae.sdk.public_api.operations.model.EnrollmentExchangeTokenQrCode
import com.futurae.sampleapp.ui.shared.elements.timeoutIndicator.startCountdown
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
import timber.log.Timber

class AccountsViewModel(
    val getTOTPUseCase: GetTOTPUseCase,
    val initiateApp2AppEnrollmentUseCase: InitiateApp2AppEnrollmentUseCase,
    val getEnrollmentExchangeTokensUseCase: GetEnrollmentExchangeTokensUseCase,
) : ViewModel() {

    companion object {
        fun provideFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T = AccountsViewModel(
                getTOTPUseCase = GetTOTPUseCase(),
                initiateApp2AppEnrollmentUseCase = InitiateApp2AppEnrollmentUseCase(),
                getEnrollmentExchangeTokensUseCase = GetEnrollmentExchangeTokensUseCase(),
            ) as T
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

    private val _onApp2AppEnrollmentInitiated = MutableSharedFlow<String>()
    val onApp2AppEnrollmentInitiated = _onApp2AppEnrollmentInitiated.asSharedFlow()

    private val _enrollmentQrCode = MutableStateFlow<EnrollmentExchangeTokenQrCode?>(null)
    val enrollmentQrCode = _enrollmentQrCode.asStateFlow()

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

    private fun getTotps() {
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

    fun initiateApp2AppEnrollment(userId: String) {
        viewModelScope.launch {
            initiateApp2AppEnrollmentUseCase(userId)
                .onSuccess { enrollmentInfo ->
                    _onApp2AppEnrollmentInitiated.emit(enrollmentInfo.toString())
                    getEnrollmentExchangeTokensUseCase(
                        userId = userId,
                        activationCodeShort = enrollmentInfo.activationCodeShort,
                    ).onSuccess { tokens ->
                        Timber.d("Enrollment exchange tokens count: ${tokens.size}")
                        _enrollmentQrCode.value = tokens.getOrNull(3)
                            .also { if (it == null) Timber.w("4th QR code not available, list size: ${tokens.size}") }
                    }
                }
                .onFailure {
                    Timber.d("Enrollment failure")
                }
        }
    }

    fun dismissEnrollmentQrCode() {
        _enrollmentQrCode.value = null
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