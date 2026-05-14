package com.efthemiosprime.pasabayan.features.bookings.components

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonSize
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailRow
import com.efthemiosprime.pasabayan.core.designsystem.component.PDivider
import com.efthemiosprime.pasabayan.core.designsystem.component.PRouteSection
import com.efthemiosprime.pasabayan.core.domain.model.UserSummary
import com.efthemiosprime.pasabayan.features.bookings.model.CompatibleTrip
import com.efthemiosprime.pasabayan.features.dashboard.components.UserCardHeader

/**
 * Shipper-side card on the "compatible trips for package" flow.
 *
 * Stateless by construction: takes the trip, the offered price the shipper
 * would send to this carrier, and `onRequest` / `onViewCarrier` callbacks.
 * The host screen owns the loading state and the actual request orchestration
 * (existing-request gating, manual price entry, counter-offer prompt).
 */
@Composable
fun CompatibleTripCard(
    trip: CompatibleTrip,
    offeredPrice: Double?,
    onRequest: () -> Unit,
    modifier: Modifier = Modifier,
    onViewCarrier: (() -> Unit)? = null,
) {
    PCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
            CarrierHeader(trip = trip, onViewCarrier = onViewCarrier)

            PRouteSection(
                origin = trip.originCity,
                destination = trip.destinationCity,
            )

            trip.availableWeightKgDouble?.let { weight ->
                PDetailRow(
                    label = stringResource(R.string.bookings_compatible_trips_weight_kg, weight),
                    value = pricingValue(trip),
                )
            }

            trip.distanceKm?.let { km ->
                PDetailRow(
                    label = stringResource(R.string.bookings_compatible_trips_distance_km, km),
                    value = trip.transportationMethodEnum.icon,
                )
            }

            offeredPrice?.let { price ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.bookings_compatible_trips_offered_price),
                        style = PasabayanTextStyles.Body.regular,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(R.string.bookings_compatible_trips_price_cad, price),
                        style = PasabayanTextStyles.Heading.h5,
                        fontWeight = FontWeight.Bold,
                        color = PasabayanColors.Success,
                    )
                }
            }

            PDivider()

            if (trip.hasActiveRequest) {
                PButton(
                    text = stringResource(R.string.bookings_compatible_trips_request_pending),
                    onClick = onRequest,
                    style = PButtonStyle.Secondary,
                    size = PButtonSize.Medium,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                PButton(
                    text = stringResource(R.string.bookings_compatible_trips_request_to_book),
                    onClick = onRequest,
                    size = PButtonSize.Medium,
                    enabled = trip.canRequestTrip,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun CarrierHeader(trip: CompatibleTrip, onViewCarrier: (() -> Unit)?) {
    val carrier = trip.carrier
    if (carrier != null) {
        UserCardHeader(user = carrier, onClick = onViewCarrier)
    } else {
        Text(
            text = trip.route(),
            style = PasabayanTextStyles.Heading.h6,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun pricingValue(trip: CompatibleTrip): String {
    val flat = trip.effectiveFlatPrice
    if (trip.usesFlatPricing && flat != null) {
        return stringResource(R.string.bookings_compatible_trips_price_cad, flat)
    }
    val perKg = trip.pricePerKgDouble
    if (perKg != null && perKg > 0.0) {
        return stringResource(R.string.bookings_compatible_trips_per_kg_rate, perKg)
    }
    return stringResource(R.string.bookings_compatible_trips_no_pricing)
}

private fun CompatibleTrip.route(): String = "$originCity → $destinationCity"

// -- Previews -----------------------------------------------------------------

@Preview(showBackground = true, name = "CompatibleTripCard — light")
@Preview(showBackground = true, name = "CompatibleTripCard — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CompatibleTripCardPreview() {
    PasabayanTheme {
        CompatibleTripCard(
            trip = previewTrip(),
            offeredPrice = 27.50,
            onRequest = {},
            onViewCarrier = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(PasabayanSpacing.lg),
        )
    }
}

@Preview(showBackground = true, name = "CompatibleTripCard — pending")
@Composable
private fun CompatibleTripCardPendingPreview() {
    PasabayanTheme {
        CompatibleTripCard(
            trip = previewTrip().copy(shipperRequestStatus = "shipper_requested"),
            offeredPrice = 27.50,
            onRequest = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(PasabayanSpacing.lg),
        )
    }
}

@Preview(showBackground = true, name = "CompatibleTripCard — no carrier")
@Composable
private fun CompatibleTripCardNoCarrierPreview() {
    PasabayanTheme {
        CompatibleTripCard(
            trip = previewTrip().copy(carrier = null),
            offeredPrice = null,
            onRequest = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(PasabayanSpacing.lg),
        )
    }
}

private fun previewTrip(): CompatibleTrip = CompatibleTrip(
    id = 1, carrierId = 7,
    originCity = "Manila", originCountry = "PH",
    destinationCity = "Cebu", destinationCountry = "PH",
    departureDate = "2026-05-25", arrivalDate = "2026-05-26",
    pickupDate = null, deliveryDate = null,
    availableWeightKg = "20.0", availableSpaceLiters = "100.0",
    pricePerKg = "5.50", flatTripPrice = null, calculatedPrice = null,
    pricingType = "per_kg", pricingMethod = null,
    tripStatus = "active", transportationMethod = "flight",
    specialNotes = null, createdAt = null, updatedAt = null,
    carrier = UserSummary(id = 7, name = "Carla Reyes", rating = "4.8", totalRatings = 24, avatar = null),
    shipperRequestStatus = null, canRequest = true,
    requestMessage = null, requestedAt = null,
    distanceKm = 12.4,
)
