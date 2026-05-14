package com.efthemiosprime.pasabayan.features.bookings.model

import com.efthemiosprime.pasabayan.features.bookings.model.CompatibleTripsManualPriceValidation as ManualPriceValidation
import com.efthemiosprime.pasabayan.features.bookings.model.CompatibleTripsOfferedPriceSource as PriceSource
import com.efthemiosprime.pasabayan.features.bookings.model.CompatibleTripsOptionalOfferResolution as OptionalResolution
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Mirrors iOS `CompatibleTripsPricingPolicy` (CompatibleTripsView.swift).
 * Pure value logic — no Compose, no Android, no coroutines.
 */
class CompatibleTripsPricingPolicyTest {

    // -- resolveOfferedPrice --

    @Test
    fun `resolveOfferedPrice returns flat price when available`() {
        val (price, source) = CompatibleTripsPricingPolicy.resolveOfferedPrice(
            tripPricePerKg = "5.50",
            packageWeightKg = 10.0,
            packageMaxBudget = 200.0,
            flatTripPrice = 120.0,
        )
        assertEquals(120.0, price, 0.0001)
        assertEquals(PriceSource.CALCULATED, source)
    }

    @Test
    fun `resolveOfferedPrice computes pricePerKg times weight when no flat price`() {
        val (price, source) = CompatibleTripsPricingPolicy.resolveOfferedPrice(
            tripPricePerKg = "5.50",
            packageWeightKg = 10.0,
            packageMaxBudget = 200.0,
        )
        assertEquals(55.0, price, 0.0001)
        assertEquals(PriceSource.CALCULATED, source)
    }

    @Test
    fun `resolveOfferedPrice falls back to package budget when no rate or weight`() {
        val (price, source) = CompatibleTripsPricingPolicy.resolveOfferedPrice(
            tripPricePerKg = "",
            packageWeightKg = null,
            packageMaxBudget = 150.0,
        )
        assertEquals(150.0, price, 0.0001)
        assertEquals(PriceSource.PACKAGE_BUDGET, source)
    }

    @Test
    fun `resolveOfferedPrice returns unavailable when nothing is known`() {
        val (price, source) = CompatibleTripsPricingPolicy.resolveOfferedPrice(
            tripPricePerKg = "",
            packageWeightKg = null,
            packageMaxBudget = null,
        )
        assertEquals(0.0, price, 0.0001)
        assertEquals(PriceSource.UNAVAILABLE, source)
    }

    @Test
    fun `resolveOfferedPrice ignores zero or negative flat price`() {
        val (price, source) = CompatibleTripsPricingPolicy.resolveOfferedPrice(
            tripPricePerKg = "5.50",
            packageWeightKg = 10.0,
            packageMaxBudget = 200.0,
            flatTripPrice = 0.0,
        )
        assertEquals(55.0, price, 0.0001)
        assertEquals(PriceSource.CALCULATED, source)
    }

    // -- effectiveBudgetLimitForOptionalOffer --

    @Test
    fun `effectiveBudgetLimitForOptionalOffer prefers max of budget vs automatic`() {
        assertEquals(
            150.0,
            CompatibleTripsPricingPolicy.effectiveBudgetLimitForOptionalOffer(
                packageMaxBudget = 100.0,
                automaticOfferedPrice = 150.0,
            )!!,
            0.0001,
        )
        assertEquals(
            200.0,
            CompatibleTripsPricingPolicy.effectiveBudgetLimitForOptionalOffer(
                packageMaxBudget = 200.0,
                automaticOfferedPrice = 50.0,
            )!!,
            0.0001,
        )
    }

    @Test
    fun `effectiveBudgetLimitForOptionalOffer returns null when no budget`() {
        assertNull(
            CompatibleTripsPricingPolicy.effectiveBudgetLimitForOptionalOffer(
                packageMaxBudget = null,
                automaticOfferedPrice = 100.0,
            ),
        )
        assertNull(
            CompatibleTripsPricingPolicy.effectiveBudgetLimitForOptionalOffer(
                packageMaxBudget = 0.0,
                automaticOfferedPrice = 100.0,
            ),
        )
    }

    // -- parseManualPriceInput --

