package com.futurae.sampleapp.arch

import com.futurae.sdk.public_api.auth.model.OnlineQR
import com.futurae.sdk.public_api.auth.model.SessionIdentificationOption
import com.futurae.sdk.public_api.auth.model.UsernamelessQR
import com.futurae.sdk.public_api.auth.model.UsernamelessURI
import com.futurae.sdk.public_api.common.model.FTAccount
import com.futurae.sdk.public_api.qr_code.model.QRCode
import com.futurae.sdk.public_api.session.model.ApproveSession
import com.futurae.sdk.public_api.session.model.ByToken
import com.futurae.sdk.public_api.session.model.SessionInfoQuery
import com.futurae.sdk.public_api.uri.model.FTRUriType

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

    sealed class Usernameless: AuthRequestData() {

        protected abstract val sessionToken: String

        abstract fun getSessionIdentificationOption(account: FTAccount): SessionIdentificationOption

        fun getSessionInfoQuery(account: FTAccount): SessionInfoQuery = SessionInfoQuery(
            sessionIdentifier = ByToken(sessionToken),
            userId = account.userId
        )

        data class QR(private val qrCode: QRCode.Usernameless): Usernameless() {

            override val sessionToken = qrCode.sessionToken

            override fun getSessionIdentificationOption(
                account: FTAccount
            ): SessionIdentificationOption = UsernamelessQR(
                userId = account.userId,
                qrCodeContent = qrCode.rawCode
            )
        }

        data class URI(val ftrUriType: FTRUriType.UsernamelessAuth) : Usernameless() {

            override val sessionToken = ftrUriType.sessionToken

            override fun getSessionIdentificationOption(
                account: FTAccount
            ): SessionIdentificationOption = UsernamelessURI(
                userId = account.userId,
                uri = ftrUriType.uri
            )
        }
    }

    data class OfflineQRCode(
        val qrCode: QRCode.Offline
    ): AuthRequestData()

    data class PushNotification(
        val approveSession: ApproveSession,
        val userId: String?
    ): AuthRequestData()

    data class AuthSession(
        val userId: String,
        val approveSession: ApproveSession
    ) : AuthRequestData()
}