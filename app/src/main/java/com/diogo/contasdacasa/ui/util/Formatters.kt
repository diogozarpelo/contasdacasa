package com.diogo.contasdacasa.ui.util

import java.math.BigDecimal
import java.text.NumberFormat
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

private val brazilianLocale = Locale.forLanguageTag("pt-BR")

fun formatCurrency(amountInCents: Long): String {
    val amount = BigDecimal(amountInCents).movePointLeft(2)

    return NumberFormat
        .getCurrencyInstance(brazilianLocale)
        .format(amount)
}

fun formatMonthName(month: Int): String {
    return Month
        .of(month)
        .getDisplayName(TextStyle.FULL, brazilianLocale)
        .replaceFirstChar { character ->
            character.uppercase()
        }
}