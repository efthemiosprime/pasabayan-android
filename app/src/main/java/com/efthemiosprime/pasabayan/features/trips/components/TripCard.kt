package com.efthemiosprime.pasabayan.features.trips.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.CardMenuAction
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PCardActionFooter
import com.efthemiosprime.pasabayan.core.designsystem.component.PCardVariant
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailRow
import com.efthemiosprime.pasabayan.core.designsystem.component.PDivider
import com.efthemiosprime.pasabayan.core.designsystem.component.PRouteSection
import com.efthemiosprime.pasabayan.core.designsystem.component.PStatusBadge
import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod
import com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus
import com.efthemiosprime.pasabayan.features.dashboard.components.UserCardHeader
import com.efthemiosprime.pasabayan.features.trips.model.Trip

@Composable
fun TripCard(
    trip: Trip,
    onViewDetails: () -> Unit,
    onRequestBook: (() -> Unit)? = null,
    showDistanceFromUser: Boolean = false,
    showCarrierHeader: Boolean = true,
    /**
     * When true and the trip is past planning/cancelled, embeds
     * [TripCardProgressSection]. iOS gate: `TripCard.swift:284-291`.
     */
    showPackageProgress: Boolean = false,
    renderSingleMenuActionDirectly: Boolean = false,
    /** When non-null, the carrier-header row is tappable and invokes this. iOS opens UserProfilePopover. */
    onOpenCarrierProfile: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    menuActions: List<CardMenuAction> = emptyList(),
) {
    val opacity = if (trip.tripStatus == TripStatus.CANCELLED) 0.6f else 1f
    val statusLabel = tripStatusLabel(trip.tripStatus)
    val cardMenuActions = if (trip.isBookable && onRequestBook != null) {
        menuActions + CardMenuAction(
            title = stringResource(R.string.trips_action_request_book),
            onClick = onRequestBook,
        )
    } else {
        menuActions
    }
    val directTrailingAction = if (renderSingleMenuActionDirectly && cardMenuActions.size == 1) {
        cardMenuActions.first()
    } else {
        null
    }
    val overflowActions = if (directTrailingAction != null) emptyList() else cardMenuActions

    PCard(
        modifier = modifier.alpha(opacity),
        variant = PCardVariant.Secondary,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            // Carrier header — only when the viewer is not the trip owner.
            if (showCarrierHeader) {
                trip.carrier?.let { carrier ->
                    UserCardHeader(user = carrier, onClick = onOpenCarrierProfile)
                    PDivider()
                }
            }

            // Header: route + transport icon + status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${trip.transportationMethod.icon} ${trip.route}",
                    style = PasabayanTextStyles.Heading.h5,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                PStatusBadge(config = TripStatusBadgeConfig(trip.tripStatus, statusLabel))
            }

            // Package progress — iOS TripCard.swift:92 packageProgressSection.
            if (showPackageProgress && trip.shouldShowPackageProgress) {
                TripCardProgressSection(
                    tripId = trip.id,
                    arrivalDateText = trip.formattedArrivalDateShort,
                    onTap = onViewDetails,
                    onDeliveredHistoryTap = onViewDetails,
                )
            }

            // Route addresses
            PRouteSection(
                origin = trip.originCity,
                destination = trip.destinationCity,
                originAddress = trip.pickupAddress,
                destinationAddress = trip.dropoffAddress,
                originLandmark = trip.pickupLandmark,
                destinationLandmark = trip.dropoffLandmark,
            )

            if (showDistanceFromUser && trip.routeDistanceKm > 0) {
                Text(
                    text = stringResource(R.string.trips_card_distance_from_you, trip.routeDistanceKm),
                    style = PasabayanTextStyles.Caption.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Schedule (Pickup vs Delivery, or Departure vs Duration) — iOS TripCard.swift:97-115.
            ScheduleColumns(trip)

            PDivider()

            // Capacity + Price — iOS TripCard.swift:120-142, price tinted accent.
            CapacityAndPriceRow(trip)

            // Pending requests (carrier-owner context)
            val pendingCount = trip.pendingRequestCount ?: 0
            if (pendingCount > 0) {
                PDetailRow(
                    label = stringResource(R.string.trips_detail_pending_requests, pendingCount),
                    value = "",
                )
            }

            // Special notes
            trip.specialNotes?.takeIf { it.isNotBlank() }?.let { notes ->
                Text(
                    text = notes,
                    style = PasabayanTextStyles.Caption.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            PCardActionFooter(
                onViewDetails = onViewDetails,
                menuActions = overflowActions,
                directTrailingAction = directTrailingAction,
            )
        }
    }
}

@Composable
private fun ScheduleColumns(trip: Trip) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
            Text(
                text = stringResource(trip.tripCardLeftLabelRes),
                style = PasabayanTextStyles.Caption.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = trip.tripCardLeftValue,
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
        ) {
            Text(
                text = stringResource(trip.tripCardRightLabelRes),
                style = PasabayanTextStyles.Caption.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = trip.tripCardRightValue,
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun CapacityAndPriceRow(trip: Trip) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
            Text(
                text = stringResource(R.string.trips_detail_available_capacity),
                style = PasabayanTextStyles.Caption.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = trip.formattedCapacity,
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
        ) {
            Text(
                text = stringResource(R.string.trips_detail_price),
                style = PasabayanTextStyles.Caption.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = trip.formattedPrice,
                style = PasabayanTextStyles.Body.small,
                color = PasabayanColors.BadgeTeal,
            )
        }
    }
}

@Composable
private fun tripStatusLabel(status: TripStatus): String = when (status) {
    TripStatus.PLANNING -> stringResource(R.string.trips_status_planning)
    TripStatus.ACTIVE -> stringResource(R.string.trips_status_active)
    TripStatus.IN_TRANSIT -> stringResource(R.string.trips_status_in_transit)
    TripStatus.COMPLETED -> stringResource(R.string.trips_status_completed)
    TripStatus.CANCELLED -> stringResource(R.string.trips_status_cancelled)
}

// -- Previews --

@Preview(showBackground = true, name = "TripCard — carrier, light")
@Preview(showBackground = true, name = "TripCard — carrier, dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TripCardCarrierPreview() {
    PasabayanTheme {
        TripCard(
            trip = previewTrip(),
            onViewDetails = {},
            showCarrierHeader = false,
            modifier = Modifier.padding(PasabayanSpacing.lg),
        )
    }
}

@Preview(showBackground = true, name = "TripCard — shipper browse, light")
@Preview(showBackground = true, name = "TripCard — shipper browse, dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TripCardShipperPreview() {
    PasabayanTheme {
        TripCard(
            trip = previewTrip(),
            onViewDetails = {},
            onRequestBook = {},
            showDistanceFromUser = true,
            modifier = Modifier.padding(PasabayanSpacing.lg),
        )
    }
}

private fun previewTrip(status: TripStatus = TripStatus.ACTIVE) = Trip(
    id = 1, carrierId = 42,
    originCity = "Montreal", originCountry = "Canada",
    originLat = 45.50, originLng = -73.57,
    destinationCity = "Toronto", destinationCountry = "Canada",
    destinationLat = 43.65, destinationLng = -79.38,
    departureDate = "2026-02-27T13:40:00Z",
    arrivalDate = "2026-03-01T13:40:00Z",
    availableWeightKg = 25.0, availableSpaceLiters = 50.0,
    pricePerKg = 3.0, tripStatus = status,
    transportationMethod = TransportationMethod.CAR,
    specialNotes = "Test trip for in_transit status", carrier = null,
    createdAt = null, updatedAt = null,
    pricingType = null, pricingMethod = null,
    flatTripPrice = null, basePrice = null, calculatedPrice = null,
    pickupAddress = null, pickupLandmark = null,
    dropoffAddress = null, dropoffLandmark = null,
    tripEarningsTotal = null, tripEarningsCurrency = null,
    tripEarningsBreakdown = null,
    hasPendingRequests = true, pendingRequestCount = 2,
    pendingRequests = null, distanceKm = 540.0,
)
