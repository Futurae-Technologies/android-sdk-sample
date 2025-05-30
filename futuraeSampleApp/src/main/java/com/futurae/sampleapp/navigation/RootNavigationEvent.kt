package com.futurae.sampleapp.navigation

import androidx.annotation.StringRes

sealed class RootNavigationEvent(val route: String) {
    data object Configuration : RootNavigationEvent(
        route = FuturaeSampleDestinations.CONFIGURATION_ROUTE.route
    )

    data object Home : RootNavigationEvent(
        route = FuturaeSampleDestinations.ACCOUNTS_ROUTE.route
    )

    class Error(@StringRes val title: Int, val message: String) : RootNavigationEvent(
        route = "${FuturaeSampleDestinations.ERROR_ROUTE.route}?" +
                "${NavigationArguments.ErrorRoute.TITLE_NAV_ARG}=$title" +
                "&${NavigationArguments.ErrorRoute.MESSAGE_NAV_ARG}=$message"
    )

    data object Recovery : RootNavigationEvent(
        route = FuturaeSampleDestinations.SDK_RESTORATION_ROUTE.route
    )
}