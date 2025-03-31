package com.futurae.demoapp.error

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.futurae.demoapp.R
import com.futurae.demoapp.ui.TextWrapper
import com.futurae.demoapp.ui.shared.elements.buttons.ActionButton
import com.futurae.demoapp.ui.theme.OnPrimaryColor

@Composable
fun ErrorScreen(
    @StringRes titleResId: Int,
    message: String?,
    actionCallback: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = OnPrimaryColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(titleResId),
            color = Color.Black,
            style = MaterialTheme.typography.headlineMedium
        )
        message?.let {
            Text(
                text = message,
                color = Color.Black,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        actionCallback?.let {
            Spacer(Modifier.height(30.dp))
            ActionButton(
                text = TextWrapper.Resource(R.string.sdk_reset),
            ) {
                it.invoke()
            }
        }
    }
}

@Preview
@Composable
fun ErrorScreenPreview() {
    ErrorScreen(
        titleResId = R.string.sdk_generic_error_title,
        message = stringResource(R.string.sdk_init_error_initialization_failed),
        actionCallback = null
    )
}