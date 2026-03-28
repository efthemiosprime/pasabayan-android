package com.efthemiosprime.pasabayan.features.trips.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailRow
import com.efthemiosprime.pasabayan.core.designsystem.component.PRouteSection
import com.efthemiosprime.pasabayan.core.designsystem.component.PStatusBadge
import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod
import com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus
import com.efthemiosprime.pasabayan.features.trips.components.TripStatusBadgeConfig
import com.efthemiosprime.pasabayan.features.trips.model.Trip

@Composable
fun TripDetailsScreen(
    trip: Trip,
    isCarrier: Boolean,
    onEdit: () -> Unit,
    onCancel: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val statusLabel = tripDetailStatusLabel(trip.tripStatus)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(PasabayanSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
    ) {
        // Header
        PStatusBadge(config = TripStatusBadgeConfig(trip.tripStatus, statusLabel))

        Text(
            text = "${trip.transportationMethod.icon} ${trip.route}",
            style = PasabayanTextStyles.Heading.h4,
            color = MaterialTheme.colorScheme.onSurface,
        )

        // Route
        PCard {
            Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                Text(
                    text = stringResource(R.string.trips_detail_route),
                    style = PasabayanTextStyles.Heading.h6,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                PRouteSection(
                    origin = trip.originCity,
                    destination = trip.destinationCity,
                    originAddress = trip.pickupAddress,
                    destinationAddress = trip.dropoffAddress,
                    originLandmark = trip.pickupLandmark,
                    destinationLandmark = trip.dropoffLandmark,
                )
            }
        }

        // Schedule
        PCard {
            Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                Text(
                    text = stringResource(R.string.trips_detail_schedule),
                    style = PasabayanTextStyles.Heading.h6,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                PDetailRow(
                    label = stringResource(R.string.trips_detail_departure),
                    value = trip.formattedDepartureDate,
                )
                PDetailRow(
                    label = stringResource(R.string.trips_detail_arrival),
                    value = trip.formattedArrivalDate,
                )
            }
        }

        // Capacity & pricing
        PCard {
            Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                Text(
                    text = stringResource(R.string.trips_detail_capacity),
                    style = PasabayanTextStyles.Heading.h6,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                PDetailRow(
                    label = stringResource(R.string.trips_detail_weight),
                    value = trip.formattedCapacity,
                )
                PDetailRow(
                    label = stringResource(R.string.trips_detail_price),
                    value = trip.formattedPrice,
                )
            }
        }

        // Special notes
        trip.specialNotes?.let { notes ->
            PCard {
                Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
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
        }

        // Actions (carrier only)
        if (isCarrier && trip.tripStatus != TripStatus.CANCELLED && trip.tripStatus != TripStatus.COMPLETED) {
            PButton(
                text = stringResource(R.string.trips_edit_trip),
                onClick = onEdit,
                modifier = Modifier.fillMaxWidth(),
            )
            PButton(
                text = stringResource(R.string.trips_cancel_trip),
                onClick = onCancel,
                style = PButtonStyle.Destructive,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun tripDetailStatusLabel(status: TripStatus): String = when (status) {
    TripStatus.PLANNING -> stringResource(R.string.trips_status_planning)
    TripStatus.ACTIVE -> stringResource(R.string.trips_status_active)
    TripStatus.IN_TRANSIT -> stringResource(R.string.trips_status_in_transit)
    TripStatus.COMPLETED -> stringResource(R.string.trips_status_completed)
    TripStatus.CANCELLED -> stringResource(R.string.trips_status_cancelled)
}

@Preview(showBackground = true, name = "TripDetails — light", heightDp = 900)
@Preview(showBackground = true, name = "TripDetails — dark", heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TripDetailsPreview() {
    PasabayanTheme {
        TripDetailsScreen(
            trip = Trip(
                id = 1, carrierId = 42,
                originCity = "Toronto", originCountry = "Canada",
                originLat = 43.65, originLng = -79.38,
                destinationCity = "Vancouver", destinationCountry = "Canada",
                destinationLat = 49.28, destinationLng = -123.12,
                departureDate = "2026-04-01T08:00:00Z",
                arrivalDate = "2026-04-01T14:00:00Z",
                availableWeightKg = 25.0, availableSpaceLiters = 50.0,
                pricePerKg = 15.0, tripStatus = TripStatus.ACTIVE,
                transportationMethod = TransportationMethod.FLIGHT,
                specialNotes = "Handle with care — fragile electronics",
                carrier = null, createdAt = null, updatedAt = null,
                pricingType = null, pricingMethod = null,
                flatTripPrice = null, basePrice = null, calculatedPrice = null,
                pickupAddress = "123 Main St", pickupLandmark = "Near Central Station",
                dropoffAddress = "456 Oak Ave", dropoffLandmark = null,
                tripEarningsTotal = null, tripEarningsCurrency = null,
                tripEarningsBreakdown = null,
                hasPendingRequests = null, pendingRequestCount = null,
                pendingRequests = null, distanceKm = 3365.0,
            ),
            isCarrier = true,
            onEdit = {},
            onCancel = {},
            onBack = {},
        )
    }
}
