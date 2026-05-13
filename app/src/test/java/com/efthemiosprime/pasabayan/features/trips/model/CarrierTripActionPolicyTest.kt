package com.efthemiosprime.pasabayan.features.trips.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class CarrierTripActionPolicyTest {

    @Test
    fun `edit trip only offered when planning`() {
        val expected = mapOf(
            TripStatus.PLANNING to true,
            TripStatus.ACTIVE to false,
            TripStatus.IN_TRANSIT to false,
            TripStatus.COMPLETED to false,
            TripStatus.CANCELLED to false,
        )
        expected.forEach { (status, want) ->
            assertEquals("edit for $status", want, CarrierTripActionPolicy.shouldOfferEditTrip(status))
        }
    }

    @Test
    fun `activate trip only offered when planning`() {
        val expected = mapOf(
            TripStatus.PLANNING to true,
            TripStatus.ACTIVE to false,
            TripStatus.IN_TRANSIT to false,
            TripStatus.COMPLETED to false,
            TripStatus.CANCELLED to false,
        )
        expected.forEach { (status, want) ->
            assertEquals("activate for $status", want, CarrierTripActionPolicy.shouldOfferActivateTrip(status))
        }
    }

    @Test
    fun `update status offered for active and in_transit only`() {
        val expected = mapOf(
            TripStatus.PLANNING to false,
            TripStatus.ACTIVE to true,
            TripStatus.IN_TRANSIT to true,
            TripStatus.COMPLETED to false,
            TripStatus.CANCELLED to false,
        )
        expected.forEach { (status, want) ->
            assertEquals("updateStatus for $status", want, CarrierTripActionPolicy.shouldOfferUpdateStatus(status))
        }
    }

    @Test
    fun `cancel offered for everything except completed and cancelled`() {
        val expected = mapOf(
            TripStatus.PLANNING to true,
            TripStatus.ACTIVE to true,
            TripStatus.IN_TRANSIT to true,
            TripStatus.COMPLETED to false,
            TripStatus.CANCELLED to false,
        )
        expected.forEach { (status, want) ->
            assertEquals("cancel for $status", want, CarrierTripActionPolicy.shouldOfferCancelTrip(status))
        }
    }
}
