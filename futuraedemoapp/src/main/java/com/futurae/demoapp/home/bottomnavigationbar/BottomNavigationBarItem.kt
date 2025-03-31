package com.futurae.demoapp.home.bottomnavigationbar

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.futurae.demoapp.FuturaeDemoDestinations
import com.futurae.demoapp.R

data class BottomNavigationBarItem(
    val destination: FuturaeDemoDestinations,
    @DrawableRes val iconWhenSelected: Int,
    @DrawableRes val iconWhenNotSelected: Int,
    @StringRes val name: Int
) {
    companion object {
        val bottomNavigationBarItems = listOf(
            BottomNavigationBarItem(
                destination = FuturaeDemoDestinations.ACCOUNTS_ROUTE,
                iconWhenSelected = R.drawable.ic_accounts_filled,
                iconWhenNotSelected = R.drawable.ic_accounts_outline,
                name = R.string.bottom_navigation_accounts_item
            ),
            BottomNavigationBarItem(
                destination = FuturaeDemoDestinations.QR_SCANNER_ROUTE,
                iconWhenSelected = R.drawable.ic_qr_scanner_filled,
                iconWhenNotSelected = R.drawable.ic_qr_scanner_outline,
                name = R.string.bottom_navigation_scan_item
            ),
            BottomNavigationBarItem(
                destination = FuturaeDemoDestinations.ACTIVATION_CODE_ROUTE,
                iconWhenSelected = R.drawable.ic_manual_entry_filled,
                iconWhenNotSelected = R.drawable.ic_manual_entry_outline,
                name = R.string.bottom_navigation_manual_entry_item
            ),
            BottomNavigationBarItem(
                destination = FuturaeDemoDestinations.MORE_ROUTE,
                iconWhenSelected = R.drawable.ic_more_filled,
                iconWhenNotSelected = R.drawable.ic_more_outline,
                name = R.string.bottom_navigation_more_item
            )
        )
    }
}