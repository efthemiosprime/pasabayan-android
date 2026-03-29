package com.efthemiosprime.pasabayan.features.bookings.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.InitiatedBy
import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import com.efthemiosprime.pasabayan.core.domain.model.UserSummary
import com.efthemiosprime.pasabayan.core.network.bookings.AutoChargeInfoJson
import com.efthemiosprime.pasabayan.core.network.bookings.DeliveryMatchJson
import org.junit.Assert.assertEquals
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
