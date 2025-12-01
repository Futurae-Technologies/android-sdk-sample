package com.futurae.sampleapp.ui.shared.elements.configuration

import androidx.annotation.StringRes
import com.futurae.sampleapp.R
import com.futurae.sampleapp.ui.TextWrapper
import com.futurae.sampleapp.utils.UITestTags
import com.futurae.sdk.public_api.common.LockConfigurationType

sealed class ConfigurationItem(
    open val title: TextWrapper,
    open val subtitle: TextWrapper,
    open val isExpanded: Boolean,
)

data class LockConfigurationItem(
    override val title: TextWrapper,
    override val subtitle: TextWrapper,
    override val isExpanded: Boolean = false,
    val selectedChoice: LockConfigurationType?,
) : ConfigurationItem(title, subtitle, isExpanded)

data class LockDurationItem(
    override val title: TextWrapper,
    override val subtitle: TextWrapper,
    override val isExpanded: Boolean = false,
    val selectedChoice: Int?,
) : ConfigurationItem(title, subtitle, isExpanded)

data class OptionConfigurationsItem(
    override val title: TextWrapper,
    override val subtitle: TextWrapper,
    override val isExpanded: Boolean = false,
    val items: Map<SdkConfigOptionalFlag, Boolean>,
) : ConfigurationItem(title, subtitle, isExpanded)

enum class SdkConfigOptionalFlag(@StringRes val title: Int, val testTag : String) {
    SKIP_HW_SECURITY(R.string.sdk_flag_skip_hw_sec, UITestTags.ToggleSkipHWSec.tag),
    REQUIRE_UNLOCKED_DEVICE(R.string.sdk_flag_req_unlocked_device, UITestTags.ToggleReqUnlockedDevice.tag),
    BIOMETRIC_INVALIDATION(R.string.sdk_flag_biometric_invalidation, UITestTags.ToggleBiometricInvalidation.tag),
    CHANGE_PIN_WITH_BIOMETRICS(R.string.sdk_flag_change_pin_with_bio, UITestTags.ToggleChangePinWithBio.tag),
}