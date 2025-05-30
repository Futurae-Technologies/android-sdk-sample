package com.futurae.demoapp.settings.more

import com.futurae.sdk.FuturaeSDK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class LogoutUseCase {

    suspend operator fun invoke(userId: String? = null) = withContext(Dispatchers.IO) {
        try {
            if (userId != null) {
                FuturaeSDK.client.accountApi.logoutAccount(userId).await()
            } else {
                FuturaeSDK.client.accountApi.getActiveAccounts().forEach {
                    FuturaeSDK.client.accountApi.logoutAccount(it.userId).await()
                }
            }
            Result.success(Unit)
        } catch (t: Throwable) {
            Timber.e(t)
            Result.failure(t)
        }
    }
}