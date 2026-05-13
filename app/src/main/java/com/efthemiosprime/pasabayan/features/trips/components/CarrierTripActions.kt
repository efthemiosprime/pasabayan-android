package com.efthemiosprime.pasabayan.features.trips.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.component.CardMenuAction
import com.efthemiosprime.pasabayan.features.trips.model.CarrierTripActionPolicy
import com.efthemiosprime.pasabayan.features.trips.model.Trip

/**
 * Builds the carrier "My Trips" TripCard overflow menu actions for [trip], gated
 * by [CarrierTripActionPolicy]. iOS parity: `TripCard.swift:293-365`.
 *
 * Callers supply the side-effect callbacks; this helper only handles policy +
 * localized labels.
 *
 * Note: `Find Packages` is intentionally omitted — the matching shipper flow
 * (`CompatiblePackagesForTripView` on iOS) is not yet implemented on Android.
 */
@Composable
fun carrierTripMenuActions(
    trip: Trip,
    onEditTrip: () -> Unit,
    onActivateTrip: () -> Unit,
    onUpdateStatus: () -> Unit,
    onCancelTrip: () -> Unit,
): List<CardMenuAction> {
    val editLabel = stringResource(R.string.trips_edit_trip)
    val activateLabel = stringResource(R.string.trips_action_activate_trip)
    val updateStatusLabel = stringResource(R.string.trips_action_update_status)
    val cancelLabel = stringResource(R.string.trips_cancel_trip)

    return buildList {
        if (CarrierTripActionPolicy.shouldOfferEditTrip(trip.tripStatus)) {
            add(CardMenuAction(title = editLabel, onClick = onEditTrip))
        }
        if (CarrierTripActionPolicy.shouldOfferActivateTrip(trip.tripStatus)) {
            add(CardMenuAction(title = activateLabel, onClick = onActivateTrip))
        }
        if (CarrierTripActionPolicy.shouldOfferUpdateStatus(trip.tripStatus)) {
            add(CardMenuAction(title = updateStatusLabel, onClick = onUpdateStatus))
        }
        if (CarrierTripActionPolicy.shouldOfferCancelTrip(trip.tripStatus)) {
            add(CardMenuAction(title = cancelLabel, onClick = onCancelTrip))
        }
    }
}
