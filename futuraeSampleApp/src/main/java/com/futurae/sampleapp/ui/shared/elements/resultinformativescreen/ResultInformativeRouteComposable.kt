package com.futurae.sampleapp.ui.shared.elements.resultinformativescreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.futurae.sampleapp.R
import com.futurae.sampleapp.arch.ResultInformativeViewModel
import com.futurae.sampleapp.ui.TextWrapper
import com.futurae.sampleapp.ui.shared.elements.serviceinfosection.ServiceInfoSection
import com.futurae.sampleapp.ui.theme.PrimaryColor
import com.futurae.sampleapp.ui.theme.SecondaryColor
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun ResultInformativeRouteComposable(
    viewModel: ResultInformativeViewModel,
    onShowResultTopAppBar: (ResultState, TextWrapper) -> Unit,
    navController: NavController
) {
    val uiState by viewModel.resultInformativeRouteUIState.collectAsStateWithLifecycle()

    val safeUIState = uiState ?: return

    ResultInformativeComposable(
        uiState = safeUIState,
        onAction = {
            viewModel.onActionClick()
        }
    )

    LaunchedEffect(uiState) {
        uiState?.let {
            onShowResultTopAppBar(
                it.resultInformativeScreenUIState.state,
                it.resultInformativeScreenUIState.title
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.exitResultScreen
            .onEach { navController.navigateUp() }
            .launchIn(this)

        viewModel.navigateTo
            .onEach { navController.navigate(it.route) }
            .launchIn(this)
    }
}

@Composable
fun ResultInformativeComposable(uiState: ResultInformativeComposableUIState, onAction: () -> Unit) {
    ResultInformativeScreen(
        uiState = uiState.resultInformativeScreenUIState,
        onAction = onAction
    ) {
        when (val contentUIState = uiState.contentUIState) {
            is ResultInformativeScreenContentUIState.NewAccountEnrolled -> {
                ServiceInfoSection(
                    modifier = Modifier.fillMaxSize(),
                    uiState = contentUIState.serviceInfoSectionUIState,
                    textColor = PrimaryColor
                )
            }

            is ResultInformativeScreenContentUIState.VerificationCodeReceived -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
                ) {
                    Text(
                        text = stringResource(R.string.offline_verification_code),
                        color = PrimaryColor,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.offline_verification_code_prompt),
                        color = SecondaryColor,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    NumberSequence(contentUIState.code)
                }
            }

            is ResultInformativeScreenContentUIState.Informative -> {
                InformativeContent(contentUIState)
            }
        }
    }
}

@Composable
private fun NumberSequence(code: String) {
    val numbers = code.toCharArray().toList()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        numbers.forEach { number ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.wrapContentSize()
            ) {
                Text(
                    text = number.toString(),
                    style = MaterialTheme.typography.headlineLarge
                )
                HorizontalDivider(
                    modifier = Modifier.width(24.dp),
                    color = PrimaryColor,
                    thickness = 2.dp
                )
            }
        }
    }
}