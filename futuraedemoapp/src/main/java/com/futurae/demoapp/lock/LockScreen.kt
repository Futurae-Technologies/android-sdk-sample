package com.futurae.demoapp.lock

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.futurae.demoapp.FuturaeDemoApplication
import com.futurae.demoapp.R
import com.futurae.demoapp.arch.PinProviderViewModel
import com.futurae.demoapp.ui.theme.BgLogo
import com.futurae.demoapp.ui.theme.H2TestStyle
import com.futurae.demoapp.ui.theme.ItemTitleStyle
import com.futurae.demoapp.ui.theme.WarningColor
import com.futurae.sdk.public_api.common.model.PresentationConfigurationForBiometricsPrompt
import com.futurae.sdk.public_api.common.model.PresentationConfigurationForDeviceCredentialsPrompt
import com.futurae.sdk.public_api.lock.model.WithBiometrics
import com.futurae.sdk.public_api.lock.model.WithBiometricsOrDeviceCredentials
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun LockScreen(
    navController: NavController,
    configuration: LockScreenConfiguration,
    pinProviderViewModel: PinProviderViewModel
) {
    val fragmentActivity = LocalActivity.current as FragmentActivity
    val application = LocalContext.current.applicationContext as FuturaeDemoApplication
    val viewModel: LockScreenViewModel = viewModel(
        factory = LockScreenViewModel.provideFactory(
            configuration = configuration,
            application = application
        )
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (configuration.lockMode) {
        LockScreenMode.UNLOCK,
        LockScreenMode.ACTIVATE_BIO -> {
            BackHandler(enabled = true) {
                // if locked state, prevent user from navigating out of current screen
            }
        }

        LockScreenMode.GET_PIN,
        LockScreenMode.CREATE_PIN,
        LockScreenMode.CHANGE_PIN -> {
            BackHandler(enabled = true) {
                // if Pin requested for operation, return null pin to pinProviderViewModel
                viewModel.onPinCanceled()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLogo)
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.wrapContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(56.dp))
            Logo()

            when (uiState) {
                is LockScreenUIState.BioCredsScreen -> {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = uiState.title.value(LocalContext.current),
                        style = H2TestStyle,
                        color = Color.White,
                        modifier = Modifier
                            .padding(horizontal = 40.dp)
                            .clickable {
                                viewModel.onSystemAuthRequested()
                            },
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.weight(2f))
                }

                is LockScreenUIState.PinScreen -> {
                    PinLockScreen(
                        uiState = uiState as LockScreenUIState.PinScreen,
                        configuration = configuration,
                        onDigitEntered = {
                            viewModel.onDigitEntered(it)
                        },
                        onDeleteDigit = {
                            viewModel.onDeleteDigitPressed()
                        }
                    )
                }
            }

            uiState.supportAlternativeAuthText?.let {
                TextButton(
                    content = {
                        Text(
                            text = it.value(LocalContext.current),
                            style = ItemTitleStyle,
                            color = Color.White
                        )
                    },
                    onClick = {
                        viewModel.onAlternativeAuthRequest()
                    }
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.unlockEventFlow
            .onEach {
                when (it) {
                    UnlockRequired.BIOMETRICS -> viewModel.unlock(
                        WithBiometrics(
                            PresentationConfigurationForBiometricsPrompt(
                                fragmentActivity,
                                fragmentActivity.getString(R.string.unlock_with_biometrics),
                                null,
                                null,
                                fragmentActivity.getString(R.string.cancel),
                            )
                        )
                    )

                    UnlockRequired.BIOMETRICS_OR_CREDS -> viewModel.unlock(
                        WithBiometricsOrDeviceCredentials(
                            PresentationConfigurationForDeviceCredentialsPrompt(
                                fragmentActivity,
                                fragmentActivity.getString(R.string.unlock_with_biometrics_or_creds),
                                null,
                                null,
                            )
                        )
                    )
                }
            }
            .launchIn(this)

        viewModel.activateBiometricsFlow
            .onEach {
                viewModel.activateBiometrics(
                    WithBiometrics(
                        PresentationConfigurationForBiometricsPrompt(
                            fragmentActivity,
                            fragmentActivity.getString(R.string.unlock_with_biometrics),
                            null,
                            fragmentActivity.getString(R.string.activate_biometrics),
                            fragmentActivity.getString(R.string.cancel),
                        )
                    )
                )
            }
            .launchIn(this)

        viewModel.exitScreenFlow
            .onEach { pin ->
                pinProviderViewModel.setResult(pin)
                navController.popBackStack()
            }
            .launchIn(this)
    }
}

@Composable
fun PinLockScreen(
    uiState: LockScreenUIState.PinScreen,
    configuration: LockScreenConfiguration,
    onDigitEntered: (Int) -> Unit,
    onDeleteDigit: () -> Unit,
) {
    Spacer(Modifier.height(8.dp))
    Text(
        text = uiState.title.value(LocalContext.current),
        style = ItemTitleStyle,
        color = Color.White
    )
    Spacer(Modifier.height(12.dp))
    DigitCounter(
        digitsEntered = uiState.digitsEntered,
        pinLength = configuration.maxDigitsAllowed,
        error = uiState.error?.value(LocalContext.current)
    )
    Spacer(Modifier.height(56.dp))
    NumPad(
        onDigitEntered = onDigitEntered,
        onBackspace = onDeleteDigit
    )
    Spacer(Modifier.height(12.dp))
}

@Composable
fun DigitCounter(
    digitsEntered: Array<Int>,
    pinLength: Int,
    error: String? = null,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.Center) {
            repeat(pinLength) { index ->
                val isFilled = index < digitsEntered.size
                Spacer(Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = if (isFilled) Color.White else Color.Gray,
                            shape = CircleShape
                        )
                )
                Spacer(Modifier.width(4.dp))
            }
        }
        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.Center) {
                Text(
                    text = error,
                    color = WarningColor,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

            }
        }
    }

}

@Composable
fun NumPad(
    onDigitEntered: (Int) -> Unit,
    onBackspace: () -> Unit
) {
    val numbers = listOf(
        listOf(1, 2, 3),
        listOf(4, 5, 6),
        listOf(7, 8, 9),
        listOf(null, 0, -1)
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (row in numbers) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (num in row) {
                    if (num != null) {
                        NumPadButton(
                            number = num,
                            onClick = {
                                if (num == -1) onBackspace() else onDigitEntered(num)
                            }
                        )
                    } else {
                        Spacer(modifier = Modifier.size(64.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun NumPadButton(number: Int, onClick: () -> Unit) {
    val label = if (number == -1) "←" else number.toString()

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true)
            ) { onClick() }
            .background(Color.DarkGray, shape = CircleShape)
            .padding(8.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun Logo() {
    Image(
        painter = painterResource(id = R.drawable.ic_futurae_logo),
        contentDescription = "Header Image",
        modifier = Modifier.size(74.dp),
        contentScale = ContentScale.Fit
    )
}