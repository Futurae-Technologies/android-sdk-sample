package com.futurae.demoapp.ui.shared.elements.authenticationconfirmationscreen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.futurae.demoapp.ui.shared.elements.authenticationconfirmationscreen.arch.AuthenticationViewModel
import com.futurae.demoapp.ui.theme.OnPrimaryColor
import com.futurae.demoapp.ui.theme.SuccessColor

@Composable
fun AuthenticationRouteComposable(
    viewModel: AuthenticationViewModel,
    navController: NavController,
    snackbarHostState: SnackbarHostState
) {
    BackHandler(enabled = true) {
        // Prevent user from navigating out of current screen
    }

    val approvalUIState by viewModel.approvalUIState.collectAsStateWithLifecycle()
    val timeoutProgress by viewModel.timeoutCountdownProgress.collectAsStateWithLifecycle()
    val shouldShowLoader by viewModel.showProgressLoader.collectAsStateWithLifecycle()

    LaunchedEffect(approvalUIState) {
        if (approvalUIState == null) {
            navController.navigateUp()
        }
    }

    when (val state = approvalUIState) {
        is AuthenticationConfirmationComposableScreenUIState.AuthenticationConfirmationScreenUIState -> {
            Box(contentAlignment = Alignment.Center) {
                AuthenticationConfirmationScreen(
                    uiState = state,
                    timeoutCountdownProgress = timeoutProgress,
                    onApprove = {
                        viewModel.responseToApprovalRequest(AuthenticationConfirmationUserResponse.APPROVE)
                    },
                    onReject = {
                        viewModel.responseToApprovalRequest(AuthenticationConfirmationUserResponse.REJECT)
                    },
                    onMultiNumberedChallengeResponse = {
                        viewModel.onMultiNumberChallengeResponse(it)
                    },
                    snackbarHostState = snackbarHostState
                )

                if (shouldShowLoader) {
                    CircularProgressIndicator(color = SuccessColor)
                }
            }
        }

        AuthenticationConfirmationComposableScreenUIState.Loading -> {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(OnPrimaryColor),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SuccessColor)
            }
        }

        null -> {}
    }
}