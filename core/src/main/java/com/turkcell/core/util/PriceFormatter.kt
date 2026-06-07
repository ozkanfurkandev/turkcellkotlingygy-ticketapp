package com.turkcell.core.util

import java.text.NumberFormat
import java.util.Locale

private val turkishCurrencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("tr-TR"))

fun formatPriceCents(cents: Long): String {
    val amount = cents / 100.0
    return turkishCurrencyFormat.format(amount)
}

fun formatPriceRangeCents(minCents: Long, maxCents: Long): String {
    if (minCents == maxCents) return formatPriceCents(minCents)
    return "${formatPriceCents(minCents)} – ${formatPriceCents(maxCents)}"
}