    @Test
    fun `parseManualPriceInput handles comma decimal separator`() {
        assertEquals(123.45, CompatibleTripsPricingPolicy.parseManualPriceInput("123,45")!!, 0.0001)
        assertEquals(123.45, CompatibleTripsPricingPolicy.parseManualPriceInput("  123.45  ")!!, 0.0001)
    }

    @Test
    fun `parseManualPriceInput returns null on invalid input`() {
        assertNull(CompatibleTripsPricingPolicy.parseManualPriceInput("abc"))
        assertNull(CompatibleTripsPricingPolicy.parseManualPriceInput(""))
    }

    // -- validateManualOfferedPrice --

    @Test
    fun `validateManualOfferedPrice rejects zero or negative`() {
        assertEquals(
            ManualPriceValidation.Invalid,
            CompatibleTripsPricingPolicy.validateManualOfferedPrice(0.0, budgetLimit = 100.0),
        )
        assertEquals(
            ManualPriceValidation.Invalid,
            CompatibleTripsPricingPolicy.validateManualOfferedPrice(-1.0, budgetLimit = 100.0),
        )
    }

    @Test
    fun `validateManualOfferedPrice rejects when over budget`() {
        val result = CompatibleTripsPricingPolicy.validateManualOfferedPrice(
            offeredPrice = 200.0,
            budgetLimit = 150.0,
        )
        assertEquals(ManualPriceValidation.OverBudget(limit = 150.0), result)
    }

    @Test
    fun `validateManualOfferedPrice rejects when over global max`() {
        val result = CompatibleTripsPricingPolicy.validateManualOfferedPrice(
            offeredPrice = 99_999.0,
            budgetLimit = null,
        )
        assertEquals(
            ManualPriceValidation.TooHigh(maxAllowed = CompatibleTripsPricingPolicy.MAX_ALLOWED_OFFERED_PRICE),
            result,
        )
    }

    @Test
    fun `validateManualOfferedPrice accepts within bounds`() {
        val result = CompatibleTripsPricingPolicy.validateManualOfferedPrice(
            offeredPrice = 100.0,
            budgetLimit = 150.0,
        )
        assertEquals(ManualPriceValidation.Valid(100.0), result)
    }

    // -- resolveOptionalShipperOffer --

    @Test
    fun `resolveOptionalShipperOffer treats blank input as useAutomatic`() {
        assertEquals(
            OptionalResolution.UseAutomatic,
            CompatibleTripsPricingPolicy.resolveOptionalShipperOffer(
                input = "  ",
                budgetLimit = 200.0,
                minimumDeliveryPrice = 10.0,
            ),
        )
    }

    @Test
    fun `resolveOptionalShipperOffer flags below-minimum`() {
        val result = CompatibleTripsPricingPolicy.resolveOptionalShipperOffer(
            input = "5.00",
            budgetLimit = 200.0,
            minimumDeliveryPrice = 10.0,
        )
        assertEquals(OptionalResolution.BelowMinimum(offered = 5.0), result)
    }

    @Test
    fun `resolveOptionalShipperOffer flags invalid input`() {
        assertEquals(
            OptionalResolution.InvalidInput,
            CompatibleTripsPricingPolicy.resolveOptionalShipperOffer(
                input = "not-a-number",
                budgetLimit = 200.0,
                minimumDeliveryPrice = 10.0,
            ),
        )
    }

    @Test
    fun `resolveOptionalShipperOffer accepts valid override`() {
        val result = CompatibleTripsPricingPolicy.resolveOptionalShipperOffer(
            input = "120.50",
            budgetLimit = 200.0,
            minimumDeliveryPrice = 10.0,
        )
        assertEquals(OptionalResolution.OverridePrice(price = 120.50), result)
    }

    @Test
    fun `resolveOptionalShipperOffer flags over budget`() {
        val result = CompatibleTripsPricingPolicy.resolveOptionalShipperOffer(
            input = "250",
            budgetLimit = 200.0,
            minimumDeliveryPrice = 10.0,
        )
        assertEquals(OptionalResolution.OverBudget(limit = 200.0), result)
    }

    @Test
    fun `resolveOptionalShipperOffer flags too-high above global max`() {
        val result = CompatibleTripsPricingPolicy.resolveOptionalShipperOffer(
            input = "99999",
            budgetLimit = null,
            minimumDeliveryPrice = 10.0,
        )
        assertTrue(result is OptionalResolution.TooHigh)
    }
}
