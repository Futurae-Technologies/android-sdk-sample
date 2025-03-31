package com.futurae.demoapp.settings.debug.uriutils

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.futurae.demoapp.R
import com.futurae.demoapp.settings.debug.DebugResultComparison
import com.futurae.demoapp.ui.TextWrapper
import com.futurae.demoapp.ui.shared.elements.buttons.ActionButton
import com.futurae.demoapp.ui.theme.FuturaeTypography
import com.futurae.demoapp.ui.theme.OnPrimaryColor
import com.futurae.demoapp.ui.theme.PrimaryColor
import com.futurae.demoapp.ui.theme.SecondaryColor
import com.futurae.demoapp.ui.theme.Tertiary

// Please note this is a debug screen, solely for testing purposes.
@Composable
fun DebugURIUtilsScreen() {
    val viewModel: DebugURIUtilsViewModel = viewModel(
        factory = DebugURIUtilsViewModel.provideFactory()
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is DebugURIUtilsViewModel.DebugURIUtilsUIState.URIResult -> {
                URIResult(uiState as DebugURIUtilsViewModel.DebugURIUtilsUIState.URIResult) {
                    viewModel.dismissResult()
                }
            }

            is DebugURIUtilsViewModel.DebugURIUtilsUIState.Input -> {
                URIInput(
                    uri = (uiState as DebugURIUtilsViewModel.DebugURIUtilsUIState.Input).uri,
                    onURIChange = { viewModel.onURIChange(it) },
                    onURISubmit = { viewModel.onURISubmit() }
                )
            }
        }
    }
}

@Composable
private fun URIInput(
    uri: String,
    onURIChange: (String) -> Unit,
    onURISubmit: () -> Unit
) {
    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(OnPrimaryColor)
            .padding(16.dp)
    ) {
        TextField(
            value = uri,
            onValueChange = onURIChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                textAlign = TextAlign.Center
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Tertiary,
                unfocusedContainerColor = Tertiary
            )
        )

        ActionButton(
            text = TextWrapper.Resource(R.string.submit),
            onClick = onURISubmit,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 40.dp)
        )
    }
}

@Composable
private fun URIResult(
    uiState: DebugURIUtilsViewModel.DebugURIUtilsUIState.URIResult,
    onDismiss: () -> Unit
) {
    DebugResultComparison(
        isSuccess = uiState.isSameResultInBothImpl,
        firstResult = {
            ResultSection(
                version = "Legacy Impl:",
                uriType = uiState.uriTypeLegacyImpl,
                extractedInfo = uiState.extractedInfoLegacyImpl
            )
        },
        secondResult = {
            ResultSection(
                version = "New Impl:",
                uriType = uiState.uriType,
                extractedInfo = uiState.extractedInfo
            )
        },
        onDismiss = onDismiss
    )
}

@Composable
fun ResultSection(
    version: String,
    @StringRes uriType: Int,
    extractedInfo: Map<String, String>
) {
    Text(
        text = version,
        style = FuturaeTypography.titleH4,
        color = PrimaryColor
    )

    Text(
        text = stringResource(uriType),
        style = FuturaeTypography.titleH4,
        color = PrimaryColor
    )

    extractedInfo.forEach {
        Text(
            text = "${it.key}:${it.value}",
            style = FuturaeTypography.bodyLarge,
            color = SecondaryColor
        )
    }
}