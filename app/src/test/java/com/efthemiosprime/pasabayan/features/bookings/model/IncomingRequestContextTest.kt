package com.efthemiosprime.pasabayan.features.bookings.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.InitiatedBy
import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import com.efthemiosprime.pasabayan.core.domain.model.UserSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IncomingRequestContextTest {

    // -- from(match, isCarrier) --

    @Test
    fun `from carrier-side returns context for SHIPPER_REQUESTED matches`() {
        val match = match(id = 7, status = MatchStatus.SHIPPER_REQUESTED, shipper = shipper("Alice"))
        val ctx = IncomingRequestContext.from(match, isCarrier = true)
        assertEquals(IncomingRequestContext(matchId = 7, requesterName = "Alice"), ctx)
    }

    @Test
    fun `from shipper-side returns context for CARRIER_REQUESTED matches`() {
        val match = match(id = 8, status = MatchStatus.CARRIER_REQUESTED, carrier = carrier("Bob"))
        val ctx = IncomingRequestContext.from(match, isCarrier = false)
        assertEquals(IncomingRequestContext(matchId = 8, requesterName = "Bob"), ctx)
    }

    @Test
    fun `from rejects matches whose status is not the role's incoming bucket`() {
        // Carrier-side ignores CARRIER_REQUESTED (carrier is the requester, not the receiver).
        val carrierSideWrong = match(id = 1, status = MatchStatus.CARRIER_REQUESTED, shipper = shipper("Alice"))
        assertNull(IncomingRequestContext.from(carrierSideWrong, isCarrier = true))

        // Shipper-side ignores SHIPPER_REQUESTED.
        val shipperSideWrong = match(id = 2, status = MatchStatus.SHIPPER_REQUESTED, carrier = carrier("Bob"))
        assertNull(IncomingRequestContext.from(shipperSideWrong, isCarrier = false))

        // Any other status is ignored regardless of role.
        listOf(
            MatchStatus.PENDING,
            MatchStatus.CONFIRMED,
            MatchStatus.PICKED_UP,
            MatchStatus.IN_TRANSIT,
            MatchStatus.DELIVERED,
            MatchStatus.CANCELLED,
            MatchStatus.SHIPPER_DECLINED,
            MatchStatus.CARRIER_DECLINED,
        ).forEach { status ->
            val m = match(id = 99, status = status, shipper = shipper("S"), carrier = carrier("C"))
            assertNull("status=$status carrier", IncomingRequestContext.from(m, isCarrier = true))
            assertNull("status=$status shipper", IncomingRequestContext.from(m, isCarrier = false))
        }
    }

    @Test
    fun `from rejects counter-offers regardless of role or status`() {
        val carrierCo = match(
            id = 3,
            status = MatchStatus.SHIPPER_REQUESTED,
            shipper = shipper("Alice"),
            isCounterOffer = true,
        )
        assertNull(IncomingRequestContext.from(carrierCo, isCarrier = true))

        val shipperCo = match(
            id = 4,
            status = MatchStatus.CARRIER_REQUESTED,
            carrier = carrier("Bob"),
            isCounterOffer = true,
        )
        assertNull(IncomingRequestContext.from(shipperCo, isCarrier = false))
    }

    @Test
    fun `from returns null when the requester party is missing on the match`() {
        // Carrier-side needs shipper.name; if shipper is null we can't render a useful snackbar.
        val carrierNoShipper = match(id = 5, status = MatchStatus.SHIPPER_REQUESTED, shipper = null)
        assertNull(IncomingRequestContext.from(carrierNoShipper, isCarrier = true))

        val shipperNoCarrier = match(id = 6, status = MatchStatus.CARRIER_REQUESTED, carrier = null)
        assertNull(IncomingRequestContext.from(shipperNoCarrier, isCarrier = false))
    }

    // -- incomingRequestSnackbarItems(matches, isCarrier, reviewedIds) --

    @Test
    fun `incomingRequestSnackbarItems caps the result at 3 items`() {
        val matches = (1..5).map {
            match(id = it, status = MatchStatus.SHIPPER_REQUESTED, shipper = shipper("S$it"))
        }
        val items = IncomingRequestContext.incomingRequestSnackbarItems(
            matches = matches,
            isCarrier = true,
            reviewedIds = emptySet(),
        )
        assertEquals(3, items.size)
        // Preserves input order (matches order is repository-controlled — usually newest first).
        assertEquals(listOf(1, 2, 3), items.map { it.matchId })
    }

    @Test
    fun `incomingRequestSnackbarItems filters out reviewed ids`() {
        val matches = (1..4).map {
            match(id = it, status = MatchStatus.SHIPPER_REQUESTED, shipper = shipper("S$it"))
        }
        val items = IncomingRequestContext.incomingRequestSnackbarItems(
            matches = matches,
            isCarrier = true,
            reviewedIds = setOf(1, 3),
        )
        assertEquals(listOf(2, 4), items.map { it.matchId })
    }

    @Test
    fun `incomingRequestSnackbarItems isolates roles and excludes counter-offers`() {
        val matches = listOf(
            match(id = 1, status = MatchStatus.SHIPPER_REQUESTED, shipper = shipper("S")),
            match(id = 2, status = MatchStatus.CARRIER_REQUESTED, carrier = carrier("C")),
            match(id = 3, status = MatchStatus.SHIPPER_REQUESTED, shipper = shipper("S2"), isCounterOffer = true),
            match(id = 4, status = MatchStatus.CONFIRMED, shipper = shipper("S3"), carrier = carrier("C2")),
        )
        val carrierItems = IncomingRequestContext.incomingRequestSnackbarItems(matches, isCarrier = true, reviewedIds = emptySet())
        assertEquals(listOf(1), carrierItems.map { it.matchId })

        val shipperItems = IncomingRequestContext.incomingRequestSnackbarItems(matches, isCarrier = false, reviewedIds = emptySet())
        assertEquals(listOf(2), shipperItems.map { it.matchId })
    }

    @Test
    fun `incomingRequestSnackbarItems returns empty when no matches qualify`() {
        val matches = listOf(match(id = 10, status = MatchStatus.CONFIRMED, shipper = shipper("S"), carrier = carrier("C")))
        assertTrue(IncomingRequestContext.incomingRequestSnackbarItems(matches, true, emptySet()).isEmpty())
        assertTrue(IncomingRequestContext.incomingRequestSnackbarItems(matches, false, emptySet()).isEmpty())
        assertTrue(IncomingRequestContext.incomingRequestSnackbarItems(emptyList(), true, emptySet()).isEmpty())
    }

    // -- dismissedIdsAfterReview(matches, isCarrier) --
    // Returns the set of match ids that are still valid candidates for "incoming request" for
    // this role. The VM intersects its `reviewedIncomingRequestIds` with this set after every
    // match-list update to garbage-collect stale entries (e.g. a request the user accepted
    // moves out of SHIPPER_REQUESTED and should drop out of the reviewed set).

    @Test
    fun `dismissedIdsAfterReview returns ids that are currently incoming for the role`() {
        val matches = listOf(
            match(id = 1, status = MatchStatus.SHIPPER_REQUESTED, shipper = shipper("S")),
            match(id = 2, status = MatchStatus.CARRIER_REQUESTED, carrier = carrier("C")),
            match(id = 3, status = MatchStatus.SHIPPER_REQUESTED, shipper = shipper("S"), isCounterOffer = true),
            match(id = 4, status = MatchStatus.CONFIRMED, shipper = shipper("S"), carrier = carrier("C")),
        )
        assertEquals(setOf(1), IncomingRequestContext.dismissedIdsAfterReview(matches, isCarrier = true))
        assertEquals(setOf(2), IncomingRequestContext.dismissedIdsAfterReview(matches, isCarrier = false))
    }

    @Test
    fun `dismissedIdsAfterReview returns empty when no matches qualify`() {
        assertTrue(IncomingRequestContext.dismissedIdsAfterReview(emptyList(), isCarrier = true).isEmpty())
        val notIncoming = listOf(match(id = 1, status = MatchStatus.CONFIRMED, shipper = shipper("S"), carrier = carrier("C")))
        assertTrue(IncomingRequestContext.dismissedIdsAfterReview(notIncoming, isCarrier = true).isEmpty())
    }

    // -- unseenIncomingRequestBadgeCount(matches, isCarrier, reviewedIds) --

    @Test
    fun `unseenIncomingRequestBadgeCount counts every pending incoming request not in reviewedIds`() {
        // 5 pending shipper-requested + 2 already reviewed → badge = 3 (not capped at MAX_VISIBLE_ITEMS).
        val matches = (1..5).map {
            match(id = it, status = MatchStatus.SHIPPER_REQUESTED, shipper = shipper("S$it"))
        }
        val count = IncomingRequestContext.unseenIncomingRequestBadgeCount(
            matches = matches,
            isCarrier = true,
            reviewedIds = setOf(1, 2),
        )
        assertEquals(3, count)
    }

    @Test
    fun `unseenIncomingRequestBadgeCount excludes counter-offers and wrong-status matches`() {
        val matches = listOf(
            match(id = 1, status = MatchStatus.SHIPPER_REQUESTED, shipper = shipper("S")),
            match(id = 2, status = MatchStatus.SHIPPER_REQUESTED, shipper = shipper("S"), isCounterOffer = true),
            match(id = 3, status = MatchStatus.CONFIRMED, shipper = shipper("S"), carrier = carrier("C")),
            match(id = 4, status = MatchStatus.CARRIER_REQUESTED, carrier = carrier("C")), // wrong role
        )
        assertEquals(1, IncomingRequestContext.unseenIncomingRequestBadgeCount(matches, isCarrier = true, reviewedIds = emptySet()))
    }

    @Test
    fun `unseenIncomingRequestBadgeCount is zero for empty inputs`() {
        assertEquals(0, IncomingRequestContext.unseenIncomingRequestBadgeCount(emptyList(), true, emptySet()))
    }

    // -- helpers --

    private fun shipper(name: String): UserSummary = UserSummary(id = 100, name = name)
    private fun carrier(name: String): UserSummary = UserSummary(id = 200, name = name)

    private fun match(
        id: Int,
        status: MatchStatus,
        shipper: UserSummary? = null,
        carrier: UserSummary? = null,
        isCounterOffer: Boolean = false,
    ): DeliveryMatch = DeliveryMatch(
        id = id,
        tripId = null,
        packageRequestId = null,
        matchStatus = status,
        agreedPrice = 100.0,
        initiatedBy = InitiatedBy.UNKNOWN,
        isCounterOffer = isCounterOffer,
        originalPrice = null,
        canCounterOffer = false,
        remainingCounterOffers = 0,
        counterOfferRound = null,
        carrierMessage = null,
        shipperMessage = null,
        carrier = carrier,
        shipper = shipper,
        chatConversationId = null,
        confirmedAt = null,
        pickedUpAt = null,
        deliveredAt = null,
        createdAt = null,
        updatedAt = null,
        platformFeePercent = null,
        transactionStatus = null,
        transaction = null,
        receiptPhoto = null,
        autoCancelAfterDays = null,
        pickupConfirmationCode = null,
        codeExpiresAt = null,
        deliveryVerificationCode = null,
        deliveryCodeExpiresAt = null,
    )
}
