package com.futurae.sampleapp.home.accounts

import com.futurae.sampleapp.R
import com.futurae.sampleapp.ui.TextWrapper
import com.futurae.sampleapp.ui.shared.elements.alertdialog.FuturaeAlertDialogUIState
import com.futurae.sdk.public_api.common.model.FTAccount

data class AccountRowUIState(
    private val account: FTAccount,
    val code: String
) {
    val isLocked = account.lockedOut
    val userId = account.userId
    val serviceLogo = account.serviceLogo
    val serviceName = account.serviceName
    val username = if (account.username.isNullOrBlank()) {
        account.userId
    } else {
        account.username ?: ""
    }

    fun getLockedAccountInformativeDialogUIState() = FuturaeAlertDialogUIState(
        title = TextWrapper.Resource(R.string.locked_account_informative_dialog_title),
        text = TextWrapper.Resource(
            R.string.locked_account_informative_dialog_content,
            listOf(account.serviceName)
        ),
        confirmButtonCta = TextWrapper.Resource(R.string.ok)
    )
}