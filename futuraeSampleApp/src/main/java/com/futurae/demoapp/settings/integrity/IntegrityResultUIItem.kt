package com.futurae.demoapp.settings.integrity

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.futurae.demoapp.R
import com.futurae.demoapp.ui.TextWrapper

data class IntegrityResultUIItem(
    val title: TextWrapper,
    val level: IntegrityLevel,
    val element: PresentationElement,
    val informativeText: TextWrapper,
    val explanationText: TextWrapper? = null
)

enum class IntegrityLevel {
    NONE,
    WEAK,
    BASIC,
    STRONG;

    @DrawableRes
    fun toGraphicRes(): Int = when (this) {
        NONE -> R.drawable.graphic_weak
        WEAK,
        BASIC -> R.drawable.graphic_basic
        STRONG -> R.drawable.graphic_strong
    }

    @DrawableRes
    fun toDrawableRes(): Int {
        return when (this) {
            NONE -> R.drawable.ic_failure
            WEAK -> R.drawable.ic_alert
            BASIC -> R.drawable.ic_help
            STRONG -> R.drawable.ic_success
        }
    }

    @StringRes
    fun toStringRes(): Int {
        return when (this) {
            NONE -> R.string.none
            WEAK -> R.string.weak
            BASIC -> R.string.basic
            STRONG -> R.string.strong
        }
    }
}

enum class PresentationElement {
    GRAPHIC,
    BAR;
}

