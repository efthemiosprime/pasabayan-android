package com.efthemiosprime.pasabayan.features.profile.viewmodel

import com.efthemiosprime.pasabayan.core.network.profile.AttentionSignalsJson
import com.efthemiosprime.pasabayan.features.profile.services.ProfileAttentionRepository
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileAttentionViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repo: ProfileAttentionRepository = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty signals`() {
        val vm = ProfileAttentionViewModel(repo)
        assertEquals(AttentionSignalsJson(), vm.attention.value)
    }

    @Test
    fun `refresh on success updates state`() = runTest(dispatcher) {
        val signals = AttentionSignalsJson(
            pendingReviewsCount = 3,
            phoneVerificationNeeded = true,
            payoutSetupNeeded = false,
            total = 4,
        )
        coEvery { repo.fetchAttention() } returns Result.success(signals)
        val vm = ProfileAttentionViewModel(repo)

        vm.refresh()
        advanceUntilIdle()

        assertEquals(signals, vm.attention.value)
    }

    @Test
    fun `refresh on failure preserves prior state`() = runTest(dispatcher) {
        val good = AttentionSignalsJson(pendingReviewsCount = 1, total = 1)
        coEvery { repo.fetchAttention() } returnsMany listOf(
            Result.success(good),
            Result.failure(RuntimeException("network down")),
        )
        val vm = ProfileAttentionViewModel(repo)

        vm.refresh()
        advanceUntilIdle()
        assertEquals(good, vm.attention.value)

        vm.refresh()
        advanceUntilIdle()
        // iOS parity: best-effort surface — failure is silenced; prior state stands.
        assertEquals(good, vm.attention.value)
    }

    @Test
    fun `concurrent refresh calls dedupe to a single repo invocation`() = runTest(dispatcher) {
        coEvery { repo.fetchAttention() } returns Result.success(AttentionSignalsJson(total = 2))
        val vm = ProfileAttentionViewModel(repo)

        // Three rapid calls before the first finishes. The AtomicBoolean guard should
        // collapse them into one. Subsequent post-completion calls fire normally.
        vm.refresh()
        vm.refresh()
        vm.refresh()
        advanceUntilIdle()

        coVerify(exactly = 1) { repo.fetchAttention() }
        assertEquals(2, vm.attention.value.total)
    }

    @Test
    fun `degraded flag from server propagates to state`() = runTest(dispatcher) {
        val signals = AttentionSignalsJson(
            pendingReviewsCount = 1,
            payoutSetupNeeded = true,
            total = 2,
            degraded = true,
        )
        coEvery { repo.fetchAttention() } returns Result.success(signals)
        val vm = ProfileAttentionViewModel(repo)

        vm.refresh()
        advanceUntilIdle()

        assertTrue(vm.attention.value.degraded)
        assertEquals(2, vm.attention.value.total)
    }
}
