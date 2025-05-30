package com.futurae.sampleapp.settings.integrity

import com.futurae.sdk.FuturaeSDK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class IntegrityCheckUseCase {

    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        try {
            val verdict = FuturaeSDK.client.operationsApi.getIntegrityVerdict().await()
            Result.success(verdict)
        } catch (t: Throwable) {
            Timber.e(t)
            Result.failure(t)
        }
    }
}