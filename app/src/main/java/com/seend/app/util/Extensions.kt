package com.seend.app.util

import java.text.SimpleDateFormat
import java.util.*

fun String.formatTime(): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(this) ?: return this
        
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        outputFormat.format(date)
    } catch (e: Exception) {
        this
    }
}

fun String.formatLastSeen(): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(this) ?: return ""
        val now = Calendar.getInstance()
        val msgDate = Calendar.getInstance().apply { time = date }
        
        val hourFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        
        val diffMs = now.timeInMillis - msgDate.timeInMillis
        val diffMin = diffMs / (1000 * 60)
        val diffHours = diffMin / 60
        val diffDays = diffHours / 24
        
        val timeStr = hourFormat.format(date)
        
        when {
            diffDays < 0 -> timeStr  // futuro
            diffDays == 0L -> "Últ. vez a las $timeStr"
            diffDays == 1L -> "Últ. vez ayer a las $timeStr"
            diffDays < 7L -> {
                val dayName = dayFormat.format(date)
                "Últ. vez el $dayName a las $timeStr"
            }
            else -> {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                "Últ. vez ${dateFormat.format(date)} a las $timeStr"
            }
        }
    } catch (e: Exception) {
        ""
    }
}
