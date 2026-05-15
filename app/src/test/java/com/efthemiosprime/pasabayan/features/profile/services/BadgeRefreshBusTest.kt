package com.efthemiosprime.pasabayan.features.profile.services

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BadgeRefreshBusTest {

    @Test(timeout = 10_000)
    fun `emit reaches an attached collector exactly once`() = runTest(UnconfinedTestDispatcher()) {
        val bus = BadgeRefreshBus()
        var count = 0
        val collectJob = backgroundScope.launch { bus.events.collect { count++ } }
        // With UnconfinedTestDispatcher, the launched collector is already
        // subscribed and parked by the time we get here.

        val accepted = bus.emit()

        assertTrue(accepted)
        assertEquals(1, count)
        collectJob.cancel()
    }

    @Test(timeout = 10_000)
    fun `emit before any collector still returns true (buffer absorbs)`() {
        val bus = BadgeRefreshBus()
        val accepted = bus.emit()
        assertTrue(accepted)
    }

    @Test(timeout = 10_000)
    fun `multiple emits reach the collector in order`() = runTest(UnconfinedTestDispatcher()) {
        val bus = BadgeRefreshBus()
        var count = 0
        val collectJob = backgroundScope.launch { bus.events.collect { count++ } }

        repeat(3) { bus.emit() }

        assertEquals(3, count)
        collectJob.cancel()
    }
}
