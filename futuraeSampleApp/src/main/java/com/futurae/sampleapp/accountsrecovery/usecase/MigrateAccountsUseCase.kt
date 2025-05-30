package com.futurae.sampleapp.accountsrecovery.usecase

import com.futurae.sampleapp.utils.LocalStorage
import com.futurae.sdk.FuturaeSDK
import com.futurae.sdk.public_api.migration.model.MigrationAccount
import com.futurae.sdk.public_api.migration.model.MigrationUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class MigrateAccountsUseCase {

    suspend operator fun invoke(
        migrationUseCase: MigrationUseCase,
        flowBindingToken: String?
    ): Result<List<MigrationAccount>> = withContext(Dispatchers.IO) {
        try {
            val result = FuturaeSDK.client.migrationApi
                .migrateAccounts(migrationUseCase, flowBindingToken)
                .await()
            LocalStorage.setAccountsRestored()
            Result.success(result)
        } catch (t: Throwable) {
            Timber.e(t)
            Result.failure(t)
        }
    }

}