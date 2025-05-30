package com.futurae.sampleapp.ui

import android.content.Context
import androidx.annotation.StringRes

sealed interface TextWrapper {

    fun value(context: Context): String

    data class Primitive(val text: String): TextWrapper {

        override fun value(context: Context): String = text
    }

    data class Resource(
        @StringRes val stringRes: Int,
        val args: List<Any> = emptyList()
    ) : TextWrapper {

        override fun value(context: Context): String = context.getString(
            stringRes,
            *args.toTypedArray()
        )
    }
}