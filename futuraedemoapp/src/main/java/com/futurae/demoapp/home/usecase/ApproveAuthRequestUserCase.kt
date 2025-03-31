package com.futurae.demoapp.home.usecase

import com.futurae.sdk.FuturaeSDK
import com.futurae.sdk.public_api.auth.model.ApproveParameters
import com.futurae.sdk.public_api.auth.model.SessionIdentificationOption
import com.futurae.sdk.public_api.session.model.ApproveInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class ApproveAuthRequestUserCase {

    suspend operator fun invoke(
        sessionIdentificationOption: SessionIdentificationOption,
        extraInfo: List<ApproveInfo>? = null,
        choice: Int? = null
    ) = withContext(Dispatchers.IO) {
        val parametersBuilder = ApproveParameters.Builder(sessionIdentificationOption)

        extraInfo?.let {
            parametersBuilder.setExtraInfo(it)
        }

        choice?.let {
            parametersBuilder.setMultiNumberedChallengeChoice(it)
        }

        val parameters = parametersBuilder.build()

        try {
            FuturaeSDK.client.authApi.approve(parameters).await()
            Result.success(Unit)
        } catch (t: Throwable) {
            Timber.e(t)
            Result.failure(t)
        }
    }
}