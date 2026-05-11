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

    // -- canCancelTrip / allPackagesDelivered (iOS-parity) --

    @Test
    fun `hasBlockingMatches returns true for any confirmed picked_up or in_transit match`() {
        assertTrue(hasBlockingMatches(listOf(testMatch(1, MatchStatus.CONFIRMED))))
        assertTrue(hasBlockingMatches(listOf(testMatch(1, MatchStatus.PICKED_UP))))
        assertTrue(hasBlockingMatches(listOf(testMatch(1, MatchStatus.IN_TRANSIT))))
    }

    @Test
    fun `hasBlockingMatches returns false when matches are only delivered or other`() {
        assertEquals(false, hasBlockingMatches(emptyList()))
        assertEquals(false, hasBlockingMatches(listOf(testMatch(1, MatchStatus.DELIVERED))))
    }

    @Test
    fun `allPackagesDelivered is true when every match is delivered`() {
        assertTrue(
            allPackagesDelivered(
                listOf(
                    testMatch(1, MatchStatus.DELIVERED),
                    testMatch(2, MatchStatus.DELIVERED),
                ),
            ),
        )
    }

    @Test
    fun `allPackagesDelivered is false when at least one match is not delivered`() {
        assertEquals(
            false,
            allPackagesDelivered(
                listOf(
                    testMatch(1, MatchStatus.DELIVERED),
                    testMatch(2, MatchStatus.PICKED_UP),
                ),
            ),
        )
    }

    @Test
    fun `allPackagesDelivered is false for an empty match list`() {
        // A trip with no matches still allows cancel/edit — iOS-parity.
        assertEquals(false, allPackagesDelivered(emptyList()))
    }

    // -- nextStatusOptions (TripStatusUpdateSheet) --

    @Test
    fun `nextStatusOptions advances planning to active`() {
        assertEquals(
            listOf(com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus.ACTIVE),
            nextStatusOptions(com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus.PLANNING),
        )
    }

    @Test
    fun `nextStatusOptions advances active to in_transit`() {
        assertEquals(
            listOf(com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus.IN_TRANSIT),
            nextStatusOptions(com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus.ACTIVE),
        )
    }

    @Test
    fun `nextStatusOptions advances in_transit to completed`() {
        assertEquals(
            listOf(com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus.COMPLETED),
            nextStatusOptions(com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus.IN_TRANSIT),
        )
    }

    @Test
    fun `nextStatusOptions is empty for terminal statuses`() {
        assertTrue(
            nextStatusOptions(com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus.COMPLETED).isEmpty(),
        )
        assertTrue(
            nextStatusOptions(com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus.CANCELLED).isEmpty(),
        )
    }

    @Test
    fun `nextStatusOptions never offers CANCELLED — that's the cancel button's job`() {
        for (current in com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus.entries) {
            assertTrue(
                nextStatusOptions(current).none { it == com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus.CANCELLED },
            )
        }
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
