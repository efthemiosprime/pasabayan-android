package com.efthemiosprime.pasabayan.features.bookings.model

/** Available actions for a match — determined by role + status. */
sealed interface BookingAction {
    data object AcceptBooking : BookingAction
    data object CounterOffer : BookingAction
    data object DeclineBooking : BookingAction
    data object MarkPickedUp : BookingAction
    data object MarkInTransit : BookingAction
    data object EnterDeliveryCode : BookingAction
    data object EnterPickupCode : BookingAction
    data object MarkDelivered : BookingAction
    data object CancelBooking : BookingAction
    data object TrackLive : BookingAction

    /**
     * Shipper confirms a `MatchStatus.PENDING` match via the auto-charge
     * sheet. Opens [com.efthemiosprime.pasabayan.features.bookings.ui.AutoChargeConfirmationSheet]
     * which drives `PUT /matches/{id}/confirm` and the add-payment-method
     * fallback. iOS parity: `ShipperMatchDetailsView`'s "Confirm Match" CTA.
     */
    data object ConfirmMatch : BookingAction
}
