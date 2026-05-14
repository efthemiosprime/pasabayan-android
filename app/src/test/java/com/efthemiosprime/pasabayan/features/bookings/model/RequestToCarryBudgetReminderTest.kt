package com.efthemiosprime.pasabayan.features.bookings.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RequestToCarryBudgetReminderTest {

    @Test
    fun `flags when estimate is strictly greater than positive budget`() {
        assertTrue(
            RequestToCarryBudgetReminder.estimatedExceedsShipperBudget(
                estimatedTotal = 120.0,
                shipperMaxBudget = 100.0,
            ),
        )
    }

    @Test
    fun `does not flag when estimate equals the budget`() {
        assertFalse(
            RequestToCarryBudgetReminder.estimatedExceedsShipperBudget(
                estimatedTotal = 100.0,
                shipperMaxBudget = 100.0,
            ),
        )
    }

    @Test
    fun `does not flag when estimate is under the budget`() {
        assertFalse(
            RequestToCarryBudgetReminder.estimatedExceedsShipperBudget(
                estimatedTotal = 75.0,
                shipperMaxBudget = 100.0,
            ),
        )
    }

    @Test
    fun `does not flag when estimate is null or non-positive`() {
        assertFalse(
            RequestToCarryBudgetReminder.estimatedExceedsShipperBudget(
                estimatedTotal = null,
                shipperMaxBudget = 100.0,
            ),
        )
        assertFalse(
            RequestToCarryBudgetReminder.estimatedExceedsShipperBudget(
                estimatedTotal = 0.0,
                shipperMaxBudget = 100.0,
            ),
        )
        assertFalse(
            RequestToCarryBudgetReminder.estimatedExceedsShipperBudget(
                estimatedTotal = -25.0,
                shipperMaxBudget = 100.0,
            ),
        )
    }

    @Test
    fun `does not flag when budget is null or non-positive`() {
        assertFalse(
            RequestToCarryBudgetReminder.estimatedExceedsShipperBudget(
                estimatedTotal = 200.0,
                shipperMaxBudget = null,
            ),
        )
        assertFalse(
            RequestToCarryBudgetReminder.estimatedExceedsShipperBudget(
                estimatedTotal = 200.0,
                shipperMaxBudget = 0.0,
            ),
        )
    }
}
