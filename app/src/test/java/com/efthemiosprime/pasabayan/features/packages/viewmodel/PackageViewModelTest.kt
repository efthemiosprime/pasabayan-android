package com.efthemiosprime.pasabayan.features.packages.viewmodel

import android.content.Context
import android.net.Uri
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageRequestStatus
import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageType
import com.efthemiosprime.pasabayan.core.domain.`enum`.UrgencyLevel
import com.efthemiosprime.pasabayan.core.network.packages.CreatePackageRequestJson
import com.efthemiosprime.pasabayan.core.network.packages.CreateServiceRequestBodyJson
import com.efthemiosprime.pasabayan.core.network.packages.PackageUpdateRequestJson
import com.efthemiosprime.pasabayan.features.bookings.model.DeliveryMatch
import com.efthemiosprime.pasabayan.features.bookings.services.BookingsRepository
import com.efthemiosprime.pasabayan.features.packages.model.AvailablePackage
import com.efthemiosprime.pasabayan.features.packages.model.PackageRequest
import com.efthemiosprime.pasabayan.features.packages.model.PackageSubmitPayload
import com.efthemiosprime.pasabayan.features.packages.model.ServiceRequestShoppingItem
import com.efthemiosprime.pasabayan.features.packages.model.ServiceRequestSubmitPayload
import com.efthemiosprime.pasabayan.features.packages.services.PackagesRepository
import com.efthemiosprime.pasabayan.features.verification.model.VerifyPhoneReason
import com.efthemiosprime.pasabayan.features.verification.services.RequirePhoneVerificationUseCase
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PackageViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockContext: Context
    private lateinit var fakeRepo: FakePackagesRepository
    private lateinit var fakeBookingsRepository: FakeBookingsRepository
    private lateinit var requirePhoneVerification: RequirePhoneVerificationUseCase
    private lateinit var viewModel: PackageViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockContext = mockk(relaxed = true)
        every { mockContext.getString(R.string.packages_error_load_packages) } returns "Failed to load packages"
        every { mockContext.getString(R.string.packages_error_load_available_packages) } returns "Failed to load available packages"
        every { mockContext.getString(R.string.packages_error_load_package_detail) } returns "Failed to load package details"
        every { mockContext.getString(R.string.packages_error_cancel_package) } returns "Failed to cancel package"
        every { mockContext.getString(R.string.packages_error_submit_package_request) } returns "Failed to submit package request"
        every { mockContext.getString(R.string.packages_error_submit_service_request) } returns "Failed to submit service request"
        every { mockContext.getString(R.string.packages_error_trip_request_send) } returns "Failed to send request"
        every { mockContext.getString(R.string.packages_error_update_package) } returns "Failed to update package"
        every { mockContext.getString(R.string.packages_success_cancel_package) } returns "Package cancelled"
        every { mockContext.getString(R.string.packages_success_submit_package_request) } returns "Package request submitted"
        every { mockContext.getString(R.string.packages_success_submit_service_request) } returns "Service request submitted"
        every { mockContext.getString(R.string.packages_success_trip_request_sent) } returns "Request sent"
        every { mockContext.getString(R.string.packages_success_update_package) } returns "Package updated"
        fakeRepo = FakePackagesRepository()
        fakeBookingsRepository = FakeBookingsRepository()
        requirePhoneVerification = mockk()
        every { requirePhoneVerification.invoke() } returns Result.success(Unit)
        viewModel = PackageViewModel(
            mockContext,
            fakeRepo,
            fakeBookingsRepository,
            requirePhoneVerification,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadPackages sets packages on success`() = runTest {
        fakeRepo.loadResult = Result.success(listOf(testPkg(1), testPkg(2)))
        viewModel.loadPackages()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.packageRequests.size)
        assertTrue(state.hasLoadedPackages)
        assertNull(state.errorMessage)
    }

    @Test
    fun `loadPackages sets error on failure`() = runTest {
        fakeRepo.loadResult = Result.failure(Exception("Network error"))
        viewModel.loadPackages()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.errorMessage != null)
    }

    @Test
    fun `cancelPackage removes package from list`() = runTest {
        fakeRepo.loadResult = Result.success(listOf(testPkg(1), testPkg(2)))
        fakeRepo.cancelResult = Result.success(Unit)
        viewModel.loadPackages()
        advanceUntilIdle()

        viewModel.cancelPackage(1)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.packageRequests.size)
        assertEquals(2, viewModel.uiState.value.packageRequests[0].id)
    }

    @Test
    fun `cancelPackage sets error on failure`() = runTest {
        fakeRepo.loadResult = Result.success(listOf(testPkg(1)))
        fakeRepo.cancelResult = Result.failure(Exception("Not authorized"))
        viewModel.loadPackages()
        advanceUntilIdle()

        viewModel.cancelPackage(1)
        advanceUntilIdle()

        // Package should still be in list
        assertEquals(1, viewModel.uiState.value.packageRequests.size)
        assertTrue(viewModel.uiState.value.errorMessage != null)
    }

    @Test
    fun `filterByStatus returns matching packages`() = runTest {
        fakeRepo.loadResult = Result.success(
            listOf(
                testPkg(1, PackageRequestStatus.OPEN),
                testPkg(2, PackageRequestStatus.MATCHED),
                testPkg(3, PackageRequestStatus.DELIVERED),
            ),
        )
        viewModel.loadPackages()
        advanceUntilIdle()

        val open = viewModel.packagesByStatus(PackageRequestStatus.OPEN)
        assertEquals(1, open.size)
        assertEquals(1, open[0].id)

        val delivered = viewModel.packagesByStatus(PackageRequestStatus.DELIVERED)
        assertEquals(1, delivered.size)
        assertEquals(3, delivered[0].id)
    }

    @Test
    fun `clearError clears errorMessage`() = runTest {
        fakeRepo.loadResult = Result.failure(Exception("fail"))
        viewModel.loadPackages()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.errorMessage != null)

        viewModel.clearError()
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `createPackageRequest updates submit state and prepends created package`() = runTest {
        fakeRepo.createResult = Result.success(testPkg(90))

        viewModel.createPackageRequest(
            payload = testPackagePayload(),
            imageUris = listOf(Uri.parse("content://photos/1")),
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSubmittingPackageRequest)
        assertEquals("Package request submitted", state.packageRequestSuccessMessage)
        assertEquals(90, state.packageRequests.first().id)
    }

    @Test
    fun `createServiceRequest updates submit error on failure`() = runTest {
        fakeRepo.createServiceResult = Result.failure(Exception("Service create failed"))

        viewModel.createServiceRequest(
            ServiceRequestSubmitPayload(
                serviceTypeCode = "grocery_shopping",
                shoppingItems = listOf(
                    ServiceRequestShoppingItem(item = "Eggs", quantity = "12", notes = null),
                ),
                deliveryCity = "Toronto",
                deliveryAddress = "123 Main",
                storeName = null,
                storeAddress = null,
                estimatedCost = null,
                maxPriceBudget = null,
                deliveryDateNeeded = LocalDate.of(2026, 4, 10),
                urgencyLevelCode = "normal",
                directionCode = null,
                recipientName = null,
                recipientPhone = null,
            ),
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSubmittingServiceRequest)
        assertEquals("Service create failed", state.serviceRequestErrorMessage)
    }

    @Test
    fun `loadAvailablePackages updates available list on success`() = runTest {
        fakeRepo.availableResult = Result.success(emptyList())

        viewModel.loadAvailablePackages(force = true)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoadingAvailablePackages)
        assertTrue(state.hasLoadedAvailablePackages)
    }

    // ---- Browse pagination state machine (iOS parity) ----

    @Test
    fun `applyBrowseFilter fetches page 1 with current filter and captures envelope`() = runTest {
        fakeRepo.pageResultQueue.addLast(
            Result.success(
                com.efthemiosprime.pasabayan.features.packages.model.AvailablePackagesPage(
                    packages = listOf(availablePkg(1), availablePkg(2)),
                    currentPage = 1,
                    lastPage = 3,
                    total = 30,
                    perPage = 15,
                    nearby = true,
                ),
            ),
        )
        viewModel.setBrowseFilter(
            com.efthemiosprime.pasabayan.features.packages.model.PackageBrowseFilter(
                searchText = "doc",
                urgency = UrgencyLevel.URGENT,
            ),
        )

        viewModel.applyBrowseFilter()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.availablePackages.size)
        assertEquals(1, state.availablePackagesCurrentPage)
        assertTrue(state.availablePackagesHasMore)
        assertEquals(true, state.availablePackagesNearby)
        assertFalse(state.isLoadingAvailablePackages)
        assertFalse(state.availablePackagesIsLoadingMore)

        // Repo received the filter + page = 1.
        assertEquals(1, fakeRepo.pageCalls.size)
        val (filter, page, _) = fakeRepo.pageCalls.first()
        assertEquals("doc", filter.searchText)
        assertEquals(UrgencyLevel.URGENT, filter.urgency)
        assertEquals(1, page)
    }

    @Test
    fun `loadMoreAvailablePackages appends and bumps currentPage`() = runTest {
        // Page 1
        fakeRepo.pageResultQueue.addLast(
            Result.success(
                com.efthemiosprime.pasabayan.features.packages.model.AvailablePackagesPage(
                    packages = listOf(availablePkg(1), availablePkg(2)),
                    currentPage = 1,
                    lastPage = 2,
                    total = 4,
                    perPage = 2,
                    nearby = null,
                ),
            ),
        )
        viewModel.applyBrowseFilter()
        advanceUntilIdle()

        // Page 2
        fakeRepo.pageResultQueue.addLast(
            Result.success(
                com.efthemiosprime.pasabayan.features.packages.model.AvailablePackagesPage(
                    packages = listOf(availablePkg(3), availablePkg(4)),
                    currentPage = 2,
                    lastPage = 2,
                    total = 4,
                    perPage = 2,
                    nearby = null,
                ),
            ),
        )
        viewModel.loadMoreAvailablePackages()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(4, state.availablePackages.size)
        assertEquals(2, state.availablePackagesCurrentPage)
        assertFalse(state.availablePackagesHasMore) // currentPage == lastPage
        // Second call should have been for page 2 with the same filter.
        assertEquals(2, fakeRepo.pageCalls.last().second)
    }

    @Test
    fun `loadMoreAvailablePackages dedupes overlapping items by effectiveId`() = runTest {
        // Page 1 contains item 2; page 2 also contains 2 plus 3.
        fakeRepo.pageResultQueue.addLast(
            Result.success(
                com.efthemiosprime.pasabayan.features.packages.model.AvailablePackagesPage(
                    packages = listOf(availablePkg(1), availablePkg(2)),
                    currentPage = 1,
                    lastPage = 2,
                    total = 4,
                    perPage = 2,
                    nearby = null,
                ),
            ),
        )
        viewModel.applyBrowseFilter()
        advanceUntilIdle()

        fakeRepo.pageResultQueue.addLast(
            Result.success(
                com.efthemiosprime.pasabayan.features.packages.model.AvailablePackagesPage(
                    packages = listOf(availablePkg(2), availablePkg(3)),
                    currentPage = 2,
                    lastPage = 2,
                    total = 3,
                    perPage = 2,
                    nearby = null,
                ),
            ),
        )
        viewModel.loadMoreAvailablePackages()
        advanceUntilIdle()

        val ids = viewModel.uiState.value.availablePackages.map { it.effectiveId }
        assertEquals(listOf(1, 2, 3), ids)
    }

    @Test
    fun `loadMoreAvailablePackages is no-op when no more pages`() = runTest {
        fakeRepo.pageResultQueue.addLast(
            Result.success(
                com.efthemiosprime.pasabayan.features.packages.model.AvailablePackagesPage(
                    packages = listOf(availablePkg(1)),
                    currentPage = 1,
                    lastPage = 1,
                    total = 1,
                    perPage = 15,
                    nearby = null,
                ),
            ),
        )
        viewModel.applyBrowseFilter()
        advanceUntilIdle()
        fakeRepo.pageCalls.clear()

        viewModel.loadMoreAvailablePackages()
        advanceUntilIdle()

        assertTrue("loadMore must not call repo when hasMore is false", fakeRepo.pageCalls.isEmpty())
    }

    @Test
    fun `loadMoreAvailablePackages is no-op before any page lands`() = runTest {
        viewModel.loadMoreAvailablePackages()
        advanceUntilIdle()

        assertTrue(fakeRepo.pageCalls.isEmpty())
    }

    @Test
    fun `applyBrowseFilter discards stale in-flight response`() = runTest {
        // Two reload calls queued back-to-back. Both response payloads are queued in call order:
        // the first call (stale generation) pops the "99" page; the second call (current generation)
        // pops the "1, 2" page. The race guard must discard the stale response.
        fakeRepo.pageResultQueue.addLast(
            Result.success(
                com.efthemiosprime.pasabayan.features.packages.model.AvailablePackagesPage(
                    packages = listOf(availablePkg(99)),
                    currentPage = 1,
                    lastPage = 1,
                    total = 1,
                    perPage = 15,
                    nearby = null,
                ),
            ),
        )
        fakeRepo.pageResultQueue.addLast(
            Result.success(
                com.efthemiosprime.pasabayan.features.packages.model.AvailablePackagesPage(
                    packages = listOf(availablePkg(1), availablePkg(2)),
                    currentPage = 1,
                    lastPage = 1,
                    total = 2,
                    perPage = 15,
                    nearby = null,
                ),
            ),
        )

        // First apply — generation 1.
        viewModel.setBrowseFilter(
            com.efthemiosprime.pasabayan.features.packages.model.PackageBrowseFilter(searchText = "stale"),
        )
        viewModel.applyBrowseFilter()
        // Second apply (before the first coroutine runs) — generation 2 supersedes.
        viewModel.setBrowseFilter(
            com.efthemiosprime.pasabayan.features.packages.model.PackageBrowseFilter(searchText = "fresh"),
        )
        viewModel.applyBrowseFilter()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf(1, 2), state.availablePackages.map { it.effectiveId })
    }

    @Test
    fun `visibleAvailablePackages applies packageType client-side filter`() = runTest {
        fakeRepo.pageResultQueue.addLast(
            Result.success(
                com.efthemiosprime.pasabayan.features.packages.model.AvailablePackagesPage(
                    packages = listOf(
                        availablePkg(1, packageType = PackageType.GENERAL),
                        availablePkg(2, packageType = PackageType.ELECTRONICS),
                        availablePkg(3, packageType = PackageType.ELECTRONICS),
                    ),
                    currentPage = 1,
                    lastPage = 1,
                    total = 3,
                    perPage = 15,
                    nearby = null,
                ),
            ),
        )
        viewModel.setBrowseFilter(
            com.efthemiosprime.pasabayan.features.packages.model.PackageBrowseFilter(packageType = PackageType.ELECTRONICS),
        )
        viewModel.applyBrowseFilter()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        // Server returned all 3 (packageType is client-side); visible should be 2.
        assertEquals(3, state.availablePackages.size)
        assertEquals(listOf(2, 3), state.visibleAvailablePackages.map { it.effectiveId })
    }

    @Test
    fun `applyBrowseFilter surfaces failure on errorMessage`() = runTest {
        fakeRepo.pageResultQueue.addLast(Result.failure(RuntimeException("boom")))

        viewModel.applyBrowseFilter()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoadingAvailablePackages)
        assertEquals("boom", state.errorMessage)
        assertTrue(state.availablePackages.isEmpty())
    }

    @Test
    fun `loadMoreAvailablePackages surfaces failure on loadMoreError without clearing list`() = runTest {
        fakeRepo.pageResultQueue.addLast(
            Result.success(
                com.efthemiosprime.pasabayan.features.packages.model.AvailablePackagesPage(
                    packages = listOf(availablePkg(1)),
                    currentPage = 1,
                    lastPage = 5,
                    total = 100,
                    perPage = 15,
                    nearby = null,
                ),
            ),
        )
        viewModel.applyBrowseFilter()
        advanceUntilIdle()

        fakeRepo.pageResultQueue.addLast(Result.failure(RuntimeException("network down")))
        viewModel.loadMoreAvailablePackages()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("network down", state.availablePackagesLoadMoreError)
        assertFalse(state.availablePackagesIsLoadingMore)
        assertEquals(1, state.availablePackages.size) // existing items preserved
        assertEquals(1, state.availablePackagesCurrentPage) // didn't advance
    }

    @Test
    fun `loadPackageDetail updates selected detail on success`() = runTest {
        fakeRepo.getResult = Result.success(testPkg(70))

        viewModel.loadPackageDetail(70)
        advanceUntilIdle()

        assertEquals(70, viewModel.uiState.value.selectedPackageDetail?.id)
    }

    @Test
    fun `updatePackage updates selected detail on success`() = runTest {
        fakeRepo.updateResult = Result.success(testPkg(11))

        viewModel.updatePackage(11, PackageUpdateRequestJson(maxPriceBudget = 88.0))
        advanceUntilIdle()

        assertEquals(11, viewModel.uiState.value.selectedPackageDetail?.id)
        assertEquals("Package updated", viewModel.uiState.value.successMessage)
    }

    @Test
    fun `createPackageRequest blocked when phone not verified does not call repo`() = runTest {
        every { requirePhoneVerification.invoke() } returns
            Result.failure(RequirePhoneVerificationUseCase.PhoneVerificationRequired)
        fakeRepo.createResult = Result.success(testPkg(91))

        viewModel.createPackageRequest(
            payload = testPackagePayload(),
            imageUris = emptyList(),
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(VerifyPhoneReason.CreatePackage, state.requiresPhoneVerification)
        assertFalse(state.isSubmittingPackageRequest)
        assertNull(state.packageRequestSuccessMessage)
        assertTrue(state.packageRequests.isEmpty())
    }

    @Test
    fun `createServiceRequest blocked when phone not verified does not call repo`() = runTest {
        every { requirePhoneVerification.invoke() } returns
            Result.failure(RequirePhoneVerificationUseCase.PhoneVerificationRequired)
        fakeRepo.createServiceResult = Result.success(testPkg(92))

        viewModel.createServiceRequest(
            ServiceRequestSubmitPayload(
                serviceTypeCode = "grocery_shopping",
                shoppingItems = listOf(
                    ServiceRequestShoppingItem(item = "Eggs", quantity = "12", notes = null),
                ),
                deliveryCity = "Toronto",
                deliveryAddress = "123 Main",
                storeName = null,
                storeAddress = null,
                estimatedCost = null,
                maxPriceBudget = null,
                deliveryDateNeeded = LocalDate.of(2026, 4, 10),
                urgencyLevelCode = "normal",
                directionCode = null,
                recipientName = null,
                recipientPhone = null,
            ),
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(VerifyPhoneReason.CreatePackage, state.requiresPhoneVerification)
        assertFalse(state.isSubmittingServiceRequest)
        assertNull(state.serviceRequestSuccessMessage)
    }

    @Test
    fun `consumeRequiresPhoneVerification clears the gate flag`() = runTest {
        every { requirePhoneVerification.invoke() } returns
            Result.failure(RequirePhoneVerificationUseCase.PhoneVerificationRequired)
        viewModel.createPackageRequest(payload = testPackagePayload(), imageUris = emptyList())
        advanceUntilIdle()
        assertEquals(VerifyPhoneReason.CreatePackage, viewModel.uiState.value.requiresPhoneVerification)

        viewModel.consumeRequiresPhoneVerification()

        assertNull(viewModel.uiState.value.requiresPhoneVerification)
    }

    @Test
    fun `requestTripForPackage blocked when phone not verified does not call repo`() = runTest {
        every { requirePhoneVerification.invoke() } returns
            Result.failure(RequirePhoneVerificationUseCase.PhoneVerificationRequired)

        viewModel.requestTripForPackage(
            packageId = 5,
            tripId = 7,
            offeredPrice = 25.0,
            message = "Please carry",
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(VerifyPhoneReason.BookTrip, state.requiresPhoneVerification)
        assertFalse(state.isSubmittingTripRequest)
        assertNull(state.tripRequestSuccessMessage)
    }

    private fun testPackagePayload() = PackageSubmitPayload(
        pickupAddress = "123 Main",
        pickupCity = "Toronto",
        pickupCountryCode = "CA",
        deliveryAddress = "456 Oak",
        deliveryCity = "Montreal",
        deliveryCountryCode = "CA",
        packageWeightKg = 1.5,
        packageTypeCode = "general",
        fragile = false,
        urgencyLevelCode = "normal",
        pickupDatePreferred = LocalDate.of(2026, 4, 5),
        pickupTimePreferred = LocalTime.of(9, 30),
        pickupDateFlexible = false,
        deliveryDateNeeded = LocalDate.of(2026, 4, 7),
        deliveryTimeNeeded = LocalTime.of(12, 0),
        packageValue = 100.0,
        packageDescription = "Test package",
        maxPriceBudget = 40.0,
        specialHandlingRequirements = null,
    )

    private fun availablePkg(
        id: Int,
        packageType: PackageType = PackageType.GENERAL,
    ) = AvailablePackage(
        id = id,
        packageRequestId = id,
        pickupCity = "Toronto",
        pickupCountry = "Canada",
        deliveryCity = "Montreal",
        deliveryCountry = "Canada",
        packageWeightKg = 5.0,
        packageDimensions = null,
        urgencyLevel = UrgencyLevel.NORMAL,
        maxPriceBudget = 60.0,
        pickupDatePreferred = "2026-04-05",
        pickupDateFlexible = false,
        deliveryDateNeeded = "2026-04-07",
        fragile = false,
        packageType = packageType,
        packageDescription = "Test",
        createdAt = "2026-04-01T00:00:00Z",
        daysSincePosted = 1.0,
        distanceKm = null,
        shipper = null,
        serviceType = null,
    )

    private fun testPkg(
        id: Int,
        status: PackageRequestStatus = PackageRequestStatus.OPEN,
    ) = PackageRequest(
        id = id, shipperId = 5,
        pickupAddress = "123 Main", pickupCity = "Toronto", pickupCountry = "Canada",
        deliveryAddress = "456 Oak", deliveryCity = "Montreal", deliveryCountry = "Canada",
        packageWeightKg = 10.0, packageDimensions = null,
        packageType = PackageType.GENERAL, fragile = false,
        packageValue = null, packageDescription = "Test",
        urgencyLevel = UrgencyLevel.NORMAL, maxPriceBudget = null,
        pickupDatePreferred = "2026-04-05", pickupTimePreferred = null,
        pickupDateFlexible = false, deliveryDateNeeded = "2026-04-07",
        deliveryTimeNeeded = null, specialHandlingRequirements = null,
        requestStatus = status, createdAt = null, updatedAt = null,
        compatibleTripsCount = null, shipper = null,
        images = null, imagesProcessing = null,
        serviceType = null, shoppingList = null,
        storeName = null, storeAddress = null, receiptRequired = null,
    )
}

class FakePackagesRepository : PackagesRepository {
    var loadResult: Result<List<PackageRequest>> = Result.success(emptyList())
    var availableResult: Result<List<AvailablePackage>> = Result.success(emptyList())
    var getResult: Result<PackageRequest>? = null
    var createResult: Result<PackageRequest>? = null
    var createServiceResult: Result<PackageRequest>? = null
    var updateResult: Result<PackageRequest>? = null
    var cancelResult: Result<Unit> = Result.success(Unit)

    // -- Browse pagination test surface --
    /** Queued page responses popped in order; default empty page when exhausted. */
    val pageResultQueue: ArrayDeque<Result<com.efthemiosprime.pasabayan.features.packages.model.AvailablePackagesPage>> = ArrayDeque()
    /** Records all calls so tests can assert filter / page propagation. */
    val pageCalls: MutableList<Triple<com.efthemiosprime.pasabayan.features.packages.model.PackageBrowseFilter, Int, Int>> = mutableListOf()
    /** When non-null, the fake suspends on this signal so tests can interleave reload + late response. */
    var pageGate: kotlinx.coroutines.CompletableDeferred<Unit>? = null

    override suspend fun loadPackages() = loadResult
    override suspend fun loadAvailablePackages(params: Map<String, String>) = availableResult
    override suspend fun loadAvailablePackagesPage(
        filter: com.efthemiosprime.pasabayan.features.packages.model.PackageBrowseFilter,
        page: Int,
        perPage: Int,
    ): Result<com.efthemiosprime.pasabayan.features.packages.model.AvailablePackagesPage> {
        pageCalls += Triple(filter, page, perPage)
        pageGate?.await()
        return if (pageResultQueue.isNotEmpty()) {
            pageResultQueue.removeFirst()
        } else {
            Result.success(
                com.efthemiosprime.pasabayan.features.packages.model.AvailablePackagesPage(
                    packages = emptyList(),
                    currentPage = page,
                    lastPage = page,
                    total = 0,
                    perPage = perPage,
                    nearby = null,
                ),
            )
        }
    }
    override suspend fun getPackage(id: Int) = getResult ?: Result.failure(Exception("Not set"))
    override suspend fun createPackage(
        request: CreatePackageRequestJson,
        imageUris: List<Uri>,
    ) = createResult ?: Result.failure(Exception("Not set"))
    override suspend fun createServiceRequest(request: CreateServiceRequestBodyJson) =
        createServiceResult ?: Result.failure(Exception("Not set"))
    override suspend fun updatePackage(id: Int, request: PackageUpdateRequestJson) = updateResult ?: Result.failure(Exception("Not set"))
    override suspend fun cancelPackage(id: Int) = cancelResult
}

private class FakeBookingsRepository : BookingsRepository {
    override suspend fun loadMatches(role: String?, status: String?): Result<List<DeliveryMatch>> = Result.success(emptyList())
    override suspend fun getMatch(matchId: Int): Result<DeliveryMatch> = Result.failure(Exception("Not used"))
    override suspend fun confirmMatch(matchId: Int): Result<DeliveryMatch> = Result.failure(Exception("Not used"))
    override suspend fun cancelMatch(matchId: Int): Result<com.efthemiosprime.pasabayan.features.bookings.model.CancelMatchResult> =
        Result.failure(Exception("Not used"))
    override suspend fun markPickedUp(matchId: Int): Result<DeliveryMatch> = Result.failure(Exception("Not used"))
    override suspend fun markInTransit(matchId: Int): Result<DeliveryMatch> = Result.failure(Exception("Not used"))
    override suspend fun markDelivered(matchId: Int): Result<DeliveryMatch> = Result.failure(Exception("Not used"))
    override suspend fun shipperAcceptCarrierRequest(matchId: Int, acknowledgeOverage: Boolean?): Result<DeliveryMatch> =
        Result.failure(Exception("Not used"))
    override suspend fun shipperDecline(matchId: Int): Result<DeliveryMatch> = Result.failure(Exception("Not used"))
    override suspend fun carrierAcceptShipperRequest(matchId: Int, acknowledgeOverage: Boolean?): Result<DeliveryMatch> =
        Result.failure(Exception("Not used"))
    override suspend fun carrierDeclineShipperRequest(matchId: Int): Result<DeliveryMatch> = Result.failure(Exception("Not used"))
    override suspend fun generatePickupCode(matchId: Int): Result<String> = Result.failure(Exception("Not used"))
    override suspend fun generateDeliveryCode(matchId: Int): Result<String> = Result.failure(Exception("Not used"))
    override suspend fun confirmPickupWithCode(matchId: Int, code: String): Result<DeliveryMatch> = Result.failure(Exception("Not used"))
    override suspend fun confirmDeliveryWithCode(matchId: Int, code: String): Result<DeliveryMatch> = Result.failure(Exception("Not used"))
    override suspend fun shipperRequestTrip(
        packageId: Int,
        tripId: Int,
        offeredPrice: Double,
        message: String?,
        isCounterOffer: Boolean,
        originalMatchId: Int?,
        originalPrice: Double?,
    ): Result<com.efthemiosprime.pasabayan.features.bookings.model.RequestMatchResult> =
        Result.failure(Exception("Not used"))

    override suspend fun carrierRequestPackage(
        tripId: Int,
        packageId: Int,
        proposedPrice: Double,
        message: String?,
        isCounterOffer: Boolean,
        originalMatchId: Int?,
        originalPrice: Double?,
    ): Result<com.efthemiosprime.pasabayan.features.bookings.model.RequestMatchResult> =
        Result.failure(Exception("Not used"))
}
