package com.efthemiosprime.pasabayan.core.network.profile

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Single source-of-truth attention payload for every badge surface (tab-bar Profile
 * badge, dashboard bell, drawer sections, per-row badges on Profile).
 * Wire format from `GET /api/me/badge-summary` (optional `?role=carrier|shipper`).
 * iOS parity: `Pasabayan/Features/Notifications/Models/BadgeSummary.swift`.
 *
 * Defaults absorb partial server payloads — missing numerics decode as 0, missing
 * bools as false, missing nested objects as their `.empty` equivalents.
 *
 * [total] is server-computed. Never re-sum on the client.
 * [degraded] = true means at least one sub-signal lookup failed server-side; render
 * what came back. The next refresh retries — do not hide the badge on degraded.
 */
@Serializable
data class BadgeSummaryJson(
    @SerialName("unread_notifications") val unreadNotifications: Int = 0,
    @SerialName("action_required") val actionRequired: ActionRequiredJson = ActionRequiredJson(),
    @SerialName("pending_reviews_count") val pendingReviewsCount: Int = 0,
    val verification: VerificationJson = VerificationJson(),
    val total: Int = 0,
    @SerialName("computed_at") val computedAt: String? = null,
    val degraded: Boolean = false,
)

@Serializable
data class ActionRequiredJson(
    val total: Int = 0,
    @SerialName("active_deliveries_needing_update") val activeDeliveriesNeedingUpdate: Int = 0,
    @SerialName("pending_booking_requests") val pendingBookingRequests: Int = 0,
    @SerialName("packages_ready_for_pickup") val packagesReadyForPickup: Int = 0,
    @SerialName("trips_without_activity") val tripsWithoutActivity: Int = 0,
)

@Serializable
data class VerificationJson(
    @SerialName("phone_verification_needed") val phoneVerificationNeeded: Boolean = false,
    @SerialName("payout_setup_needed") val payoutSetupNeeded: Boolean = false,
)
