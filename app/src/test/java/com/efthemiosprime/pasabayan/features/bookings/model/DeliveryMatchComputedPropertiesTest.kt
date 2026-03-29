package com.efthemiosprime.pasabayan.features.bookings.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.InitiatedBy
import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeliveryMatchComputedPropertiesTest {

    private fun baseMatch(
        status: MatchStatus = MatchStatus.CONFIRMED,
        agreedPrice: Double = 150.0,
        isCounterOffer: Boolean = false,
        canCounterOffer: Boolean = false,
        remainingCounterOffers: Int = 3,
        initiatedBy: InitiatedBy = InitiatedBy.CARRIER,
        platformFeePercent: Int? = 10,
        originalPrice: String? = null,
        transactionStatus: String? = null,
        receiptPhoto: String? = null,
    ) = DeliveryMatch(
        id = 100, tripId = 1, packageRequestId = 10,
        matchStatus = status, agreedPrice = agreedPrice,
        initiatedBy = initiatedBy,
        isCounterOffer = isCounterOffer,
        originalPrice = originalPrice,
        canCounterOffer = canCounterOffer,
        remainingCounterOffers = remainingCounterOffers,
        counterOfferRound = 0,
        carrierMessage = null, shipperMessage = null,
        carrier = null, shipper = null,
        chatConversationId = null,
        confirmedAt = null, pickedUpAt = null, deliveredAt = null,
        createdAt = null, updatedAt = null,
        platformFeePercent = platformFeePercent,
        transactionStatus = transactionStatus,
        receiptPhoto = receiptPhoto,
        autoCancelAfterDays = 10,
        pickupConfirmationCode = null, codeExpiresAt = null,
        deliveryVerificationCode = null, deliveryCodeExpiresAt = null,
    )

    // -- Available actions --

    @Test
    fun `carrier actions for shipper_requested include accept and decline`() {
        val match = baseMatch(status = MatchStatus.SHIPPER_REQUESTED)
        val actions = match.availableActions(isCarrier = true, currentUserId = 42)
        assertTrue(actions.contains(BookingAction.AcceptBooking))
        assertTrue(actions.contains(BookingAction.DeclineBooking))
    }

    @Test
    fun `shipper actions for carrier_requested include accept, decline, and counter-offer`() {
        val match = baseMatch(
            status = MatchStatus.CARRIER_REQUESTED,
            canCounterOffer = true,
        )
        val actions = match.availableActions(isCarrier = false, currentUserId = 5)
        assertTrue(actions.contains(BookingAction.AcceptBooking))
        assertTrue(actions.contains(BookingAction.DeclineBooking))
        assertTrue(actions.contains(BookingAction.CounterOffer))
    }

    @Test
    fun `shipper counter-offer not available when canCounterOffer is false`() {
        val match = baseMatch(
            status = MatchStatus.CARRIER_REQUESTED,
            canCounterOffer = false,
        )
        val actions = match.availableActions(isCarrier = false, currentUserId = 5)
        assertFalse(actions.contains(BookingAction.CounterOffer))
    }

    @Test
    fun `carrier confirmed status shows markPickedUp`() {
        val match = baseMatch(status = MatchStatus.CONFIRMED)
        val actions = match.availableActions(isCarrier = true, currentUserId = 42)
        assertTrue(actions.contains(BookingAction.MarkPickedUp))
    }

    @Test
    fun `carrier pickedUp status shows markInTransit`() {
        val match = baseMatch(status = MatchStatus.PICKED_UP)
        val actions = match.availableActions(isCarrier = true, currentUserId = 42)
        assertTrue(actions.contains(BookingAction.MarkInTransit))
    }

    @Test
    fun `carrier inTransit status shows markDelivered`() {
        val match = baseMatch(status = MatchStatus.IN_TRANSIT)
        val actions = match.availableActions(isCarrier = true, currentUserId = 42)
        assertTrue(actions.contains(BookingAction.MarkDelivered))
    }

    @Test
    fun `shipper inTransit shows trackLive`() {
        val match = baseMatch(status = MatchStatus.IN_TRANSIT)
        val actions = match.availableActions(isCarrier = false, currentUserId = 5)
        assertTrue(actions.contains(BookingAction.TrackLive))
    }

    @Test
    fun `cancel available for cancellable statuses`() {
        val match = baseMatch(status = MatchStatus.PENDING)
        val actions = match.availableActions(isCarrier = true, currentUserId = 42)
        assertTrue(actions.contains(BookingAction.CancelBooking))
    }

    @Test
    fun `cancel not available for delivered`() {
        val match = baseMatch(status = MatchStatus.DELIVERED)
        val actions = match.availableActions(isCarrier = true, currentUserId = 42)
        assertFalse(actions.contains(BookingAction.CancelBooking))
    }

    @Test
    fun `shipper confirmed shows enterPickupCode`() {
        val match = baseMatch(status = MatchStatus.CONFIRMED)
        val actions = match.availableActions(isCarrier = false, currentUserId = 5)
        assertTrue(actions.contains(BookingAction.EnterPickupCode))
    }

    // -- Computed properties --

    @Test
    fun `originalPriceValue parses string`() {
        val match = baseMatch(originalPrice = "120.50")
        assertEquals(120.50, match.originalPriceValue!!, 0.001)
    }

    @Test
    fun `originalPriceValue null when no original price`() {
        val match = baseMatch(originalPrice = null)
        assertEquals(null, match.originalPriceValue)
    }

    @Test
    fun `platformFeePercentValue normalizes percentage`() {
        assertEquals(0.10, baseMatch(platformFeePercent = 10).platformFeePercentValue, 0.001)
    }

    @Test
    fun `isPaymentCompleted true for completed transaction`() {
        assertTrue(baseMatch(transactionStatus = "completed").isPaymentCompleted)
    }

    @Test
    fun `isPaymentCompleted false for pending transaction`() {
        assertFalse(baseMatch(transactionStatus = "pending").isPaymentCompleted)
    }

    @Test
    fun `hasReceipt true when receiptPhoto exists`() {
        assertTrue(baseMatch(receiptPhoto = "https://example.com/receipt.jpg").hasReceipt)
    }

    @Test
    fun `hasReceipt false when no receiptPhoto`() {
        assertFalse(baseMatch(receiptPhoto = null).hasReceipt)
    }

    @Test
    fun `autoCancelWindowDays defaults to 10`() {
        assertEquals(10, baseMatch().autoCancelWindowDays)
    }

    @Test
    fun `isCounterOfferAllowedByLimit checks both fields`() {
        assertTrue(baseMatch(canCounterOffer = true, remainingCounterOffers = 2).isCounterOfferAllowedByLimit)
        assertFalse(baseMatch(canCounterOffer = false, remainingCounterOffers = 2).isCounterOfferAllowedByLimit)
        assertFalse(baseMatch(canCounterOffer = true, remainingCounterOffers = 0).isCounterOfferAllowedByLimit)
    }
}
