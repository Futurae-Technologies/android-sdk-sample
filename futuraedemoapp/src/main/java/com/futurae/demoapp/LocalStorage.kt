package com.futurae.demoapp

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.futurae.sdk.public_api.common.LockConfigurationType
import com.futurae.sdk.public_api.common.SDKConfiguration

object LocalStorage {

    private const val SP_NAME = "FUTURAE_DEMO_APP_SHARED_PREF"
    private const val SP_KEY_LOCK_CONFIGURATION = "SHARED_PREF_LOCK_CONFIG_KEY"
    private const val SP_KEY_DURATION = "SHARED_PREF_LOCK_DURATION_KEY"
    private const val SP_KEY_INVALIDATE_BY_BIOMETRICS = "SHARED_PREF_INVALIDATE_BY_BIOMETRICS_KEY"
    private const val SP_KEY_REQUIRE_DEVICE_UNLOCKED = "SHARED_PREF_REQUIRE_DEVICE_UNLOCKED_KEY"
    private const val SP_KEY_SKIP_HARDWARE_SECURITY = "SHARED_PREF_SKIP_HARDWARE_SECURITY_KEY"
    private const val SP_KEY_ALLOW_SDK_PIN_CHANGE_WITH_BIO =
        "SHARED_PREF_SDK_PIN_CHANGE_WITH_BIO_KEY"

    private const val SP_KEY_FLOW_BINDING_ENABLED = "SP_KEY_FLOW_BINDING_ENABLED"
    private const val SP_KEY_ALLOW_SESSION_INFO_WITHOUT_UNLOCK_ENABLED = "SP_KEY_ALLOW_SESSION_INFO_WITHOUT_UNLOCK_ENABLED"
    private const val SP_KEY_ADAPTIVE_ENABLED = "SP_KEY_ADAPTIVE_ENABLED"
    private const val SP_KEY_ADAPTIVE_AUTH_ENABLED = "SP_KEY_ADAPTIVE_AUTH_ENABLED"
    private const val SP_KEY_ADAPTIVE_MIGRATION_ENABLED = "SP_KEY_ADAPTIVE_MIGRATION_ENABLED"
    private const val SP_KEY_DEVICE_ENROLLED = "SP_KEY_DEVICE_ENROLLED"
    private const val SP_KEY_ACCOUNT_RESTORATION_INFORMED = "SP_KEY_ACCOUNT_RESTORATION_INFORMED"
    private const val SP_KEY_ACCOUNTS_RESTORED = "SP_KEY_ACCOUNTS_RESTORED"

    private lateinit var sharedPrefs: SharedPreferences

    fun init(context: Context) {
        sharedPrefs = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
    }

    val hasExistingConfiguration: Boolean
        get() = sharedPrefs.getString(SP_KEY_LOCK_CONFIGURATION, null) != null &&
                sharedPrefs.getInt(SP_KEY_DURATION, -1) >= 0

    val persistedSDKConfig: SDKConfiguration
        get() = SDKConfiguration.Builder()
            .setLockConfigurationType(
                LockConfigurationType.valueOf(
                    sharedPrefs.getString(SP_KEY_LOCK_CONFIGURATION, "") ?: ""
                )
            )
            .setUnlockDuration(sharedPrefs.getInt(SP_KEY_DURATION, -1))
            .setInvalidatedByBiometricChange(
                sharedPrefs.getBoolean(SP_KEY_INVALIDATE_BY_BIOMETRICS, false)
            )
            .setUnlockedDeviceRequired(
                sharedPrefs.getBoolean(SP_KEY_REQUIRE_DEVICE_UNLOCKED, false)
            )
            .setSkipHardwareSecurity(
                sharedPrefs.getBoolean(SP_KEY_SKIP_HARDWARE_SECURITY, false)
            )
            .setAllowChangePinCodeWithBiometricUnlock(
                sharedPrefs.getBoolean(SP_KEY_ALLOW_SDK_PIN_CHANGE_WITH_BIO, false)
            )
            .build()

