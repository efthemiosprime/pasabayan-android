package com.efthemiosprime.pasabayan.features.trips.viewmodel

import com.efthemiosprime.pasabayan.features.trips.model.CreateTripFromPackageRequest
import com.efthemiosprime.pasabayan.features.trips.model.TripTemplateData
import com.efthemiosprime.pasabayan.features.trips.model.Trip
import com.efthemiosprime.pasabayan.features.trips.services.CreateTripFromPackageUseCase
import com.efthemiosprime.pasabayan.features.trips.services.TripsLocalStateUpdater
import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod
import com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateTripFromPackageViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeTripsRepository
    private lateinit var fakeLocalStateUpdater: TripsLocalStateUpdater
    private lateinit var useCase: CreateTripFromPackageUseCase
    private lateinit var viewModel: CreateTripFromPackageViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        fakeRepo = FakeTripsRepository()
        fakeLocalStateUpdater = object : TripsLocalStateUpdater {
            override fun onTripCreationSucceeded(
                userId: Long,
                request: CreateTripFromPackageRequest,
            ) = Unit

            override fun onCarrierDisclaimerAcknowledgeAttempt(userId: Long, syncSucceeded: Boolean) = Unit

            override suspend fun retryPendingCarrierDisclaimerSync(
                userId: Long,
                syncAction: suspend () -> Result<Unit>,
            ): Boolean = false
        }
        useCase = CreateTripFromPackageUseCase(fakeRepo, fakeLocalStateUpdater)
        viewModel = CreateTripFromPackageViewModel(useCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadTemplate stores returned template`() = runTest {
        fakeRepo.tripTemplateResult = Result.success(
            TripTemplateData(
                packageId = 4,
                originCity = "Toronto",
                originCountry = "Canada",
                destinationCity = "Montreal",
                destinationCountry = "Canada",
                suggestedDepartureDate = null,
                suggestedArrivalDate = null,
                suggestedWeightKg = 12.0,
                suggestedSpaceLiters = 50.0,
                packageDescription = "Books",
                packageWeightKg = 2.0,
                packageUrgencyLevel = "normal",
            ),
        )
        viewModel.loadTemplate(4)
        advanceUntilIdle()
        assertEquals(4, viewModel.uiState.value.template?.packageId)
    }

    @Test
    fun `createTrip publishes created trip`() = runTest {
        fakeRepo.createResult = Result.success(
            testTrip(id = 99),
        )
        viewModel.createTrip(
            CreateTripFromPackageRequest(
                packageId = 4,
                originCity = "Toronto",
                originCountry = "Canada",
                destinationCity = "Montreal",
                destinationCountry = "Canada",
                departureDate = "2026-06-01T08:00:00Z",
                arrivalDate = "2026-06-01T12:00:00Z",
                availableWeightKg = 10.0,
                availableSpaceLiters = 30.0,
                transportationMethod = "car",
                pricePerKg = null,
                flatTripPrice = 25.0,
                specialNotes = null,
                pickupAddress = null,
                dropoffAddress = null,
                proposedPrice = 80.0,
                requestMessage = "Can carry",
            ),
        )
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.createdTrip)
        assertEquals(99, viewModel.uiState.value.createdTrip?.id)
    }

    private fun testTrip(id: Int): Trip = Trip(
        id = id,
        carrierId = 1,
        originCity = "Toronto",
        originCountry = "Canada",
        originLat = null,
        originLng = null,
        destinationCity = "Montreal",
        destinationCountry = "Canada",
        destinationLat = null,
        destinationLng = null,
        departureDate = "2026-06-01T08:00:00Z",
        arrivalDate = "2026-06-01T12:00:00Z",
        availableWeightKg = 10.0,
        availableSpaceLiters = 30.0,
        pricePerKg = null,
        tripStatus = TripStatus.PLANNING,
        transportationMethod = TransportationMethod.CAR,
        specialNotes = null,
        carrier = null,
        createdAt = null,
        updatedAt = null,
        pricingType = "flat",
        pricingMethod = null,
        flatTripPrice = 25.0,
        basePrice = null,
        calculatedPrice = null,
        pickupAddress = null,
        pickupLandmark = null,
        dropoffAddress = null,
        dropoffLandmark = null,
        tripEarningsTotal = null,
        tripEarningsCurrency = null,
        tripEarningsBreakdown = null,
        hasPendingRequests = null,
        pendingRequestCount = null,
        pendingRequests = null,
        distanceKm = null,
    )
}
