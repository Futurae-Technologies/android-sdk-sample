package com.futurae.demoapp.home.accounts

data class AccountsScreenUIState(
    val accountRowUIStates: List<AccountRowUIState>
) {
    val hasEnrolledAccounts: Boolean = accountRowUIStates.isNotEmpty()
}