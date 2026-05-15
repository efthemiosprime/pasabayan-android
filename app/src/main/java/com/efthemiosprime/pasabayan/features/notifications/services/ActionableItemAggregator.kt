package com.efthemiosprime.pasabayan.features.notifications.services

import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole
import com.efthemiosprime.pasabayan.features.notifications.model.ActionableItemType
import com.efthemiosprime.pasabayan.features.profile.model.BadgeSummary

/**
 * Stable IDs for each spec. The notifications screen uses these for list keying and to dispatch
 * on-tap routing.
 *
 * VERIFY_PHONE / SETUP_PAYOUT match iOS verbatim
 * (`ComprehensiveNotificationsView.verificationItems` IDs) so cross-platform telemetry lines up.
 */
object ActionableItemIds {
    const val BOOKING_REQUESTS = "pending_requests_consolidated"
    const val STATUS_UPDATES = "active_deliveries_consolidated"
    const val INACTIVE_TRIPS = "inactive_trips_consolidated"
    const val CARRIER_RESPONSES = "pending_responses_consolidated"
    const val PICKUP_READY = "pickup_ready_consolidated"
    const val VERIFY_PHONE = "verification_phone_needed"
    const val SETUP_PAYOUT = "verification_payout_needed"
}

/**
 * Locally-rendered row in the **Action Required** or **Account Setup** section. The composable
 * host resolves [type] + [count] into localized copy via Compose `pluralStringResource` /
 * `stringResource`. iOS parity:
 * `ComprehensiveNotificationsView.actionableItems` + `.verificationItems`.
 */
data class ActionableItemSpec(
    val id: String,
    val type: ActionableItemType,
    /** Item count for plural resolution. `0` means the item has no count (e.g. setup cards). */
    val count: Int,
)

/**
 * Pure mapper from server `BadgeSummary.actionRequired` to the Action Required spec list. Card
 * visibility is **strictly** server-driven — local match/package/trip state is never consulted
 * here (it only steers on-tap navigation, which happens in the host).
 *
 * Per-role split mirrors iOS:
 * - Carrier sees `BOOKING_REQUEST`, `STATUS_UPDATE`, `INACTIVE_TRIPS`.
 * - Shipper sees `CARRIER_RESPONSE`, `PICKUP_READY`.
 */
fun buildActionableItemSpecs(
    summary: BadgeSummary?,
    role: UserRole,
): List<ActionableItemSpec> {
    val counts = summary?.actionRequired ?: return emptyList()
    val items = mutableListOf<ActionableItemSpec>()

    when (role) {
        UserRole.CARRIER -> {
            if (counts.pendingBookingRequests > 0) {
                items += ActionableItemSpec(
                    id = ActionableItemIds.BOOKING_REQUESTS,
                    type = ActionableItemType.BOOKING_REQUEST,
                    count = counts.pendingBookingRequests,
                )
            }
            if (counts.activeDeliveriesNeedingUpdate > 0) {
                items += ActionableItemSpec(
                    id = ActionableItemIds.STATUS_UPDATES,
                    type = ActionableItemType.STATUS_UPDATE,
                    count = counts.activeDeliveriesNeedingUpdate,
                )
            }
            if (counts.tripsWithoutActivity > 0) {
                items += ActionableItemSpec(
                    id = ActionableItemIds.INACTIVE_TRIPS,
                    type = ActionableItemType.INACTIVE_TRIPS,
                    count = counts.tripsWithoutActivity,
                )
            }
        }
        UserRole.SHIPPER -> {
            if (counts.pendingBookingRequests > 0) {
                items += ActionableItemSpec(
                    id = ActionableItemIds.CARRIER_RESPONSES,
                    type = ActionableItemType.CARRIER_RESPONSE,
                    count = counts.pendingBookingRequests,
                )
            }
            if (counts.packagesReadyForPickup > 0) {
                items += ActionableItemSpec(
                    id = ActionableItemIds.PICKUP_READY,
                    type = ActionableItemType.PICKUP_READY,
                    count = counts.packagesReadyForPickup,
                )
            }
        }
    }

    return items
}

/**
 * Pure mapper from server `BadgeSummary.verification` to the Account Setup spec list. Returns
 * an empty list when both flags are false; the host hides the section in that case.
 *
 * iOS parity: `ComprehensiveNotificationsView.verificationItems` — verify-phone first, payout
 * second.
 */
fun buildAccountSetupSpecs(summary: BadgeSummary?): List<ActionableItemSpec> {
    val verification = summary?.verification ?: return emptyList()
    val items = mutableListOf<ActionableItemSpec>()
    if (verification.phoneVerificationNeeded) {
        items += ActionableItemSpec(
            id = ActionableItemIds.VERIFY_PHONE,
            type = ActionableItemType.VERIFY_PHONE,
            count = 0,
        )
    }
    if (verification.payoutSetupNeeded) {
        items += ActionableItemSpec(
            id = ActionableItemIds.SETUP_PAYOUT,
            type = ActionableItemType.SETUP_PAYOUT,
            count = 0,
        )
    }
    return items
}
