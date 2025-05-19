package com.futurae.demoapp.settings.adaptive.collections

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.futurae.demoapp.R
import com.futurae.demoapp.ui.shared.elements.error.ErrorScreen
import com.futurae.demoapp.ui.TextWrapper
import com.futurae.demoapp.ui.shared.elements.buttons.ActionButton
import com.futurae.demoapp.ui.theme.OnPrimaryColor

@Composable
fun AdaptiveCollectionsScreen(
    onCollectionClick: (String) -> Unit
) {
    val viewModel: AdaptiveCollectionsScreenViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val safeState = state) {
        is AdaptiveCollectionsUIState.BlankSlate -> ErrorScreen(
            titleResId = safeState.message,
            message = null
        )
        is AdaptiveCollectionsUIState.Collections -> CollectionList(
            collections = safeState.collections,
            onCollectionClick = onCollectionClick,
            onClearCollectionsClick = viewModel::clearCollections,
        )
        AdaptiveCollectionsUIState.Idle -> {
            // do nothing
        }
    }
}

@Composable
private fun CollectionList(
    collections: List<AdaptiveCollectionUIState>,
    onCollectionClick: (String) -> Unit,
    onClearCollectionsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OnPrimaryColor)
            .padding(vertical = 24.dp, horizontal = 16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(collections) { item ->
                Column(
                    modifier = Modifier.clickable { onCollectionClick(item.json) }
                ) {
                    Text(
                        text = item.formattedTimestamp,
                        color = Color.Black,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = item.json,
                        color = Color.Black,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis
                    )
                    HorizontalDivider(modifier = Modifier.padding(top = 16.dp))
                }
            }
        }

        ActionButton(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            text = TextWrapper.Resource(R.string.adaptive_clear_collections),
            onClick = onClearCollectionsClick
        )
    }
}
