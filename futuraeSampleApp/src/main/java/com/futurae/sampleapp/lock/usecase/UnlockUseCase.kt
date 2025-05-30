package com.futurae.sampleapp.lock.usecase

import com.futurae.sdk.FuturaeSDK
import com.futurae.sdk.public_api.lock.model.UserPresenceVerificationMode

class UnlockUseCase {

    suspend operator fun invoke(userPresenceVerificationMode: UserPresenceVerificationMode) {
        FuturaeSDK.client.lockApi.unlock(
            userPresenceVerificationMode = userPresenceVerificationMode,
            shouldWaitForSDKSync = true
        ).await()
    }
}