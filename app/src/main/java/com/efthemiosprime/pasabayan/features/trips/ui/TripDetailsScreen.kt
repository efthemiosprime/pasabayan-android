package com.efthemiosprime.pasabayan.features.trips.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailSectionTitle
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailSheetCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailSheetScaffold
import com.efthemiosprime.pasabayan.core.designsystem.component.PDivider
import com.efthemiosprime.pasabayan.core.domain.`enum`.PricingType
import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod
import com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus
import com.efthemiosprime.pasabayan.features.trips.model.Trip

@Composable
fun TripDetailsScreen(
    trip: Trip,
    isCarrier: Boolean,
    onEdit: () -> Unit,
    onCancel: () -> Unit,
    onBack: () -> Unit,
    onRequestBook: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val statusLabel = tripDetailStatusLabel(trip.tripStatus)
    PDetailSheetScaffold(
        title = stringResource(R.string.trips_detail_title),
        closeContentDescription = stringResource(R.string.trips_detail_close),
        onClose = onBack,
        modifier = modifier,
    ) {

        // Header card
        PDetailSheetCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                Text(
                    text = trip.route,
                    style = PasabayanTextStyles.Heading.h3,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
                    StatusChip(label = statusLabel, status = trip.tripStatus)
                    TransportChip(label = trip.transportationMethodLabel())
                }
            }
        }

        // Route information card
        PDetailSheetCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
                PDetailSectionTitle(text = stringResource(R.string.trips_detail_route_information))
                LabeledIconRow(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = PasabayanColors.Info,
                            modifier = Modifier.size(24.dp),
                        )
                    },
                    label = stringResource(R.string.trips_detail_start_location),
                    value = "${trip.originCity}, ${trip.originCountry}",
                    caption = listOfNotNull(trip.pickupAddress, trip.pickupInstructions)
                        .filter { it.isNotBlank() }
                        .joinToString(separator = " • ")
                        .ifBlank { null },
                )
                PDivider()
                LabeledIconRow(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = PasabayanColors.Info,
                            modifier = Modifier.size(24.dp),
                        )
                    },
                    label = stringResource(R.string.trips_detail_end_location),
                    value = "${trip.destinationCity}, ${trip.destinationCountry}",
                    caption = listOfNotNull(trip.dropoffAddress, trip.dropoffInstructions)
                        .filter { it.isNotBlank() }
                        .joinToString(separator = " • ")
                        .ifBlank { null },
                )
            }
        }

        // Schedule card
        PDetailSheetCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
                PDetailSectionTitle(text = stringResource(R.string.trips_detail_schedule))
                LabeledIconRow(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            tint = PasabayanColors.Info,
                            modifier = Modifier.size(24.dp),
                        )
                    },
                    label = stringResource(R.string.trips_detail_departure),
                    value = trip.formattedDepartureDate,
                )
                LabeledIconRow(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            tint = PasabayanColors.Info,
                            modifier = Modifier.size(24.dp),
                        )
                    },
                    label = stringResource(R.string.trips_detail_arrival),
                    value = trip.formattedArrivalDate,
                )
            }
        }

        // Capacity card
        PDetailSheetCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
                PDetailSectionTitle(text = stringResource(R.string.trips_detail_available_capacity))
                LabeledIconRow(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Scale,
                            contentDescription = null,
                            tint = PasabayanColors.Info,
                            modifier = Modifier.size(24.dp),
                        )
                    },
                    label = stringResource(R.string.trips_detail_weight),
                    value = trip.formattedCapacity,
                )
            }
        }

        // Pricing card
        PDetailSheetCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
                PDetailSectionTitle(text = stringResource(R.string.trips_detail_pricing_title))
                LabeledIconRow(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.AttachMoney,
                            contentDescription = null,
                            tint = PasabayanColors.Success,
                            modifier = Modifier.size(26.dp),
                        )
                    },
                    label = stringResource(
                        if (trip.effectivePricingType == PricingType.PER_KG) {
                            R.string.trips_detail_price_per_kg_label
                        } else {
                            R.string.trips_detail_flat_rate_label
                        },
                    ),
                    value = trip.formattedPrice,
                    valueColor = PasabayanColors.Success,
                )
            }
        }

        // Special notes
        trip.specialNotes?.let { notes ->
            PDetailSheetCard(modifier = Modifier.fillMaxWidth()) {
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

        // Shipper booking section parity with iOS TripDetailsView.
        if (!isCarrier) {
            if (trip.isBookable) {
                PButton(
                    text = stringResource(R.string.trips_detail_request_to_book),
                    onClick = { onRequestBook?.invoke() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = onRequestBook != null,
                )
            } else {
                bookingUnavailableMessage(trip)?.let { message ->
                    Text(
                        text = message,
                        style = PasabayanTextStyles.Body.small,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
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
private fun LabeledIconRow(
    icon: @Composable () -> Unit,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    caption: String? = null,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        icon()
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value.ifBlank { "-" },
                style = PasabayanTextStyles.Body.medium,
                color = valueColor,
                fontWeight = FontWeight.SemiBold,
            )
            if (!caption.isNullOrBlank()) {
                Text(
                    text = caption,
                    style = PasabayanTextStyles.Body.small,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TransportChip(label: String) {
    HeaderChip(
        text = label,
        textColor = MaterialTheme.colorScheme.onSurfaceVariant,
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
    )
}

@Composable
private fun StatusChip(label: String, status: TripStatus) {
    val (textColor, backgroundColor) = when (status) {
        TripStatus.ACTIVE -> PasabayanColors.StatusActive to PasabayanColors.BadgeBlueLight
        TripStatus.COMPLETED -> PasabayanColors.StatusCompleted to PasabayanColors.BadgeGreenLight
        TripStatus.IN_TRANSIT -> PasabayanColors.StatusInTransit to PasabayanColors.BadgePurpleLight
        TripStatus.CANCELLED -> PasabayanColors.StatusCancelled to PasabayanColors.BadgeRedLight
        TripStatus.PLANNING -> PasabayanColors.StatusPending to PasabayanColors.BadgeOrangeLight
    }
    HeaderChip(
        text = label,
        textColor = textColor,
        backgroundColor = backgroundColor,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(12.dp),
            )
        },
    )
}

@Composable
private fun HeaderChip(
    text: String,
    textColor: Color,
    backgroundColor: Color,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(20.dp),
            )
            .padding(horizontal = PasabayanSpacing.md, vertical = 6.dp),
    ) {
        leadingIcon?.invoke()
        Text(
            text = text,
            style = PasabayanTextStyles.Caption.large,
            color = textColor,
        )
    }
}

@Composable
private fun Trip.transportationMethodLabel(): String = when (transportationMethod) {
    TransportationMethod.NONE -> stringResource(R.string.trips_transport_other)
    TransportationMethod.FLIGHT -> stringResource(R.string.trips_transport_flight)
    TransportationMethod.BUS -> stringResource(R.string.trips_transport_bus)
    TransportationMethod.CAR -> stringResource(R.string.trips_transport_car)
    TransportationMethod.TRUCK -> stringResource(R.string.trips_transport_truck)
    TransportationMethod.VAN -> stringResource(R.string.trips_transport_van)
    TransportationMethod.MOTORCYCLE -> stringResource(R.string.trips_transport_motorcycle)
    TransportationMethod.SHIP -> stringResource(R.string.trips_transport_ship)
    TransportationMethod.TRAIN -> stringResource(R.string.trips_transport_train)
    TransportationMethod.OTHER -> stringResource(R.string.trips_transport_other)
}

@Composable
private fun bookingUnavailableMessage(trip: Trip): String? {
    if (!trip.hasCapacity) {
        return stringResource(R.string.trips_detail_unavailable_fully_booked)
    }
    return when (trip.tripStatus) {
        TripStatus.IN_TRANSIT -> stringResource(R.string.trips_detail_unavailable_in_transit)
        TripStatus.COMPLETED -> stringResource(R.string.trips_detail_unavailable_completed)
        TripStatus.CANCELLED -> stringResource(R.string.trips_detail_unavailable_cancelled)
        TripStatus.PLANNING, TripStatus.ACTIVE -> null
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
