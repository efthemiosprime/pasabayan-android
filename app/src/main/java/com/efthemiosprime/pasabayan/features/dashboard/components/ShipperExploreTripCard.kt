package com.efthemiosprime.pasabayan.features.dashboard.components

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod
import com.efthemiosprime.pasabayan.core.domain.`enum`.TripStatus
import com.efthemiosprime.pasabayan.features.trips.components.TripCard
import com.efthemiosprime.pasabayan.features.trips.model.Trip

/**
 * Shipper Explore-specific trip card wrapper.
 * Keeps parity work scoped to Explore without affecting other trip card surfaces.
 */
@Composable
fun ShipperExploreTripCard(
    trip: Trip,
    onViewDetails: () -> Unit,
    onRequestBook: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TripCard(
        trip = trip,
        onViewDetails = onViewDetails,
        onRequestBook = onRequestBook,
        showDistanceFromUser = true,
        showCompactPriceInCollapsed = true,
        renderSingleMenuActionDirectly = true,
        modifier = modifier,
    )
}

@Preview(showBackground = true, name = "ShipperExploreTripCard - light")
@Preview(
    showBackground = true,
    name = "ShipperExploreTripCard - dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ShipperExploreTripCardPreview() {
    PasabayanTheme {
        ShipperExploreTripCard(
            trip = previewTrip(),
            onViewDetails = {},
            onRequestBook = {},
        )
    }
}

private fun previewTrip() = Trip(
    id = 101,
    carrierId = 5,
    originCity = "Manila",
    originCountry = "Philippines",
    originLat = 14.5995,
    originLng = 120.9842,
    destinationCity = "Cebu",
    destinationCountry = "Philippines",
    destinationLat = 10.3157,
    destinationLng = 123.8854,
    departureDate = "2026-04-01T19:29:00Z",
    arrivalDate = "2026-04-01T21:00:00Z",
    availableWeightKg = 15.0,
    availableSpaceLiters = 50.0,
    pricePerKg = 0.61,
    tripStatus = TripStatus.ACTIVE,
    transportationMethod = TransportationMethod.FLIGHT,
    specialNotes = "Regular PAL flight, reliable schedule. Can handle fragile items with care.",
    carrier = null,
    createdAt = null,
    updatedAt = null,
    pricingType = null,
    pricingMethod = null,
    flatTripPrice = null,
    basePrice = null,
    calculatedPrice = null,
    pickupAddress = "NAIA Terminal 3",
    pickupLandmark = null,
    dropoffAddress = "Mactan-Cebu International Airport",
    dropoffLandmark = null,
    tripEarningsTotal = null,
    tripEarningsCurrency = null,
    tripEarningsBreakdown = null,
    hasPendingRequests = false,
    pendingRequestCount = 0,
    pendingRequests = null,
    distanceKm = 569.0,
)
