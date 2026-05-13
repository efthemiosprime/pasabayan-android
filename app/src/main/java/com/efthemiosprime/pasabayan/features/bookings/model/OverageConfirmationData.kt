package com.efthemiosprime.pasabayan.features.bookings.model

/**
 * UI state for the "Accept Anyway?" confirmation sheet that gates accepting an
 * over-capacity match (advisory-weight policy).
 *
 * Populated either by the ViewModel's pre-flight check (local match weight vs
 * carrier's stated available capacity) or by the 422 fallback when the server
 * returns [com.efthemiosprime.pasabayan.core.domain.error.DomainError.CapacityAcknowledgmentRequired].
 *
 * The sheet uses [isCarrierAccepting] to pick role-specific copy and feeds
 * back to the same `acknowledge_overage = true` retry path either way.
 */
data class OverageConfirmationData(
    val matchId: Int,
    val isCarrierAccepting: Boolean,
    val packageWeightKg: Double,
    val availableWeightKg: Double,
    val overageKg: Double,
)
