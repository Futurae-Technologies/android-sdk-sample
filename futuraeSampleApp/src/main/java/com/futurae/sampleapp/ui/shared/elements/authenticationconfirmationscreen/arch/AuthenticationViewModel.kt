package com.futurae.sampleapp.ui.shared.elements.authenticationconfirmationscreen.arch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.futurae.sampleapp.R
import com.futurae.sampleapp.arch.AuthRequestData
import com.futurae.sampleapp.ui.shared.elements.authenticationconfirmationscreen.usecase.ApproveAuthRequestUserCase
import com.futurae.sampleapp.ui.shared.elements.authenticationconfirmationscreen.usecase.GetApproveSessionUseCase
import com.futurae.sampleapp.ui.shared.elements.authenticationconfirmationscreen.usecase.GetOfflineQRVerificationCodeUseCase
import com.futurae.sampleapp.ui.shared.elements.authenticationconfirmationscreen.usecase.RejectAuthRequestUseCase
import com.futurae.sampleapp.ui.TextWrapper
import com.futurae.sampleapp.ui.shared.elements.authenticationconfirmationscreen.AuthenticationConfirmationComposableScreenUIState
import com.futurae.sampleapp.ui.shared.elements.authenticationconfirmationscreen.AuthenticationConfirmationUserResponse
import com.futurae.sampleapp.ui.shared.elements.authenticationconfirmationscreen.AuthenticationScreenContent
import com.futurae.sampleapp.ui.shared.elements.authenticationconfirmationscreen.InfoItemUIState
import com.futurae.sampleapp.ui.shared.elements.serviceinfosection.ServiceInfoSectionUIState
import com.futurae.sampleapp.ui.shared.elements.snackbar.FuturaeSnackbarUIState
import com.futurae.sampleapp.ui.shared.elements.timeoutIndicator.startCountdown
import com.futurae.sdk.FuturaeSDK
import com.futurae.sdk.public_api.account.model.AccountQuery
import com.futurae.sdk.public_api.auth.model.SessionId
import com.futurae.sdk.public_api.auth.model.SessionIdentificationOption
import com.futurae.sdk.public_api.common.model.FTAccount
import com.futurae.sdk.public_api.exception.FTException
import com.futurae.sdk.public_api.session.model.ApproveInfo
import com.futurae.sdk.public_api.session.model.ApproveSession
import com.futurae.sdk.public_api.session.model.ById
import com.futurae.sdk.public_api.session.model.SessionInfoQuery
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class AuthenticationViewModel(
    private val getApproveSessionUseCase: GetApproveSessionUseCase,
    private val approveAuthRequestUserCase: ApproveAuthRequestUserCase,
    private val rejectAuthRequestUseCase: RejectAuthRequestUseCase,
    private val getOfflineQRVerificationCodeUseCase: GetOfflineQRVerificationCodeUseCase
) : ViewModel() {

    companion object {
        fun provideFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T = AuthenticationViewModel(
                getApproveSessionUseCase = GetApproveSessionUseCase(),
                approveAuthRequestUserCase = ApproveAuthRequestUserCase(),
                rejectAuthRequestUseCase = RejectAuthRequestUseCase(),
                getOfflineQRVerificationCodeUseCase = GetOfflineQRVerificationCodeUseCase()
            ) as T
        }
    }

    private var countdownJob: Job? = null

    private val _state = MutableStateFlow(FuturaeViewModelState())

    val approvalUIState = _state
        .map(FuturaeViewModelState::toAuthenticationConfirmationScreenUIState)
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            _state.value.toAuthenticationConfirmationScreenUIState()
        )

    val showProgressLoader = _state
        .map { it.showLoader }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            _state.value.showLoader
        )

    private var _timeoutCountdownProgress = MutableStateFlow(0f)
    val timeoutCountdownProgress = _timeoutCountdownProgress.asStateFlow()

    private val _notifyUser = MutableSharedFlow<FuturaeSnackbarUIState>()
    val notifyUser = _notifyUser.asSharedFlow()

    private val _navigateToApprovalScreen = MutableSharedFlow<Unit>()
    val navigateToApprovalScreen = _navigateToApprovalScreen.asSharedFlow()

    private val _navigateToAccounts = MutableSharedFlow<Unit>()
    val navigateToAccounts = _navigateToAccounts.asSharedFlow()

    private val _onOfflineVerificationCodeReceived = MutableSharedFlow<String>()
    val onOfflineVerificationCodeReceived = _onOfflineVerificationCodeReceived.asSharedFlow()

    fun responseToApprovalRequest(userResponse: AuthenticationConfirmationUserResponse) {
        val state = _state.value

        viewModelScope.launch {
            when (userResponse) {
                AuthenticationConfirmationUserResponse.APPROVE -> {
                    when {
                        state.isOfflineQRCode -> getOfflineQRVerificationCode()
                        state.hasMultiNumberedChallenge -> _state.update {
                            it.copy(showMultiNumberedChallenge = true)
                        }
                        else -> approveAuthentication(state.sessionIdentificationOption!!, state.extraInfo)
                    }
                }

                AuthenticationConfirmationUserResponse.REJECT -> {
                    if (_state.value.isOfflineQRCode) {
                        reset()
                    } else {
                        rejectAuthentication(state.sessionIdentificationOption!!, state.extraInfo)
                    }
                }
            }
        }
    }

    fun onMultiNumberChallengeResponse(choice: Int) {
        val state = _state.value

        viewModelScope.launch {
            approveAuthentication(
                state.sessionIdentificationOption!!,
                state.extraInfo,
                choice
           )
        }
    }

    fun handleAuthRequest(authRequestData: AuthRequestData) {
        viewModelScope.launch {
            _state.update {
                FuturaeViewModelState(
                    showLoader = authRequestData !is AuthRequestData.OfflineQRCode
                )
            }
            _navigateToApprovalScreen.emit(Unit)
        }

        when (authRequestData) {
            is AuthRequestData.OnlineQRCode -> handleOnlineQRAuthRequest(
                authRequestData.sessionInfoQuery,
                authRequestData.sessionIdentificationOption
            )

            is AuthRequestData.UsernamelessQRCode -> handleOnlineQRAuthRequest(
                authRequestData.sessionInfoQuery,
                authRequestData.sessionIdentificationOption,
                authRequestData.ftAccount
            )

            is AuthRequestData.PushNotification -> handlePushAuthRequest(authRequestData)
            is AuthRequestData.OfflineQRCode -> handleOfflineQRAuthRequest(authRequestData)
            is AuthRequestData.AuthSession -> handleApproveSession(authRequestData)
        }
    }

    private fun showLoader() {
        _state.update {
            it.copy(showLoader = true)
        }
    }

    private fun handleApproveSession(authRequestData: AuthRequestData.AuthSession) {
        viewModelScope.launch {
            getSessionInfo(
                sessionInfoQuery = SessionInfoQuery(
                    sessionIdentifier = ById(authRequestData.approveSession.sessionId),
                    userId = authRequestData.userId
                )
            ).onSuccess {
                notifyUserForApproval(
                    sessionIdentificationOption = SessionId(it.userId!!, it.sessionId),
                    approveSession = ApproveSession(it),
                    extraInfo = it.approveInfo
                )
            }
        }
    }

    private fun handleOfflineQRAuthRequest(authRequestData: AuthRequestData.OfflineQRCode) {
        val account = authRequestData.qrCode.userId.let {
            FuturaeSDK.client.accountApi.getAccount(
                accountQuery = AccountQuery.WhereUserId(it)
            )
        }

        viewModelScope.launch {
            _state.update {
                FuturaeViewModelState(
                    offlineQRCode = authRequestData.qrCode.rawCode,
                    extraInfo = authRequestData.qrCode.extraInfo,
                    account = account
                )
            }
        }
    }

    private fun handleOnlineQRAuthRequest(
        sessionInfoQuery: SessionInfoQuery,
        sessionIdentificationOption: SessionIdentificationOption,
        ftAccount: FTAccount? = null
    ) {
        viewModelScope.launch {
            getSessionInfo(sessionInfoQuery = sessionInfoQuery)
                .onSuccess {
                    if (ftAccount == null && it.userId.isNullOrBlank()) {
                        failureOnFetchingSessionInfo(
                            message = TextWrapper.Resource(
                                R.string.error_message_session_is_missing_user_id
                            )
                        )
                        return@onSuccess
                    }

                    notifyUserForApproval(
                        sessionIdentificationOption = sessionIdentificationOption,
                        approveSession = ApproveSession(sessionInfo = it),
                        extraInfo = it.approveInfo,
                        ftAccount = ftAccount
                    )
                }
        }
    }

    private suspend fun approveAuthentication(
        sessionIdentificationOption: SessionIdentificationOption,
        extraInfo: List<ApproveInfo>?,
        choice: Int? = null
    ) {
        showLoader()

        approveAuthRequestUserCase(sessionIdentificationOption, extraInfo, choice)
            .onSuccess {
                notifyUser(
                    message = TextWrapper.Resource(R.string.approved),
                    isError = false
                )
            }
            .onFailure {
                notifyUser(
                    message = TextWrapper.Resource(
                        R.string.error_message_approve_session,
                        listOf(it.message ?: "")
                    ),
                    isError = true
                )
            }
            .also {
                reRouteUserToAccounts()
                authenticationSessionCompleted()
            }
    }

    private suspend fun rejectAuthentication(
        sessionIdentificationOption: SessionIdentificationOption,
        extraInfo: List<ApproveInfo>?
    ) {
        showLoader()

        rejectAuthRequestUseCase(sessionIdentificationOption, extraInfo)
            .onSuccess {
                notifyUser(
                    message = TextWrapper.Resource(R.string.denied),
                    isError = false
                )
            }
            .onFailure {
                notifyUser(
                    message = TextWrapper.Resource(
                        R.string.error_message_reject_session,
                        listOf(it.message ?: "")
                    ),
                    isError = true
                )
            }
            .also {
                reRouteUserToAccounts()
                authenticationSessionCompleted()
            }
    }

    private fun getOfflineQRVerificationCode() {
        val qrCode = _state.value.offlineQRCode!!

        viewModelScope.launch {
            getOfflineQRVerificationCodeUseCase(qrCode = qrCode)
                .onSuccess {
                    _onOfflineVerificationCodeReceived.emit(it)
                }
                .onFailure {
                    reset()
                    notifyUser(
                        message = TextWrapper.Resource(
                            R.string.error_message_fetching_verification_code_failed,
                            listOf(it.localizedMessage ?: "")
                        ),
                        isError = true
                    )
                }
        }
    }

    private fun handlePushAuthRequest(authRequestData: AuthRequestData.PushNotification) {
        val approveSession = authRequestData.approveSession
        val userId = authRequestData.userId
        if (userId == null) {
            notifyUser(
                message = TextWrapper.Resource(R.string.error_message_approve_session_is_missing_user_id),
                isError = true
            )
            return
        }

        when {
            authRequestData.encryptedExtras != null -> {
                decryptExtras(
                    userId = userId,
                    encryptedExtras = authRequestData.encryptedExtras
                ) {
                    notifyUserForApproval(
                        sessionIdentificationOption = SessionId(userId, approveSession.sessionId),
                        approveSession = approveSession,
                        extraInfo = it
                    )
                }
            }

            approveSession.hasExtraInfo() -> {
                viewModelScope.launch {
                    getSessionInfo(
                        sessionInfoQuery = SessionInfoQuery(
                            sessionIdentifier = ById(approveSession.sessionId),
                            userId = userId
                        )
                    ).onSuccess {
                        notifyUserForApproval(
                            sessionIdentificationOption = SessionId(it.userId!!, it.sessionId),
                            approveSession = approveSession,
                            extraInfo = it.approveInfo
                        )
                    }
                }
            }

            else -> {
                notifyUserForApproval(
                    sessionIdentificationOption = SessionId(userId, approveSession.sessionId),
                    approveSession = approveSession
                )
            }
        }
    }

    private fun decryptExtras(
        userId: String,
        encryptedExtras: String,
        onSuccessfulDecryption: (List<ApproveInfo>?) -> Unit
    ) {
        val decryptedExtras = try {
            FuturaeSDK.client.operationsApi.decryptPushNotificationExtraInfo(
                userId = userId,
                encryptedExtrasString = encryptedExtras
            )
        } catch (e: FTException) {
            Timber.e("Unable to parse decrypted extra info: ${e.message}")
            null
        }

        onSuccessfulDecryption(decryptedExtras)
    }

    private fun notifyUserForApproval(
        sessionIdentificationOption: SessionIdentificationOption,
        approveSession: ApproveSession,
        extraInfo: List<ApproveInfo>? = null,
        ftAccount: FTAccount? = null
    ) {
        val account = ftAccount ?: approveSession.userId?.let {
            FuturaeSDK.client.accountApi.getAccount(
                accountQuery = AccountQuery.WhereUserId(it)
            )
        }

        viewModelScope.launch {
            _state.emit(
                FuturaeViewModelState(
                    session = approveSession,
                    extraInfo = extraInfo,
                    account = account,
                    sessionIdentificationOption = sessionIdentificationOption
                )
            )

            countdownJob?.cancel()
            countdownJob = startCountdown(
                timeoutInSeconds = approveSession.sessionTimeout.toLong(),
                onProgressUpdate = {
                    _timeoutCountdownProgress.value = it
                },
                onTimeout = {
                    authenticationSessionCompleted()
                    countdownJob?.cancel()
                }
            )

            _navigateToApprovalScreen.emit(Unit)
        }
    }

    private suspend fun getSessionInfo(
        sessionInfoQuery: SessionInfoQuery
    ) = getApproveSessionUseCase(query = sessionInfoQuery)
        .onFailure {
            failureOnFetchingSessionInfo(
                message = TextWrapper.Resource(
                    R.string.error_message_fetching_session,
                    listOf(it.localizedMessage ?: "")
                ),
            )
        }

    private fun failureOnFetchingSessionInfo(message: TextWrapper) {
        _state.update { currentState ->
            currentState.copy(showLoader = false)
        }

        notifyUser(
            message = message,
            isError = true
        )
    }

    private fun notifyUser(message: TextWrapper, isError: Boolean) {
        val snackbarUIState = if (isError) {
            FuturaeSnackbarUIState.Error(message = message)
        } else {
            FuturaeSnackbarUIState.Success(message = message)
        }

        viewModelScope.launch {
            _notifyUser.emit(snackbarUIState)
        }
    }

    private fun authenticationSessionCompleted() {
        cancelCountdown()
        _state.update {
            FuturaeViewModelState()
        }
    }

    private fun cancelCountdown() {
        countdownJob?.cancel()
    }

    fun reset() {
        authenticationSessionCompleted()
    }

    private fun reRouteUserToAccounts() {
        viewModelScope.launch {
            _navigateToAccounts.emit(Unit)
        }
    }


    data class FuturaeViewModelState(
        val session: ApproveSession? = null,
        val extraInfo: List<ApproveInfo>? = null,
        val account: FTAccount? = null,
        val showLoader: Boolean = false,
        val offlineQRCode: String? = null,
        val sessionIdentificationOption: SessionIdentificationOption? = null,
        val showMultiNumberedChallenge: Boolean = false
    ) {
        val isOfflineQRCode: Boolean = offlineQRCode != null
        val hasMultiNumberedChallenge: Boolean = session?.multiNumberedChallenge.isNullOrEmpty().not()

        fun toAuthenticationConfirmationScreenUIState(): AuthenticationConfirmationComposableScreenUIState? {
            if (showLoader) {
                return AuthenticationConfirmationComposableScreenUIState.Loading
            }

            if (isOfflineQRCode) {
                return AuthenticationConfirmationComposableScreenUIState.AuthenticationConfirmationScreenUIState(
                    serviceInfoSectionUIState = ServiceInfoSectionUIState(
                        serviceLogo = account?.serviceLogo ?: "",
                        serviceName = account?.serviceName,
                        username = account?.username
                    ),
                    content = AuthenticationScreenContent.Details(extraInfo?.map { it.mapToInfoItemUIState() } ?: emptyList()),
                    authenticationType = TextWrapper.Resource(R.string.authenticate_default_type),
                    timeoutInSeconds = 0
                )
            }

            session ?: return null

            return AuthenticationConfirmationComposableScreenUIState.AuthenticationConfirmationScreenUIState(
                serviceInfoSectionUIState = ServiceInfoSectionUIState(
                    serviceLogo = account?.serviceLogo ?: "",
                    serviceName = account?.serviceName,
                    username = account?.username
                ),
                content = if (showMultiNumberedChallenge) {
                    AuthenticationScreenContent.MultiNumberedChallenge(
                        options = session.multiNumberedChallenge!!
                    )
                } else {
                    val details = mutableListOf<InfoItemUIState>()
                    session.info?.forEach {
                        details.add(it.mapToInfoItemUIState())
                    }
                    extraInfo?.forEach {
                        details.add(it.mapToInfoItemUIState())
                    }

                    AuthenticationScreenContent.Details(details = details)
                },
                authenticationType = TextWrapper.Resource(
                    R.string.authenticate_type,
                    listOf(session.type)
                ),
                timeoutInSeconds = session.sessionTimeout
            )
        }

        private fun ApproveInfo.mapToInfoItemUIState() = InfoItemUIState(
            label = TextWrapper.Primitive(key),
            value = TextWrapper.Primitive(value)
        )
    }
}