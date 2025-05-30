package com.futurae.demoapp.home.accounts.history.usecase

import com.futurae.sdk.FuturaeSDK
import com.futurae.sdk.public_api.account.model.AccountQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetAccountForUserIdUseCase {

    suspend operator fun invoke(userId: String) = withContext(Dispatchers.IO) {
        FuturaeSDK.client.accountApi.getAccount(
            accountQuery = AccountQuery.WhereUserId(userId = userId)
        )
    }
}