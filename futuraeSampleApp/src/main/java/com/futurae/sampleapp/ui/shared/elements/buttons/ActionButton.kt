package com.futurae.sampleapp.ui.shared.elements.buttons

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.futurae.sampleapp.ui.TextWrapper
import com.futurae.sampleapp.ui.theme.DisableColor
import com.futurae.sampleapp.ui.theme.FuturaeTypography
import com.futurae.sampleapp.ui.theme.OnPrimaryColor
import com.futurae.sampleapp.ui.theme.PrimaryColor
import com.futurae.sampleapp.ui.theme.SuccessColor
import com.futurae.sampleapp.ui.theme.WarningColor

sealed class ActionButtonType(
    val containerColor: Color,
    val contentColor: Color = OnPrimaryColor,
    val disabledContainerColor: Color = DisableColor,
    val disabledContentColor: Color = OnPrimaryColor
) {
    data object Primary : ActionButtonType(containerColor = PrimaryColor)
    data object Success : ActionButtonType(containerColor = SuccessColor)
    data object Warning : ActionButtonType(containerColor = WarningColor)
}

@Composable
fun ActionButton(
    text: TextWrapper,
    type: ActionButtonType = ActionButtonType.Primary,
    modifier: Modifier = Modifier,
    @DrawableRes iconResId: Int? = null,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = type.containerColor,
            contentColor = type.contentColor,
            disabledContainerColor = type.disabledContainerColor,
            disabledContentColor = type.disabledContentColor
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ButtonDefaults.IconSpacing)
        ) {
            val textValue = text.value(LocalContext.current)
            iconResId?.let {
                Icon(
                    painter = painterResource(it),
                    contentDescription = textValue,
                    modifier = Modifier.size(40.dp)
                )
            }

            Text(
                text = textValue,
                style = FuturaeTypography.button
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ActionButtonPreview() {
    ActionButton(
        onClick = { },
        text = TextWrapper.Primitive("Click Me"),
        enabled = true,
        modifier = Modifier.padding(16.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun DisabledActionButtonPreview() {
    ActionButton(
        onClick = { },
        text = TextWrapper.Primitive("Click Me"),
        enabled = false,
        modifier = Modifier.padding(16.dp)
    )
}
