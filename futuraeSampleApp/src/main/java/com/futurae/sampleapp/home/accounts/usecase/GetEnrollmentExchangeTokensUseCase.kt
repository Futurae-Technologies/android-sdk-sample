package com.futurae.sampleapp.home.accounts.usecase

import com.futurae.sdk.FuturaeSDK
import com.futurae.sdk.public_api.operations.model.EnrollmentExchangeTokenQrCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class GetEnrollmentExchangeTokensUseCase {

    suspend operator fun invoke(
        userId: String,
        activationCodeShort: String,
        totalExchangeTokens: Int = 4,
        exchangeTokenLifetimeSeconds: Int = 60,
    ): Result<List<EnrollmentExchangeTokenQrCode>> = withContext(Dispatchers.IO) {
        try {
            val tokens = FuturaeSDK.client.operationsApi.getEnrollmentExchangeTokens(
                userId = userId,
                activationCodeShort = activationCodeShort,
                totalExchangeTokens = totalExchangeTokens,
                exchangeTokenLifetimeSeconds = exchangeTokenLifetimeSeconds,
            ).await()
            Result.success(tokens)
        } catch (t: Throwable) {
            Timber.e(t)
            Result.failure(t)
        }
    }
}
