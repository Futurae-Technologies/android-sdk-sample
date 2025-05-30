package com.futurae.sampleapp.ui.shared.elements.bottomnavigationbar

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.futurae.sampleapp.navigation.FuturaeSampleDestinations
import com.futurae.sampleapp.ui.shared.elements.bottomnavigationbar.BottomNavigationBarItem.Companion.bottomNavigationBarItems
import com.futurae.sampleapp.ui.theme.OnPrimaryColor
import com.futurae.sampleapp.ui.theme.OnSecondaryColor
import com.futurae.sampleapp.ui.theme.PrimaryColor

@Composable
fun FuturaeBottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    if (!currentDestination?.route.hasBottomNavigation()) {
        return
    }

    NavigationBar(
        containerColor = PrimaryColor,
        contentColor = OnSecondaryColor
    ) {
        bottomNavigationBarItems.forEach { navigationItem ->
            val isSelected = currentDestination
                ?.hierarchy
                ?.any { it.route?.startsWith(navigationItem.destination.route) ?: false } == true

            val tintColor = if (isSelected) {
                OnPrimaryColor
            } else {
                OnSecondaryColor
            }

            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(navigationItem.iconWhenSelected),
                        contentDescription = stringResource(navigationItem.name),
                        tint = tintColor
                    )
                },
                label = {
                    Text(
                        text = stringResource(navigationItem.name),
                        color = tintColor
                    )
                },
                selected = isSelected,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,
                ),
                onClick = {
                    navController.bottomNavigationTo(navigationItem.destination)
                }
            )
        }
    }
}

fun NavController.bottomNavigationTo(destination: FuturaeSampleDestinations) {
    navigate(destination.route) {
        // Pop up to the start destination of the graph to
        // avoid building up a large stack of destinations
        // on the back stack as users select items
        popUpTo(FuturaeSampleDestinations.ACCOUNTS_ROUTE.route) {
            inclusive = false
            saveState = true
        }

        // Avoid multiple copies of the same destination when
        // reselecting the same item
        launchSingleTop = true
        restoreState = destination.route !in listOf(
            FuturaeSampleDestinations.QR_SCANNER_ROUTE.route,
            FuturaeSampleDestinations.ACTIVATION_CODE_ROUTE.route
        )
    }
}

fun String?.hasBottomNavigation(): Boolean {
    this ?: return false

    return this.startsWith("home/")
}