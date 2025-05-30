package com.futurae.demoapp.settings.debug

import com.futurae.sdk.FuturaeSDK

class DeactivateBiometricsUseCase {

    suspend operator fun invoke() {
        FuturaeSDK.client.lockApi.deactivateBiometrics().await()
    }
}