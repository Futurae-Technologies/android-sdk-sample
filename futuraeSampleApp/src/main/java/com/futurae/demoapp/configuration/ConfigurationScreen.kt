package com.futurae.demoapp.configuration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.futurae.demoapp.FuturaeDemoApplication
import com.futurae.demoapp.R
import com.futurae.demoapp.arch.PinProviderViewModel
import com.futurae.demoapp.configuration.arch.ConfigurationUIState
import com.futurae.demoapp.configuration.arch.ConfigurationViewModel
import com.futurae.demoapp.ui.TextWrapper
import com.futurae.demoapp.ui.shared.elements.buttons.ActionButton
import com.futurae.demoapp.ui.shared.elements.configuration.ConfigurationItem
import com.futurae.demoapp.ui.shared.elements.configuration.ConfigurationList
import com.futurae.demoapp.ui.shared.elements.configuration.LockConfigurationItem
import com.futurae.demoapp.ui.shared.elements.snackbar.FuturaeSnackbarUIState
import com.futurae.demoapp.ui.theme.OnPrimaryColor
import com.futurae.sdk.public_api.common.LockConfigurationType
import com.futurae.sdk.public_api.common.model.PresentationConfigurationForBiometricsPrompt
import com.futurae.sdk.public_api.common.model.PresentationConfigurationForDeviceCredentialsPrompt
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun ConfigurationScreenRoute(
    onConfigurationComplete: () -> Unit,
    isConfigurationChange: Boolean,
    onPinRequested: () -> Unit,
    showSnackbar: suspend (FuturaeSnackbarUIState) -> Unit,
    pinProviderViewModel: PinProviderViewModel? = null,
) {
    val context = LocalContext.current
    val application = context.applicationContext as FuturaeDemoApplication
    val configurationViewModel: ConfigurationViewModel = viewModel(
        factory = ConfigurationViewModel.provideFactory(application = application)
    )

    val configurationItems by configurationViewModel.configurationItems.collectAsStateWithLifecycle()
    val submitButtonEnabled by configurationViewModel.configurationStateFlow.collectAsStateWithLifecycle()

    ConfigurationScreen(
        configurationItems = configurationItems,
        isSubmitButtonEnabled = submitButtonEnabled is ConfigurationUIState.InProgress,
        onSubmit = { configurationViewModel.submitConfiguration(isConfigurationChange) },
        onItemChanged = configurationViewModel::onItemsUpdated
    )

    LaunchedEffect(Unit) {
        configurationViewModel
            .onUnlockRequired
            .onEach {
                when (it) {
                    LockConfigurationType.NONE -> {
                        configurationViewModel.switchToSdkConfigurationNone()
                    }

                    LockConfigurationType.BIOMETRICS_ONLY -> configurationViewModel.switchToSDKConfigurationBiometrics(
                        PresentationConfigurationForBiometricsPrompt(
                            context as FragmentActivity,
                            TextWrapper.Resource(R.string.sdk_switch_config_title).value(context),
                            TextWrapper.Resource(R.string.sdk_switch_config_subtitle)
                                .value(context),
                            TextWrapper.Resource(R.string.sdk_switch_config_description)
                                .value(context),
                            TextWrapper.Resource(R.string.cancel).value(context),
                        )
                    )

                    LockConfigurationType.BIOMETRICS_OR_DEVICE_CREDENTIALS -> configurationViewModel.switchToSDKConfigurationBiometricsOrDeviceCreds(
                        PresentationConfigurationForDeviceCredentialsPrompt(
                            context as FragmentActivity,
                            TextWrapper.Resource(R.string.sdk_switch_config_title).value(context),
                            TextWrapper.Resource(R.string.sdk_switch_config_subtitle)
                                .value(context),
                            TextWrapper.Resource(R.string.sdk_switch_config_description)
                                .value(context),
                        )
                    )

                    LockConfigurationType.SDK_PIN_WITH_BIOMETRICS_OPTIONAL -> {
                        onPinRequested()
                    }
                }
            }
            .launchIn(this)

        pinProviderViewModel
            ?.pinFlow
            ?.onEach {
                configurationViewModel.onPinProvided(it)
                pinProviderViewModel.reset()
            }
            ?.launchIn(this)

        configurationViewModel
            .operationStatusFlow
            .onEach { result ->
                if (result.isFailure) {
                    showSnackbar(
                        FuturaeSnackbarUIState.Error(
                            TextWrapper.Primitive(
                                result.exceptionOrNull()?.message ?: "Unknown Error"
                            )
                        )
                    )
                } else {
                    if (isConfigurationChange) {
                        showSnackbar(
                            FuturaeSnackbarUIState.Success(TextWrapper.Resource(R.string.sdk_configuration_success))
                        )
                    } else {
                        onConfigurationComplete()
                    }
                }
            }
            .launchIn(this)
    }
}

@Composable
private fun ConfigurationScreen(
    configurationItems: List<ConfigurationItem>,
    isSubmitButtonEnabled: Boolean,
    onSubmit: () -> Unit,
    onItemChanged: (Int, ConfigurationItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OnPrimaryColor)
    ) {
        ConfigurationScreenComposable(
            onSubmit = onSubmit,
            onItemChanged = onItemChanged,
            configurationItems = configurationItems,
            submitEnabled = isSubmitButtonEnabled
        )

    }
}

@Composable
private fun ConfigurationScreenComposable(
    onSubmit: () -> Unit,
    onItemChanged: (Int, ConfigurationItem) -> Unit,
    configurationItems: List<ConfigurationItem>,
    submitEnabled: Boolean
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            ConfigurationList(
                items = configurationItems,
                onItemUpdate = onItemChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(OnPrimaryColor)
            )
        }

        ActionButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(all = 32.dp),
            onClick = {
                onSubmit()
            },
            text = TextWrapper.Resource(R.string.submit),
            enabled = submitEnabled,
        )
    }
}


@Preview(showBackground = true)
@Composable
fun ConfigurationScreenPreview() {
    ConfigurationScreen(
        onSubmit = { },
        onItemChanged = { _, _ -> },
        configurationItems = listOf(
            LockConfigurationItem(
                title = TextWrapper.Primitive("Test"),
                subtitle = TextWrapper.Primitive("Subtitle"),
                isExpanded = false,
                selectedChoice = null
            ),
        ),
        isSubmitButtonEnabled = true
    )
}