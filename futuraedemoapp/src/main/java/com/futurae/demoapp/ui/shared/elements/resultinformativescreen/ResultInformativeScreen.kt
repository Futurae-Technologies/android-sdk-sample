package com.futurae.demoapp.ui.shared.elements.resultinformativescreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.futurae.demoapp.ui.shared.elements.buttons.FuturaeOutlinedButton
import com.futurae.demoapp.ui.theme.OnPrimaryColor
import com.futurae.demoapp.ui.theme.PrimaryColor
import com.futurae.demoapp.ui.theme.SecondaryColor

@Composable
fun ResultInformativeScreen(
    uiState: ResultInformativeScreenUIState,
    onAction: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = OnPrimaryColor),
    ) {
        Box(modifier = Modifier.weight(1f, true)) {
            content()
        }

        uiState.actionCta?.let {
            FuturaeOutlinedButton(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                text = uiState.actionCta,
                onClick = onAction
            )
        }
    }
}

@Composable
fun InformativeContent(contentUIState: ResultInformativeScreenContentUIState.Informative) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = contentUIState.title.value(context),
                color = PrimaryColor,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = contentUIState.description.value(context),
                color = PrimaryColor,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }

        contentUIState.secondaryInfo?.let {
            Text(
                text = contentUIState.secondaryInfo.value(context),
                color = SecondaryColor,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}