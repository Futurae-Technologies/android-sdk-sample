package com.futurae.sampleapp.ui.shared.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.futurae.sampleapp.ui.shared.elements.configuration.SdkConfigOptionalFlag
import com.futurae.sampleapp.ui.theme.SubtitleStyle
import com.futurae.sampleapp.ui.theme.fTSwitchTheme

@Composable
fun ToggleGroup(
    options: Map<SdkConfigOptionalFlag, Boolean>,
    onSelectionChanged: (Pair<SdkConfigOptionalFlag, Boolean>) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        options.forEach { option ->
            ToggleRow(option.key, option.value, onSelectionChanged)
        }
    }
}


@Composable
fun ToggleRow(
    flag: SdkConfigOptionalFlag,
    isChecked: Boolean,
    onCheckChanged: (Pair<SdkConfigOptionalFlag, Boolean>) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = LocalContext.current.getString(flag.title),
            style = SubtitleStyle,
        )
        Switch(
            checked = isChecked,
            onCheckedChange = { onCheckChanged(flag to it) },
            colors = fTSwitchTheme(),
            modifier = Modifier.testTag(LocalContext.current.getString(flag.testTag))
        )
    }
}