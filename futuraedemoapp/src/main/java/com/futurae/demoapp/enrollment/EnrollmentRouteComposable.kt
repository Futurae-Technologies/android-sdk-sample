package com.futurae.demoapp.enrollment

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.futurae.demoapp.navigation.FuturaeDemoDestinations
import com.futurae.demoapp.R
import com.futurae.demoapp.arch.PinProviderViewModel
import com.futurae.demoapp.enrollment.arch.EnrollmentViewModel
import com.futurae.demoapp.lock.arch.LockScreenMode
import com.futurae.demoapp.navigation.navigateToLockScreen
import com.futurae.demoapp.ui.TextWrapper
import com.futurae.demoapp.ui.shared.elements.alertdialog.FuturaeAlertDialog
import com.futurae.demoapp.ui.shared.elements.alertdialog.FuturaeAlertDialogUIState
import com.futurae.demoapp.ui.shared.elements.bottomnavigationbar.bottomNavigationTo
import com.futurae.demoapp.ui.shared.elements.buttons.ActionButton
import com.futurae.demoapp.ui.shared.elements.resultinformativescreen.ResultInformativeComposable
import com.futurae.demoapp.ui.shared.elements.resultinformativescreen.ResultState
import com.futurae.demoapp.ui.theme.FuturaeTypography
import com.futurae.demoapp.ui.theme.OnPrimaryColor
import com.futurae.demoapp.ui.theme.Tertiary
import com.futurae.sdk.public_api.account.model.EnrollmentInput
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun EnrollmentRouteComposable(
    enrollmentInput: EnrollmentInput,
    pinProviderViewModel: PinProviderViewModel,
    onHideTopAppBar: () -> Unit,
    onShowCommonTopAppBar: (Int) -> Unit,
    onShowResultTopAppBar: (ResultState, TextWrapper) -> Unit,
    navController: NavController,
) {
    val viewModel: EnrollmentViewModel = viewModel(
        factory = EnrollmentViewModel.provideFactory(
            enrollmentInput = enrollmentInput
        )
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val shouldRequestPin by viewModel.pinRequestFlow.collectAsStateWithLifecycle()
    val shouldActivateBiometrics by viewModel.activateBiometricsFlow.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is EnrollmentUIState.FlowBindingTokenInput -> {
            FlowBindingTokenComposable(
                token = state.token,
                onTextChanges = viewModel::onFlowBindingTokenChange,
                onSubmit = viewModel::onFlowBindingTokenSubmitted
            )
        }

        is EnrollmentUIState.Idle -> {
            // waiting for pin
            Box(modifier = Modifier.fillMaxSize())
        }

        is EnrollmentUIState.Result -> {
            ResultInformativeComposable(
                uiState = state.uiState,
                onAction = {
                    viewModel.onResultActionClick()
                }
            )

            if (state.shouldPromptUserToEnableBiometrics) {
                FuturaeAlertDialog(
                    uiState = FuturaeAlertDialogUIState(
                        title = TextWrapper.Resource(R.string.enable_biometrics_prompt_title),
                        text = TextWrapper.Resource(R.string.enable_biometrics_prompt_description),
                        confirmButtonCta = TextWrapper.Resource(R.string.enable_biometrics_prompt_confirmation),
                        dismissButtonCta = TextWrapper.Resource(R.string.enable_biometrics_prompt_skip)
                    ),
                    onDismiss = viewModel::skipEnablingBiometrics,
                    onDeny = viewModel::skipEnablingBiometrics,
                    onConfirm = viewModel::enableBiometrics
                )
            }
        }
    }

    LaunchedEffect(uiState) {
        when (val currentState = uiState) {
            is EnrollmentUIState.FlowBindingTokenInput -> onShowCommonTopAppBar(R.string.flow_binding_token)
            EnrollmentUIState.Idle -> onHideTopAppBar()
            is EnrollmentUIState.Result -> onShowResultTopAppBar(
                currentState.uiState.resultInformativeScreenUIState.state,
                currentState.uiState.resultInformativeScreenUIState.title
            )
        }
    }

    LaunchedEffect(shouldRequestPin) {
        if (shouldRequestPin) {
            navController.navigateToLockScreen(LockScreenMode.CREATE_PIN)
            viewModel.pinRequested()
        }
    }

    LaunchedEffect(shouldActivateBiometrics) {
        if (shouldActivateBiometrics) {
            navController.navigateToLockScreen(LockScreenMode.ACTIVATE_BIO)
            viewModel.activatingBiometricsRequested()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.exitScreen
            .onEach { navController.popBackStack() }
            .launchIn(this)

        viewModel.navigateToAccounts
            .onEach {
                navController.popBackStack()
                navController.bottomNavigationTo(FuturaeDemoDestinations.ACCOUNTS_ROUTE)
            }
            .launchIn(this)

        pinProviderViewModel.pinFlow
            .onEach {
                viewModel.onPinProvided(it)
                pinProviderViewModel.reset()
            }
            .launchIn(this)
    }
}

@Composable
fun FlowBindingTokenComposable(
    token: String,
    onTextChanges: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OnPrimaryColor),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
        ) {
            Image(
                painter = painterResource(id = R.drawable.graphic_manual_entry),
                contentDescription = "Flow binding token",
            )

            Text(
                text = stringResource(R.string.flow_binding_token_prompt),
                textAlign = TextAlign.Center,
                style = FuturaeTypography.titleH5
            )


            TextField(
                value = token,
                onValueChange = { onTextChanges(it) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    textAlign = TextAlign.Center
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Tertiary,
                    unfocusedContainerColor = Tertiary
                ),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                    }
                )
            )
        }

        ActionButton(
            text = TextWrapper.Resource(R.string.submit),
            onClick = { onSubmit() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 40.dp),
            enabled = token.replace(" ", "").isNotEmpty()
        )
    }

}
