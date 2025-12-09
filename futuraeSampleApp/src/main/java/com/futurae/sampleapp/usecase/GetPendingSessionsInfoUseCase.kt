package com.futurae.sampleapp.usecase

import com.futurae.sdk.FuturaeSDK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class GetPendingSessionsInfoUseCase {

    suspend operator fun invoke(userIds: List<String>) = withContext(Dispatchers.IO) {
        try {
            val pendingSessionsInfo = FuturaeSDK.client.accountApi
                .getPendingSessions(*userIds.toTypedArray())
                .await()
            Result.success(pendingSessionsInfo)
        } catch (t: Throwable) {
            Timber.e(t)
            Result.failure(t)
        }
    }
}