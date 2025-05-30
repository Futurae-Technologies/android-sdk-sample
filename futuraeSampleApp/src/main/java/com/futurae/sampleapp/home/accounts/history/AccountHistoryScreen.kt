package com.futurae.sampleapp.home.accounts.history

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.futurae.sampleapp.utils.ILCEState
import com.futurae.sampleapp.R
import com.futurae.sampleapp.home.accounts.history.arch.AccountHistoryViewModel
import com.futurae.sampleapp.ui.shared.elements.serviceinfosection.ServiceInfoSectionUIState
import com.futurae.sampleapp.ui.theme.FuturaeTypography
import com.futurae.sampleapp.ui.theme.OnPrimaryColor
import com.futurae.sampleapp.ui.theme.PrimaryColor
import com.futurae.sampleapp.ui.theme.SecondaryColor

@Composable
fun AccountHistoryScreen(
    userId: String,
    onUpdateTopAppBar: (ServiceInfoSectionUIState) -> Unit
) {
    val accountHistoryViewModel: AccountHistoryViewModel = viewModel(
        factory = AccountHistoryViewModel.provideFactory(userId)
    )

    val uiState by accountHistoryViewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = OnPrimaryColor)
    ) {
        when (val details = uiState.details) {
            is ILCEState.Content -> AccountHistoryDetails(items = details.data)
            is ILCEState.Error -> AccountHistoryErrorBlankSlate()
            else -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }

    LaunchedEffect(uiState.serviceInfoSectionUIState) {
        onUpdateTopAppBar(uiState.serviceInfoSectionUIState)
    }
}

@Composable
private fun AccountHistoryDetails(items: List<AccountHistoryItemUIState>) {
    if (items.isEmpty()) {
        AccountHistoryBlankSlate()
    } else {
        AccountHistoryList(items)
    }
}

@Composable
private fun AccountHistoryList(items: List<AccountHistoryItemUIState>) {
    val context = LocalContext.current

    LazyColumn {
        items(items = items) { item ->
            val icon = if (item.isSuccessful) {
                R.drawable.ic_success
            } else {
                R.drawable.ic_failure
            }
            val fullDescription = "${item.description.first.value(context)} " +
                    item.description.second.value(context)

            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Image(
                        painterResource(icon),
                        modifier = Modifier.size(22.dp),
                        contentDescription = "Result"
                    )

                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        text = fullDescription,
                        style = FuturaeTypography.titleH5,
                        color = PrimaryColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = item.date,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryColor
                    )
                }

                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun AccountHistoryBlankSlate() {
    Column (
        modifier = Modifier
            .background(color = OnPrimaryColor)
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(id = R.drawable.graphic_account_history),
            contentDescription = "Empty Account History"
        )

        Text(
            text = stringResource(R.string.account_history_blank_slate_title),
            color = PrimaryColor,
            style = FuturaeTypography.titleH4
        )
    }
}

@Composable
private fun AccountHistoryErrorBlankSlate() {
    Column (
        modifier = Modifier
            .background(color = OnPrimaryColor)
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_failure),
            contentDescription = "Empty Accounts"
        )

        Text(
            text = stringResource(R.string.account_history_error_blank_slate_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = PrimaryColor
        )

        Text(
            text = stringResource(R.string.account_history_error_blank_slate_description),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = PrimaryColor
        )
    }
}
