package com.vitor.controlefinanceiro.core.money

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.absoluteValue

object MoneyFormatter {
    private val brLocale = Locale("pt", "BR")

    fun formatCentsToBrl(amountCents: Long): String {
        return NumberFormat.getCurrencyInstance(brLocale).format(amountCents / 100.0)
    }

    fun parseBrlToCents(input: String): Long? {
        val raw = input.trim()
        if (raw.isBlank()) return null
        val clean = raw
            .replace("R$", "")
            .replace(" ", "")
            .replace(".", "")
            .replace(",", ".")
        val parts = clean.split('.', limit = 2)
        if (parts.size > 2 || clean.startsWith("-")) return null
        val reais = parts.getOrNull(0)?.filter { it.isDigit() }?.toLongOrNull() ?: return null
        val centText = parts.getOrNull(1).orEmpty()
        if (centText.length > 2 || centText.any { !it.isDigit() }) return null
        val cents = when (centText.length) {
            0 -> 0
            1 -> centText.toLong() * 10
            else -> centText.toLong()
        }
        return reais * 100 + cents
    }

    fun signedFormat(amountCents: Long): String {
        val prefix = if (amountCents < 0) "-" else ""
        return prefix + formatCentsToBrl(amountCents.absoluteValue)
    }
}
