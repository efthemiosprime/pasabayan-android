package com.efthemiosprime.pasabayan.features.trips.viewmodel

import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import com.efthemiosprime.pasabayan.features.trips.components.TripProgressStatus
import com.efthemiosprime.pasabayan.features.trips.model.TripMatchPackage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TripPackageProgressViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeTripsRepository
    private lateinit var viewModel: TripPackageProgressViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        fakeRepo = FakeTripsRepository()
        viewModel = TripPackageProgressViewModel(fakeRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is idle`() {
        assertTrue(viewModel.state.value is TripPackageProgressState.Idle)
    }

    @Test
    fun `load emits empty state when no matches`() = runTest {
        fakeRepo.tripMatchesResult = Result.success(emptyList())
        viewModel.loadMatches(1, "Apr 1")
        advanceUntilIdle()
        assertTrue(viewModel.state.value is TripPackageProgressState.Empty)
    }

    @Test
    fun `load derives loaded metrics`() = runTest {
        fakeRepo.tripMatchesResult = Result.success(
            listOf(
                TripMatchPackage(1, MatchStatus.DELIVERED, null, null, null, null, null, null, null, null, null, null, null, null, null, null),
                TripMatchPackage(2, MatchStatus.CONFIRMED, null, null, null, null, null, null, null, null, null, null, null, null, null, null),
            ),
        )
        viewModel.loadMatches(1, "Apr 1")
        advanceUntilIdle()
        val state = viewModel.state.value as TripPackageProgressState.Loaded
        assertEquals(2, state.metrics.totalMatches)
        assertEquals(1, state.metrics.deliveredMatches)
        assertEquals(1, state.metrics.activeMatches)
        assertEquals(TripProgressStatus.IN_PROGRESS, state.metrics.status)
        assertEquals(0.5f, state.metrics.deliveredRatio, 0.0001f)
        assertEquals(listOf(1, 2), state.metrics.deliveredLabel.formatArgs)
        assertEquals(listOf(1), state.metrics.nextActionLabel.formatArgs)
    }

    @Test
    fun `load derives complete status when all matches delivered`() = runTest {
        fakeRepo.tripMatchesResult = Result.success(
            listOf(
                TripMatchPackage(1, MatchStatus.DELIVERED, null, null, null, null, null, null, null, null, null, null, null, null, null, null),
                TripMatchPackage(2, MatchStatus.DELIVERED, null, null, null, null, null, null, null, null, null, null, null, null, null, null),
            ),
        )
        viewModel.loadMatches(8, "Apr 3")
        advanceUntilIdle()
        val state = viewModel.state.value as TripPackageProgressState.Loaded
        assertEquals(TripProgressStatus.COMPLETE, state.metrics.status)
        assertTrue(state.metrics.nextActionLabel.formatArgs.isEmpty())
    }

    @Test
    fun `refresh reruns last load parameters`() = runTest {
        fakeRepo.tripMatchesResult = Result.success(
            listOf(
                TripMatchPackage(1, MatchStatus.CONFIRMED, null, null, null, null, null, null, null, null, null, null, null, null, null, null),
            ),
        )
        viewModel.loadMatches(42, "Apr 9")
        advanceUntilIdle()

        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(listOf(42, 42), fakeRepo.loadedTripMatchIds)
    }

    @Test
    fun `refresh without prior load is no-op`() = runTest {
        viewModel.refresh()
        advanceUntilIdle()
        assertTrue(fakeRepo.loadedTripMatchIds.isEmpty())
        assertTrue(viewModel.state.value is TripPackageProgressState.Idle)
    }

    @Test
    fun `load emits error when matches request fails`() = runTest {
        fakeRepo.tripMatchesResult = Result.failure(Exception("boom"))
        viewModel.loadMatches(2, "Apr 2")
        advanceUntilIdle()
        val state = viewModel.state.value as TripPackageProgressState.Error
        assertEquals("boom", state.message)
    }
}
