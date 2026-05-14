package com.efthemiosprime.pasabayan.features.bookings.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.features.bookings.model.BookingAction

/**
 * Localized label for a [BookingAction]. Mirrors iOS `BookingActions.label(for:)`.
 * Extracted to a shared helper because three composable surfaces (MatchCard,
 * ShipperMatchDetailsSheet, BookingActions row) need the same mapping.
 */
@Composable
@ReadOnlyComposable
fun bookingActionLabel(action: BookingAction): String = when (action) {
    BookingAction.AcceptBooking -> stringResource(R.string.bookings_action_accept)
    BookingAction.DeclineBooking -> stringResource(R.string.bookings_action_decline)
    BookingAction.CounterOffer -> stringResource(R.string.bookings_action_counter_offer)
    BookingAction.CancelBooking -> stringResource(R.string.bookings_action_cancel)
    BookingAction.MarkPickedUp -> stringResource(R.string.bookings_action_mark_picked_up)
    BookingAction.MarkInTransit -> stringResource(R.string.bookings_action_mark_in_transit)
    BookingAction.MarkDelivered -> stringResource(R.string.bookings_action_mark_delivered)
    BookingAction.TrackLive -> stringResource(R.string.bookings_action_track_live)
    BookingAction.EnterPickupCode -> stringResource(R.string.bookings_action_generate_code)
    BookingAction.EnterDeliveryCode -> stringResource(R.string.bookings_action_enter_code)
    BookingAction.ConfirmMatch -> stringResource(R.string.bookings_action_confirm_match)
}

/**
 * Visual style for a [BookingAction] button. Mirrors iOS `BookingActions.style(for:)`.
 * Pure value function (not composable) — unit-testable in JVM.
 *
 * - Destructive: decline, cancel.
 * - Secondary: counter-offer, navigation/utility (track live, enter codes).
 * - Primary: everything else (accept, confirm, status transitions).
 */
fun bookingActionStyle(action: BookingAction): PButtonStyle = when (action) {
    BookingAction.DeclineBooking,
    BookingAction.CancelBooking -> PButtonStyle.Destructive
    BookingAction.CounterOffer,
    BookingAction.TrackLive,
    BookingAction.EnterPickupCode,
    BookingAction.EnterDeliveryCode -> PButtonStyle.Secondary
    BookingAction.AcceptBooking,
    BookingAction.MarkPickedUp,
    BookingAction.MarkInTransit,
    BookingAction.MarkDelivered,
    BookingAction.ConfirmMatch -> PButtonStyle.Primary
}
