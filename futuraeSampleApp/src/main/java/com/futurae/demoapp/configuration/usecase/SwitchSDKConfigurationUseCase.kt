package com.futurae.demoapp.configuration.usecase

import android.app.Application
import com.futurae.demoapp.utils.LocalStorage
import com.futurae.sdk.FuturaeSDK
import com.futurae.sdk.public_api.lock.model.SwitchTargetLockConfiguration

class SwitchSDKConfigurationUseCase {

    suspend operator fun invoke(
        app: Application,
        targetLockConfiguration: SwitchTargetLockConfiguration,
    ) {
        FuturaeSDK.client.lockApi.switchToLockConfiguration(
            app,
            targetLockConfiguration
        ).await()
        LocalStorage.persistSDKConfiguration(targetLockConfiguration.sdkConfiguration)
    }
}