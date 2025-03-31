package com.futurae.demoapp.home.usecase

import com.futurae.sdk.FuturaeSDK
import com.futurae.sdk.public_api.auth.model.RejectParameters
import com.futurae.sdk.public_api.auth.model.SessionIdentificationOption
import com.futurae.sdk.public_api.session.model.ApproveInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class RejectAuthRequestUseCase {

    suspend operator fun invoke(
        sessionIdentificationOption: SessionIdentificationOption,
        extraInfo: List<ApproveInfo>?
    ) = withContext(Dispatchers.IO) {
        val parametersBuilder = RejectParameters.Builder(sessionIdentificationOption)

        extraInfo?.let {
            parametersBuilder.setExtraInfo(extraInfo)
        }

        val parameters = parametersBuilder.build()

        try {
            FuturaeSDK.client.authApi.reject(parameters).await()
            Result.success(Unit)
        } catch (t: Throwable) {
            Timber.e(t)
            Result.failure(t)
        }
    }
}