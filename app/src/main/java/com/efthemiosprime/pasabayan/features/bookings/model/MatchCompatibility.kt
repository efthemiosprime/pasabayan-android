package com.efthemiosprime.pasabayan.features.bookings.model

/**
 * Domain mirror of the inline `compatibility` object the backend returns on
 * 201 from the request / counter-offer endpoints and echoes in the 422
 * capacity-ack-required body.
 *
 * Per the advisory-weight policy, weight is informational at request time and
 * only enforced at accept time via [requiresCapacityAcknowledgment]. Date and
 * route flags are surfaced for UI banners but are not gate-keepers.
 */
data class MatchCompatibility(
    val weightOverCapacity: Boolean,
    val packageWeightKg: Double?,
    val tripAvailableWeightKg: Double?,
    val overageKg: Double?,
    val datesMisaligned: Boolean,
    val routeUncertain: Boolean,
    val requiresCapacityAcknowledgment: Boolean,
)
