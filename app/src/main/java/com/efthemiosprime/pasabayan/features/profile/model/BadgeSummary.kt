package com.efthemiosprime.pasabayan.features.profile.model

import java.time.Instant

/**
 * Single source-of-truth attention model driving every badge surface
 * (tab-bar Profile badge, dashboard bell, drawer sections, per-row badges).
 * iOS parity: `Pasabayan/Features/Notifications/Models/BadgeSummary.swift`.
 *
 * [total] is server-computed; consumers must read it as-is, never re-sum.
 * [degraded] = true means at least one sub-signal lookup failed server-side;
 * render the partial counts anyway.
 */
data class BadgeSummary(
    val unreadNotifications: Int = 0,
    val actionRequired: ActionRequired = ActionRequired.empty,
    val pendingReviewsCount: Int = 0,
    val verification: Verification = Verification.empty,
    val total: Int = 0,
    val computedAt: Instant? = null,
    val degraded: Boolean = false,
) {
    companion object {
        val empty = BadgeSummary()
    }
}

data class ActionRequired(
    val total: Int = 0,
    val activeDeliveriesNeedingUpdate: Int = 0,
    val pendingBookingRequests: Int = 0,
    val packagesReadyForPickup: Int = 0,
    val tripsWithoutActivity: Int = 0,
) {
    companion object {
        val empty = ActionRequired()
    }
}

data class Verification(
    val phoneVerificationNeeded: Boolean = false,
    val payoutSetupNeeded: Boolean = false,
) {
    companion object {
        val empty = Verification()
    }
}
