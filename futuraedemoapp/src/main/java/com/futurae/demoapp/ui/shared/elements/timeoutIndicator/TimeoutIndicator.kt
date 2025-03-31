package com.futurae.demoapp.ui.shared.elements.timeoutIndicator

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.futurae.demoapp.ui.theme.DisableColor
import com.futurae.demoapp.ui.theme.SuccessColor
import com.futurae.demoapp.ui.theme.WarningColor

@Composable
fun TimeoutIndicator(progress: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "Timeout animation"
    )

    val progressColor = when {
        animatedProgress > 0.2f -> SuccessColor
        else -> WarningColor
    }

    val animatedColor by animateColorAsState(
        targetValue = progressColor,
        label = "Color animation"
    )

    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = Modifier.fillMaxWidth().height(6.dp),
        color = animatedColor,
        strokeCap = StrokeCap.Square,
        trackColor = DisableColor,
        gapSize = (-15).dp,
        drawStopIndicator = {}
    )
}