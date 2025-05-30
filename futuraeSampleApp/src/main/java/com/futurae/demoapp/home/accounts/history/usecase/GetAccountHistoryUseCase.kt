package com.futurae.demoapp.home.accounts.history.usecase

import com.futurae.sdk.FuturaeSDK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetAccountHistoryUseCase {

    suspend operator fun invoke(userId: String) = withContext(Dispatchers.IO) {
        try {
            val history = FuturaeSDK.client.accountApi.getAccountHistory(userId).await()
            Result.success(history)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }
}