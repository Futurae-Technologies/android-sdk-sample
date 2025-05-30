package com.futurae.sampleapp.home.accounts

data class AccountsScreenUIState(
    val accountRowUIStates: List<AccountRowUIState>
) {
    val hasEnrolledAccounts: Boolean = accountRowUIStates.isNotEmpty()
}