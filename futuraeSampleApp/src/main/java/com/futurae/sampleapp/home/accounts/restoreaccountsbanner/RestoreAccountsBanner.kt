package com.futurae.sampleapp.home.accounts.restoreaccountsbanner

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.futurae.sampleapp.R
import com.futurae.sampleapp.ui.theme.FuturaeTypography
import com.futurae.sampleapp.ui.theme.OnPrimaryColor
import com.futurae.sampleapp.ui.theme.SuccessColor

@Composable
fun RestoreAccountsBanner(
    restoreAccountsBannerUIState: RestoreAccountsBannerUIState,
    onAccountRestorationBannerDismiss: () -> Unit,
    onRestoreAccountsClick: (Boolean) -> Unit
) {
    val (titleResId, descriptionResId) = when (restoreAccountsBannerUIState) {
        is RestoreAccountsBannerUIState.SuccessfulCheck -> {
            R.string.restore_accounts_banner_title to R.string.restore_accounts_banner_description
        }
        RestoreAccountsBannerUIState.FailedCheck -> {
            R.string.restore_accounts_error_banner_title to
                    R.string.restore_accounts_banner_error_description
        }
        RestoreAccountsBannerUIState.None -> return
        RestoreAccountsBannerUIState.InformativeForSettingsEntryPoint -> {
            R.string.restore_accounts_informative_banner to null
        }
    }

    if (descriptionResId == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SuccessColor)
                .padding(12.dp)
        ) {
            Text(
                text = stringResource(titleResId),
                color = OnPrimaryColor,
                style = FuturaeTypography.titleH5
            )
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = restoreAccountsBannerUIState is RestoreAccountsBannerUIState.SuccessfulCheck) {
                    restoreAccountsBannerUIState as RestoreAccountsBannerUIState.SuccessfulCheck
                    onRestoreAccountsClick(restoreAccountsBannerUIState.isPinProtected)
                }
                .background(SuccessColor)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_restore),
                contentDescription = "Restore Accounts Image"
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(titleResId),
                        color = OnPrimaryColor,
                        style = FuturaeTypography.titleH5
                    )

                    Icon(
                        modifier = Modifier.clickable {
                            onAccountRestorationBannerDismiss()
                        },
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Dismiss banner",
                        tint = OnPrimaryColor
                    )
                }

                Text(
                    text = stringResource(descriptionResId),
                    color = OnPrimaryColor,
                    style = FuturaeTypography.bodySmallRegular
                )
            }
        }
    }
}

class RestoreAccountsBannerProvider : PreviewParameterProvider<RestoreAccountsBannerUIState> {
    override val values = sequenceOf(
        RestoreAccountsBannerUIState.None,
        RestoreAccountsBannerUIState.FailedCheck,
        RestoreAccountsBannerUIState.SuccessfulCheck(true),
        RestoreAccountsBannerUIState.InformativeForSettingsEntryPoint
    )
}

@SuppressLint("UnrememberedMutableState")
@Preview(showBackground = true)
@Composable
private fun RestoreAccountsBannerPreview(
    @PreviewParameter(RestoreAccountsBannerProvider::class) state: RestoreAccountsBannerUIState
) {
    RestoreAccountsBanner(
        restoreAccountsBannerUIState = state,
        onAccountRestorationBannerDismiss = {},
        onRestoreAccountsClick = {}
    )
}
