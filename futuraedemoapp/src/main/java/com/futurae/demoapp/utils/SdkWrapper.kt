package com.futurae.demoapp.utils

import android.app.Application
import com.futurae.demoapp.R
import com.futurae.sdk.FuturaeSDK
import com.futurae.sdk.public_api.common.SDKConfiguration
import com.futurae.sdk.public_api.exception.FTInvalidArgumentException
import com.futurae.sdk.public_api.exception.FTInvalidStateException
import com.futurae.sdk.public_api.exception.FTKeyNotFoundException
import com.futurae.sdk.public_api.exception.FTKeystoreOperationException
import com.futurae.sdk.public_api.exception.FTLockInvalidConfigurationException
import com.futurae.sdk.public_api.exception.FTLockMechanismUnavailableException

object SdkWrapper {

    fun attemptToLaunchSDK(
        application: Application,
        sdkConfiguration: SDKConfiguration,

        ) {
        FuturaeSDK.launch(
            application = application,
            sdkConfiguration = sdkConfiguration
        )
        LocalStorage.persistSDKConfiguration(sdkConfiguration)
    }

    fun attemptToLaunchSDKSilently(application: Application): Boolean {
        if (!LocalStorage.hasExistingConfiguration) return false

        return try {
            FuturaeSDK.launch(
                application = application,
                sdkConfiguration = LocalStorage.persistedSDKConfig
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getStringForSDKError(throwable: Throwable): Int {
        return throwable.findErrorTitleResId()
    }

    private fun Throwable.findErrorTitleResId() = when (this) {
        is FTInvalidStateException -> R.string.sdk_init_error_already_initialized

        // Indicates `futurae.xml` SDK credentials are invalid
        is FTInvalidArgumentException -> R.string.sdk_init_error_invalid_arg

        // Indicates that provided SDK configuration is invalid
        is FTLockInvalidConfigurationException -> R.string.sdk_init_error_config_error

        // Indicates that provided SDK configuration is valid but cannot be supported on this device
        is FTLockMechanismUnavailableException -> R.string.sdk_init_error_unsupported_config

        // Indicates that an SDK cryptographic operation failed
        is FTKeystoreOperationException -> R.string.sdk_init_error_cryptography_error

        // Indicates that cryptographic keys are missing.
        is FTKeyNotFoundException -> R.string.sdk_init_error_missing_key

        else -> R.string.sdk_init_error_initialization_failed
    }
}