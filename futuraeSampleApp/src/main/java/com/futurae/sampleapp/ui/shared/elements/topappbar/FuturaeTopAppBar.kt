package com.futurae.sampleapp.ui.shared.elements.topappbar

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.futurae.sampleapp.BuildConfig
import com.futurae.sampleapp.R
import com.futurae.sampleapp.ui.TextWrapper
import com.futurae.sampleapp.ui.shared.elements.resultinformativescreen.ResultState
import com.futurae.sampleapp.ui.shared.elements.serviceinfosection.ServiceInfoSection
import com.futurae.sampleapp.ui.shared.elements.serviceinfosection.ServiceInfoSectionUIState
import com.futurae.sampleapp.ui.theme.BgPoweredBy
import com.futurae.sampleapp.ui.theme.FuturaeTypography
import com.futurae.sampleapp.ui.theme.OnPrimaryColor
import com.futurae.sampleapp.ui.theme.PrimaryColor
import com.futurae.sampleapp.ui.theme.SubtitleStyle
import com.futurae.sampleapp.ui.theme.SuccessColor
import com.futurae.sampleapp.ui.theme.TextAlternative

@Composable
fun FuturaeTopAppBar(
    state: FuturaeTopAppBarUIState,
    navigateUp: () -> Unit
) {
    AnimatedContent(
        targetState = state,
        label = "FuturaeTopAppBar content animation"
    ) {
        when (it) {
            is FuturaeTopAppBarUIState.AccountHistory -> AccountHistoryTopAppBar(
                serviceInfoSectionUIState = it.serviceInfoSectionUIState,
                navigateUp = navigateUp
            )

            is FuturaeTopAppBarUIState.CommonTopBar -> {
                CommonTopAppBar(
                    titleResId = it.titleResId,
                    hasBackNavigation = it.hasBackNavigation,
                    navigateUp = navigateUp
                )
            }

            FuturaeTopAppBarUIState.FuturaeMore -> FuturaeSettingTopAppBar()

            is FuturaeTopAppBarUIState.ResultTopBar -> ResultTopAppBar(
                state = it.state,
                label = it.label
            )

            FuturaeTopAppBarUIState.AccountPicker -> AccountPickerTopAppBar(
                navigateUp = navigateUp
            )

            FuturaeTopAppBarUIState.None -> {
                // show nothing
            }
        }
    }
}

@Composable
private fun AccountHistoryTopAppBar(
    serviceInfoSectionUIState: ServiceInfoSectionUIState,
    navigateUp: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = PrimaryColor)
            .padding(horizontal = 4.dp)
            .padding(top = 34.dp)
            .defaultMinSize(minHeight = 150.dp),
    ) {
        IconButton(
            modifier = Modifier.align(Alignment.Top),
            onClick = navigateUp
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                contentDescription = "Back",
                tint = OnPrimaryColor
            )
        }

        ServiceInfoSection(
            modifier = Modifier
                .weight(1f)
                .padding(top = 18.dp, end = 40.dp)
                .wrapContentSize(),
            uiState = serviceInfoSectionUIState,
            textColor = OnPrimaryColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommonTopAppBar(
    @StringRes titleResId: Int,
    hasBackNavigation: Boolean,
    navigateUp: () -> Unit
) {
    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = PrimaryColor,
            titleContentColor = OnPrimaryColor
        ),
        navigationIcon = {
            if (hasBackNavigation) {
                val navIconTestTag = stringResource(R.string.selector_header_back_button)
                IconButton(
                    onClick = navigateUp,
                    modifier = Modifier.testTag(navIconTestTag)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = OnPrimaryColor
                    )
                }
            }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(titleResId),
                    style = FuturaeTypography.titleH5,
                    color = OnPrimaryColor,
                    textAlign = TextAlign.Center
                )

                if (hasBackNavigation) {
                    // Ugly hack to center title when nav icon is present
                    Spacer(modifier = Modifier.width(40.dp))
                }
            }
        }
    )
}

@Composable
private fun ResultTopAppBar(state: ResultState, label: TextWrapper) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = PrimaryColor)
            .padding(16.dp)
            .padding(top = 16.dp)
            .defaultMinSize(minHeight = 150.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
    ) {
        if (state == ResultState.LOADING) {
            CircularProgressIndicator(
                modifier = Modifier.size(72.dp),
                color = SuccessColor,
            )
        } else {
            Image(
                modifier = Modifier.size(72.dp),
                painter = painterResource(
                    id = if (state == ResultState.SUCCESS) {
                        R.drawable.ic_success
                    } else {
                        R.drawable.ic_failure
                    }
                ),
                contentDescription = "Result indicator"
            )
        }

        Text(
            text = label.value(LocalContext.current),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = OnPrimaryColor
        )
    }
}

@Composable
private fun AccountPickerTopAppBar(navigateUp: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = PrimaryColor)
            .padding(16.dp)
            .padding(top = 16.dp)
            .defaultMinSize(minHeight = 150.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
        ) {
            Image(
                modifier = Modifier.size(72.dp),
                painter = painterResource(id = R.drawable.graphic_account),
                contentDescription = "Account to be authenticated"
            )

            Text(
                text = stringResource(R.string.select_account_prompt),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = OnPrimaryColor
            )
        }

        IconButton(
            onClick = navigateUp,
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Close",
                tint = OnPrimaryColor
            )
        }
    }
}

@Composable
private fun FuturaeSettingTopAppBar() {
    val statusBarHeight = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(PrimaryColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(statusBarHeight))
            Spacer(modifier = Modifier.height(35.dp))
            Image(
                painter = painterResource(id = R.drawable.ic_futurae_logo),
                contentDescription = "Header Image",
                modifier = Modifier.size(74.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                style = SubtitleStyle,
                color = TextAlternative,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Image(
                painter = painterResource(id = R.drawable.ic_powered_by_futurae),
                contentDescription = "Header Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(color = BgPoweredBy),
                contentScale = ContentScale.None
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun FuturaeTopAppBarPreview() {
    FuturaeTopAppBar(
        state = FuturaeTopAppBarUIState.None,
        navigateUp = { }
    )
}