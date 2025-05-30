package com.futurae.sampleapp.settings.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.futurae.sampleapp.ui.theme.FuturaeTypography
import com.futurae.sampleapp.ui.theme.SuccessColor
import com.futurae.sampleapp.ui.theme.WarningColor

@Composable
fun ResultComparisonVerdict(isSuccess: Boolean) {
    Text(
        text = if (isSuccess) {
            "Identical"
        } else {
            "Differentiation"
        },
        style = FuturaeTypography.titleH2,
        modifier = Modifier
            .background(
                color = if (isSuccess) {
                    SuccessColor
                } else {
                    WarningColor
                },
                shape = RoundedCornerShape(4.dp)
            )
            .padding(16.dp)
    )
}