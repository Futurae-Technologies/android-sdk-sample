package com.futurae.demoapp.recovery

import android.app.Application
import com.futurae.demoapp.LocalStorage
import com.futurae.sdk.Callback
import com.futurae.sdk.FuturaeSDK
import com.futurae.sdk.public_api.lock.model.UserPresenceVerificationMode
import kotlin.coroutines.suspendCoroutine

class SDKRecoveryUseCase {

    suspend operator fun invoke(
        application: Application,
        userPresenceVerificationMode: UserPresenceVerificationMode?
    ): Unit = suspendCoroutine { continuation ->
            val sdkConfiguration = LocalStorage.persistedSDKConfig
            FuturaeSDK.launchAccountRecovery(
                application = application,
                sdkConfiguration = sdkConfiguration,
                userPresenceVerificationMode = userPresenceVerificationMode,
                callback = object : Callback<Unit> {
                    override fun onSuccess(result: Unit) {
                        continuation.resumeWith(Result.success(Unit))
                    }

                    override fun onError(throwable: Throwable) {
                        continuation.resumeWith(Result.failure(throwable))
                    }

                })
        }
}