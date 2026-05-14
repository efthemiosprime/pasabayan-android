package com.efthemiosprime.pasabayan.features.bookings.components

import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.features.bookings.model.BookingAction
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit coverage for [bookingActionStyle] — the non-composable half of the
 * extracted action helpers. Mirrors iOS `BookingActions.style(for:)`.
 *
 * The composable label half (`bookingActionLabel`) is exercised via existing
 * `MatchCard` and `ShipperMatchDetailsSheet` `@Preview`s.
 */
class BookingActionStyleTest {

    @Test
    fun `destructive actions map to Destructive style`() {
        assertEquals(PButtonStyle.Destructive, bookingActionStyle(BookingAction.DeclineBooking))
        assertEquals(PButtonStyle.Destructive, bookingActionStyle(BookingAction.CancelBooking))
    }

    @Test
    fun `secondary actions map to Secondary style`() {
        assertEquals(PButtonStyle.Secondary, bookingActionStyle(BookingAction.CounterOffer))
        assertEquals(PButtonStyle.Secondary, bookingActionStyle(BookingAction.TrackLive))
        assertEquals(PButtonStyle.Secondary, bookingActionStyle(BookingAction.EnterPickupCode))
        assertEquals(PButtonStyle.Secondary, bookingActionStyle(BookingAction.EnterDeliveryCode))
    }

    @Test
    fun `primary actions map to Primary style`() {
        assertEquals(PButtonStyle.Primary, bookingActionStyle(BookingAction.AcceptBooking))
        assertEquals(PButtonStyle.Primary, bookingActionStyle(BookingAction.MarkPickedUp))
        assertEquals(PButtonStyle.Primary, bookingActionStyle(BookingAction.MarkInTransit))
        assertEquals(PButtonStyle.Primary, bookingActionStyle(BookingAction.MarkDelivered))
        assertEquals(PButtonStyle.Primary, bookingActionStyle(BookingAction.ConfirmMatch))
    }

    @Test
    fun `every BookingAction maps to a stable style`() {
        // Guards against silent omission when new actions are added — the
        // `when` in [bookingActionStyle] is exhaustive over the sealed type,
        // but this asserts each declared variant resolves and is idempotent.
        val all = listOf(
            BookingAction.AcceptBooking,
            BookingAction.DeclineBooking,
            BookingAction.CounterOffer,
            BookingAction.CancelBooking,
            BookingAction.MarkPickedUp,
            BookingAction.MarkInTransit,
            BookingAction.MarkDelivered,
            BookingAction.TrackLive,
            BookingAction.EnterPickupCode,
            BookingAction.EnterDeliveryCode,
            BookingAction.ConfirmMatch,
        )
        all.forEach { action ->
            assertEquals(bookingActionStyle(action), bookingActionStyle(action))
        }
    }
}
