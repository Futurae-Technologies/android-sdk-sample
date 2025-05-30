package com.futurae.demoapp.settings.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.futurae.demoapp.settings.SettingsItem
import com.futurae.demoapp.settings.SettingsListItem
import com.futurae.demoapp.settings.SettingsNestedToggle
import com.futurae.demoapp.settings.SettingsNestedToggleGroup
import com.futurae.demoapp.settings.SettingsSpacer
import com.futurae.demoapp.settings.SettingsToggle
import com.futurae.demoapp.ui.TextWrapper
import com.futurae.demoapp.ui.theme.ItemTitleStyle
import com.futurae.demoapp.ui.theme.OnPrimaryColor
import com.futurae.demoapp.ui.theme.SubtitleStyle
import com.futurae.demoapp.ui.theme.TextAlternative
import com.futurae.demoapp.ui.theme.WarningColor
import com.futurae.demoapp.ui.theme.fTSwitchTheme

@Composable
fun SettingsRowComposable(item: SettingsListItem) {
    when (item) {
        is SettingsItem -> SettingsItemComposable(item)
        is SettingsToggle -> SettingsToggleComposable(item)
        is SettingsNestedToggleGroup -> SettingsNestedToggleGroupComposable(item)
        SettingsSpacer -> Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun SettingsItemComposable(item: SettingsItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(OnPrimaryColor)
            .clickable(item.isItemClickable) { item.actionCallback() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title.value(LocalContext.current),
                    style = ItemTitleStyle,
                    color = when {
                        item.isItemWithWarning -> WarningColor
                        item.isItemClickable -> ItemTitleStyle.color
                        else -> TextAlternative
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.subtitle.value(LocalContext.current),
                    style = SubtitleStyle,
                )
            }

            item.icon?.let {
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = it,
                    contentDescription = "Expand Indicator",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
        }
        HorizontalDivider()
    }
}


@Composable
fun SettingsToggleComposable(item: SettingsToggle) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(OnPrimaryColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = item.title.value(LocalContext.current),
                    style = ItemTitleStyle
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.subtitle.value(LocalContext.current),
                    style = SubtitleStyle,
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                checked = item.isEnabled,
                onCheckedChange = { item.onToggleChanged(it) },
                colors = fTSwitchTheme()
            )
        }
        HorizontalDivider()
    }
}

@Composable
fun SettingsNestedToggleGroupComposable(item: SettingsNestedToggleGroup) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(OnPrimaryColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title.value(LocalContext.current),
                    style = ItemTitleStyle
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.subtitle.value(LocalContext.current),
                    style = SubtitleStyle,
                )
            }
            Switch(
                checked = item.isToggled,
                onCheckedChange = { item.onToggleChanged(it) },
                colors = fTSwitchTheme()
            )
        }
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        item.children.forEach { child ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .alpha(if (!item.isToggled) 0.5f else 1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = child.title.value(LocalContext.current),
                    style = SubtitleStyle,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = child.isToggled,
                    onCheckedChange = { child.onToggleChanged(it) },
                    enabled = item.isToggled,
                    colors = fTSwitchTheme()
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsNestedToggleGroupPreview() {
    var parentToggle by remember { mutableStateOf(true) }
    var childToggle1 by remember { mutableStateOf(true) }
    var childToggle2 by remember { mutableStateOf(false) }

    val item = SettingsNestedToggleGroup(
        title = TextWrapper.Primitive("Adaptive Settings"),
        subtitle = TextWrapper.Primitive("Manage your adaptive features"),
        isToggled = parentToggle,
        onToggleChanged = { parentToggle = it },
        children = listOf(
            SettingsNestedToggle(
                title = TextWrapper.Primitive("Authentication"),
                isToggled = childToggle1,
                isEnabled = false,
                onToggleChanged = { childToggle1 = it }
            ),
            SettingsNestedToggle(
                title = TextWrapper.Primitive("Migration"),
                isToggled = childToggle2,
                isEnabled = true,
                onToggleChanged = { childToggle2 = it }
            )
        )
    )

    MaterialTheme {
        SettingsNestedToggleGroupComposable(item = item)
    }
}


@Preview(showBackground = true)
@Composable
fun SettingsToggleComposablePreview() {
    val item = SettingsToggle(
        title = TextWrapper.Primitive("Adaptive Settings"),
        subtitle = TextWrapper.Primitive("Manage your adaptive features"),
        isEnabled = true,
        onToggleChanged = { },
    )

    MaterialTheme {
        SettingsToggleComposable(item = item)
    }
}


@Preview(showBackground = true)
@Composable
fun SettingsItemComposablePreview() {
    val item = SettingsItem(
        title = TextWrapper.Primitive("Adaptive Settings"),
        subtitle = TextWrapper.Primitive("Manage your adaptive features"),
        actionCallback = { }
    )

    MaterialTheme {
        SettingsItemComposable(item = item)
    }
}



@Preview(showBackground = true)
@Composable
fun SettingsItemDisabledComposablePreview() {
    val item = SettingsItem(
        title = TextWrapper.Primitive("Adaptive Settings"),
        subtitle = TextWrapper.Primitive("Manage your adaptive features"),
        isItemClickable = false,
        actionCallback = { }
    )

    MaterialTheme {
        SettingsItemComposable(item = item)
    }
}