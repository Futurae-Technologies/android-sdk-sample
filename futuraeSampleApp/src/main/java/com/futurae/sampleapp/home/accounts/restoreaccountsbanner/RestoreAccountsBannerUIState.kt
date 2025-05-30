package com.futurae.sampleapp.home.accounts.restoreaccountsbanner


sealed class RestoreAccountsBannerUIState {
    data object None : RestoreAccountsBannerUIState()
    data object InformativeForSettingsEntryPoint : RestoreAccountsBannerUIState()
    data class SuccessfulCheck(val isPinProtected: Boolean) : RestoreAccountsBannerUIState()
    data object FailedCheck : RestoreAccountsBannerUIState()
}