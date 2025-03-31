package com.futurae.demoapp.ui.shared.elements.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.futurae.demoapp.ui.TextWrapper
import com.futurae.demoapp.ui.theme.FuturaeTypography
import com.futurae.demoapp.ui.theme.InactiveColor
import com.futurae.demoapp.ui.theme.PrimaryColor

@Composable
fun FuturaeOutlinedButton(
    modifier: Modifier = Modifier,
    text: TextWrapper,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    OutlinedButton(
        modifier = modifier,
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = PrimaryColor,
            disabledContentColor = InactiveColor
        ),
        border = BorderStroke(
            width = 3.dp,
            color = if (enabled) {
                PrimaryColor
            } else {
                InactiveColor
            }
        )
    ) {
        Text(
            text = text.value(LocalContext.current),
            style = FuturaeTypography.button
        )
    }
}