package com.futurae.sampleapp.settings.adaptive.collections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.futurae.sampleapp.ui.theme.OnPrimaryColor

@Composable
fun AdaptiveCollectDetailsScreen(details: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OnPrimaryColor)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            modifier = Modifier.fillMaxSize(),
            text = details,
            color = Color.Black,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}