package com.futurae.sampleapp.ui.shared.elements.authenticationconfirmationscreen.usecase

import com.futurae.sdk.FuturaeSDK
import com.futurae.sdk.public_api.auth.model.SDKAuthMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class GetOfflineQRVerificationCodeUseCase {

    suspend operator fun invoke(qrCode: String) = withContext(Dispatchers.IO) {
        try {
            // todo pass correct SDKAuthMode
            val verificationCode = FuturaeSDK.client.authApi.getOfflineQRVerificationCode(
                qrCode,
                SDKAuthMode.Unlock
            ).await()
            Result.success(verificationCode)
        } catch (t: Throwable) {
            Timber.e(t)
            Result.failure(t)
        }
    }
}