package com.futurae.demoapp.settings.sdksettings

import com.futurae.sdk.FuturaeSDK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class ChangeSDKPinUseCase {

    suspend operator fun invoke(newSdkPin: CharArray) = withContext(Dispatchers.IO) {
        try {
            FuturaeSDK.client.lockApi.changeSDKPin(newSdkPin).await()
            Result.success(Unit)
        } catch (t: Throwable) {
            Timber.e(t)
            Result.failure(t)
        }
    }
}