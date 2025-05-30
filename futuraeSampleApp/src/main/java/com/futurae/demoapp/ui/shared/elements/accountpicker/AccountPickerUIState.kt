package com.futurae.demoapp.ui.shared.elements.accountpicker

import com.futurae.sdk.public_api.common.model.FTAccount

data class AccountPickerUIState(
    val items: List<ActiveAccountItemUIState>,
    val selectedAccount: FTAccount?
) {
    val canProceed = selectedAccount != null
}

data class ActiveAccountItemUIState(
    val account: FTAccount,
    val isSelected: Boolean
) {
    val username = account.username ?: "-"
    val serviceName = account.serviceName
    val serviceLogo = account.serviceLogo
}