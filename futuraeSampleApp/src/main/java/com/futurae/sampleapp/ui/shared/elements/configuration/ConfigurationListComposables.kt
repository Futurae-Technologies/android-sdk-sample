package com.futurae.sampleapp.ui.shared.elements.configuration

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.futurae.sampleapp.ui.TextWrapper
import com.futurae.sampleapp.ui.shared.elements.RadioButtonGroup
import com.futurae.sampleapp.ui.shared.elements.ToggleGroup
import com.futurae.sampleapp.ui.theme.ItemTitleStyle
import com.futurae.sampleapp.ui.theme.OnPrimaryColor
import com.futurae.sampleapp.ui.theme.SubtitleStyle
import com.futurae.sdk.public_api.common.LockConfigurationType

@Composable
fun ConfigurationList(
    items: List<ConfigurationItem>,
    onItemUpdate: (Int, ConfigurationItem) -> Unit,
    modifier: Modifier = Modifier.fillMaxSize()
) {
    LazyColumn(modifier = modifier) {
        itemsIndexed(items) { index, item ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
                color = OnPrimaryColor,
            ) {
                when (item) {
                    is LockConfigurationItem -> {
                        SingleChoiceLockTypeComposable(
                            item = item,
                            index = index,
                            onItemUpdate = onItemUpdate,
                            onExpandToggle = {
                                onItemUpdate(index, item.copy(isExpanded = !item.isExpanded))
                            },
                        )
                    }

                    is LockDurationItem -> {
                        SingleChoiceLockDurationComposable(
                            item = item,
                            index = index,
                            onItemUpdate = onItemUpdate,
                            onExpandToggle = {
                                onItemUpdate(index, item.copy(isExpanded = !item.isExpanded))
                            },
                        )
                    }

                    is OptionConfigurationsItem -> MultipleToggleComposable(
                        item = item,
                        onExpandToggle = {
                            onItemUpdate(index, item.copy(isExpanded = !item.isExpanded))
                        },
                        onItemUpdate = { pair ->
                            val updatedMap = item.items.toMutableMap().apply {
                                this[pair.first] = pair.second
                            }
                            onItemUpdate(index, item.copy(items = updatedMap))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SingleChoiceLockDurationComposable(
    item: LockDurationItem,
    index: Int,
    onItemUpdate: (Int, ConfigurationItem) -> Unit,
    onExpandToggle: () -> Unit,
) {
    val context = LocalContext.current
    SingleChoiceComposable(
        item = item,
        onExpandToggle = onExpandToggle,
        expandedContent = {
            RadioButtonGroup(
                options = listOf(5, 10, 30, 60, 120, 240),
                selectedOption = item.selectedChoice,
                onSelectionChanged = {
                    onItemUpdate(
                        index,
                        item.copy(
                            selectedChoice = it,
                            isExpanded = false,
                            subtitle = TextWrapper.Primitive("${it}s")
                        )
                    )
                },
                valueFormatter = { choice -> "${choice}s" }
            )
        }
    )
}

@Composable
fun SingleChoiceLockTypeComposable(
    item: LockConfigurationItem,
    index: Int,
    onItemUpdate: (Int, ConfigurationItem) -> Unit,
    onExpandToggle: () -> Unit,
) {
    SingleChoiceComposable(
        item = item,
        onExpandToggle = onExpandToggle,
        expandedContent = {
            RadioButtonGroup(
                options = LockConfigurationType.entries,
                selectedOption = item.selectedChoice,
                onSelectionChanged = {
                    onItemUpdate(
                        index,
                        item.copy(
                            selectedChoice = it,
                            isExpanded = false,
                            subtitle = TextWrapper.Primitive(it.name)
                        )
                    )
                },
            )
        }
    )
}

@Composable
fun SingleChoiceComposable(
    item: ConfigurationItem,
    onExpandToggle: () -> Unit,
    expandedContent: @Composable () -> Unit,
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Surface(
            modifier = Modifier.clickable { onExpandToggle() },
            color = OnPrimaryColor
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = item.title.value(context),
                        style = ItemTitleStyle,
                    )
                    Text(
                        text = item.subtitle.value(context),
                        style = SubtitleStyle,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                val rotationAngle = animateFloatAsState(
                    targetValue = if (item.isExpanded) 180f else 0f,
                    label = "ArrowRotation"
                ).value

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand Indicator",
                    modifier = Modifier
                        .rotate(rotationAngle)
                        .size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (item.isExpanded) {
            HorizontalDivider()
            expandedContent()
        }

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
        )
    }
}

@Composable
fun MultipleToggleComposable(
    item: OptionConfigurationsItem,
    onItemUpdate: (Pair<SdkConfigOptionalFlag, Boolean>) -> Unit,
    onExpandToggle: () -> Unit,
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Surface(
            modifier = Modifier.clickable { onExpandToggle() },
            color = OnPrimaryColor
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = item.title.value(context),
                        style = ItemTitleStyle,
                    )
                    Text(
                        text = item.items.entries
                            .filter { it.value }
                            .takeIf { it.isNotEmpty() }
                            ?.joinToString { context.getString(it.key.title) }
                            ?: item.subtitle.value(context),
                        style = SubtitleStyle,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                val rotationAngle = animateFloatAsState(
                    targetValue = if (item.isExpanded) 180f else 0f,
                    label = "ArrowRotation"
                ).value

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand Indicator",
                    modifier = Modifier
                        .rotate(rotationAngle)
                        .size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (item.isExpanded) {
            HorizontalDivider()
            ToggleGroup(
                item.items,
                onItemUpdate
            )
        }

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
        )
    }
}

@Composable
@Preview
fun PreviewExpandableList() {
    val sampleItems = remember {
        mutableStateListOf(
            LockConfigurationItem(
                title = TextWrapper.Primitive("Lock Type"),
                subtitle = TextWrapper.Primitive("Select Lock Config"),
                selectedChoice = null,
            ),
            LockDurationItem(
                title = TextWrapper.Primitive("Duration"),
                subtitle = TextWrapper.Primitive("10s"),
                selectedChoice = 10,
            ),
        )
    }

    MaterialTheme {
        ConfigurationList(
            items = sampleItems,
            onItemUpdate = { index, updatedItem ->
                sampleItems[index] = updatedItem
            }
        )
    }
}