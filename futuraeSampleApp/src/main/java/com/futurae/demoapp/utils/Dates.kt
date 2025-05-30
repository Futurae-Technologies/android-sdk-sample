package com.futurae.demoapp.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.fullTimestampFormat(): String {
    val date = Date(this * 1000)
    return SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(date)
}