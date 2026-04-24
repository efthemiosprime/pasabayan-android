package com.efthemiosprime.pasabayan.features.trips.viewmodel

import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod
import com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus
import com.efthemiosprime.pasabayan.features.trips.model.Trip
import com.efthemiosprime.pasabayan.features.trips.services.SavedRouteTemplate
import com.efthemiosprime.pasabayan.features.trips.services.SavedRouteTemplatesStore
import com.efthemiosprime.pasabayan.features.trips.services.TripTutorialStore
import com.efthemiosprime.pasabayan.features.trips.services.UsualTransportStore
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import io.mockk.confirmVerified
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TripCreationViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeTripsRepository
    private lateinit var usualTransportStore: UsualTransportStore
    private lateinit var savedRouteTemplatesStore: SavedRouteTemplatesStore
    private lateinit var tutorialStore: TripTutorialStore
    private lateinit var viewModel: TripCreationViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = FakeTripsRepository()
        usualTransportStore = mockk(relaxed = true)
        savedRouteTemplatesStore = mockk(relaxed = true)
        tutorialStore = mockk(relaxed = true)
        viewModel = TripCreationViewModel(
            tripsRepository = repo,
            usualTransportStore = usualTransportStore,
            savedRouteTemplatesStore = savedRouteTemplatesStore,
            tripTutorialStore = tutorialStore,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initializeTutorial shows overlay when user has not seen tutorial`() {
        every { tutorialStore.hasSeenTutorial(11L) } returns false

        viewModel.initializeTutorial(11L)

        assertTrue(viewModel.uiState.value.shouldShowTutorial)
    }

    @Test
    fun `dismissTutorial marks tutorial as seen and hides overlay`() {
        every { tutorialStore.hasSeenTutorial(11L) } returns false
        viewModel.initializeTutorial(11L)

        viewModel.dismissTutorial(11L)

        verify(exactly = 1) { tutorialStore.markTutorialSeen(11L) }
        assertFalse(viewModel.uiState.value.shouldShowTutorial)
    }

    @Test
    fun `createTrip success sets save route prompt`() = runTest {
        repo.createResult = Result.success(testTrip(31))

        viewModel.createTrip(testCreateTripRequest())
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(31, state.createdTrip?.id)
        assertTrue(state.shouldShowSaveRoutePrompt)
        assertFalse(state.isSubmitting)
    }

    @Test
    fun `completeSuccessFlow saves transport and optional route then clears prompt`() {
        val route = SavedRouteTemplate(
            startCountryCode = "CA",
            startLocation = "Toronto",
            endCountryCode = "CA",
            endLocation = "Montreal",
        )

        viewModel.completeSuccessFlow(
            userId = 9L,
            transportationMethod = TransportationMethod.CAR,
            saveRouteTemplate = route,
            saveRoute = true,
        )

        verify(exactly = 1) { usualTransportStore.set(9, TransportationMethod.CAR) }
        verify(exactly = 1) { savedRouteTemplatesStore.save(route) }
        assertFalse(viewModel.uiState.value.shouldShowSaveRoutePrompt)
    }

    @Test
    fun `completeSuccessFlow skip route does not persist template`() {
        val route = SavedRouteTemplate(
            startCountryCode = "CA",
            startLocation = "Toronto",
            endCountryCode = "CA",
            endLocation = "Montreal",
        )

        viewModel.completeSuccessFlow(
            userId = 9L,
            transportationMethod = TransportationMethod.CAR,
            saveRouteTemplate = route,
            saveRoute = false,
        )

        verify(exactly = 1) { usualTransportStore.set(9, TransportationMethod.CAR) }
        verify(exactly = 0) { savedRouteTemplatesStore.save(any()) }
        assertFalse(viewModel.uiState.value.shouldShowSaveRoutePrompt)
    }

    @Test
    fun `completeSuccessFlow with no transport does not persist usual method`() {
        viewModel.completeSuccessFlow(
            userId = 9L,
            transportationMethod = TransportationMethod.NONE,
            saveRouteTemplate = null,
            saveRoute = false,
        )

        verify(exactly = 0) { usualTransportStore.set(any(), any()) }
        verify(exactly = 0) { savedRouteTemplatesStore.save(any()) }
        assertFalse(viewModel.uiState.value.shouldShowSaveRoutePrompt)
        confirmVerified(usualTransportStore, savedRouteTemplatesStore)
    }

    private fun testCreateTripRequest() =
        com.efthemiosprime.pasabayan.core.network.trips.CreateTripRequestJson(
            originCity = "Toronto",
            originCountry = "CA",
            destinationCity = "Montreal",
            destinationCountry = "CA",
            departureDate = "2026-06-01T08:00:00Z",
            arrivalDate = "2026-06-01T12:00:00Z",
            availableWeightKg = 10.0,
            transportationMethod = "car",
            flatTripPrice = 25.0,
        )

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
