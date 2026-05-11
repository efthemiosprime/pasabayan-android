package com.efthemiosprime.pasabayan.features.bookings.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.InitiatedBy
import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import com.efthemiosprime.pasabayan.core.domain.model.UserSummary
import com.efthemiosprime.pasabayan.core.network.bookings.AutoChargeInfoJson
import com.efthemiosprime.pasabayan.core.network.bookings.CancelMatchResponseJson
import com.efthemiosprime.pasabayan.core.network.bookings.CarrierRequestResponseJson
import com.efthemiosprime.pasabayan.core.network.bookings.DeliveryMatchJson
import com.efthemiosprime.pasabayan.core.network.bookings.MatchTransactionJson
import com.efthemiosprime.pasabayan.core.network.bookings.RefundResultJson
import com.efthemiosprime.pasabayan.core.network.bookings.ShipperRequestResponseJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BookingMapperTest {

    @Test
    fun `DeliveryMatchJson toDomain maps all core fields`() {
        val json = DeliveryMatchJson(
            id = 100,
            tripId = 1,
            packageRequestId = 10,
            matchStatus = MatchStatus.CONFIRMED,
            agreedPrice = 150.50,
            initiatedBy = InitiatedBy.CARRIER,
            isCounterOffer = false,
            carrierMessage = "I can carry",
            carrier = UserSummary(id = 42, name = "John"),
            shipper = UserSummary(id = 5, name = "Alice"),
            chatConversationId = 77,
            platformFeePercent = 10,
            autoCancelAfterDays = 10,
        )
        val match = json.toDomain()

        assertEquals(100, match.id)
        assertEquals(1, match.tripId)
        assertEquals(10, match.packageRequestId)
        assertEquals(MatchStatus.CONFIRMED, match.matchStatus)
        assertEquals(150.50, match.agreedPrice, 0.001)
        assertEquals(InitiatedBy.CARRIER, match.initiatedBy)
        assertEquals("I can carry", match.carrierMessage)
        assertNotNull(match.carrier)
        assertNotNull(match.shipper)
        assertEquals(77, match.chatConversationId)
    }

    @Test
    fun `DeliveryMatchJson toDomain handles null optionals`() {
        val json = DeliveryMatchJson(id = 50)
        val match = json.toDomain()

        assertEquals(50, match.id)
        assertNull(match.tripId)
        assertNull(match.carrier)
        assertNull(match.shipper)
        assertNull(match.carrierMessage)
        assertEquals(0.0, match.agreedPrice, 0.001)
    }

    @Test
    fun `DeliveryMatchJson toDomain wires transactionStatus from DTO`() {
        val json = DeliveryMatchJson(id = 100, transactionStatus = "captured")
        val match = json.toDomain()

        assertEquals("captured", match.transactionStatus)
    }

    @Test
    fun `DeliveryMatchJson toDomain maps nested transaction to domain MatchTransaction`() {
        val json = DeliveryMatchJson(
            id = 100,
            transaction = MatchTransactionJson(
                id = 555,
                status = "captured",
                totalAmount = "150.50",
                platformFee = "15.05",
                carrierAmount = "135.45",
                currency = "CAD",
                createdAt = "2026-03-29T10:00:00Z",
            ),
        )
        val match = json.toDomain()

        assertNotNull(match.transaction)
        assertEquals(555, match.transaction!!.id)
        assertEquals("captured", match.transaction!!.status)
        assertEquals("150.50", match.transaction!!.totalAmount)
        assertEquals("CAD", match.transaction!!.currency)
    }

    @Test
    fun `DeliveryMatchJson toDomain leaves transaction null when absent`() {
        val match = DeliveryMatchJson(id = 100).toDomain()
        assertNull(match.transaction)
    }

    @Test
    fun `isPaymentCompleted prefers nested transaction status over transactionStatus`() {
        val json = DeliveryMatchJson(
            id = 100,
            transactionStatus = "pending",
            transaction = MatchTransactionJson(id = 1, status = "completed"),
        )
        val match = json.toDomain()

        assertTrue(match.isPaymentCompleted)
    }

    @Test
    fun `isPaymentCompleted false when both statuses are non-terminal`() {
        val json = DeliveryMatchJson(
            id = 100,
            transactionStatus = "pending",
            transaction = MatchTransactionJson(id = 1, status = "pending"),
        )
        val match = json.toDomain()

        assertFalse(match.isPaymentCompleted)
    }

    // -- Request envelope mapping (iOS parity 8c9646d) --

    @Test
    fun `CarrierRequestResponseJson toDomain captures match and negotiation`() {
        val envelope = CarrierRequestResponseJson(
            success = true,
            message = "OK",
            data = DeliveryMatchJson(id = 300, matchStatus = MatchStatus.CARRIER_REQUESTED),
            warnings = listOf("pickup_address_outside_range"),
            negotiationNeeded = true,
            isCounterOffer = false,
        )
        val result = envelope.toDomain()

        assertNotNull(result)
        assertEquals(300, result!!.match.id)
        assertEquals(listOf("pickup_address_outside_range"), result.negotiation.warnings)
        assertTrue(result.negotiation.negotiationNeeded)
        assertFalse(result.negotiation.isCounterOffer)
    }

    @Test
    fun `ShipperRequestResponseJson toDomain captures counter-offer envelope`() {
        val envelope = ShipperRequestResponseJson(
            success = true,
            message = "OK",
            data = DeliveryMatchJson(id = 301, matchStatus = MatchStatus.SHIPPER_REQUESTED),
            warnings = null,
            negotiationNeeded = false,
            isCounterOffer = true,
        )
        val result = envelope.toDomain()

        assertNotNull(result)
        assertTrue(result!!.negotiation.isCounterOffer)
        assertEquals(emptyList<String>(), result.negotiation.warnings)
        assertFalse(result.negotiation.hasWarnings)
    }

    @Test
    fun `CarrierRequestResponseJson toDomain returns null when data is absent`() {
        val envelope = CarrierRequestResponseJson(success = false, message = "Bad request", data = null)
        assertNull(envelope.toDomain())
    }

    @Test
    fun `CarrierRequestResponseJson toDomain defaults missing envelope fields`() {
        val envelope = CarrierRequestResponseJson(
            success = true,
            data = DeliveryMatchJson(id = 1, matchStatus = MatchStatus.PENDING),
        )
        val result = envelope.toDomain()!!

        assertEquals(emptyList<String>(), result.negotiation.warnings)
        assertFalse(result.negotiation.negotiationNeeded)
        assertFalse(result.negotiation.isCounterOffer)
    }

    // -- CancelMatchResponseJson mapping (iOS parity b3d7675) --

    @Test
    fun `CancelMatchResponseJson toDomain maps match, conversation, and refund`() {
        val response = CancelMatchResponseJson(
            message = "Cancelled and refunded",
            data = DeliveryMatchJson(id = 100, matchStatus = MatchStatus.CANCELLED),
            chatConversationId = 77,
            refund = RefundResultJson(processed = true, amount = 150.50, transactionId = 555),
        )
        val result = response.toDomain()

        assertNotNull(result)
        assertEquals(100, result!!.match.id)
        assertEquals(77, result.chatConversationId)
        assertNotNull(result.refund)
        assertTrue(result.refund!!.processed)
        assertEquals(150.50, result.refund!!.amount!!, 0.001)
        assertEquals(555, result.refund!!.transactionId)
    }

    @Test
    fun `CancelMatchResponseJson toDomain returns null when data is absent`() {
        val response = CancelMatchResponseJson(message = "Cancelled", data = null)
        assertNull(response.toDomain())
    }

    @Test
    fun `CancelMatchResponseJson toDomain handles missing refund and conversation`() {
        val response = CancelMatchResponseJson(
            message = "Cancelled",
            data = DeliveryMatchJson(id = 100, matchStatus = MatchStatus.CANCELLED),
        )
        val result = response.toDomain()

        assertNotNull(result)
        assertNull(result!!.chatConversationId)
        assertNull(result.refund)
    }

    @Test
    fun `RefundResultJson toDomain surfaces processing failure`() {
        val json = RefundResultJson(processed = false, error = "stripe_error")
        val refund = json.toDomain()

        assertEquals(false, refund.processed)
        assertEquals("stripe_error", refund.error)
        assertNull(refund.amount)
    }

    @Test
    fun `DeliveryMatchJson toDomain maps counter-offer fields`() {
        val json = DeliveryMatchJson(
            id = 100,
            isCounterOffer = true,
            originalPrice = "120.00",
            counterOfferRound = 1,
            remainingCounterOffers = 2,
            canCounterOffer = true,
        )
        val match = json.toDomain()

        assertTrue(match.isCounterOffer)
        assertEquals("120.00", match.originalPrice)
        assertEquals(1, match.counterOfferRound)
        assertEquals(2, match.remainingCounterOffers)
        assertTrue(match.canCounterOffer)
    }
}
