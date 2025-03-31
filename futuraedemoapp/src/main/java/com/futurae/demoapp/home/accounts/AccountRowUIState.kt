package com.futurae.demoapp.home.accounts

import com.futurae.demoapp.R
import com.futurae.demoapp.ui.TextWrapper
import com.futurae.demoapp.ui.shared.elements.alertdialog.FuturaeAlertDialogUIState
import com.futurae.sdk.public_api.common.model.FTAccount

data class AccountRowUIState(
    private val account: FTAccount,
    val code: String
) {
    val isLocked = account.lockedOut
    val userId = account.userId
    val serviceLogo = account.serviceLogo
    val serviceName = account.serviceName
    val username = account.username ?: "-"

    fun getLockedAccountInformativeDialogUIState() = FuturaeAlertDialogUIState(
        title = TextWrapper.Resource(R.string.locked_account_informative_dialog_title),
        text = TextWrapper.Resource(
            R.string.locked_account_informative_dialog_content,
            listOf(account.serviceName)
        ),
        confirmButtonCta = TextWrapper.Resource(R.string.ok)
    )
}