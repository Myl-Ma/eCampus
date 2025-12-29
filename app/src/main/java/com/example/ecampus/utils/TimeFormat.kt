package com.example.ecampus.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.toDisplayTime(): String {
    if (this <= 0L) return ""
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date(this))
}
