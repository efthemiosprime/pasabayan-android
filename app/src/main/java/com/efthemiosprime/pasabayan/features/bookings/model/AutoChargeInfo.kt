package com.efthemiosprime.pasabayan.features.bookings.model

/**
 * Domain mirror of the envelope-level `auto_charge` block returned by
 * `PUT /matches/{id}/confirm`. Matches iOS `AutoChargeInfo`.
 *
 * - [queued] — backend successfully queued the charge against the shipper's
 *   default payment method.
 * - [shipperHasDefaultPaymentMethod] — false when the shipper still needs
 *   to add a card; the UI surfaces "Add Payment Method" instead of "Confirm
 *   & Pay" and reattempts via `POST /payments/matches/{id}/auto-charge`
 *   after the card is saved.
 */
data class AutoChargeInfo(
    val queued: Boolean,
    val shipperHasDefaultPaymentMethod: Boolean,
)
