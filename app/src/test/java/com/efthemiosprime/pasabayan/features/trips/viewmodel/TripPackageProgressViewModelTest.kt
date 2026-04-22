package com.efthemiosprime.pasabayan.features.trips.viewmodel

import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
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
    fun `load emits empty state when no matches`() = runTest {
        fakeRepo.tripMatchesResult = Result.success(emptyList())
        viewModel.load(1, "Apr 1")
        advanceUntilIdle()
        assertTrue(viewModel.state.value is TripPackageProgressState.Empty)
    }

    @Test
    fun `load derives loaded metrics`() = runTest {
        fakeRepo.tripMatchesResult = Result.success(
            listOf(
                TripMatchPackage(1, MatchStatus.DELIVERED, null, null, null, null, null, null, null, null, null, null),
                TripMatchPackage(2, MatchStatus.CONFIRMED, null, null, null, null, null, null, null, null, null, null),
            ),
        )
        viewModel.load(1, "Apr 1")
        advanceUntilIdle()
        val state = viewModel.state.value as TripPackageProgressState.Loaded
        assertEquals(2, state.metrics.totalMatches)
        assertEquals(1, state.metrics.deliveredMatches)
        assertEquals(1, state.metrics.activeMatches)
    }
}
