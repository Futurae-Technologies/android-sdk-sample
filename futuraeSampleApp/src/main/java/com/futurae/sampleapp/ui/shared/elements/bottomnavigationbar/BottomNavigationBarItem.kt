package com.futurae.sampleapp.ui.shared.elements.bottomnavigationbar

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.futurae.sampleapp.navigation.FuturaeSampleDestinations
import com.futurae.sampleapp.R

data class BottomNavigationBarItem(
    val destination: FuturaeSampleDestinations,
    @DrawableRes val iconWhenSelected: Int,
    @DrawableRes val iconWhenNotSelected: Int,
    @StringRes val name: Int
) {
    companion object {
        val bottomNavigationBarItems = listOf(
            BottomNavigationBarItem(
                destination = FuturaeSampleDestinations.ACCOUNTS_ROUTE,
                iconWhenSelected = R.drawable.ic_accounts_filled,
                iconWhenNotSelected = R.drawable.ic_accounts_outline,
                name = R.string.bottom_navigation_accounts_item
            ),
            BottomNavigationBarItem(
                destination = FuturaeSampleDestinations.QR_SCANNER_ROUTE,
                iconWhenSelected = R.drawable.ic_qr_scanner_filled,
                iconWhenNotSelected = R.drawable.ic_qr_scanner_outline,
                name = R.string.bottom_navigation_scan_item
            ),
            BottomNavigationBarItem(
                destination = FuturaeSampleDestinations.ACTIVATION_CODE_ROUTE,
                iconWhenSelected = R.drawable.ic_manual_entry_filled,
                iconWhenNotSelected = R.drawable.ic_manual_entry_outline,
                name = R.string.bottom_navigation_manual_entry_item
            ),
            BottomNavigationBarItem(
                destination = FuturaeSampleDestinations.MORE_ROUTE,
                iconWhenSelected = R.drawable.ic_more_filled,
                iconWhenNotSelected = R.drawable.ic_more_outline,
                name = R.string.bottom_navigation_more_item
            )
        )
    }
}