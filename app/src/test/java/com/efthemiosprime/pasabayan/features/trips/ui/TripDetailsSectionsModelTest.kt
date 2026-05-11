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

    // -- pickupCodeState / deliveryCodeState (iOS-parity) --

    private val noFormat: (String) -> String? = { null }

    @Test
    fun `pickupCodeState verified when pickedUpAt is present`() {
        val match = testMatch(1, MatchStatus.PICKED_UP)
            .copy(pickedUpAt = "2026-04-01T10:00:00Z")
        val state = pickupCodeState(match) { "Apr 1, 2026" }
        assertTrue(state is CodeState.Verified)
        assertEquals("Apr 1, 2026", (state as CodeState.Verified).dateText)
    }

    @Test
    fun `pickupCodeState verified without date when status implies it but timestamp missing`() {
        for (status in listOf(MatchStatus.PICKED_UP, MatchStatus.IN_TRANSIT, MatchStatus.DELIVERED)) {
            val state = pickupCodeState(testMatch(1, status), noFormat)
            assertTrue("expected Verified for $status, got $state", state is CodeState.Verified)
            assertEquals("date should be null for $status", null, (state as CodeState.Verified).dateText)
        }
    }

    @Test
    fun `pickupCodeState requested for confirmed and pending`() {
        for (status in listOf(MatchStatus.CONFIRMED, MatchStatus.PENDING)) {
            val state = pickupCodeState(testMatch(1, status), noFormat)
            assertEquals("expected Requested for $status", CodeState.Requested, state)
        }
    }

    @Test
    fun `pickupCodeState ignores blank pickedUpAt and falls back to status`() {
        val state = pickupCodeState(
            testMatch(1, MatchStatus.CONFIRMED).copy(pickedUpAt = "   "),
            noFormat,
        )
        assertEquals(CodeState.Requested, state)
    }

    @Test
    fun `deliveryCodeState verified when deliveredAt is present`() {
        val match = testMatch(1, MatchStatus.DELIVERED)
            .copy(deliveredAt = "2026-04-03T16:00:00Z")
        val state = deliveryCodeState(match) { "Apr 3, 2026" }
        assertTrue(state is CodeState.Verified)
        assertEquals("Apr 3, 2026", (state as CodeState.Verified).dateText)
    }

    @Test
    fun `deliveryCodeState verified without date when status is DELIVERED`() {
        val state = deliveryCodeState(testMatch(1, MatchStatus.DELIVERED), noFormat)
        assertTrue(state is CodeState.Verified)
        assertEquals(null, (state as CodeState.Verified).dateText)
    }

    @Test
    fun `deliveryCodeState requested while in transit`() {
        // iOS distinguishes pickup-verified vs delivery-still-requested during IN_TRANSIT.
        assertEquals(
            CodeState.Requested,
            deliveryCodeState(testMatch(1, MatchStatus.IN_TRANSIT), noFormat),
        )
    }

    @Test
    fun `deliveryCodeState requested for picked_up — pickup verified but delivery not yet`() {
        assertEquals(
            CodeState.Requested,
            deliveryCodeState(testMatch(1, MatchStatus.PICKED_UP), noFormat),
        )
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
