package com.efthemiosprime.pasabayan.features.bookings.model

/**
 * Compares carrier estimated earnings to the shipper's stated max budget on the
 * package request. Mirrors iOS `RequestToCarryBudgetReminder.swift` — pure
 * value logic that lets the carrier-side request flow render a budget alert.
 */
object RequestToCarryBudgetReminder {

    /**
     * `true` when the estimated total is positive *and* the shipper's
     * `max_price_budget` is positive *and* the estimate exceeds it. Missing
     * inputs return `false` so the alert never appears for unknown budgets.
     */
    fun estimatedExceedsShipperBudget(
        estimatedTotal: Double?,
        shipperMaxBudget: Double?,
    ): Boolean {
        val estimated = estimatedTotal?.takeIf { it > 0.0 } ?: return false
        val cap = shipperMaxBudget?.takeIf { it > 0.0 } ?: return false
        return estimated > cap
    }
}
