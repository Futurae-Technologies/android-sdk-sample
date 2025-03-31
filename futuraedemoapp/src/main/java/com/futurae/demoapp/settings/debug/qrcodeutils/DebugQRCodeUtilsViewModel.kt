package com.futurae.demoapp.settings.debug.qrcodeutils

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.futurae.demoapp.R
import com.futurae.sdk.FuturaeSDK
import com.futurae.sdk.public_api.qr_code.model.QRCode
import com.futurae.sdk.utils.FTQRCodeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DebugQRCodeUtilsViewModel : ViewModel() {

    companion object {
        fun provideFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T = DebugQRCodeUtilsViewModel() as T
        }
    }

    private val _uiState = MutableStateFlow<DebugQRCodeUtilsUIState>(DebugQRCodeUtilsUIState.Scanning)
    val uiState = _uiState.asStateFlow()

    fun onQRCodeScanned(qrCode: String) {
        val (qrCodeTypeLegacyImpl, userIdLegacyImpl, sessionTokenLegacyImpl) = qrCode.handleUsingLegacyImplementation()
        val (qrCodeType, userId, sessionToken) = qrCode.handle()

        val isSameQRCodeType = when (qrCodeTypeLegacyImpl) {
            FTQRCodeUtils.QRType.Enroll -> qrCodeType is QRCode.Enroll
            FTQRCodeUtils.QRType.Invalid -> qrCodeType is QRCode.Invalid
            FTQRCodeUtils.QRType.Offline -> qrCodeType is QRCode.Offline
            FTQRCodeUtils.QRType.Online -> qrCodeType is QRCode.Online
            FTQRCodeUtils.QRType.Usernameless -> qrCodeType is QRCode.Usernameless
        }
        val isSameUserId = userIdLegacyImpl == userId
        val isSessionToken = sessionTokenLegacyImpl == sessionToken

        viewModelScope.launch {
            _uiState.update {
                DebugQRCodeUtilsUIState.ScanResult(
                    qrCodeTypeLegacyImpl = qrCodeTypeLegacyImpl.toLabel(),
                    userIdLegacyImpl = userIdLegacyImpl,
                    sessionTokenLegacyImpl = sessionTokenLegacyImpl,
                    qrCodeType = qrCodeType.toLabel(),
                    userId = userId,
                    sessionToken = sessionToken,
                    isSameResultInBothImpl = isSameQRCodeType && isSameUserId && isSessionToken
                )
            }
        }
    }

    fun dismissResult() {
        viewModelScope.launch {
            _uiState.update {
                DebugQRCodeUtilsUIState.Scanning
            }
        }
    }

    private fun String.handleUsingLegacyImplementation(): Triple<FTQRCodeUtils.QRType, String?, String?> {
        return when (val qrCodeType = FTQRCodeUtils.getQrcodeType(this)) {
            is FTQRCodeUtils.QRType.Enroll -> {
                Triple(
                    qrCodeType,
                    FTQRCodeUtils.getUserIdFromQrcode(this),
                    null
                )
            }

            is FTQRCodeUtils.QRType.Invalid -> {
                Triple(qrCodeType, null, null)
            }

            is FTQRCodeUtils.QRType.Offline -> {
                Triple(
                    qrCodeType,
                    FTQRCodeUtils.getUserIdFromQrcode(this),
                    null
                )
            }

            is FTQRCodeUtils.QRType.Online -> {
                Triple(
                    qrCodeType,
                    FTQRCodeUtils.getUserIdFromQrcode(this),
                    FTQRCodeUtils.getSessionTokenFromQrcode(this)
                )
            }

            is FTQRCodeUtils.QRType.Usernameless -> {
                Triple(
                    qrCodeType,
                    null,
                    FTQRCodeUtils.getSessionTokenFromQrcode(this)
                )
            }
        }
    }

    @StringRes
    private fun FTQRCodeUtils.QRType.toLabel(): Int = when(this) {
        FTQRCodeUtils.QRType.Enroll -> R.string.debug_qr_code_enroll_type
        FTQRCodeUtils.QRType.Invalid -> R.string.debug_qr_code_invalid_type
        FTQRCodeUtils.QRType.Offline -> R.string.debug_qr_code_offline_type
        FTQRCodeUtils.QRType.Online -> R.string.debug_qr_code_online_type
        FTQRCodeUtils.QRType.Usernameless -> R.string.debug_qr_code_usernameless_type
    }


    private fun String.handle(): Triple<QRCode, String?, String?> {
        return when (val qrCodeType = FuturaeSDK.client.qrCodeApi.getQRCode(this)) {
            is QRCode.Enroll -> {
                Triple(
                    qrCodeType,
                    qrCodeType.userId,
                    null
                )
            }

            is QRCode.Invalid -> {
                Triple(qrCodeType, null, null)
            }

            is QRCode.Offline -> {
                Triple(
                    qrCodeType,
                    qrCodeType.userId,
                    null
                )
            }

            is QRCode.Online -> {
                Triple(
                    qrCodeType,
                    qrCodeType.userId,
                    qrCodeType.sessionToken
                )
            }

            is QRCode.Usernameless -> {
                Triple(
                    qrCodeType,
                    null,
                    qrCodeType.sessionToken
                )
            }
        }
    }

    @StringRes
    private fun QRCode.toLabel(): Int = when(this) {
        is QRCode.Enroll -> R.string.debug_qr_code_enroll_type
        is QRCode.Invalid -> R.string.debug_qr_code_invalid_type
        is QRCode.Offline -> R.string.debug_qr_code_offline_type
        is QRCode.Online -> R.string.debug_qr_code_online_type
        is QRCode.Usernameless -> R.string.debug_qr_code_usernameless_type
    }

    sealed class DebugQRCodeUtilsUIState {
        data object Scanning: DebugQRCodeUtilsUIState()

        data class ScanResult(
            @StringRes val qrCodeTypeLegacyImpl: Int,
            val userIdLegacyImpl: String?,
            val sessionTokenLegacyImpl: String?,
            @StringRes val qrCodeType: Int,
            val userId: String?,
            val sessionToken: String?,
            val isSameResultInBothImpl: Boolean
        ): DebugQRCodeUtilsUIState()
    }
}
