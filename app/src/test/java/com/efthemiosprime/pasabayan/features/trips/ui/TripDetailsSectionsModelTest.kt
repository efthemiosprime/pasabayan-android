package com.efthemiosprime.pasabayan.features.trips.ui

import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import com.efthemiosprime.pasabayan.features.trips.model.TripMatchPackage
import com.efthemiosprime.pasabayan.features.trips.model.TripPackagesFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TripDetailsSectionsModelTest {

    @Test
    fun `filterTripMatches returns delivered matches for delivered filter`() {
        val matches = listOf(
            testMatch(1, MatchStatus.DELIVERED),
            testMatch(2, MatchStatus.CONFIRMED),
            testMatch(3, MatchStatus.PICKED_UP),
        )

        val filtered = filterTripMatches(matches, TripPackagesFilter.DELIVERED)

        assertEquals(1, filtered.size)
        assertEquals(MatchStatus.DELIVERED, filtered.first().matchStatus)
    }

    @Test
    fun `filterTripMatches returns non delivered matches for remaining filter`() {
        val matches = listOf(
            testMatch(1, MatchStatus.DELIVERED),
            testMatch(2, MatchStatus.IN_TRANSIT),
            testMatch(3, MatchStatus.CONFIRMED),
        )

        val filtered = filterTripMatches(matches, TripPackagesFilter.REMAINING)

        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.matchStatus != MatchStatus.DELIVERED })
    }

    @Test
    fun `toProgressMetrics calculates delivered and active counts`() {
        val matches = listOf(
            testMatch(1, MatchStatus.DELIVERED),
            testMatch(2, MatchStatus.CONFIRMED),
            testMatch(3, MatchStatus.PICKED_UP),
            testMatch(4, MatchStatus.IN_TRANSIT),
        )

        val metrics = toProgressMetrics(matches, "Apr 2")

        requireNotNull(metrics)
        assertEquals(4, metrics.totalMatches)
        assertEquals(1, metrics.deliveredMatches)
        assertEquals(3, metrics.activeMatches)
        assertEquals("Apr 2", metrics.arrivalDateText)
    }

    private fun testMatch(id: Int, status: MatchStatus): TripMatchPackage = TripMatchPackage(
        id = id,
        matchStatus = status,
        agreedPrice = null,
        packageDescription = null,
        packageWeightKg = null,
        packageId = null,
        shipper = null,
        chatConversationId = null,
        confirmedAt = null,
        pickedUpAt = null,
        deliveredAt = null,
        createdAt = null,
    )
}
