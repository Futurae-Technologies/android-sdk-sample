package com.futurae.demoapp

import androidx.annotation.StringRes

sealed class RootNavigationEvent(val route: String) {
    data object Configuration : RootNavigationEvent(
        route = FuturaeDemoDestinations.CONFIGURATION_ROUTE.route
    )

    data object Home : RootNavigationEvent(
        route = FuturaeDemoDestinations.ACCOUNTS_ROUTE.route
    )

    class Error(@StringRes val title: Int, val message: String) : RootNavigationEvent(
        route = "${FuturaeDemoDestinations.ERROR_ROUTE.route}?" +
                "${NavigationArguments.ErrorRoute.TITLE_NAV_ARG}=$title" +
                "&${NavigationArguments.ErrorRoute.MESSAGE_NAV_ARG}=$message"
    )

    data object Recovery : RootNavigationEvent(
        route = FuturaeDemoDestinations.SDK_RESTORATION_ROUTE.route
    )
}