package com.futurae.demoapp.settings.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.futurae.demoapp.R
import com.futurae.demoapp.ui.TextWrapper
import com.futurae.demoapp.ui.shared.elements.buttons.ActionButton
import com.futurae.demoapp.ui.theme.OnPrimaryColor

@Composable
fun DebugResultComparison(
    isSuccess: Boolean,
    onDismiss: () -> Unit,
    firstResult: @Composable () -> Unit,
    secondResult: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OnPrimaryColor)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ResultComparisonVerdict(isSuccess = isSuccess)
        Spacer(Modifier.size(16.dp))

        firstResult()

        Spacer(Modifier.size(8.dp))

        secondResult()

        Spacer(Modifier.size(16.dp))

        ActionButton(
            text = TextWrapper.Resource(R.string.dismiss),
            onClick = onDismiss
        )
    }
}