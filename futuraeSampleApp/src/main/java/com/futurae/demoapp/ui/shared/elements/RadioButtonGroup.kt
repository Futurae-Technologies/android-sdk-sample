package com.futurae.demoapp.ui.shared.elements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.futurae.demoapp.ui.theme.SubtitleStyle
import com.futurae.demoapp.ui.theme.fTRadioButtonTheme

@Composable
fun <T> RadioButtonGroup(
    options: List<T>,
    selectedOption: T?,
    onSelectionChanged: (T) -> Unit,
    valueFormatter: (T) -> String = { t -> t.toString() },
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        options.forEach { option ->
            RadioButtonRow(option, option === selectedOption, valueFormatter, onSelectionChanged)
        }
    }
}

@Composable
fun <T> RadioButtonRow(
    item: T,
    isSelected: Boolean,
    valueFormatter: (T) -> String = { t -> t.toString() },
    onSelectionChanged: (T) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onSelectionChanged(item)
            }
            .padding(vertical = 8.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = valueFormatter.invoke(item),
            style = SubtitleStyle,
            modifier = Modifier.weight(1f)
        )
        RadioButton(
            selected = isSelected,
            onClick = { onSelectionChanged(item) },
            colors = fTRadioButtonTheme()
        )
    }
}

@Composable
@Preview
fun PreviewRadioButtonGroup() {
    val selectedOption = "Option 1"

    MaterialTheme {
        RadioButtonGroup(
            options = listOf("Option 1", "Option 2", "Option 3"),
            selectedOption = selectedOption,
            onSelectionChanged = { },
            valueFormatter = { item -> item }
        )
    }
}
