package com.futurae.demoapp.home.accounts.usecase

import com.futurae.sdk.FuturaeSDK
import com.futurae.sdk.public_api.auth.model.SDKAuthMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class GetTOTPUseCase {

    suspend operator fun invoke(
        userId: String,
        mode: SDKAuthMode
    ) = withContext(Dispatchers.IO) {
        try {
            val totp = FuturaeSDK.client.authApi.getTOTP(
                userId,
                mode
            ).await()
            Result.success(totp)
        } catch (t: Throwable) {
            Timber.e(t)
            Result.failure(t)
        }
    }
}