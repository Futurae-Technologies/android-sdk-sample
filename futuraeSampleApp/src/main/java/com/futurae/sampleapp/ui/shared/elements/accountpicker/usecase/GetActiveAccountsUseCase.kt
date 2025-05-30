package com.futurae.sampleapp.ui.shared.elements.accountpicker.usecase

import com.futurae.sdk.FuturaeSDK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetActiveAccountsUseCase {

    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        FuturaeSDK.client.accountApi.getActiveAccounts()
    }
}