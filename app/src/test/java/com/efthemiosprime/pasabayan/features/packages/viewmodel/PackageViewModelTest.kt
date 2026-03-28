package com.efthemiosprime.pasabayan.features.packages.viewmodel

import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageRequestStatus
import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageType
import com.efthemiosprime.pasabayan.core.domain.`enum`.UrgencyLevel
import com.efthemiosprime.pasabayan.core.network.packages.CreatePackageRequestJson
import com.efthemiosprime.pasabayan.core.network.packages.PackageUpdateRequestJson
import com.efthemiosprime.pasabayan.features.packages.model.AvailablePackage
import com.efthemiosprime.pasabayan.features.packages.model.PackageRequest
import com.efthemiosprime.pasabayan.features.packages.services.PackagesRepository
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
class PackageViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakePackagesRepository
    private lateinit var viewModel: PackageViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakePackagesRepository()
        viewModel = PackageViewModel(fakeRepo)
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
    var updateResult: Result<PackageRequest>? = null
    var cancelResult: Result<Unit> = Result.success(Unit)

    override suspend fun loadPackages() = loadResult
    override suspend fun loadAvailablePackages(params: Map<String, String>) = availableResult
    override suspend fun getPackage(id: Int) = getResult ?: Result.failure(Exception("Not set"))
    override suspend fun createPackage(request: CreatePackageRequestJson) = createResult ?: Result.failure(Exception("Not set"))
    override suspend fun updatePackage(id: Int, request: PackageUpdateRequestJson) = updateResult ?: Result.failure(Exception("Not set"))
    override suspend fun cancelPackage(id: Int) = cancelResult
}
