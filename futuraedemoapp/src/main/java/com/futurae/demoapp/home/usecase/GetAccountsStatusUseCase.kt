package com.futurae.demoapp.home.usecase

import com.futurae.sdk.FuturaeSDK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class GetAccountsStatusUseCase {

    suspend operator fun invoke(userIds: List<String>) = withContext(Dispatchers.IO) {
        try {
            val accountStatus = FuturaeSDK.client.accountApi
                .getAccountsStatus(*userIds.toTypedArray())
                .await()
            Result.success(accountStatus)
        } catch (t: Throwable) {
            Timber.e(t)
            Result.failure(t)
        }
    }
}