package com.efthemiosprime.pasabayan.features.trips.viewmodel

import com.efthemiosprime.pasabayan.features.trips.services.TripsLocalStateUpdater
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TripsLocalStateViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val updater: TripsLocalStateUpdater = mockk()
    private lateinit var viewModel: TripsLocalStateViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        viewModel = TripsLocalStateViewModel(updater)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `retryCarrierDisclaimerPendingSync delegates to updater`() = runTest {
        coEvery {
            updater.retryPendingCarrierDisclaimerSync(any(), any())
        } returns true

        viewModel.retryCarrierDisclaimerPendingSync(42L)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            updater.retryPendingCarrierDisclaimerSync(42L, any())
        }
    }
}
