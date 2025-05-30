package com.futurae.demoapp.ui.shared.elements.decisionmodal

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.futurae.demoapp.ui.TextWrapper
import com.futurae.demoapp.ui.shared.elements.buttons.ActionButton
import com.futurae.demoapp.ui.theme.FuturaeTypography
import com.futurae.demoapp.ui.theme.OnPrimaryColor
import com.futurae.demoapp.ui.theme.PrimaryColor
import com.futurae.demoapp.ui.theme.WarningColor

@Composable
fun FuturaeFullScreenDecisionModal(
    uiState: FuturaeFullScreenDecisionModalUIState,
    onPrimaryActionClick: () -> Unit,
    onSecondaryActionClick: () -> Unit,
    navController: NavController
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OnPrimaryColor)
    ) {
        IconButton(
            onClick = {
                navController.navigateUp()
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Close",
                tint = PrimaryColor
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = uiState.drawableResId),
                    contentDescription = "Graphic",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Text(
                    modifier = Modifier.padding(top = 48.dp),
                    text = stringResource(uiState.titleResId),
                    style = FuturaeTypography.titleH4,
                    textAlign = TextAlign.Center,
                    color = PrimaryColor
                )
                Text(
                    text = stringResource(uiState.descriptionResId),
                    style = FuturaeTypography.bodySmallRegular,
                    textAlign = TextAlign.Center,
                    color = PrimaryColor
                )
                Text(
                    modifier = Modifier.padding(top = 18.dp),
                    text = stringResource(uiState.noticeResId),
                    style = FuturaeTypography.bodySmallRegular,
                    textAlign = TextAlign.Center,
                    color = WarningColor
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ActionButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = TextWrapper.Resource(uiState.primaryActionResId),
                    onClick = onPrimaryActionClick
                )

                TextButton(
                    onClick = onSecondaryActionClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = stringResource(uiState.secondaryAction)
                    )
                }
            }
        }
    }
}
