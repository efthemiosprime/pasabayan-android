package com.efthemiosprime.pasabayan.core.network.profile

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Aggregated "needs attention" signals for the Profile tab badges.
 * Wire format from `GET /api/me/attention`. iOS parity:
 * `Pasabayan/Features/Profile/Models/AttentionSignals.swift`.
 *
 * Server owns the rules for what counts as actionable. Clients render
 * what comes back; if [degraded] is true at least one sub-signal lookup
 * failed server-side — render the partial values rather than show an
 * error. Refresh on Profile-tab open + after the actionable flow
 * completes (verify, payout, rating). No client-side polling.
 */
@Serializable
data class AttentionSignalsJson(
    @SerialName("pending_reviews_count") val pendingReviewsCount: Int = 0,
    @SerialName("phone_verification_needed") val phoneVerificationNeeded: Boolean = false,
    @SerialName("payout_setup_needed") val payoutSetupNeeded: Boolean = false,
    val total: Int = 0,
    @SerialName("computed_at") val computedAt: String? = null,
    val degraded: Boolean = false,
)
