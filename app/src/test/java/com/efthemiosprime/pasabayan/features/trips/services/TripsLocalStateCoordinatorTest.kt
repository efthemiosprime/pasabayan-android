package com.efthemiosprime.pasabayan.features.trips.services

import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod
import com.efthemiosprime.pasabayan.features.trips.model.CreateTripFromPackageRequest
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TripsLocalStateCoordinatorTest {

    private val usualTransportStore: UsualTransportStore = mockk(relaxed = true)
    private val savedRouteTemplatesStore: SavedRouteTemplatesStore = mockk(relaxed = true)
    private val carrierDisclaimerStore: CarrierDisclaimerStore = mockk(relaxed = true)

    private val coordinator = TripsLocalStateCoordinator(
        usualTransportStore = usualTransportStore,
        savedRouteTemplatesStore = savedRouteTemplatesStore,
        carrierDisclaimerStore = carrierDisclaimerStore,
    )

    @Test
    fun `onTripCreationSucceeded saves transport and route template`() {
        every { savedRouteTemplatesStore.save(any()) } just runs

        coordinator.onTripCreationSucceeded(
            userId = 55L,
            request = request(transportationMethod = "car"),
        )

        verify(exactly = 1) {
            usualTransportStore.set(55, TransportationMethod.CAR)
        }
        verify(exactly = 1) {
            savedRouteTemplatesStore.save(any())
        }
    }

    @Test
    fun `onCarrierDisclaimerAcknowledgeAttempt stores ack and marks pending on failed sync`() {
        coordinator.onCarrierDisclaimerAcknowledgeAttempt(
            userId = 88L,
            syncSucceeded = false,
        )

        verify(exactly = 1) { carrierDisclaimerStore.setAcknowledged(88) }
        verify(exactly = 1) { carrierDisclaimerStore.setPendingSync(88, true) }
    }

    @Test
    fun `retryPendingCarrierDisclaimerSync clears pending flag on success`() = runTest {
        every { carrierDisclaimerStore.isPendingSync(91) } returns true

        val retried = coordinator.retryPendingCarrierDisclaimerSync(91L) {
            Result.success(Unit)
        }

        assertTrue(retried)
        verify(exactly = 1) { carrierDisclaimerStore.setPendingSync(91, false) }
    }

    @Test
    fun `retryPendingCarrierDisclaimerSync is no-op when nothing pending`() = runTest {
        every { carrierDisclaimerStore.isPendingSync(24) } returns false

        val retried = coordinator.retryPendingCarrierDisclaimerSync(24L) {
            Result.success(Unit)
        }

        assertTrue(!retried)
        verify(exactly = 0) { carrierDisclaimerStore.setPendingSync(any(), any()) }
    }

    private fun request(transportationMethod: String): CreateTripFromPackageRequest =
        CreateTripFromPackageRequest(
            packageId = 1,
            originCity = "Toronto",
            originCountry = "CA",
            destinationCity = "Montreal",
            destinationCountry = "CA",
            departureDate = "2026-07-01T08:00:00Z",
            arrivalDate = "2026-07-01T12:00:00Z",
            availableWeightKg = 5.0,
            availableSpaceLiters = 30.0,
            transportationMethod = transportationMethod,
            pricePerKg = null,
            flatTripPrice = 20.0,
            specialNotes = null,
            pickupAddress = "A",
            dropoffAddress = "B",
            proposedPrice = null,
            requestMessage = null,
        )
}
