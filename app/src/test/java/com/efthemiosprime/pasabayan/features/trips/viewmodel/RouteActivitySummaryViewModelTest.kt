package com.efthemiosprime.pasabayan.features.trips.viewmodel

import com.efthemiosprime.pasabayan.features.trips.model.RouteActivitySummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RouteActivitySummaryViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeTripsRepository
    private lateinit var viewModel: RouteActivitySummaryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        fakeRepo = FakeTripsRepository()
        viewModel = RouteActivitySummaryViewModel(fakeRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadSummary publishes summary data`() = runTest {
        fakeRepo.routeActivitySummaryResult = Result.success(
            RouteActivitySummary(
                totalTrips = 4,
                activeTrips = 2,
                completedTrips = 1,
                totalEarnings = 99.5,
                currency = "CAD",
            ),
        )
        viewModel.loadSummary()
        advanceUntilIdle()
        assertEquals(4, viewModel.uiState.value.summary?.totalTrips)
        assertEquals(2, viewModel.uiState.value.summary?.activeTrips)
        assertEquals(1, viewModel.uiState.value.summary?.completedTrips)
    }
}
