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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.CardMenuAction
import com.efthemiosprime.pasabayan.core.designsystem.component.PCardActionFooter
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailRow
import com.efthemiosprime.pasabayan.core.designsystem.component.PExpandableCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PRouteSection
import com.efthemiosprime.pasabayan.core.designsystem.component.PStatusBadge
import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod
import com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus
import com.efthemiosprime.pasabayan.features.trips.model.Trip

@Composable
fun TripCard(
    trip: Trip,
    onViewDetails: () -> Unit,
    modifier: Modifier = Modifier,
    menuActions: List<CardMenuAction> = emptyList(),
) {
    val opacity = if (trip.tripStatus == TripStatus.CANCELLED) 0.5f else 1f
    val statusLabel = tripStatusLabel(trip.tripStatus)
    var expanded by rememberSaveable { mutableStateOf(false) }

    PExpandableCard(
        expanded = expanded,
        onExpandChange = { expanded = it },
        modifier = modifier.alpha(opacity),
        collapsedContent = {
            // Summary view
            Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                // Header: route + transport + status badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${trip.transportationMethod.icon} ${trip.route}",
                        style = PasabayanTextStyles.Heading.h6,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    PStatusBadge(config = TripStatusBadgeConfig(trip.tripStatus, statusLabel))
                }

                // Route section
                PRouteSection(
                    origin = trip.originCity,
                    destination = trip.destinationCity,
                    originAddress = trip.pickupAddress,
                    destinationAddress = trip.dropoffAddress,
                )

                // Schedule
                if (trip.formattedDepartureDate.isNotEmpty()) {
                    PDetailRow(
                        label = stringResource(R.string.trips_detail_departure),
                        value = trip.formattedDepartureDate,
                    )
                }

                // Capacity & price
                PDetailRow(
                    label = stringResource(R.string.trips_detail_capacity),
                    value = trip.formattedCapacity,
                )

                // Footer — "View Details" expands the card
                PCardActionFooter(
                    onViewDetails = { expanded = true },
                    menuActions = menuActions,
                )
            }
        },
        expandedContent = {
            // Full detail view inside the expanded card
            Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${trip.transportationMethod.icon} ${trip.route}",
                        style = PasabayanTextStyles.Heading.h4,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    PStatusBadge(config = TripStatusBadgeConfig(trip.tripStatus, statusLabel))
                }

                // Route
                PRouteSection(
                    origin = trip.originCity,
                    destination = trip.destinationCity,
                    originAddress = trip.pickupAddress,
                    destinationAddress = trip.dropoffAddress,
                    originLandmark = trip.pickupLandmark,
                    destinationLandmark = trip.dropoffLandmark,
                )

                // Schedule
                if (trip.formattedDepartureDate.isNotEmpty()) {
                    PDetailRow(
                        label = stringResource(R.string.trips_detail_departure),
                        value = trip.formattedDepartureDate,
                    )
                }
                if (trip.formattedArrivalDate.isNotEmpty()) {
                    PDetailRow(
                        label = stringResource(R.string.trips_detail_arrival),
                        value = trip.formattedArrivalDate,
                    )
                }

                // Capacity & pricing
                PDetailRow(
                    label = stringResource(R.string.trips_detail_capacity),
                    value = trip.formattedCapacity,
                )
                PDetailRow(
                    label = stringResource(R.string.trips_detail_price),
                    value = trip.formattedPrice,
                )

                // Pending requests
                val pendingCount = trip.pendingRequestCount ?: 0
                if (pendingCount > 0) {
                    PDetailRow(
                        label = stringResource(R.string.trips_detail_pending_requests, pendingCount),
                        value = "",
                    )
                }

                // Special notes
                trip.specialNotes?.let { notes ->
                    Text(
                        text = stringResource(R.string.trips_detail_notes),
                        style = PasabayanTextStyles.Heading.h6,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = notes,
                        style = PasabayanTextStyles.Body.regular,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
    )
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

@Preview(showBackground = true, name = "TripCard — light")
@Preview(showBackground = true, name = "TripCard — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TripCardPreview() {
    PasabayanTheme {
        TripCard(
            trip = previewTrip(),
            onViewDetails = {},
            modifier = Modifier.padding(PasabayanSpacing.lg),
        )
    }
}

private fun previewTrip(status: TripStatus = TripStatus.ACTIVE) = Trip(
    id = 1, carrierId = 42,
    originCity = "Toronto", originCountry = "Canada",
    originLat = 43.65, originLng = -79.38,
    destinationCity = "Vancouver", destinationCountry = "Canada",
    destinationLat = 49.28, destinationLng = -123.12,
    departureDate = "2026-04-01T08:00:00Z",
    arrivalDate = "2026-04-01T14:00:00Z",
    availableWeightKg = 25.0, availableSpaceLiters = 50.0,
    pricePerKg = 15.0, tripStatus = status,
    transportationMethod = TransportationMethod.FLIGHT,
    specialNotes = "Handle with care", carrier = null,
    createdAt = null, updatedAt = null,
    pricingType = null, pricingMethod = null,
    flatTripPrice = null, basePrice = null, calculatedPrice = null,
    pickupAddress = "123 Main St", pickupLandmark = null,
    dropoffAddress = "456 Oak Ave", dropoffLandmark = null,
    tripEarningsTotal = null, tripEarningsCurrency = null,
    tripEarningsBreakdown = null,
    hasPendingRequests = true, pendingRequestCount = 2,
    pendingRequests = null, distanceKm = 3365.0,
)
