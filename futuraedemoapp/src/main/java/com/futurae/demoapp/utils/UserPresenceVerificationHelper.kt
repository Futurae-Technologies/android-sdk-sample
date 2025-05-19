package com.futurae.demoapp.utils

import androidx.fragment.app.FragmentActivity
import com.futurae.sdk.public_api.common.LockConfigurationType.BIOMETRICS_ONLY
import com.futurae.sdk.public_api.common.LockConfigurationType.BIOMETRICS_OR_DEVICE_CREDENTIALS
import com.futurae.sdk.public_api.common.LockConfigurationType.NONE
import com.futurae.sdk.public_api.common.LockConfigurationType.SDK_PIN_WITH_BIOMETRICS_OPTIONAL
import com.futurae.sdk.public_api.common.model.PresentationConfigurationForBiometricsPrompt
import com.futurae.sdk.public_api.common.model.PresentationConfigurationForDeviceCredentialsPrompt
import com.futurae.sdk.public_api.lock.model.UserPresenceVerificationMode
import com.futurae.sdk.public_api.lock.model.WithBiometrics
import com.futurae.sdk.public_api.lock.model.WithBiometricsOrDeviceCredentials

object UserPresenceVerificationHelper {

    fun getUPVForSystemAuth(activity: FragmentActivity): UserPresenceVerificationMode {
        return when (LocalStorage.persistedSDKConfig.lockConfigurationType) {
            BIOMETRICS_ONLY -> WithBiometrics(
                PresentationConfigurationForBiometricsPrompt(
                    activity,
                    "SDK Recovery",
                    "Authenticate to recover",
                    "Authenticate to recover",
                    "Cancel",
                )
            )

            BIOMETRICS_OR_DEVICE_CREDENTIALS -> WithBiometricsOrDeviceCredentials(
                PresentationConfigurationForDeviceCredentialsPrompt(
                    activity,
                    "SDK Recovery",
                    "Authenticate to recover",
                    "Authenticate to recover",
                )
            )

            NONE,
            SDK_PIN_WITH_BIOMETRICS_OPTIONAL -> throw IllegalStateException("System Auth not supported for config NONE or SDK_PIN_WITH_BIOMETRICS_OPTIONAL")
        }
    }

}