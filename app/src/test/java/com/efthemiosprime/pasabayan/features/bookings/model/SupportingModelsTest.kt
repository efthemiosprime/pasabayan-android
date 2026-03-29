package com.efthemiosprime.pasabayan.features.bookings.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.InitiatedBy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SupportingModelsTest {

    // -- CounterOfferContext --

    @Test
    fun `CounterOfferContext computes price increase`() {
        val ctx = CounterOfferContext(
            newPrice = 160.0,
            originalPrice = 150.0,
            counterOffererName = "Alice",
            counterOffererId = 5,
            initiatedBy = InitiatedBy.SHIPPER,
            isCounterOffer = true,
        )
        assertEquals(10.0, ctx.priceDifference, 0.001)
        assertEquals("up", ctx.direction)
        assertTrue(ctx.isPriceIncrease)
    }

    @Test
    fun `CounterOfferContext computes price decrease`() {
        val ctx = CounterOfferContext(
            newPrice = 120.0,
            originalPrice = 150.0,
            counterOffererName = "Bob",
            counterOffererId = 42,
            initiatedBy = InitiatedBy.CARRIER,
            isCounterOffer = true,
        )
        assertEquals(-30.0, ctx.priceDifference, 0.001)
        assertEquals("down", ctx.direction)
        assertFalse(ctx.isPriceIncrease)
    }

    @Test
    fun `CounterOfferContext same price`() {
        val ctx = CounterOfferContext(
            newPrice = 150.0,
            originalPrice = 150.0,
            counterOffererName = "Alice",
            counterOffererId = 5,
            initiatedBy = InitiatedBy.SHIPPER,
            isCounterOffer = true,
        )
        assertEquals(0.0, ctx.priceDifference, 0.001)
        assertFalse(ctx.isPriceIncrease)
    }

    // -- TripStatusError --

    @Test
    fun `TripStatusError from message parses completed`() {
        val error = TripStatusError.from("This trip has been completed")
        assertEquals(TripStatusError.COMPLETED, error)
    }

    @Test
    fun `TripStatusError from message parses cancelled`() {
        val error = TripStatusError.from("Trip has been cancelled by carrier")
        assertEquals(TripStatusError.CANCELLED, error)
    }

    @Test
    fun `TripStatusError from message parses planning`() {
        val error = TripStatusError.from("Trip is still in planning phase")
        assertEquals(TripStatusError.PLANNING, error)
    }

    @Test
    fun `TripStatusError from message parses in_transit`() {
        val error = TripStatusError.from("Trip is currently in transit")
        assertEquals(TripStatusError.IN_TRANSIT, error)
    }

    @Test
    fun `TripStatusError from unrecognized message returns null`() {
        assertNull(TripStatusError.from("Some random error"))
    }

    @Test
    fun `TripStatusError shouldRemoveFromList correct`() {
        assertTrue(TripStatusError.COMPLETED.shouldRemoveFromList)
        assertTrue(TripStatusError.CANCELLED.shouldRemoveFromList)
        assertTrue(TripStatusError.DEPARTED.shouldRemoveFromList)
        assertTrue(TripStatusError.UNAVAILABLE.shouldRemoveFromList)
        assertFalse(TripStatusError.PLANNING.shouldRemoveFromList)
        assertFalse(TripStatusError.IN_TRANSIT.shouldRemoveFromList)
    }

    // -- PickupCodeData --

    @Test
    fun `PickupCodeData holds code fields`() {
        val data = PickupCodeData(
            confirmationCode = "123456",
            expiresAt = "2026-04-01T18:00:00Z",
            matchId = 100,
            shipperId = 5,
            carrierId = 42,
        )
        assertEquals("123456", data.confirmationCode)
        assertEquals(100, data.matchId)
    }

    // -- BookingStats --

    @Test
    fun `BookingStats holds stats fields`() {
        val stats = BookingStats(
            totalBookings = 50,
            pendingBookings = 5,
            confirmedBookings = 10,
            activeBookings = 3,
            completedBookings = 30,
            cancelledBookings = 2,
            totalEarnings = 5000.0,
            averageRating = 4.7,
        )
        assertEquals(50, stats.totalBookings)
        assertEquals(5000.0, stats.totalEarnings, 0.001)
        assertEquals(4.7, stats.averageRating!!, 0.001)
    }
}