    val isPinConfig: Boolean
        get() = persistedSDKConfig.lockConfigurationType == LockConfigurationType.SDK_PIN_WITH_BIOMETRICS_OPTIONAL

    val shouldSetupPin : Boolean
        get() = isPinConfig && !isDeviceEnrolled()

    fun persistSDKConfiguration(config: SDKConfiguration) {
        sharedPrefs.edit {
            putString(SP_KEY_LOCK_CONFIGURATION, config.lockConfigurationType.name)
            putInt(SP_KEY_DURATION, config.unlockDuration)
            putBoolean(SP_KEY_INVALIDATE_BY_BIOMETRICS, config.invalidatedByBiometricChange)
            putBoolean(SP_KEY_REQUIRE_DEVICE_UNLOCKED, config.unlockedDeviceRequired)
            putBoolean(SP_KEY_SKIP_HARDWARE_SECURITY, config.skipHardwareSecurity)
            putBoolean(
                SP_KEY_ALLOW_SDK_PIN_CHANGE_WITH_BIO,
                config.allowChangePinCodeWithBiometricUnlock
            )
        }
    }

    fun reset() = sharedPrefs.edit {
        clear()
    }

    fun isFlowBindingEnabled() = sharedPrefs.getBoolean(SP_KEY_FLOW_BINDING_ENABLED, false)
    fun setFlowBindingEnabled(enabled: Boolean) = sharedPrefs.edit {
        putBoolean(SP_KEY_FLOW_BINDING_ENABLED, enabled)
    }

    fun isAdaptiveEnabled() = sharedPrefs.getBoolean(SP_KEY_ADAPTIVE_ENABLED, false)
    fun setAdaptiveEnabled(enabled: Boolean) = sharedPrefs.edit {
        putBoolean(SP_KEY_ADAPTIVE_ENABLED, enabled)
    }

    fun isAdaptiveAuthEnabled() = sharedPrefs.getBoolean(SP_KEY_ADAPTIVE_AUTH_ENABLED, false)
    fun setAdaptiveAuthEnabled(enabled: Boolean) = sharedPrefs.edit {
        putBoolean(SP_KEY_ADAPTIVE_AUTH_ENABLED, enabled)
    }

    fun isAdaptiveMigrationEnabled() =
        sharedPrefs.getBoolean(SP_KEY_ADAPTIVE_MIGRATION_ENABLED, false)

    fun setAdaptiveMigrationEnabled(enabled: Boolean) = sharedPrefs.edit {
        putBoolean(SP_KEY_ADAPTIVE_MIGRATION_ENABLED, enabled)
    }

    val isSessionInfoWithoutUnlockEnabled: Boolean
        get() = sharedPrefs.getBoolean(SP_KEY_ALLOW_SESSION_INFO_WITHOUT_UNLOCK_ENABLED, false)

    fun setSessionInfoWithoutUnlockEnabled(enabled: Boolean) = sharedPrefs.edit {
        putBoolean(SP_KEY_ALLOW_SESSION_INFO_WITHOUT_UNLOCK_ENABLED, enabled)
    }

    fun isDeviceEnrolled() = sharedPrefs.getBoolean(SP_KEY_DEVICE_ENROLLED, false)
    fun setDeviceEnrolled() = sharedPrefs.edit {
        putBoolean(SP_KEY_DEVICE_ENROLLED, true)
    }

    fun setAccountsRestored() = sharedPrefs.edit {
        putBoolean(SP_KEY_ACCOUNTS_RESTORED, true)
    }

    val haveAccountsBeenRestored: Boolean
        get() = sharedPrefs.getBoolean(SP_KEY_ACCOUNTS_RESTORED, false)

    fun setUserHasBeenInformedAboutAccountRestoration() = sharedPrefs.edit {
        putBoolean(SP_KEY_ACCOUNT_RESTORATION_INFORMED, true)
    }

    val hasUserBeenInformedAboutAccountRestoration: Boolean
        get() = sharedPrefs.getBoolean(SP_KEY_ACCOUNT_RESTORATION_INFORMED, false)
}