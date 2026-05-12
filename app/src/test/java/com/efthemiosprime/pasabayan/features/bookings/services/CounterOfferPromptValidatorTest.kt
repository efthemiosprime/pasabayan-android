package com.efthemiosprime.pasabayan.features.bookings.services

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CounterOfferPromptValidatorTest {

    @Test
    fun `empty input is silently empty`() {
        val result = CounterOfferPromptValidator.validate("", originalPrice = 100.0, minDeliveryPrice = 5.0)
        assertEquals(CounterOfferPromptValidation.Empty, result)
    }

    @Test
    fun `whitespace only is empty`() {
        val result = CounterOfferPromptValidator.validate("   ", originalPrice = 100.0, minDeliveryPrice = 5.0)
        assertEquals(CounterOfferPromptValidation.Empty, result)
    }

    @Test
    fun `non-numeric input is NotNumeric`() {
        val result = CounterOfferPromptValidator.validate("abc", originalPrice = 100.0, minDeliveryPrice = 5.0)
        assertEquals(CounterOfferPromptValidation.NotNumeric, result)
    }

    @Test
    fun `zero is NotPositive`() {
        val result = CounterOfferPromptValidator.validate("0", originalPrice = 100.0, minDeliveryPrice = 5.0)
        assertEquals(CounterOfferPromptValidation.NotPositive, result)
    }

    @Test
    fun `negative is NotPositive`() {
        val result = CounterOfferPromptValidator.validate("-5", originalPrice = 100.0, minDeliveryPrice = 5.0)
        assertEquals(CounterOfferPromptValidation.NotPositive, result)
    }

    @Test
    fun `below minimum reports the minimum`() {
        val result = CounterOfferPromptValidator.validate("3", originalPrice = 100.0, minDeliveryPrice = 5.0)
        assertEquals(CounterOfferPromptValidation.BelowMinimum(5.0), result)
    }

    @Test
    fun `exact match to original is SameAsOriginal`() {
        val result = CounterOfferPromptValidator.validate("100", originalPrice = 100.0, minDeliveryPrice = 5.0)
        assertEquals(CounterOfferPromptValidation.SameAsOriginal, result)
    }

    @Test
    fun `within tolerance of original is SameAsOriginal`() {
        // 100.004 vs 100.0 → diff = 0.004 < 0.005 → SameAsOriginal
        val result = CounterOfferPromptValidator.validate("100.004", originalPrice = 100.0, minDeliveryPrice = 5.0)
        assertEquals(CounterOfferPromptValidation.SameAsOriginal, result)
    }

    @Test
    fun `just outside tolerance is Valid`() {
        // 100.01 vs 100.0 → diff = 0.01 >= 0.005 → Valid
        val result = CounterOfferPromptValidator.validate("100.01", originalPrice = 100.0, minDeliveryPrice = 5.0)
        assertEquals(CounterOfferPromptValidation.Valid(100.01), result)
    }

    @Test
    fun `valid price returns Valid with parsed value`() {
        val result = CounterOfferPromptValidator.validate("120.50", originalPrice = 100.0, minDeliveryPrice = 5.0)
        assertEquals(CounterOfferPromptValidation.Valid(120.50), result)
    }

    @Test
    fun `validation order — BelowMinimum beats SameAsOriginal`() {
        // 3.0 is below min AND equal to "original 3.0" — the minimum check fires first.
        val result = CounterOfferPromptValidator.validate("3", originalPrice = 3.0, minDeliveryPrice = 5.0)
        assertEquals(CounterOfferPromptValidation.BelowMinimum(5.0), result)
    }

    @Test
    fun `isSamePrice tolerance is symmetric`() {
        assertTrue(CounterOfferPromptValidator.isSamePrice(100.004, 100.0))
        assertTrue(CounterOfferPromptValidator.isSamePrice(99.996, 100.0))
        assertFalse(CounterOfferPromptValidator.isSamePrice(100.01, 100.0))
        assertFalse(CounterOfferPromptValidator.isSamePrice(99.99, 100.0))
    }
}
