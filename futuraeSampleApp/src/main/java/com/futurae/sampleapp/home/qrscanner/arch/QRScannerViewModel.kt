package com.futurae.sampleapp.home.qrscanner.arch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.futurae.sampleapp.arch.AuthRequestData
import com.futurae.sampleapp.enrollment.EnrollmentCase
import com.futurae.sampleapp.home.qrscanner.QRScannerScreenUserInteraction
import com.futurae.sdk.FuturaeSDK
import com.futurae.sdk.public_api.qr_code.model.QRCode
import com.futurae.sdk.public_api.session.model.ApproveSession
import com.futurae.sdk.public_api.session.model.ByToken
import com.futurae.sdk.public_api.session.model.SessionInfoQuery
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class QRScannerViewModel : ViewModel() {

    companion object {
        fun provideFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T = QRScannerViewModel() as T
        }
    }

    private val _onAuthRequest = MutableSharedFlow<AuthRequestData>()
    val onAuthRequest: SharedFlow<AuthRequestData> = _onAuthRequest

    private val _onFailure = MutableSharedFlow<Unit>()
    val onFailure: SharedFlow<Unit> = _onFailure

    private val _onEnrollmentFlowRequested = MutableSharedFlow<EnrollmentCase.ActivationCodeInput>()
    val onEnrollmentFlowRequest: SharedFlow<EnrollmentCase.ActivationCodeInput> = _onEnrollmentFlowRequested

    fun handleUserInteraction(userInteraction: QRScannerScreenUserInteraction) {
        when (userInteraction) {
            is QRScannerScreenUserInteraction.OnQRCodeScanned -> {
                onQRCodeScanned(userInteraction.code)
            }
        }
    }

    private fun onQRCodeScanned(code: String) {
        when (val qrCode = FuturaeSDK.client.qrCodeApi.getQRCode(code)) {
            is QRCode.Enroll -> initiateEnrollmentFlow(code)
            is QRCode.Invalid -> notifyUserForInvalidQRCodeScanned()
            is QRCode.Offline -> qrCode.handleOfflineAuthQRCodeScanned()
            is QRCode.Online -> qrCode.handleOnlineAuthQRCodeScanned()
            is QRCode.Usernameless -> qrCode.handleUsernamelessQRCodeScanned()
            is QRCode.EnrollTokenExchange -> qrCode.handleEnrollExchangeToken()
            is QRCode.AuthTokenExchange -> qrCode.handleAuthExchangeToken()
        }
    }

    private fun notifyForAuthRequest(authRequestData: AuthRequestData) {
        viewModelScope.launch {
            _onAuthRequest.emit(authRequestData)
        }
    }

    private fun initiateEnrollmentFlow(qrCode: String) {
        viewModelScope.launch {
            _onEnrollmentFlowRequested.emit(EnrollmentCase.ActivationCodeInput(qrCode))
        }
    }

    private fun QRCode.Offline.handleOfflineAuthQRCodeScanned() {
        val authRequestData = AuthRequestData.OfflineQRCode(qrCode = this)
        notifyForAuthRequest(authRequestData)
    }

    private fun QRCode.Online.handleOnlineAuthQRCodeScanned() {
        val authRequestData = AuthRequestData.OnlineQRCode(qrCode = this)
        notifyForAuthRequest(authRequestData)
    }

    private fun QRCode.Usernameless.handleUsernamelessQRCodeScanned() {
        val authRequestData = AuthRequestData.Usernameless.QR(qrCode = this)
        notifyForAuthRequest(authRequestData)
    }

    private fun notifyUserForInvalidQRCodeScanned() {
        viewModelScope.launch {
            _onFailure.emit(Unit)
        }
    }

    private fun QRCode.AuthTokenExchange.handleAuthExchangeToken() {
        viewModelScope.launch {
            try {
                val sessionToken =
                    FuturaeSDK.client.operationsApi.exchangeTokenForSessionToken(exchangeToken)
                        .await()
                val sessionInfo = FuturaeSDK.client.sessionApi.getSessionInfo(
                    SessionInfoQuery(
                        ByToken(sessionToken),
                        userId = userId
                    )
                ).await()
                _onAuthRequest.emit(
                    AuthRequestData.AuthSession(
                        userId,
                        ApproveSession(sessionInfo)
                    )
                )
            } catch (e: Throwable) {
                Timber.e(e)
            }
        }
    }

    private fun QRCode.EnrollTokenExchange.handleEnrollExchangeToken() {
        viewModelScope.launch {
            try {
                val activationCode =
                    FuturaeSDK.client.operationsApi.exchangeTokenForEnrollmentActivationCode(
                        exchangeToken
                    ).await()
                _onEnrollmentFlowRequested.emit(EnrollmentCase.ActivationCodeInput(activationCode))
            } catch (e: Throwable) {
                Timber.e(e)
            }
        }
    }
}
