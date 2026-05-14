package com.futurae.sampleapp.ui.shared.elements.timeoutIndicator

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
import com.futurae.sampleapp.BuildConfig
import com.futurae.sampleapp.ui.theme.DisableColor
import com.futurae.sampleapp.ui.theme.SuccessColor
import com.futurae.sampleapp.ui.theme.WarningColor

@Composable
fun TimeoutIndicator(progress: Float) {
    val displayProgress = if (BuildConfig.BUILD_TYPE == "qa") {
        progress
    } else {
        val animatedProgress by animateFloatAsState(
            targetValue = progress,
            label = "Timeout animation"
        )
        animatedProgress
    }

    val progressColor = when {
        displayProgress > 0.2f -> SuccessColor
        else -> WarningColor
    }

    val displayColor = if (BuildConfig.BUILD_TYPE == "qa") {
        progressColor
    } else {
        val animatedColor by animateColorAsState(
            targetValue = progressColor,
            label = "Color animation"
        )
        animatedColor
    }

    LinearProgressIndicator(
        progress = { displayProgress },
        modifier = Modifier.fillMaxWidth().height(6.dp),
        color = displayColor,
        strokeCap = StrokeCap.Square,
        trackColor = DisableColor,
        gapSize = (-15).dp,
        drawStopIndicator = {}
    )
}