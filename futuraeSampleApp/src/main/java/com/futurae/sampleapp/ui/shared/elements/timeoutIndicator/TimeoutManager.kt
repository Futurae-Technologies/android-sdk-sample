package com.futurae.sampleapp.ui.shared.elements.timeoutIndicator

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun CoroutineScope.startCountdown(
    timeoutInSeconds: Long,
    onProgressUpdate: (Float) -> Unit,
    onTimeout: () -> Unit
): Job {
    onProgressUpdate(1f)

    return launch {
        val totalMillis = timeoutInSeconds * 1000L
        val startTime = System.currentTimeMillis()

        while (true) {
            val elapsedTime = System.currentTimeMillis() - startTime
            val remainingProgress = 1f - elapsedTime.toFloat() / totalMillis

            if (remainingProgress <= 0f) {
                onProgressUpdate(0f)
                onTimeout()
                break
            }

            onProgressUpdate(remainingProgress)
            delay(30L)
        }
    }
}