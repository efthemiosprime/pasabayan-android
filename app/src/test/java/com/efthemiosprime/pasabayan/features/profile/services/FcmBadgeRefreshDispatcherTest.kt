package com.efthemiosprime.pasabayan.features.profile.services

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FcmBadgeRefreshDispatcherTest {

    @Test(timeout = 10_000)
    fun `badge_refresh data emits to bus and reports consumed`() = runTest(UnconfinedTestDispatcher()) {
        val bus = BadgeRefreshBus()
        var emissions = 0
        backgroundScope.launch { bus.events.collect { emissions++ } }
        val dispatcher = FcmBadgeRefreshDispatcher(bus)

        val consumed = dispatcher.handleIfBadgeRefresh(
            data = mapOf("type" to "badge_refresh", "reason" to "stripe_onboarded"),
        )

        assertTrue(consumed)
        assertEquals(1, emissions)
    }

    @Test(timeout = 10_000)
    fun `badge_refresh without reason still emits`() = runTest(UnconfinedTestDispatcher()) {
        val bus = BadgeRefreshBus()
        var emissions = 0
        backgroundScope.launch { bus.events.collect { emissions++ } }
        val dispatcher = FcmBadgeRefreshDispatcher(bus)

        val consumed = dispatcher.handleIfBadgeRefresh(mapOf("type" to "badge_refresh"))

        assertTrue(consumed)
        assertEquals(1, emissions)
    }

    @Test(timeout = 10_000)
    fun `non-badge payload reports not consumed and does not emit`() = runTest(UnconfinedTestDispatcher()) {
        val bus = BadgeRefreshBus()
        var emissions = 0
        backgroundScope.launch { bus.events.collect { emissions++ } }
        val dispatcher = FcmBadgeRefreshDispatcher(bus)

        val consumed = dispatcher.handleIfBadgeRefresh(
            data = mapOf("type" to "new_message", "title" to "Hi"),
        )

        assertFalse(consumed)
        assertEquals(0, emissions)
    }

    @Test(timeout = 10_000)
    fun `empty data reports not consumed`() {
        val dispatcher = FcmBadgeRefreshDispatcher(BadgeRefreshBus())
        assertFalse(dispatcher.handleIfBadgeRefresh(emptyMap()))
    }

    @Test(timeout = 10_000)
    fun `missing type field reports not consumed`() {
        val dispatcher = FcmBadgeRefreshDispatcher(BadgeRefreshBus())
        assertFalse(dispatcher.handleIfBadgeRefresh(mapOf("reason" to "stripe_onboarded")))
    }
}
