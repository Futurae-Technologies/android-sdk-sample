package com.futurae.demoapp.arch

import com.futurae.sdk.public_api.auth.model.OnlineQR
import com.futurae.sdk.public_api.auth.model.SessionIdentificationOption
import com.futurae.sdk.public_api.auth.model.UsernamelessQR
import com.futurae.sdk.public_api.common.model.FTAccount
import com.futurae.sdk.public_api.qr_code.model.QRCode
import com.futurae.sdk.public_api.session.model.ApproveSession
import com.futurae.sdk.public_api.session.model.ByToken
import com.futurae.sdk.public_api.session.model.SessionInfoQuery

sealed class AuthRequestData {

    data class OnlineQRCode(
        private val qrCode: QRCode.Online
    ): AuthRequestData() {

        private val userId = qrCode.userId
        private val sessionToken = qrCode.sessionToken

        val sessionInfoQuery: SessionInfoQuery = SessionInfoQuery(
            ByToken(sessionToken),
            userId
        )

        val sessionIdentificationOption: SessionIdentificationOption =
            OnlineQR(qrCodeContent = qrCode.rawCode)
    }

    data class UsernamelessQRCode(
        private val qrCode: QRCode.Usernameless,
        val ftAccount: FTAccount
    ): AuthRequestData() {

        private val sessionToken = qrCode.sessionToken

        val sessionInfoQuery: SessionInfoQuery = SessionInfoQuery(
            ByToken(sessionToken),
            ftAccount.userId
        )

        val sessionIdentificationOption: SessionIdentificationOption =
            UsernamelessQR(userId = ftAccount.userId, qrCodeContent = qrCode.rawCode)
    }

    data class OfflineQRCode(
        val qrCode: QRCode.Offline
    ): AuthRequestData()

    data class PushNotification(
        val approveSession: ApproveSession,
        val userId: String?,
        val encryptedExtras: String?
    ): AuthRequestData()

    data class AuthSession(
        val userId: String,
        val approveSession: ApproveSession
    ) : AuthRequestData()
}