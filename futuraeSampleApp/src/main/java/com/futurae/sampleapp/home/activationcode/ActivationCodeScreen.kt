package com.futurae.sampleapp.home.activationcode

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.futurae.sampleapp.R
import com.futurae.sampleapp.enrollment.EnrollmentCase
import com.futurae.sampleapp.home.activationcode.arch.ActivationCodeViewModel
import com.futurae.sampleapp.ui.TextWrapper
import com.futurae.sampleapp.ui.shared.elements.buttons.ActionButton
import com.futurae.sampleapp.ui.theme.FuturaeTypography
import com.futurae.sampleapp.ui.theme.OnPrimaryColor
import com.futurae.sampleapp.ui.theme.OnSecondaryColor
import com.futurae.sampleapp.ui.theme.Tertiary
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun ActivationCodeScreen(
    onEnrollmentRequest: (EnrollmentCase.ManualEntry) -> Unit
) {
    val activationCodeViewModel: ActivationCodeViewModel = viewModel(
        factory = ActivationCodeViewModel.provideFactory()
    )

    val keyboardController = LocalSoftwareKeyboardController.current
    val code by activationCodeViewModel.activationCode.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OnPrimaryColor),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
        ) {
            Image(
                painter = painterResource(id = R.drawable.graphic_manual_entry),
                contentDescription = "Manual entry",
            )

            Text(
                text = stringResource(R.string.please_enter_the_activation_code_that_was_provided_to_you),
                textAlign = TextAlign.Center,
                style = FuturaeTypography.titleH5
            )


            TextField(
                value = code,
                onValueChange = { activationCodeViewModel.onCodeChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "0000 0000 0000 0000",
                        color = OnSecondaryColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                textStyle = TextStyle(
                    textAlign = TextAlign.Center
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Tertiary,
                    unfocusedContainerColor = Tertiary
                ),
                visualTransformation = VisualTransformation.None,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Password
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                    }
                )
            )
        }

        ActionButton(
            text = TextWrapper.Resource(R.string.submit),
            onClick = { activationCodeViewModel.submitCode() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 40.dp),
            enabled = code.replace(" ", "").length == 16
        )
    }

    LaunchedEffect(Unit) {
        activationCodeViewModel.onEnrollmentFlowRequest
            .onEach { onEnrollmentRequest(it) }
            .launchIn(this)
    }
}