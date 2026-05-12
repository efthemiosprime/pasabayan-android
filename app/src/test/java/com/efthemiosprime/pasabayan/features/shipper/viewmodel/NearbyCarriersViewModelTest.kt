package com.efthemiosprime.pasabayan.features.shipper.viewmodel

import com.efthemiosprime.pasabayan.features.shipper.model.NearbyCarrier
import com.efthemiosprime.pasabayan.features.shipper.model.NearbyCarriers
import com.efthemiosprime.pasabayan.features.shipper.services.ShipperRepository
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
class NearbyCarriersViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeShipperRepository
    private lateinit var viewModel: NearbyCarriersViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeShipperRepository()
        viewModel = NearbyCarriersViewModel(fakeRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadNearbyCarriers populates carriers + home city`() = runTest {
        fakeRepo.result = Result.success(
            NearbyCarriers(
                homeCityId = 7,
                homeCityName = "Toronto",
                radiusKm = 25.0,
                carriers = listOf(carrier(1, completed = 3), carrier(2, completed = 0)),
            ),
        )

        viewModel.loadNearbyCarriers()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.hasLoaded)
        assertEquals(2, state.carriers.size)
        assertEquals("Toronto", state.homeCityName)
        assertEquals(25.0, state.radiusKm!!, 0.001)
        assertTrue(state.isHomeCitySet)
        assertNull(state.errorMessage)
    }

    @Test
    fun `carriersWithCompletedDeliveries filters out zero-delivery carriers`() = runTest {
        fakeRepo.result = Result.success(
            NearbyCarriers(
                homeCityId = 7,
                homeCityName = "Toronto",
                radiusKm = 25.0,
                carriers = listOf(
                    carrier(1, completed = 3),
                    carrier(2, completed = 0),
                    carrier(3, completed = 1),
                ),
            ),
        )

        viewModel.loadNearbyCarriers()
        advanceUntilIdle()

        val ids = viewModel.uiState.value.carriersWithCompletedDeliveries.map { it.id }
        assertEquals(listOf(1, 3), ids)
    }

    @Test
    fun `loadNearbyCarriersIfNeeded fetches once then no-ops`() = runTest {
        fakeRepo.result = Result.success(
            NearbyCarriers(homeCityId = 7, homeCityName = "Toronto", radiusKm = 25.0, carriers = emptyList()),
        )

        viewModel.loadNearbyCarriersIfNeeded()
        advanceUntilIdle()
        viewModel.loadNearbyCarriersIfNeeded()
        advanceUntilIdle()

        assertEquals(1, fakeRepo.callCount)
    }

    @Test
    fun `resetForNewSession clears state so next load re-fetches`() = runTest {
        fakeRepo.result = Result.success(
            NearbyCarriers(homeCityId = 7, homeCityName = "Toronto", radiusKm = 25.0, carriers = emptyList()),
        )
        viewModel.loadNearbyCarriersIfNeeded()
        advanceUntilIdle()
        assertEquals(1, fakeRepo.callCount)

        viewModel.resetForNewSession()
        assertFalse(viewModel.uiState.value.hasLoaded)
        assertTrue(viewModel.uiState.value.carriers.isEmpty())

        viewModel.loadNearbyCarriersIfNeeded()
        advanceUntilIdle()
        assertEquals(2, fakeRepo.callCount)
    }

    @Test
    fun `failure populates errorMessage and clears carriers`() = runTest {
        fakeRepo.result = Result.failure(RuntimeException("boom"))

        viewModel.loadNearbyCarriers()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.hasLoaded) // still considered "loaded" so we don't loop
        assertTrue(state.carriers.isEmpty())
        assertEquals("boom", state.errorMessage)
    }

    @Test
    fun `isHomeCitySet false when backend returns null home city`() = runTest {
        fakeRepo.result = Result.success(
            NearbyCarriers(homeCityId = null, homeCityName = null, radiusKm = null, carriers = emptyList()),
        )

        viewModel.loadNearbyCarriers()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isHomeCitySet)
    }

    private fun carrier(id: Int, completed: Int): NearbyCarrier = NearbyCarrier(
        id = id,
        name = "Carrier $id",
        avatar = null,
        completedDeliveries = completed,
        distanceKm = id.toDouble(),
    )
}

private class FakeShipperRepository : ShipperRepository {
    var result: Result<NearbyCarriers> = Result.success(
        NearbyCarriers(homeCityId = null, homeCityName = null, radiusKm = null, carriers = emptyList()),
    )
    var callCount: Int = 0

    override suspend fun fetchNearbyCarriers(): Result<NearbyCarriers> {
        callCount += 1
        return result
    }
}
