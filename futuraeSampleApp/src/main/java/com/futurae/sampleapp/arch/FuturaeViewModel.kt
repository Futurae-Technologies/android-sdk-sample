package com.futurae.sampleapp.arch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.futurae.sampleapp.utils.LocalStorage
import com.futurae.sampleapp.R
import com.futurae.sampleapp.enrollment.EnrollmentCase
import com.futurae.sampleapp.usecase.GetAccountsStatusUseCase
import com.futurae.sampleapp.usecase.HandleURIUseCase
import com.futurae.sampleapp.ui.TextWrapper
import com.futurae.sampleapp.ui.shared.elements.alertdialog.FuturaeAlertDialogUIState
import com.futurae.sampleapp.ui.shared.elements.snackbar.FuturaeSnackbarUIState
import com.futurae.sdk.FuturaeSDK
import com.futurae.sdk.messaging.FTRNotificationEvent
import com.futurae.sdk.model.internal.FTNotificationData
import com.futurae.sdk.public_api.account.model.AccountQuery
import com.futurae.sdk.public_api.common.FuturaeSDKStatus
import com.futurae.sdk.public_api.exception.FTApiTimeoutException
import com.futurae.sdk.public_api.session.model.ApproveSession
import com.futurae.sdk.public_api.uri.model.FTRUriType
import com.futurae.sdk.utils.FTUriUtils
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import timber.log.Timber

