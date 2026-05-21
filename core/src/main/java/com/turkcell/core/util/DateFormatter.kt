package com.turkcell.core.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

private val turkishMonthsShort = arrayOf(
    "Oca", "Şub", "Mar", "Nis", "May", "Haz", "Tem", "Ağu", "Eyl", "Eki", "Kas", "Ara",
)

private val isoPatterns = listOf(
    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
    "yyyy-MM-dd'T'HH:mm:ss'Z'",
)

private fun parseIsoDate(isoDate: String): Calendar? {
    for (pattern in isoPatterns) {
        val parser = SimpleDateFormat(pattern, Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val parsed = runCatching { parser.parse(isoDate) }.getOrNull() ?: continue
        return Calendar.getInstance().apply { time = parsed }
    }
    return null
}

fun formatEventDate(isoDate: String): String {
    if (isoDate.isBlank()) return ""

    val calendar = parseIsoDate(isoDate) ?: return isoDate
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val month = turkishMonthsShort[calendar.get(Calendar.MONTH)]
    val year = calendar.get(Calendar.YEAR)
    val hour = calendar.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
    val minute = calendar.get(Calendar.MINUTE).toString().padStart(2, '0')
    return "$day $month $year · $hour:$minute"
}

fun formatEventDateShort(isoDate: String): String {
    if (isoDate.isBlank()) return ""

    val calendar = parseIsoDate(isoDate) ?: return isoDate
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val month = turkishMonthsShort[calendar.get(Calendar.MONTH)]
    return "$day $month"
}
