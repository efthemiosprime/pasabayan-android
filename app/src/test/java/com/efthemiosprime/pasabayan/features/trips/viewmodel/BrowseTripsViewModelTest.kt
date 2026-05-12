package com.efthemiosprime.pasabayan.features.trips.viewmodel

import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod
import com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus
import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageRequestStatus
import com.efthemiosprime.pasabayan.features.trips.model.PopularRoute
import com.efthemiosprime.pasabayan.features.trips.model.Trip
import com.efthemiosprime.pasabayan.features.packages.model.PackageRequest
import com.efthemiosprime.pasabayan.features.verification.model.VerifyPhoneReason
import com.efthemiosprime.pasabayan.features.verification.services.RequirePhoneVerificationUseCase
import io.mockk.every
import io.mockk.mockk
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BrowseTripsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeTripsRepository
    private lateinit var requirePhoneVerification: RequirePhoneVerificationUseCase
    private lateinit var viewModel: BrowseTripsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeTripsRepository()
        requirePhoneVerification = mockk()
        every { requirePhoneVerification.invoke() } returns Result.success(Unit)
        viewModel = BrowseTripsViewModel(fakeRepo, requirePhoneVerification)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadAvailableTrips sets trips on success`() = runTest {
        fakeRepo.tripsPageResultQueue.addLast(
            Result.success(tripsPage(trips = listOf(testTrip(1), testTrip(2)), currentPage = 1, lastPage = 1)),
        )
        viewModel.loadAvailableTrips()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.availableTrips.size)
        assertTrue(state.hasLoadedTrips)
        assertNull(state.errorMessage)
        assertFalse(state.hasMore) // currentPage == lastPage
    }

    @Test
    fun `loadAvailableTrips sets error on failure`() = runTest {
        fakeRepo.tripsPageResultQueue.addLast(Result.failure(Exception("No internet")))
        viewModel.loadAvailableTrips()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.errorMessage != null)
    }

    @Test
    fun `loadAvailableTrips filters out non-bookable statuses`() = runTest {
        fakeRepo.tripsPageResultQueue.addLast(
            Result.success(
                tripsPage(
                    trips = listOf(
                        testTrip(1, TripStatus.ACTIVE),
                        testTrip(2, TripStatus.COMPLETED),
                        testTrip(3, TripStatus.PLANNING),
                        testTrip(4, TripStatus.CANCELLED),
                    ),
                    currentPage = 1,
                    lastPage = 1,
                ),
            ),
        )
        viewModel.loadAvailableTrips()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.availableTrips.size)
        assertTrue(state.availableTrips.all { it.tripStatus in listOf(TripStatus.PLANNING, TripStatus.ACTIVE) })
    }

    @Test
    fun `updateSearchText updates filter`() = runTest {
        viewModel.updateSearchText("Montreal")
        assertEquals("Montreal", viewModel.uiState.value.filter.searchText)
    }

    @Test
    fun `updateOrigin updates filter`() = runTest {
        viewModel.updateOrigin("Toronto")
        assertEquals("Toronto", viewModel.uiState.value.filter.origin)
    }

    @Test
    fun `updateDestination updates filter`() = runTest {
        viewModel.updateDestination("Montreal")
        assertEquals("Montreal", viewModel.uiState.value.filter.destination)
    }

    @Test
    fun `applyFilterAndFetch uses latest search route filter values`() = runTest {
        fakeRepo.tripsPageResultQueue.addLast(
            Result.success(tripsPage(trips = emptyList(), currentPage = 1, lastPage = 1)),
        )
        viewModel.updateSearchText("electronics")
        viewModel.updateOrigin("Toronto")
        viewModel.updateDestination("Montreal")

        viewModel.applyFilterAndFetch()
        advanceUntilIdle()

        val (filter, page, _) = fakeRepo.tripsPageCalls.first()
        assertEquals("electronics", filter.searchText)
        assertEquals("Toronto", filter.origin)
        assertEquals("Montreal", filter.destination)
        assertEquals(1, page)
    }

    @Test
    fun `clearFilters resets filter`() = runTest {
        viewModel.updateSearchText("test")
        viewModel.updateOrigin("A")
        viewModel.updateDestination("B")
        viewModel.clearFilters()
        val filter = viewModel.uiState.value.filter
        assertTrue(filter.searchText.isEmpty())
        assertTrue(filter.origin.isEmpty())
        assertTrue(filter.destination.isEmpty())
    }

    @Test
    fun `loadPopularRoutes stores popular routes in state`() = runTest {
        fakeRepo.popularRoutesResult = Result.success(
            listOf(
                PopularRoute(
                    originCity = "Toronto",
                    destinationCity = "Montreal",
                    packageCount = 5,
                    averagePrice = 34.0,
                ),
            ),
        )
        viewModel.loadPopularRoutes()
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.popularRoutes.size)
    }

    @Test
    fun `loadMoreTrips appends next page results`() = runTest {
        fakeRepo.tripsPageResultQueue.addLast(
            Result.success(tripsPage(trips = listOf(testTrip(1)), currentPage = 1, lastPage = 2)),
        )
        viewModel.loadAvailableTrips()
        advanceUntilIdle()

        fakeRepo.tripsPageResultQueue.addLast(
            Result.success(tripsPage(trips = listOf(testTrip(2)), currentPage = 2, lastPage = 2)),
        )
        viewModel.loadMoreTrips()
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.availableTrips.size)
        assertEquals(2, viewModel.uiState.value.currentPage)
        assertFalse(viewModel.uiState.value.hasMore)
        assertEquals(2, fakeRepo.tripsPageCalls.last().second)
    }

    // ---- iOS-parity pagination (Slice 3) ----

    @Test
    fun `loadMoreTrips is no-op when no more pages`() = runTest {
        fakeRepo.tripsPageResultQueue.addLast(
            Result.success(tripsPage(trips = listOf(testTrip(1)), currentPage = 1, lastPage = 1)),
        )
        viewModel.loadAvailableTrips()
        advanceUntilIdle()
        fakeRepo.tripsPageCalls.clear()

        viewModel.loadMoreTrips()
        advanceUntilIdle()

        assertTrue("loadMore must not call repo when hasMore is false", fakeRepo.tripsPageCalls.isEmpty())
    }

    @Test
    fun `loadMoreTrips is no-op before first page lands`() = runTest {
        viewModel.loadMoreTrips()
        advanceUntilIdle()

        assertTrue(fakeRepo.tripsPageCalls.isEmpty())
    }

    @Test
    fun `loadAvailableTrips reset discards stale in-flight response`() = runTest {
        // Two reload calls queued back-to-back. Page 1 returns "stale" id 99,
        // page 2 (same generation 2) returns "fresh" ids 1, 2.
        fakeRepo.tripsPageResultQueue.addLast(
            Result.success(tripsPage(trips = listOf(testTrip(99)), currentPage = 1, lastPage = 1)),
        )
        fakeRepo.tripsPageResultQueue.addLast(
            Result.success(tripsPage(trips = listOf(testTrip(1), testTrip(2)), currentPage = 1, lastPage = 1)),
        )

        viewModel.updateSearchText("stale")
        viewModel.loadAvailableTrips(reset = true)
        viewModel.updateSearchText("fresh")
        viewModel.loadAvailableTrips(reset = true)
        advanceUntilIdle()

        val ids = viewModel.uiState.value.availableTrips.map { it.id }
        assertEquals(listOf(1, 2), ids)
    }

    @Test
    fun `hasMore is envelope-driven not empty-list driven`() = runTest {
        // Page comes back with zero bookable trips (client-side filtered) but
        // server says lastPage = 3 — the old heuristic would have set hasMore = false.
        fakeRepo.tripsPageResultQueue.addLast(
            Result.success(
                tripsPage(
                    // All COMPLETED — filtered out client-side, leaving zero bookable.
                    trips = listOf(testTrip(1, TripStatus.COMPLETED), testTrip(2, TripStatus.COMPLETED)),
                    currentPage = 1,
                    lastPage = 3,
                ),
            ),
        )
        viewModel.loadAvailableTrips()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(0, state.availableTrips.size)
        assertTrue("Envelope says lastPage > currentPage, hasMore must be true", state.hasMore)
    }

    @Test
    fun `selectTrip and selectPackage compute compatibility result`() = runTest {
        val trip = testTrip(
            id = 11,
            status = TripStatus.ACTIVE,
        ).copy(
            originCity = "Toronto",
            destinationCity = "Montreal",
            availableWeightKg = 20.0,
            departureDate = "2026-04-01T08:00:00Z",
            arrivalDate = "2026-04-02T08:00:00Z",
        )
        val pkg = testPackage(
            id = 44,
            pickupCity = "Toronto",
            deliveryCity = "Montreal",
            packageWeightKg = 5.0,
            maxPriceBudget = 300.0,
            pickupDatePreferred = "2026-04-01T12:00:00Z",
        )

        viewModel.selectTrip(trip)
        viewModel.selectPackage(pkg)
        advanceUntilIdle()

        val compatibility = viewModel.uiState.value.compatibilityResult
        assertTrue(compatibility != null)
        assertTrue(compatibility!!.isCompatible)
        assertTrue(compatibility.capacitySufficient)
        assertTrue(compatibility.dateCompatible)
        assertTrue(compatibility.priceCompatible)
    }

    @Test
    fun `selectPackage marks incompatible when capacity is insufficient`() = runTest {
        val trip = testTrip(id = 3, status = TripStatus.ACTIVE).copy(availableWeightKg = 1.0)
        val pkg = testPackage(id = 9, packageWeightKg = 10.0)

        viewModel.selectTrip(trip)
        viewModel.selectPackage(pkg)
        advanceUntilIdle()

        val compatibility = viewModel.uiState.value.compatibilityResult
        assertTrue(compatibility != null)
        assertFalse(compatibility!!.isCompatible)
        assertFalse(compatibility.capacitySufficient)
    }

    @Test
    fun `showBookingSheet requires compatible selection`() = runTest {
        val trip = testTrip(id = 21, status = TripStatus.ACTIVE)
        val pkg = testPackage(id = 22, packageWeightKg = 1000.0)
        viewModel.selectTrip(trip.copy(availableWeightKg = 2.0))
        viewModel.selectPackage(pkg)
        advanceUntilIdle()

        viewModel.showBookingSheet()
        assertFalse(viewModel.uiState.value.isBookingSheetPresented)

        viewModel.selectPackage(testPackage(id = 23, packageWeightKg = 1.0))
        advanceUntilIdle()
        viewModel.showBookingSheet()
        assertTrue(viewModel.uiState.value.isBookingSheetPresented)
    }

    @Test
    fun `bookTripDirectly toggles booking state and clearBookingState resets`() = runTest {
        val trip = testTrip(id = 31, status = TripStatus.ACTIVE).copy(availableWeightKg = 5.0)
        val pkg = testPackage(id = 32, packageWeightKg = 1.0)
        viewModel.selectTrip(trip)
        viewModel.selectPackage(pkg)
        advanceUntilIdle()
        viewModel.showBookingSheet()

        viewModel.bookTripDirectly()
        assertTrue(viewModel.uiState.value.isBookingTrip)

        viewModel.clearBookingState()
        val state = viewModel.uiState.value
        assertFalse(state.isBookingTrip)
        assertFalse(state.isBookingSheetPresented)
        assertNull(state.selectedTrip)
        assertNull(state.selectedPackage)
        assertNull(state.compatibilityResult)
        assertNull(state.bookingSuccessMessage)
    }

    @Test
    fun `bookTrip blocked when phone not verified emits gate flag`() = runTest {
        val trip = testTrip(id = 41, status = TripStatus.ACTIVE).copy(availableWeightKg = 5.0)
        val pkg = testPackage(id = 42, packageWeightKg = 1.0)
        viewModel.selectTrip(trip)
        viewModel.selectPackage(pkg)
        advanceUntilIdle()
        viewModel.showBookingSheet()
        every { requirePhoneVerification.invoke() } returns
            Result.failure(RequirePhoneVerificationUseCase.PhoneVerificationRequired)

        viewModel.bookTrip()

        val state = viewModel.uiState.value
        assertEquals(VerifyPhoneReason.BookTrip, state.requiresPhoneVerification)
        assertFalse(state.isBookingTrip)
    }

    @Test
    fun `bookTripDirectly blocked when phone not verified emits gate flag`() = runTest {
        val trip = testTrip(id = 51, status = TripStatus.ACTIVE).copy(availableWeightKg = 5.0)
        val pkg = testPackage(id = 52, packageWeightKg = 1.0)
        viewModel.selectTrip(trip)
        viewModel.selectPackage(pkg)
        advanceUntilIdle()
        viewModel.showBookingSheet()
        every { requirePhoneVerification.invoke() } returns
            Result.failure(RequirePhoneVerificationUseCase.PhoneVerificationRequired)

        viewModel.bookTripDirectly()

        val state = viewModel.uiState.value
        assertEquals(VerifyPhoneReason.BookTrip, state.requiresPhoneVerification)
        assertFalse(state.isBookingTrip)
    }

    @Test
    fun `consumeRequiresPhoneVerification clears the gate flag`() = runTest {
        every { requirePhoneVerification.invoke() } returns
            Result.failure(RequirePhoneVerificationUseCase.PhoneVerificationRequired)
        viewModel.bookTrip()
        assertEquals(VerifyPhoneReason.BookTrip, viewModel.uiState.value.requiresPhoneVerification)

        viewModel.consumeRequiresPhoneVerification()

        assertNull(viewModel.uiState.value.requiresPhoneVerification)
    }

    private fun testPackage(
        id: Int,
        pickupCity: String = "Toronto",
        deliveryCity: String = "Vancouver",
        packageWeightKg: Double = 2.0,
        maxPriceBudget: Double = 50.0,
        pickupDatePreferred: String? = null,
    ) = PackageRequest(
        id = id,
        shipperId = 1,
        pickupAddress = "A",
        pickupCity = pickupCity,
        pickupCountry = "Canada",
        deliveryAddress = "B",
        deliveryCity = deliveryCity,
        deliveryCountry = "Canada",
        packageWeightKg = packageWeightKg,
        packageDimensions = null,
        packageType = null,
        fragile = false,
        packageValue = null,
        packageDescription = "Books",
        urgencyLevel = null,
        maxPriceBudget = maxPriceBudget,
        pickupDatePreferred = pickupDatePreferred,
        pickupTimePreferred = null,
        pickupDateFlexible = null,
        deliveryDateNeeded = null,
        deliveryTimeNeeded = null,
        specialHandlingRequirements = null,
        requestStatus = PackageRequestStatus.OPEN,
        createdAt = null,
        updatedAt = null,
        compatibleTripsCount = null,
        shipper = null,
        images = null,
        imagesProcessing = null,
        serviceType = null,
        shoppingList = null,
        storeName = null,
        storeAddress = null,
        receiptRequired = null,
    )

    private fun tripsPage(
        trips: List<Trip>,
        currentPage: Int,
        lastPage: Int,
        perPage: Int = 15,
    ): com.efthemiosprime.pasabayan.features.trips.model.AvailableTripsPage =
        com.efthemiosprime.pasabayan.features.trips.model.AvailableTripsPage(
            trips = trips,
            currentPage = currentPage,
            lastPage = lastPage,
            total = trips.size,
            perPage = perPage,
        )

    private fun testTrip(
        id: Int,
        status: TripStatus = TripStatus.ACTIVE,
    ) = Trip(
        id = id, carrierId = 1,
        originCity = "Toronto", originCountry = "Canada",
        originLat = null, originLng = null,
        destinationCity = "Vancouver", destinationCountry = "Canada",
        destinationLat = null, destinationLng = null,
        departureDate = "2026-04-01T08:00:00Z",
        arrivalDate = "2026-04-01T14:00:00Z",
        availableWeightKg = 25.0, availableSpaceLiters = 50.0,
        pricePerKg = 15.0, tripStatus = status,
        transportationMethod = TransportationMethod.FLIGHT,
        specialNotes = null, carrier = null,
        createdAt = null, updatedAt = null,
        pricingType = null, pricingMethod = null,
        flatTripPrice = null, basePrice = null, calculatedPrice = null,
        pickupAddress = null, pickupLandmark = null,
        dropoffAddress = null, dropoffLandmark = null,
        tripEarningsTotal = null, tripEarningsCurrency = null,
        tripEarningsBreakdown = null,
        hasPendingRequests = null, pendingRequestCount = null,
        pendingRequests = null, distanceKm = null,
    )
}
