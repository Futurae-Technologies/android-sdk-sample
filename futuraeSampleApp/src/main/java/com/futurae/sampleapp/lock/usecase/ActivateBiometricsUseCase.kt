package com.futurae.sampleapp.lock.usecase

import com.futurae.sdk.FuturaeSDK
import com.futurae.sdk.public_api.lock.model.WithBiometrics

class ActivateBiometricsUseCase {

    suspend operator fun invoke(userPresenceVerificationMode: WithBiometrics) {
        FuturaeSDK.client.lockApi
            .activateBiometrics(presentationConfigurationForBiometricsPrompt = userPresenceVerificationMode.presentationConfiguration)
            .await()
    }
}