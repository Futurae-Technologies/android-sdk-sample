package com.futurae.sampleapp.ui.shared.elements.serviceinfosection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.futurae.sampleapp.ui.shared.elements.servicelogo.ServiceLogo

@Composable
fun ServiceInfoSection(
    modifier: Modifier = Modifier,
    uiState: ServiceInfoSectionUIState,
    textColor: Color
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
    ) {
        ServiceLogo(
            modifier = Modifier.size(70.dp),
            url = uiState.serviceLogo
        )

        uiState.serviceName?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.headlineMedium,
                color = textColor
            )
        }

        uiState.username?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
        }
    }
}