package com.futurae.demoapp.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.futurae.demoapp.FuturaeDemoApplication
import com.futurae.demoapp.R
import com.futurae.demoapp.navigation.RootNavigationEvent
import com.futurae.demoapp.splash.arch.SplashViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun SplashScreenRoute(
    checkForMigratableAccounts: () -> Unit,
    navigateTo: (route: RootNavigationEvent) -> Unit,
) {
    val application = LocalContext.current.applicationContext as FuturaeDemoApplication
    val splashViewModel: SplashViewModel = viewModel(
        factory = SplashViewModel.provideFactory(application = application)
    )

    SplashScreen()

    LaunchedEffect(Unit) {
        splashViewModel.navigationInfo
            .onEach { navigateTo(it) }
            .launchIn(this)

        splashViewModel.checkForMigratableAccounts
            .onEach { checkForMigratableAccounts() }
            .launchIn(this)
    }
}

@Composable
private fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = "Futurae Technologies Logo",
            modifier = Modifier.size(240.dp)
        )
    }
}

@Preview(showSystemUi = false)
@Composable
private fun SplashScreenPreview() {
    SplashScreen()
}