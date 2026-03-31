package com.efthemiosprime.pasabayan.features.bookings.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.InitiatedBy
import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import com.efthemiosprime.pasabayan.core.domain.model.UserSummary
import com.efthemiosprime.pasabayan.features.bookings.model.nested.CarrierTripInfo
import com.efthemiosprime.pasabayan.features.bookings.model.nested.PackageRequestInfo

/**
 * DeliveryMatch domain model — the central model for bookings/matches.
 * Parity with iOS `DeliveryMatch` from MatchingModels.swift.
 */
data class DeliveryMatch(
    val id: Int,
    val tripId: Int?,
    val packageRequestId: Int?,
    val matchStatus: MatchStatus,
    val agreedPrice: Double,
    val initiatedBy: InitiatedBy,
    // Counter-offer
    val isCounterOffer: Boolean,
    val originalPrice: String?,
    val canCounterOffer: Boolean,
    val remainingCounterOffers: Int,
    val counterOfferRound: Int?,
    // Messages
    val carrierMessage: String?,
    val shipperMessage: String?,
    // Related
    val carrier: UserSummary?,
    val shipper: UserSummary?,
    val chatConversationId: Int?,
    // Timestamps
    val confirmedAt: String?,
    val pickedUpAt: String?,
    val deliveredAt: String?,
    val createdAt: String?,
    val updatedAt: String?,
    // Pricing
    val platformFeePercent: Int?,
    val transactionStatus: String?,
    val receiptPhoto: String?,
    val autoCancelAfterDays: Int?,
    // Codes
    val pickupConfirmationCode: String?,
    val codeExpiresAt: String?,
    val deliveryVerificationCode: String?,
    val deliveryCodeExpiresAt: String?,
    // Expanded nested details (used by shipper match details sheet)
    val carrierTrip: CarrierTripInfo? = null,
    val packageRequest: PackageRequestInfo? = null,
) {
    val originalPriceValue: Double?
        get() = originalPrice?.toDoubleOrNull()

    val platformFeePercentValue: Double
        get() {
            val raw = platformFeePercent ?: return 0.0
            return if (raw > 1) raw / 100.0 else raw.toDouble()
        }

    val isPaymentCompleted: Boolean
        get() = transactionStatus in listOf("completed", "captured")

    val hasReceipt: Boolean
        get() = receiptPhoto != null

    val autoCancelWindowDays: Int
        get() = maxOf(autoCancelAfterDays ?: 10, 1)

    val isCounterOfferAllowedByLimit: Boolean
        get() = canCounterOffer && remainingCounterOffers > 0

    /**
     * Determines available actions based on role and current status.
     */
    fun availableActions(isCarrier: Boolean, currentUserId: Int): List<BookingAction> = buildList {
        when (matchStatus) {
            MatchStatus.CARRIER_REQUESTED -> {
                if (!isCarrier) {
                    add(BookingAction.AcceptBooking)
                    add(BookingAction.DeclineBooking)
                    if (canCounterOffer) add(BookingAction.CounterOffer)
                }
            }
            MatchStatus.SHIPPER_REQUESTED -> {
                if (isCarrier) {
                    add(BookingAction.AcceptBooking)
                    add(BookingAction.DeclineBooking)
                }
            }
            MatchStatus.CONFIRMED, MatchStatus.SHIPPER_ACCEPTED, MatchStatus.CARRIER_ACCEPTED -> {
                if (isCarrier) add(BookingAction.MarkPickedUp)
                if (!isCarrier) add(BookingAction.EnterPickupCode)
            }
            MatchStatus.PICKED_UP -> {
                if (isCarrier) add(BookingAction.MarkInTransit)
            }
            MatchStatus.IN_TRANSIT -> {
                if (isCarrier) {
                    add(BookingAction.EnterDeliveryCode)
                    add(BookingAction.MarkDelivered)
                }
                if (!isCarrier) add(BookingAction.TrackLive)
            }
            MatchStatus.DELIVERED, MatchStatus.CANCELLED,
            MatchStatus.SHIPPER_DECLINED, MatchStatus.CARRIER_DECLINED -> {
                // No actions
            }
            MatchStatus.PENDING -> {
                // Waiting
            }
        }

        // Cancel available for cancellable statuses
        if (matchStatus.isCancellable) {
            add(BookingAction.CancelBooking)
        }
    }
}
