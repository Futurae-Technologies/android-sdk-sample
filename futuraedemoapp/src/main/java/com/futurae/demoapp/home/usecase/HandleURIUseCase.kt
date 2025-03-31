package com.futurae.demoapp.home.usecase

import com.futurae.sdk.FuturaeSDK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class HandleURIUseCase {

    suspend operator fun invoke(uri: String) = withContext(Dispatchers.IO) {
        try {
            FuturaeSDK.client.operationsApi.handleUri(uri).await()
            Result.success(Unit)
        } catch (t: Throwable) {
            Timber.e(t)
            Result.failure(t)
        }
    }
}