package com.efthemiosprime.pasabayan.features.bookings.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.InitiatedBy
import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import com.efthemiosprime.pasabayan.core.domain.model.UserSummary
import com.efthemiosprime.pasabayan.features.notifications.model.NotificationData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CounterOfferContextTest {

    // -- Computed helpers --

    @Test
    fun `lower offer flags computed from prices`() {
        val ctx = context(newPrice = 70.0, originalPrice = 100.0)
        assertTrue(ctx.isLowerOffer)
        assertFalse(ctx.isHigherOffer)
        assertFalse(ctx.isPriceIncrease)
        assertEquals(CounterOfferSummaryKind.LOWER, ctx.summaryKind)
        assertEquals("down", ctx.direction)
        assertEquals(-30.0, ctx.priceDifference, 0.001)
    }

    @Test
    fun `higher offer flags computed from prices`() {
        val ctx = context(newPrice = 120.0, originalPrice = 100.0)
        assertFalse(ctx.isLowerOffer)
        assertTrue(ctx.isHigherOffer)
        assertTrue(ctx.isPriceIncrease)
        assertEquals(CounterOfferSummaryKind.HIGHER, ctx.summaryKind)
        assertEquals("up", ctx.direction)
        assertEquals(20.0, ctx.priceDifference, 0.001)
    }

    @Test
    fun `equal offer reports unchanged`() {
        val ctx = context(newPrice = 100.0, originalPrice = 100.0)
        assertFalse(ctx.isLowerOffer)
        assertFalse(ctx.isHigherOffer)
        assertFalse(ctx.isPriceIncrease)
        assertEquals(CounterOfferSummaryKind.UNCHANGED, ctx.summaryKind)
    }

    @Test
    fun `formatted prices use dollar prefix and two decimals`() {
        val ctx = context(newPrice = 70.0, originalPrice = 85.5)
        assertEquals("$70.00", ctx.formattedNewPrice)
        assertEquals("$85.50", ctx.formattedOriginalPrice)
    }

    @Test
    fun `formatted difference is always absolute`() {
        val lower = context(newPrice = 70.0, originalPrice = 100.0)
        val higher = context(newPrice = 130.0, originalPrice = 100.0)
        assertEquals("$30.00", lower.formattedDifference)
        assertEquals("$30.00", higher.formattedDifference)
    }

    // -- fromMatch --

    @Test
    fun `fromMatch builds context from structured originalPrice`() {
        val match = match(
            isCounterOffer = true,
            agreedPrice = 70.0,
            originalPrice = "100.00",
            initiatedBy = InitiatedBy.CARRIER,
            counterOffererId = 42,
            counterOffererName = "Bob",
            counterOfferRound = 1,
            remainingCounterOffers = 2,
            canCounterOffer = true,
        )

        val ctx = CounterOfferContext.fromMatch(match)
        requireNotNull(ctx)

        assertEquals(70.0, ctx.newPrice, 0.001)
        assertEquals(100.0, ctx.originalPrice, 0.001)
        assertEquals("Bob", ctx.counterOffererName)
        assertEquals(42, ctx.counterOffererId)
        assertEquals(InitiatedBy.CARRIER, ctx.initiatedBy)
        assertEquals(1, ctx.counterOfferRound)
        assertEquals(2, ctx.remainingCounterOffers)
        assertEquals(true, ctx.canCounterOffer)
        assertTrue(ctx.isCounterOffer)
    }

    @Test
    fun `fromMatch falls back to message regex when originalPrice is null`() {
        val match = match(
            isCounterOffer = true,
            agreedPrice = 70.0,
            originalPrice = null,
            shipperMessage = "I would like to counter (was $100.00)",
            initiatedBy = InitiatedBy.SHIPPER,
            shipper = UserSummary(id = 7, name = "Alice"),
        )

        val ctx = CounterOfferContext.fromMatch(match)
        // Regex-only fallback is intentionally rejected when isCounterOffer is true
        // but originalPrice is null — we still need the structured field as a signal.
        assertNull(ctx)
    }

    @Test
    fun `fromMatch returns null when not a counter offer and no original price`() {
        val match = match(
            isCounterOffer = false,
            agreedPrice = 70.0,
            originalPrice = null,
            carrierMessage = "Just a plain message with no price hint",
        )
        assertNull(CounterOfferContext.fromMatch(match))
    }

    @Test
    fun `fromMatch resolves missing counterOffererName from initiatedBy party`() {
        val match = match(
            isCounterOffer = true,
            agreedPrice = 80.0,
            originalPrice = "100.00",
            initiatedBy = InitiatedBy.CARRIER,
            counterOffererId = null,
            counterOffererName = null,
            carrier = UserSummary(id = 99, name = "Carrie"),
            shipper = UserSummary(id = 7, name = "Alice"),
        )

        val ctx = CounterOfferContext.fromMatch(match)
        requireNotNull(ctx)
        assertEquals("Carrie", ctx.counterOffererName)
        assertEquals(99, ctx.counterOffererId)
    }

    @Test
    fun `originalPriceFromMessage parses parenthesized was form`() {
        val match = match(
            isCounterOffer = true,
            agreedPrice = 80.0,
            originalPrice = null,
            shipperMessage = "Counter-offer accepted (was $125.50)",
        )
        assertEquals(125.50, CounterOfferContext.originalPriceFromMessage(match)!!, 0.001)
    }

    @Test
    fun `originalPriceFromMessage parses bare was form`() {
        val match = match(
            isCounterOffer = true,
            agreedPrice = 60.0,
            originalPrice = null,
            carrierMessage = "Going lower — was $90",
        )
        assertEquals(90.0, CounterOfferContext.originalPriceFromMessage(match)!!, 0.001)
    }

    @Test
    fun `originalPriceFromMessage returns null when no hint present`() {
        val match = match(
            isCounterOffer = true,
            agreedPrice = 60.0,
            originalPrice = null,
            carrierMessage = "Nothing about price here",
            shipperMessage = "Same here",
        )
        assertNull(CounterOfferContext.originalPriceFromMessage(match))
    }

    // -- fromNotificationData --

    @Test
    fun `fromNotificationData builds context when prices present`() {
        val data = NotificationData(
            newPrice = 80.0,
            originalPrice = 100.0,
            counterOffererName = "Alice",
            counterOffererId = 5,
            counterOfferRound = 2,
            initiatedBy = "shipper",
            remainingCounterOffers = 1,
            canCounterOffer = true,
        )

        val ctx = CounterOfferContext.fromNotificationData(data)
        requireNotNull(ctx)
        assertEquals(80.0, ctx.newPrice, 0.001)
        assertEquals(100.0, ctx.originalPrice, 0.001)
        assertEquals("Alice", ctx.counterOffererName)
        assertEquals(5, ctx.counterOffererId)
        assertEquals(InitiatedBy.SHIPPER, ctx.initiatedBy)
        assertTrue(ctx.isCounterOffer)
    }

    @Test
    fun `fromNotificationData returns null when newPrice missing`() {
        val data = NotificationData(newPrice = null, originalPrice = 100.0)
        assertNull(CounterOfferContext.fromNotificationData(data))
    }

    @Test
    fun `fromNotificationData returns null when originalPrice missing`() {
        val data = NotificationData(newPrice = 100.0, originalPrice = null)
        assertNull(CounterOfferContext.fromNotificationData(data))
    }

    @Test
    fun `fromNotificationData uses fallback name when payload name is blank`() {
        val data = NotificationData(
            newPrice = 80.0,
            originalPrice = 100.0,
            counterOffererName = "  ",
        )
        val ctx = CounterOfferContext.fromNotificationData(data, fallbackName = "User")
        requireNotNull(ctx)
        assertEquals("User", ctx.counterOffererName)
    }

    @Test
    fun `fromNotificationData defaults to UNKNOWN for unrecognized initiatedBy`() {
        val data = NotificationData(
            newPrice = 80.0,
            originalPrice = 100.0,
            initiatedBy = "bystander",
        )
        val ctx = CounterOfferContext.fromNotificationData(data)
        requireNotNull(ctx)
        assertEquals(InitiatedBy.UNKNOWN, ctx.initiatedBy)
    }

    // -- helpers --

    private fun context(newPrice: Double, originalPrice: Double): CounterOfferContext =
        CounterOfferContext(
            newPrice = newPrice,
            originalPrice = originalPrice,
            counterOffererName = "Alice",
            counterOffererId = 5,
            initiatedBy = InitiatedBy.CARRIER,
            isCounterOffer = true,
        )

    private fun match(
        isCounterOffer: Boolean,
        agreedPrice: Double,
        originalPrice: String?,
        initiatedBy: InitiatedBy = InitiatedBy.UNKNOWN,
        matchStatus: MatchStatus = MatchStatus.CARRIER_REQUESTED,
        counterOffererId: Int? = null,
        counterOffererName: String? = null,
        counterOfferRound: Int? = null,
        remainingCounterOffers: Int = 0,
        canCounterOffer: Boolean = false,
        carrierMessage: String? = null,
        shipperMessage: String? = null,
        carrier: UserSummary? = null,
        shipper: UserSummary? = null,
    ): DeliveryMatch = DeliveryMatch(
        id = 1,
        tripId = null,
        packageRequestId = null,
        matchStatus = matchStatus,
        agreedPrice = agreedPrice,
        initiatedBy = initiatedBy,
        isCounterOffer = isCounterOffer,
        originalPrice = originalPrice,
        canCounterOffer = canCounterOffer,
        remainingCounterOffers = remainingCounterOffers,
        counterOfferRound = counterOfferRound,
        counterOffererId = counterOffererId,
        counterOffererName = counterOffererName,
        carrierMessage = carrierMessage,
        shipperMessage = shipperMessage,
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
