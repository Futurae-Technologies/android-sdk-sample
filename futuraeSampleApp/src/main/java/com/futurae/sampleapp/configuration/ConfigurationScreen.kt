package com.futurae.sampleapp.configuration

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
import androidx.compose.ui.util.fastCbrt
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.futurae.sampleapp.FuturaeSampleApplication
import com.futurae.sampleapp.R
import com.futurae.sampleapp.arch.PinProviderViewModel
import com.futurae.sampleapp.configuration.arch.ConfigurationUIState
import com.futurae.sampleapp.configuration.arch.ConfigurationViewModel
import com.futurae.sampleapp.ui.TextWrapper
import com.futurae.sampleapp.ui.shared.elements.buttons.ActionButton
import com.futurae.sampleapp.ui.shared.elements.configuration.ConfigurationItem
import com.futurae.sampleapp.ui.shared.elements.configuration.ConfigurationList
import com.futurae.sampleapp.ui.shared.elements.configuration.LockConfigurationItem
import com.futurae.sampleapp.ui.shared.elements.snackbar.FuturaeSnackbarUIState
import com.futurae.sampleapp.ui.theme.OnPrimaryColor
import com.futurae.sdk.debug.FuturaeDebugUtil
import com.futurae.sdk.public_api.common.LockConfigurationType
import com.futurae.sdk.public_api.common.model.PresentationConfigurationForBiometricsPrompt
import com.futurae.sdk.public_api.common.model.PresentationConfigurationForDeviceCredentialsPrompt
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun ConfigurationScreenRoute(
    onConfigurationComplete: () -> Unit,
    navigateToRecovery: () -> Unit,
    isConfigurationChange: Boolean,
    onPinRequested: () -> Unit,
    showSnackbar: suspend (FuturaeSnackbarUIState) -> Unit,
    pinProviderViewModel: PinProviderViewModel? = null,
) {
    val context = LocalContext.current
    val application = context.applicationContext as FuturaeSampleApplication
    val configurationViewModel: ConfigurationViewModel = viewModel(
        factory = ConfigurationViewModel.provideFactory(application = application)
    )

    val configurationItems by configurationViewModel.configurationItems.collectAsStateWithLifecycle()
    val submitButtonEnabled by configurationViewModel.configurationStateFlow.collectAsStateWithLifecycle()

    ConfigurationScreen(
        configurationItems = configurationItems,
        isConfigurationChange = isConfigurationChange,
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

        configurationViewModel
            .navigateToRecovery
            .onEach {
                navigateToRecovery()
            }
            .launchIn(this)
    }
}

@Composable
private fun ConfigurationScreen(
    configurationItems: List<ConfigurationItem>,
    isConfigurationChange: Boolean,
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
            isConfigurationChange = isConfigurationChange,
            onSubmit = onSubmit,
            onItemChanged = onItemChanged,
            configurationItems = configurationItems,
            submitEnabled = isSubmitButtonEnabled
        )

    }
}

@Composable
private fun ConfigurationScreenComposable(
    isConfigurationChange: Boolean,
    onSubmit: () -> Unit,
    onItemChanged: (Int, ConfigurationItem) -> Unit,
    configurationItems: List<ConfigurationItem>,
    submitEnabled: Boolean
) {
    val appContext = LocalContext.current

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

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(all = 32.dp)
        ) {
            if (!isConfigurationChange) {
                ActionButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        FuturaeDebugUtil.corruptDBTokens(appContext)
                    },
                    text = TextWrapper.Resource(R.string.corrupt_v1)
                )
            }

            ActionButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onSubmit()
                },
                text = TextWrapper.Resource(R.string.submit),
                enabled = submitEnabled,
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ConfigurationScreenPreview() {
    ConfigurationScreen(
        onSubmit = { },
        onItemChanged = { _, _ -> },
        isConfigurationChange = false,
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