package com.futurae.demoapp.ui.shared.elements.authenticationconfirmationscreen.usecase

import com.futurae.demoapp.utils.LocalStorage
import com.futurae.sdk.FuturaeSDK
import com.futurae.sdk.public_api.session.model.SessionInfoQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class GetApproveSessionUseCase {

    suspend operator fun invoke(query: SessionInfoQuery) = withContext(Dispatchers.IO) {
        try {
            val sessionInfo = if (LocalStorage.isSessionInfoWithoutUnlockEnabled) {
                FuturaeSDK.client.sessionApi.getSessionInfoWithoutUnlock(query).await()
            } else {
                FuturaeSDK.client.sessionApi.getSessionInfo(query).await()
            }

            Result.success(sessionInfo)
        } catch (t: Throwable) {
            Timber.e(t)
            Result.failure(t)
        }
    }
}