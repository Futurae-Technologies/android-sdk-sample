package com.futurae.demoapp.ui.shared.elements.accountpicker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.futurae.demoapp.R
import com.futurae.demoapp.ui.shared.elements.accountpicker.arch.AccountPickerViewModel
import com.futurae.demoapp.ui.shared.elements.servicelogo.ServiceLogo
import com.futurae.demoapp.ui.theme.DisableColor
import com.futurae.demoapp.ui.theme.OnPrimaryColor
import com.futurae.demoapp.ui.theme.PrimaryColor
import com.futurae.demoapp.ui.theme.SecondaryColor
import com.futurae.demoapp.ui.theme.SuccessColor
import com.futurae.sdk.public_api.common.model.FTAccount

@Composable
fun AccountPicker(
    onAccountSelectionConfirmed: (FTAccount) -> Unit
) {
    val accountPickerViewModel: AccountPickerViewModel = viewModel(
        factory = AccountPickerViewModel.provideFactory()
    )

    val uiState by accountPickerViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = OnPrimaryColor),
    ) {
        Box(modifier = Modifier.weight(1f, true)) {
            AccountsList(items = uiState.items) {
                accountPickerViewModel.onAccountSelected(it)
            }
        }

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            onClick = { onAccountSelectionConfirmed(uiState.selectedAccount!!) },
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = PrimaryColor,
            ),
            border = BorderStroke(
                width = 3.dp,
                color = PrimaryColor
            ),
            enabled = uiState.canProceed
        ) {
            Text(
                text = stringResource(R.string.authenticate)
            )
        }
    }
}

@Composable
private fun AccountsList(
    modifier: Modifier = Modifier,
    items: List<ActiveAccountItemUIState>,
    onAccountSelected: (FTAccount) -> Unit
) {
    LazyColumn(modifier = modifier) {
        items(
            items = items,
            key = { it.account.userId }
        ) { item ->

            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            onClick = { onAccountSelected(item.account) },
                        )
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ServiceLogo(
                        modifier = Modifier.size(56.dp),
                        url = item.serviceLogo
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.serviceName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryColor
                        )
                        Text(
                            text = item.username,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SecondaryColor
                        )
                    }

                    CustomRadioButton(isSelected = item.isSelected)
                }

                HorizontalDivider(color = DisableColor)
            }
        }
    }
}

@Composable
private fun CustomRadioButton(isSelected: Boolean) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(27.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = if (isSelected) SuccessColor else DisableColor,
                radius = size.minDimension / 2,
                style = if (isSelected) Fill else Stroke(width = 2.dp.toPx())
            )
        }

        // Show checkmark only if selected
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = OnPrimaryColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}