package com.efthemiosprime.pasabayan.features.payments.model

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object MoneyFormatter {

    /**
     * Locale-aware currency rendering. Falls back to `"$%.2f CODE"` when the currency
     * code is unknown to the JDK (e.g. test data, deprecated codes).
     */
    fun formatCurrency(
        value: Double,
        currency: String = DEFAULT_CURRENCY,
        locale: Locale = Locale.getDefault(),
    ): String {
        val code = currency.uppercase()
        val currencyInstance = runCatching { Currency.getInstance(code) }.getOrNull()
            ?: return formatFallback(value, code)
        val formatter = NumberFormat.getCurrencyInstance(locale)
        formatter.currency = currencyInstance
        return formatter.format(value)
    }

    /** Prepends `+` to strictly positive values; zero and negative use [formatCurrency] unchanged. */
    fun formatSignedCurrency(
        value: Double,
        currency: String = DEFAULT_CURRENCY,
        locale: Locale = Locale.getDefault(),
    ): String = if (value > 0) {
        "+${formatCurrency(value, currency, locale)}"
    } else {
        formatCurrency(value, currency, locale)
    }

    private fun formatFallback(value: Double, currencyCode: String): String =
        String.format(Locale.US, "$%.2f %s", value, currencyCode)

    const val DEFAULT_CURRENCY: String = "cad"
}
