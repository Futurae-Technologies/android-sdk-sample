package com.futurae.demoapp.arch

import com.futurae.sdk.public_api.common.model.FTAccount

sealed class AccountInput {
    data object Unspecified: AccountInput()
    data class Specified(val account: FTAccount): AccountInput()
}