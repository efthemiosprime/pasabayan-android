package com.efthemiosprime.pasabayan.features.bookings.services

import kotlin.math.abs

/**
 * Validation outcomes for the counter-offer prompt.
 *
 * Parity with iOS `CounterOfferPromptView`:
 * - Empty / NotNumeric are silent (no error chrome, hint shown instead).
 * - NotPositive / BelowMinimum / SameAsOriginal show an inline error.
 * - Only Valid enables submit.
 */
sealed class CounterOfferPromptValidation {
    data object Empty : CounterOfferPromptValidation()
    data object NotNumeric : CounterOfferPromptValidation()
    data object NotPositive : CounterOfferPromptValidation()
    data class BelowMinimum(val minimum: Double) : CounterOfferPromptValidation()
    data object SameAsOriginal : CounterOfferPromptValidation()
    data class Valid(val price: Double) : CounterOfferPromptValidation()
}

/**
 * Pure validator extracted so the rules are unit-testable and stay aligned
 * with iOS. Mirrors `CounterOfferPromptView.validatePrice` and the same-price
 * ±$0.005 tolerance from `CounterOfferPromptView.isSamePrice`.
 */
object CounterOfferPromptValidator {

    /**
     * Two prices are "the same" if they differ by less than half a cent —
     * accounts for decimal input precision (e.g. "100" vs "100.00").
     */
    const val SAME_PRICE_TOLERANCE = 0.005

    fun validate(
        rawInput: String,
        originalPrice: Double,
        minDeliveryPrice: Double,
    ): CounterOfferPromptValidation {
        if (rawInput.isBlank()) return CounterOfferPromptValidation.Empty
        val price = rawInput.toDoubleOrNull() ?: return CounterOfferPromptValidation.NotNumeric
        if (price <= 0.0) return CounterOfferPromptValidation.NotPositive
        if (price < minDeliveryPrice) return CounterOfferPromptValidation.BelowMinimum(minDeliveryPrice)
        if (isSamePrice(price, originalPrice)) return CounterOfferPromptValidation.SameAsOriginal
        return CounterOfferPromptValidation.Valid(price)
    }

    fun isSamePrice(price: Double, originalPrice: Double): Boolean =
        abs(price - originalPrice) < SAME_PRICE_TOLERANCE
}
