package com.futurae.demoapp.ui.shared.elements.alertdialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.futurae.demoapp.ui.shared.elements.buttons.ActionButton
import com.futurae.demoapp.ui.theme.FuturaeTypography
import com.futurae.demoapp.ui.theme.OnPrimaryColor
import com.futurae.demoapp.ui.theme.OnSecondaryColor
import com.futurae.demoapp.ui.theme.PrimaryColor

@Composable
fun FuturaeAlertDialogWithFilledButtons(
    uiState: FuturaeAlertDialogWithFilledButtonsUIState,
    onConfirm: () -> Unit,
    onDeny: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null
) {
    val context = LocalContext.current
    AlertDialog(
        containerColor = OnPrimaryColor,
        onDismissRequest = {
            onDismiss?.invoke()
        },
        icon = {
            Image(
                painter = painterResource(uiState.drawableRes),
                contentDescription = "Dialog graphic"
            )
        },
        title = uiState.title.let {
            {
                Text(
                    text = it.value(context),
                    style = FuturaeTypography.titleH4,
                    color = PrimaryColor
                )
            }
        },
        text = {
            Text(
                text = uiState.text.value(context),
                style = FuturaeTypography.bodySmallRegular,
                color = OnSecondaryColor
            )
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                uiState.dismissButtonCta?.let {
                    ActionButton(
                        modifier = Modifier.weight(1f),
                        text = it,
                        onClick = { onDeny?.invoke() }
                    )
                }

                ActionButton(
                    modifier = Modifier.weight(1f),
                    text = uiState.confirmButtonCta,
                    onClick = onConfirm,
                    type = uiState.confirmButtonType
                )
            }
        }
    )
}