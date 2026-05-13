package com.efthemiosprime.pasabayan.features.trips.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus

/**
 * Carrier TripCard action visibility rules. iOS parity:
 * `TripCard.swift:15-19` (`CarrierTripEditEntryPolicy`) and `293-365` (`buildMenuActions`).
 *
 * Pure policy — no Compose, no Hilt — so it is unit-testable in JVM tests.
 */
object CarrierTripActionPolicy {

    /** Full editing only makes sense before publishing; later flows use Update Status. */
    fun shouldOfferEditTrip(status: TripStatus): Boolean =
        status == TripStatus.PLANNING

    fun shouldOfferActivateTrip(status: TripStatus): Boolean =
        status == TripStatus.PLANNING

    fun shouldOfferUpdateStatus(status: TripStatus): Boolean =
        status != TripStatus.PLANNING && status != TripStatus.COMPLETED && status != TripStatus.CANCELLED

    fun shouldOfferCancelTrip(status: TripStatus): Boolean =
        status != TripStatus.COMPLETED && status != TripStatus.CANCELLED
}
