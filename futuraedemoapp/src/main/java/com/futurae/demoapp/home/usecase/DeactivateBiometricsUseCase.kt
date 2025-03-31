package com.futurae.demoapp.home.usecase

import com.futurae.sdk.FuturaeSDK

class DeactivateBiometricsUseCase {

    suspend operator fun invoke() {
        FuturaeSDK.client.lockApi.deactivateBiometrics().await()
    }
}