package com.futurae.demoapp.migration.usecase

import com.futurae.sdk.FuturaeSDK
import com.futurae.sdk.public_api.migration.model.MigratableAccounts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class GetMigratableAccountsUseCase {

    suspend operator fun invoke(): Result<MigratableAccounts> = withContext(Dispatchers.IO) {
        try {
            val result = FuturaeSDK.client.migrationApi.getMigratableAccounts().await()
            Result.success(result)
        } catch (t: Throwable) {
            Timber.e(t)
            Result.failure(t)
        }
    }
}