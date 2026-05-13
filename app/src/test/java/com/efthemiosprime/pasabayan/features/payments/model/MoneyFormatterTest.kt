package com.efthemiosprime.pasabayan.features.payments.model

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyFormatterTest {

    @Test
    fun `USD in en-US renders dollar prefix`() {
        val result = MoneyFormatter.formatCurrency(99.99, "USD", Locale.US)
        assertEquals("$99.99", result)
    }

    @Test
    fun `CAD in en-CA renders dollar prefix`() {
        // en_CA formats CAD without a currency-prefix qualifier ("$1.99", not "CA$1.99").
        val result = MoneyFormatter.formatCurrency(99.99, "CAD", Locale.CANADA)
        assertEquals("$99.99", result)
    }

    @Test
    fun `currency code is case insensitive`() {
        val lower = MoneyFormatter.formatCurrency(50.0, "usd", Locale.US)
        val upper = MoneyFormatter.formatCurrency(50.0, "USD", Locale.US)
        assertEquals(upper, lower)
    }

    @Test
    fun `unknown currency code falls back to dollar plus suffix`() {
        val result = MoneyFormatter.formatCurrency(99.99, "XYZ")
        assertEquals("$99.99 XYZ", result)
    }

    @Test
    fun `empty currency code falls back to dollar plus suffix`() {
        val result = MoneyFormatter.formatCurrency(50.0, "")
        assertEquals("$50.00 ", result)
    }

    @Test
    fun `negative values include negative sign`() {
        val result = MoneyFormatter.formatCurrency(-5.0, "USD", Locale.US)
        // en_US renders negatives with a leading minus by default.
        assertTrue("expected leading minus, got '$result'", result.startsWith("-"))
        assertTrue(result.contains("5.00"))
    }

    @Test
    fun `formatSignedCurrency prepends plus for positive`() {
        val result = MoneyFormatter.formatSignedCurrency(5.0, "USD", Locale.US)
        assertEquals("+$5.00", result)
    }

    @Test
    fun `formatSignedCurrency does not prepend plus for zero`() {
        val result = MoneyFormatter.formatSignedCurrency(0.0, "USD", Locale.US)
        assertEquals("$0.00", result)
    }

    @Test
    fun `formatSignedCurrency leaves negative sign untouched`() {
        val result = MoneyFormatter.formatSignedCurrency(-5.0, "USD", Locale.US)
        assertTrue("expected leading minus, got '$result'", result.startsWith("-"))
        assertTrue(result.contains("5.00"))
    }

    @Test
    fun `formatSignedCurrency with unknown currency uses fallback`() {
        val result = MoneyFormatter.formatSignedCurrency(2.5, "XYZ")
        assertEquals("+$2.50 XYZ", result)
    }

    @Test
    fun `default currency is cad`() {
        assertEquals("cad", MoneyFormatter.DEFAULT_CURRENCY)
    }
}
