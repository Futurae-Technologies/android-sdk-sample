package com.futurae.sampleapp.ui.shared.elements.authenticationconfirmationscreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.futurae.sampleapp.R
import com.futurae.sampleapp.ui.TextWrapper
import com.futurae.sampleapp.ui.shared.elements.buttons.ActionButton
import com.futurae.sampleapp.ui.shared.elements.buttons.ActionButtonType
import com.futurae.sampleapp.ui.shared.elements.buttons.FuturaeOutlinedButton
import com.futurae.sampleapp.ui.shared.elements.serviceinfosection.ServiceInfoSection
import com.futurae.sampleapp.ui.shared.elements.snackbar.FuturaeSnackbarVisuals
import com.futurae.sampleapp.ui.shared.elements.timeoutIndicator.TimeoutIndicator
import com.futurae.sampleapp.ui.theme.DisableColor
import com.futurae.sampleapp.ui.theme.FuturaeTypography
import com.futurae.sampleapp.ui.theme.OnPrimaryColor
import com.futurae.sampleapp.ui.theme.PrimaryColor
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@Composable
fun AuthenticationConfirmationScreen(
    uiState: AuthenticationConfirmationComposableScreenUIState.AuthenticationConfirmationScreenUIState,
    timeoutCountdownProgress: Float,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onMultiNumberedChallengeResponse: (Int) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val listState = rememberLazyListState()
    var areButtonsEnabled by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = OnPrimaryColor)
    ) {
        ServiceInfoSection(
            Modifier
                .fillMaxWidth()
                .background(color = PrimaryColor)
                .padding(top = 52.dp)
                .wrapContentSize(),
            uiState = uiState.serviceInfoSectionUIState,
            textColor = OnPrimaryColor
        )

        TimeoutIndicator(progress = timeoutCountdownProgress)

        AnimatedContent(
            targetState = uiState.content,
            label = "authentication confirmation screen content animation"
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                val shouldShowMultiNumberedChallenge =
                    it is AuthenticationScreenContent.MultiNumberedChallenge

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(
                        if (shouldShowMultiNumberedChallenge) {
                            0.dp
                        } else {
                            16.dp
                        }
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    state = listState
                ) {
                    if (!shouldShowMultiNumberedChallenge) {
                        item {
                            Text(
                                text = uiState.authenticationType.value(context = LocalContext.current),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryColor
                            )
                        }
                    }
                    // todo add date and time

                    when (it) {
                        is AuthenticationScreenContent.Details -> {
                            items(it.details) { detail ->
                                AuthenticationDetailItem(label = detail.label, value = detail.value)
                            }
                        }

                        is AuthenticationScreenContent.MultiNumberedChallenge -> {
                            item {
                                Text(
                                    modifier = Modifier.padding(bottom = 16.dp),
                                    text = stringResource(R.string.multi_numbered_challenge_prompt),
                                    color = PrimaryColor,
                                    style = FuturaeTypography.bodySmallRegular
                                )
                            }

                            items(it.options) { option ->
                                FuturaeOutlinedButton(
                                    modifier = Modifier.fillMaxSize(),
                                    text = TextWrapper.Primitive("$option")
                                ) {
                                    onMultiNumberedChallengeResponse(option)
                                }
                            }
                        }
                    }
                }

                if (listState.canScrollForward) {
                    HorizontalDivider(color = DisableColor)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp)
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    when (it) {
                        is AuthenticationScreenContent.Details -> {
                            ActionButton(
                                text = TextWrapper.Resource(R.string.reject),
                                iconResId = R.drawable.ic_failure,
                                type = ActionButtonType.Warning,
                                onClick = onReject,
                                modifier = Modifier.weight(1f),
                                enabled = areButtonsEnabled,
                            )

                            ActionButton(
                                text = TextWrapper.Resource(R.string.approve),
                                iconResId = R.drawable.ic_success,
                                type = ActionButtonType.Success,
                                onClick = onApprove,
                                modifier = Modifier.weight(1f),
                                enabled = areButtonsEnabled,
                            )
                        }

                        is AuthenticationScreenContent.MultiNumberedChallenge -> {
                            ActionButton(
                                text = TextWrapper.Resource(R.string.deny),
                                type = ActionButtonType.Warning,
                                onClick = onReject,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }

    val promptToScrollMessage = stringResource(R.string.scroll_to_see_all_details_prompt)
    LaunchedEffect(Unit) {
        launch {
            snapshotFlow { listState.canScrollForward }
                .collect { canScrollForward ->
                    if (canScrollForward) {
                        val hasAlreadyPromptUserToScroll = areButtonsEnabled
                        if (!hasAlreadyPromptUserToScroll) {
                            snackbarHostState.showSnackbar(
                                FuturaeSnackbarVisuals(
                                    message = promptToScrollMessage,
                                    isError = false
                                )
                            )
                        }
                    } else {
                        areButtonsEnabled = true
                    }
                }
        }

        launch {
            snapshotFlow { listState.isScrollInProgress }
                .filter { it }
                .collect {
                    snackbarHostState.currentSnackbarData?.dismiss()
                }
        }
    }
}

@Composable
private fun AuthenticationDetailItem(label: TextWrapper, value: TextWrapper) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label.value(LocalContext.current),
            style = MaterialTheme.typography.bodyMedium,
            color = PrimaryColor
        )
        Text(
            text = value.value(LocalContext.current),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = PrimaryColor
        )
    }
}