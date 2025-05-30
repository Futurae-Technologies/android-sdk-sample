package com.futurae.demoapp.ui.shared.elements.alertdialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.futurae.demoapp.ui.theme.FuturaeTypography
import com.futurae.demoapp.ui.theme.OnPrimaryColor
import com.futurae.demoapp.ui.theme.OnSecondaryColor
import com.futurae.demoapp.ui.theme.PrimaryColor

@Composable
fun FuturaeAlertDialog(
    uiState: FuturaeAlertDialogUIState,
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
        title = uiState.title.let {
            {
                Text(
                    text = it.value(context),
                    style = FuturaeTypography.titleH4,
                    color = PrimaryColor
                )
            }
        },
        text = uiState.text.let {
            {
                Text(
                    text = it.value(context),
                    style = FuturaeTypography.bodySmallRegular,
                    color = OnSecondaryColor
                )
            }
        },
        confirmButton = {
            TextButton (onClick = onConfirm) {
                Text(
                    text = uiState.confirmButtonCta.value(context),
                    style = FuturaeTypography.button,
                    color = PrimaryColor
                )
            }
        },
        dismissButton = uiState.dismissButtonCta?.let {
            {
                TextButton(
                    onClick = { onDeny?.invoke() }
                ) {
                    Text(
                        text = it.value(context),
                        style = FuturaeTypography.button,
                        color = PrimaryColor
                    )
                }
            }
        }
    )
}