class FuturaeViewModel(
    private val handleURIUseCase: HandleURIUseCase,
    private val getAccountsStatusUseCase: GetAccountsStatusUseCase,
) : ViewModel() {

    companion object {
        fun provideFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T = FuturaeViewModel(
                handleURIUseCase = HandleURIUseCase(),
                getAccountsStatusUseCase = GetAccountsStatusUseCase()
            ) as T
        }
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        if (throwable is FTApiTimeoutException) {
            Timber.e(throwable.diagnostics)
        } else if (throwable.cause is FTApiTimeoutException) {
            Timber.e((throwable.cause as FTApiTimeoutException).diagnostics)
        }

        notifyUser(
            message = TextWrapper.Resource(R.string.uri_handling_error_message),
            isError = true
        )
    }

    private val _onAuthRequest = MutableSharedFlow<AuthRequestData>()
    val onAuthRequest: SharedFlow<AuthRequestData> = _onAuthRequest

    // Replay is needed here to not lose an emission due to cases this flow emitting
    // prior someone observing, e.g. when app is opened due to an enrollment URI
    private val _onEnrollmentRequest = MutableSharedFlow<EnrollmentCase>(replay = 1)
    val onEnrollmentRequest: SharedFlow<EnrollmentCase> = _onEnrollmentRequest

    private val _snackbarUIState = MutableSharedFlow<FuturaeSnackbarUIState>()
    val snackbarUIState = _snackbarUIState.asSharedFlow()

    private val _notifyUser = MutableSharedFlow<NotificationUI>()
    val notifyUserFlow = _notifyUser.asSharedFlow()

    private var pendingUri: String? = null
    private var pendingBroadcastReceivedMessage: FTRNotificationEvent? = null

    private var getAccountStatus: Job? = null

    init {
        viewModelScope.launch {
            FuturaeSDK.sdkState()
                .distinctUntilChangedBy { it.status }
                .filter { it.status is FuturaeSDKStatus.Unlocked }
                .collect {
                    checkForPendingURI()
                    checkForPendingBroadcastReceivedMessage()
                }
        }
    }

    fun fetchAccountsStatus() {
        getAccountStatus?.cancel()

        getAccountStatus = viewModelScope.launch {
            val userIds = FuturaeSDK.client.accountApi.getActiveAccounts().map { it.userId }
            if (userIds.isEmpty()) return@launch

            getAccountsStatusUseCase.invoke(userIds)
                .onSuccess { accStatus ->
                    // handle auth sessions
                    accStatus.statuses.firstOrNull { it.activeSessions.isNotEmpty() }?.let {
                        val activeSession = it.activeSessions.first()
                        val authRequestData = AuthRequestData.AuthSession(
                            it.userId,
                            ApproveSession(activeSession)
                        )

                        _onAuthRequest.emit(authRequestData)
                    }
                }.onFailure {
                    _snackbarUIState.emit(
                        FuturaeSnackbarUIState.Error(
                            TextWrapper.Primitive(
                                it.message ?: "Unknown Error"
                            )
                        )
                    )
                }
        }
    }

    fun onNewURI(uri: String) {
        if (!FuturaeSDK.isSDKInitialized) {
            pendingUri = uri
            return
        }

        when {
            LocalStorage.shouldSetupPin && FTUriUtils.isEnrollUri(uri) -> {
                handleEnrollmentURI(uri)
            }

            LocalStorage.shouldSetupPin -> {
                // cannot handle any other URIs in this state
                return
            }

            FuturaeSDK.client.lockApi.isLocked() -> {
                pendingUri = uri
            }

            else -> {
                handleUri(uri)
            }
        }
    }

    fun handleNotificationReceived(notification: FTRNotificationEvent) {
        if (FuturaeSDK.client.lockApi.isLocked()) {
            pendingBroadcastReceivedMessage = notification
            return
        }

        when (notification) {
            is FTRNotificationEvent.AccountUnenrollment -> onAccountUnenroll(notification)
            is FTRNotificationEvent.CustomInAppMessaging -> handleCustomNotification(notification.data)
            is FTRNotificationEvent.QRCodeScanRequest -> handleQRScanNotification(notification)
            is FTRNotificationEvent.Authentication -> handleApproveAuth(notification)
        }
    }

    private fun handleQRScanNotification(notification: FTRNotificationEvent.QRCodeScanRequest) {
        viewModelScope.launch {
            _notifyUser.emit(
                NotificationUI(
                    type = NotificationType.QR_SCAN,
                    dialogState = FuturaeAlertDialogUIState(
                        title = TextWrapper.Resource(R.string.sdk_qr_scan_notification_title),
                        text = TextWrapper.Resource(
                            R.string.sdk_qr_scan_notification_body,
                            listOf(notification.userId)
                        ),
                        confirmButtonCta = TextWrapper.Resource(R.string.ok),
                    )
                )
            )
        }
    }

    private fun handleCustomNotification(ftNotificationData: FTNotificationData) {
        viewModelScope.launch {
            _notifyUser.emit(
                NotificationUI(
                    type = NotificationType.INFO,
                    dialogState = FuturaeAlertDialogUIState(
                        title = TextWrapper.Resource(R.string.sdk_informative_notification),
                        text = TextWrapper.Primitive(ftNotificationData.payload.toList()
                            .joinToString(
                                separator = ",\n",
                                transform = { pair -> "key: ${pair.first}, value:${pair.second}" }
                            ) + "\n\nUserId = ${ftNotificationData.userId}"),
                        confirmButtonCta = TextWrapper.Resource(R.string.ok),
                    )
                )
            )
        }
    }

    private fun checkForPendingURI() {
        pendingUri?.let { uri ->
            handleUri(uri)
            pendingUri = null
        }
    }

    private fun checkForPendingBroadcastReceivedMessage() {
        pendingBroadcastReceivedMessage?.let {
            handleNotificationReceived(it)
            pendingBroadcastReceivedMessage = null
        }
    }

    private fun handleUri(uri: String) {
        when (FTUriUtils.getFTRUriType(uri)) {
            is FTRUriType.Auth -> handleAuthenticationURI(uri)
            is FTRUriType.Enroll -> handleEnrollmentURI(uri)
            FTRUriType.Unknown -> {
                // do nothing
            }
        }
    }

    private fun handleEnrollmentURI(uri: String) {
        // Optional: you may use the enrollAccount API instead of handleUri API,
        // to support flow-binding-token
        val enrollmentCase = EnrollmentCase.URIHandler(uri = uri)
        viewModelScope.launch {
            _onEnrollmentRequest.emit(enrollmentCase)
        }
    }

    private fun handleAuthenticationURI(uri: String) {
        viewModelScope.launch(exceptionHandler) {
            handleURIUseCase(uri)
                .onSuccess {
                    notifyUser(
                        message = TextWrapper.Resource(R.string.authenticated_successfully)
                    )
                }
                .onFailure {
                    notifyUser(
                        message = TextWrapper.Resource(
                            R.string.error_message_authentication_failed,
                            listOf(it.localizedMessage ?: "")
                        ),
                        isError = true
                    )
                }
        }
    }

    private fun onAccountUnenroll(notification: FTRNotificationEvent.AccountUnenrollment) {
        FuturaeSDK.client.accountApi
            .getAccount(
                accountQuery = AccountQuery.WhereUserIdAndDevice(
                    userId = notification.userId,
                    deviceId = notification.deviceId
                )
            )
            ?.let {
                viewModelScope.launch {
                    _notifyUser.emit(
                        NotificationUI(
                            type = NotificationType.UNENROLL,
                            dialogState = FuturaeAlertDialogUIState(
                                title = TextWrapper.Resource(R.string.sdk_notification_unenroll_title),
                                text = TextWrapper.Resource(
                                    R.string.sdk_notification_unenroll_body,
                                    listOf(it.userId)
                                ),
                                confirmButtonCta = TextWrapper.Resource(R.string.ok),
                            )
                        )
                    )
                }
            }
    }

    private fun handleApproveAuth(authenticationSessionData: FTRNotificationEvent.Authentication) {
        viewModelScope.launch {
            _onAuthRequest.emit(
                AuthRequestData.PushNotification(
                    authenticationSessionData.session,
                    authenticationSessionData.session.userId,
                    authenticationSessionData.encryptedExtras
                )
            )
            _notifyUser.emit(
                NotificationUI(
                    type = NotificationType.AUTH,
                    dialogState = FuturaeAlertDialogUIState(
                        title = TextWrapper.Resource(R.string.sdk_notification_auth_title),
                        text = TextWrapper.Resource(R.string.sdk_notification_auth_body),
                        confirmButtonCta = TextWrapper.Resource(R.string.ok),
                    )
                )
            )
        }
    }

    private fun notifyUser(message: TextWrapper, isError: Boolean = false) {
        val snackbarUIState = if (isError) {
            FuturaeSnackbarUIState.Error(message)
        } else {
            FuturaeSnackbarUIState.Success(message)
        }

        viewModelScope.launch {
            _snackbarUIState.emit(snackbarUIState)
        }
    }

    override fun onCleared() {
        super.onCleared()
        getAccountStatus?.cancel()
    }
}