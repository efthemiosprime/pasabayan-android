package com.efthemiosprime.pasabayan.features.notifications.services

import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageRequestStatus
import com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus
import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole
import com.efthemiosprime.pasabayan.core.domain.`enum`.VerificationLevel
import com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch
import com.efthemiosprime.pasabayan.features.notifications.model.ActionableItemType
import com.efthemiosprime.pasabayan.features.packages.model.PackageRequest
import com.efthemiosprime.pasabayan.features.trips.model.Trip

/**
 * Stable IDs for each spec. The notifications screen uses these to key list items and to
 * de-duplicate across re-aggregations.
 */
object ActionableItemIds {
    const val BOOKING_REQUESTS = "pending_requests_consolidated"
    const val STATUS_UPDATES = "active_deliveries_consolidated"
    const val INACTIVE_TRIPS = "inactive_trips_consolidated"
    const val CARRIER_RESPONSES = "pending_responses_consolidated"
    const val PICKUP_READY = "pickup_ready_consolidated"
    const val UNREAD_MESSAGES = "unread_messages"
    const val VERIFY_NUMBER = "verify_number"
    const val UPGRADE_PREMIUM = "upgrade_premium"
}

/**
 * Locally-aggregated spec for one row in the **Action Required** section of the notifications
 * screen. The composable host resolves [type] + [count] into localized strings via Compose
 * `pluralStringResource`.
 *
 * iOS parity: `ComprehensiveNotificationsView.actionableItems` (lines 338–536).
 */
data class ActionableItemSpec(
    val id: String,
    val type: ActionableItemType,
    /** Item count for plural resolution. `0` means the item has no count (e.g. UPGRADE). */
    val count: Int,
)

/**
 * Pure aggregator: turns cross-feature state into a list of action specs. The composable host
 * supplies role + the four data sources; this function applies the iOS filtering rules and
 * returns the specs in iOS display order.
 *
 * - Carrier-only:
 *   - `BOOKING_REQUEST` — `matches` with status [MatchStatus.SHIPPER_REQUESTED].
 *   - `STATUS_UPDATE` — `matches` with status [MatchStatus.CONFIRMED] or [MatchStatus.PICKED_UP].
 *   - `INACTIVE_TRIPS` — `carrierTrips` with status [TripStatus.PLANNING].
 * - Shipper-only:
 *   - `CARRIER_RESPONSE` — `matches` with status [MatchStatus.SHIPPER_REQUESTED].
 *   - `PICKUP_READY` — `shipperPackages` with status [PackageRequestStatus.MATCHED].
 * - Both:
 *   - `UNREAD_MESSAGES` — when [unreadMessageCount] > 0.
 *   - `UPGRADE` — `VERIFY_NUMBER` if BASIC, `UPGRADE_PREMIUM` if VERIFIED, none if PREMIUM.
 */
fun buildActionableItemSpecs(
    role: UserRole,
    carrierTrips: List<Trip>,
    matches: List<DeliveryMatch>,
    shipperPackages: List<PackageRequest>,
    unreadMessageCount: Int,
    verificationLevel: VerificationLevel,
): List<ActionableItemSpec> {
    val items = mutableListOf<ActionableItemSpec>()

    if (role == UserRole.CARRIER) {
        val bookingRequests = matches.count { it.matchStatus == MatchStatus.SHIPPER_REQUESTED }
        if (bookingRequests > 0) {
            items += ActionableItemSpec(
                id = ActionableItemIds.BOOKING_REQUESTS,
                type = ActionableItemType.BOOKING_REQUEST,
                count = bookingRequests,
            )
        }

        val activeDeliveries = matches.count {
            it.matchStatus == MatchStatus.CONFIRMED || it.matchStatus == MatchStatus.PICKED_UP
        }
        if (activeDeliveries > 0) {
            items += ActionableItemSpec(
                id = ActionableItemIds.STATUS_UPDATES,
                type = ActionableItemType.STATUS_UPDATE,
                count = activeDeliveries,
            )
        }

        val inactiveTrips = carrierTrips.count { it.tripStatus == TripStatus.PLANNING }
        if (inactiveTrips > 0) {
            items += ActionableItemSpec(
                id = ActionableItemIds.INACTIVE_TRIPS,
                type = ActionableItemType.INACTIVE_TRIPS,
                count = inactiveTrips,
            )
        }
    }

    if (role == UserRole.SHIPPER) {
        val pendingResponses = matches.count { it.matchStatus == MatchStatus.SHIPPER_REQUESTED }
        if (pendingResponses > 0) {
            items += ActionableItemSpec(
                id = ActionableItemIds.CARRIER_RESPONSES,
                type = ActionableItemType.CARRIER_RESPONSE,
                count = pendingResponses,
            )
        }

        val pickupReady = shipperPackages.count { it.status == PackageRequestStatus.MATCHED }
        if (pickupReady > 0) {
            items += ActionableItemSpec(
                id = ActionableItemIds.PICKUP_READY,
                type = ActionableItemType.PICKUP_READY,
                count = pickupReady,
            )
        }
    }

    if (unreadMessageCount > 0) {
        items += ActionableItemSpec(
            id = ActionableItemIds.UNREAD_MESSAGES,
            type = ActionableItemType.UNREAD_MESSAGES,
            count = unreadMessageCount,
        )
    }

    when (verificationLevel) {
        VerificationLevel.BASIC -> items += ActionableItemSpec(
            id = ActionableItemIds.VERIFY_NUMBER,
            type = ActionableItemType.UPGRADE,
            count = 0,
        )
        VerificationLevel.VERIFIED -> items += ActionableItemSpec(
            id = ActionableItemIds.UPGRADE_PREMIUM,
            type = ActionableItemType.UPGRADE,
            count = 0,
        )
        VerificationLevel.PREMIUM -> Unit
    }

    return items
}
