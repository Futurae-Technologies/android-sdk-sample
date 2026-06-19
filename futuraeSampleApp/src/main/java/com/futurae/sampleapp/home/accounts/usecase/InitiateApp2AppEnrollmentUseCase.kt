package com.futurae.sampleapp.home.accounts.usecase

import com.futurae.sdk.FuturaeSDK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class InitiateApp2AppEnrollmentUseCase {

    suspend operator fun invoke(userId: String) = withContext(Dispatchers.IO) {
        try {
            val info = FuturaeSDK.client.operationsApi.spawnEnrollment(userId).await()
            Result.success(info)
        } catch (t: Throwable) {
            Timber.e(t)
            Result.failure(t)
        }
    }
}