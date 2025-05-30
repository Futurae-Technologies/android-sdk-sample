package com.futurae.sampleapp.enrollment.usecase

import com.futurae.sampleapp.utils.LocalStorage
import com.futurae.sdk.FuturaeSDK
import com.futurae.sdk.public_api.account.model.EnrollmentParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class EnrollUserUseCase {

    suspend operator fun invoke(
        enrollmentParams: EnrollmentParams
    ) = withContext(Dispatchers.IO) {
        try {
            val account = FuturaeSDK.client.accountApi.enrollAndGetAccount(enrollmentParams).await()
            LocalStorage.setDeviceEnrolled()
            Result.success(account)
        } catch (t: Throwable) {
            Timber.e(t)
            Result.failure(t)
        }
    }
}