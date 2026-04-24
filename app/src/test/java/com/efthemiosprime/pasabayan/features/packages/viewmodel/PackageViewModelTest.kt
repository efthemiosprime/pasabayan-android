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
        viewModel = PackageViewModel(mockContext, fakeRepo, fakeBookingsRepository)
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

    override suspend fun loadPackages() = loadResult
    override suspend fun loadAvailablePackages(params: Map<String, String>) = availableResult
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
    override suspend fun cancelMatch(matchId: Int): Result<Unit> = Result.failure(Exception("Not used"))
    override suspend fun markPickedUp(matchId: Int): Result<DeliveryMatch> = Result.failure(Exception("Not used"))
    override suspend fun markInTransit(matchId: Int): Result<DeliveryMatch> = Result.failure(Exception("Not used"))
    override suspend fun markDelivered(matchId: Int): Result<DeliveryMatch> = Result.failure(Exception("Not used"))
    override suspend fun shipperAccept(matchId: Int): Result<DeliveryMatch> = Result.failure(Exception("Not used"))
    override suspend fun shipperDecline(matchId: Int): Result<DeliveryMatch> = Result.failure(Exception("Not used"))
    override suspend fun carrierAcceptShipperRequest(matchId: Int): Result<DeliveryMatch> = Result.failure(Exception("Not used"))
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
    ): Result<DeliveryMatch> = Result.failure(Exception("Not used"))
}
