package com.efthemiosprime.pasabayan.features.bookings.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PCircularProgress
import com.efthemiosprime.pasabayan.core.designsystem.component.PRouteSection
import com.efthemiosprime.pasabayan.core.designsystem.component.PScaffold
import com.efthemiosprime.pasabayan.core.designsystem.component.PTopBar
import com.efthemiosprime.pasabayan.core.domain.model.UserSummary
import com.efthemiosprime.pasabayan.features.bookings.components.CompatibleTripCard
import com.efthemiosprime.pasabayan.features.bookings.model.CompatibleTrip
import com.efthemiosprime.pasabayan.features.bookings.model.CompatibleTripsOfferedPriceSource
import com.efthemiosprime.pasabayan.features.bookings.model.CompatibleTripsPricingPolicy
import com.efthemiosprime.pasabayan.features.bookings.viewmodel.CompatibleTripsUiState
import com.efthemiosprime.pasabayan.features.bookings.viewmodel.CompatibleTripsViewModel
import com.efthemiosprime.pasabayan.features.packages.model.PackageRequest

/**
 * Shipper-side screen showing carrier trips compatible with a package request.
 * iOS parity: `Features/Bookings/Views/Shipper/CompatibleTripsView.swift`.
 *
 * Backed by `GET /packages/{packageRequestId}/compatible-trips`. The request
 * action emits a [TripRequestIntent] for the host to dispatch — keeping the
 * screen lean. Manual price entry, existing-request gating, and counter-offer
 * prompts (iOS lines 100-340) are deferred to a follow-up slice.
 */
@Composable
fun CompatibleTripsScreen(
    pkg: PackageRequest,
    onClose: () -> Unit,
    onRequestTrip: (TripRequestIntent) -> Unit,
    modifier: Modifier = Modifier,
    onViewCarrier: ((UserSummary) -> Unit)? = null,
    carrierIdFilter: Int? = null,
    viewModel: CompatibleTripsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(pkg.id, carrierIdFilter) {
        viewModel.loadCompatibleTrips(packageRequestId = pkg.id, carrierId = carrierIdFilter)
    }
    CompatibleTripsScreenContent(
        pkg = pkg,
        state = state,
        onClose = onClose,
        onRetry = viewModel::refresh,
        onRequestTrip = onRequestTrip,
        onViewCarrier = onViewCarrier,
        modifier = modifier,
    )
}

/** Payload emitted when the shipper taps "Request Carrier". */
data class TripRequestIntent(
    val trip: CompatibleTrip,
    val offeredPrice: Double,
    val priceSource: CompatibleTripsOfferedPriceSource,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CompatibleTripsScreenContent(
    pkg: PackageRequest,
    state: CompatibleTripsUiState,
    onClose: () -> Unit,
    onRetry: () -> Unit,
    onRequestTrip: (TripRequestIntent) -> Unit,
    modifier: Modifier = Modifier,
    onViewCarrier: ((UserSummary) -> Unit)? = null,
) {
    PScaffold(
        modifier = modifier,
        topBar = {
            PTopBar(
                title = stringResource(R.string.bookings_compatible_trips_title),
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.bookings_compatible_trips_close),
                        )
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.isLoading && state.trips.isEmpty() -> LoadingState(padding)
            state.errorMessage != null && state.trips.isEmpty() ->
                ErrorState(message = state.errorMessage, onRetry = onRetry, padding = padding)
            state.trips.isEmpty() && state.hasLoaded ->
                EmptyState(pkg = pkg, padding = padding)
            else -> ContentList(
                pkg = pkg,
                trips = state.trips,
                onRequestTrip = onRequestTrip,
                onViewCarrier = onViewCarrier,
                padding = padding,
            )
        }
    }
}

@Composable
private fun ContentList(
    pkg: PackageRequest,
    trips: List<CompatibleTrip>,
    onRequestTrip: (TripRequestIntent) -> Unit,
    onViewCarrier: ((UserSummary) -> Unit)?,
    padding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(PasabayanSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.lg),
    ) {
        PackageSummaryHeader(pkg = pkg)
        trips.forEach { trip ->
            val resolved = CompatibleTripsPricingPolicy.resolveOfferedPrice(
                tripPricePerKg = trip.pricePerKg ?: "",
                packageWeightKg = pkg.packageWeightKg,
                packageMaxBudget = pkg.maxPriceBudget,
                flatTripPrice = if (trip.usesFlatPricing) trip.effectiveFlatPrice else null,
            )
            CompatibleTripCard(
                trip = trip,
                offeredPrice = resolved.first.takeIf {
                    resolved.second != CompatibleTripsOfferedPriceSource.UNAVAILABLE
                },
                onRequest = {
                    if (resolved.second != CompatibleTripsOfferedPriceSource.UNAVAILABLE) {
                        onRequestTrip(
                            TripRequestIntent(
                                trip = trip,
                                offeredPrice = resolved.first,
                                priceSource = resolved.second,
                            ),
                        )
                    }
                },
                onViewCarrier = trip.carrier?.let { c -> onViewCarrier?.let { handler -> { handler(c) } } },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PackageSummaryHeader(pkg: PackageRequest) {
    PCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            Text(
                text = stringResource(R.string.bookings_compatible_trips_subtitle),
                style = PasabayanTextStyles.Caption.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (pkg.pickupCity?.isNotBlank() == true && pkg.deliveryCity?.isNotBlank() == true) {
                PRouteSection(
                    origin = pkg.pickupCity ?: "",
                    destination = pkg.deliveryCity ?: "",
                )
            }
        }
    }
}

@Composable
private fun LoadingState(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
        ) {
            PCircularProgress()
            Text(
                text = stringResource(R.string.bookings_compatible_trips_finding),
                style = PasabayanTextStyles.Body.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    padding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(PasabayanSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(60.dp),
        )
        Text(
            text = stringResource(R.string.bookings_compatible_trips_error_title),
            style = PasabayanTextStyles.Heading.h5,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = message,
            style = PasabayanTextStyles.Body.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        PButton(
            text = stringResource(R.string.bookings_compatible_trips_try_again),
            onClick = onRetry,
        )
    }
}

@Composable
private fun EmptyState(pkg: PackageRequest, padding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(PasabayanSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PackageSummaryHeader(pkg = pkg)
        Icon(
            imageVector = Icons.Default.FlightTakeoff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(72.dp),
        )
        Text(
            text = stringResource(R.string.bookings_compatible_trips_empty_title),
            style = PasabayanTextStyles.Heading.h5,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.bookings_compatible_trips_empty_description),
            style = PasabayanTextStyles.Body.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

// -- Previews -----------------------------------------------------------------

private fun previewPackage(): PackageRequest = PackageRequest(
    id = 42, shipperId = 1,
    pickupAddress = null, pickupCity = "Manila", pickupCountry = "PH",
    deliveryAddress = null, deliveryCity = "Cebu", deliveryCountry = "PH",
    packageWeightKg = 5.0, packageDimensions = null, packageType = null,
    fragile = false, packageValue = null,
    packageDescription = "Small package",
    urgencyLevel = null, maxPriceBudget = 100.0,
    pickupDatePreferred = null, pickupTimePreferred = null,
    pickupDateFlexible = null, deliveryDateNeeded = null,
    deliveryTimeNeeded = null, specialHandlingRequirements = null,
    requestStatus = null, createdAt = null, updatedAt = null,
    compatibleTripsCount = null, shipper = null, images = null,
    imagesProcessing = null, serviceType = null, shoppingList = null,
    storeName = null, storeAddress = null, receiptRequired = null,
)

private fun previewTrips(): List<CompatibleTrip> = listOf(
    CompatibleTrip(
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
        requestMessage = null, requestedAt = null, distanceKm = 12.4,
    ),
)

@Preview(showBackground = true, name = "CompatibleTrips — light", heightDp = 1000)
@Preview(showBackground = true, name = "CompatibleTrips — dark", heightDp = 1000, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CompatibleTripsScreenPreview() {
    PasabayanTheme {
        CompatibleTripsScreenContent(
            pkg = previewPackage(),
            state = CompatibleTripsUiState(
                packageRequestId = 42,
                trips = previewTrips(),
                hasLoaded = true,
            ),
            onClose = {}, onRetry = {}, onRequestTrip = {},
        )
    }
}

@Preview(showBackground = true, name = "CompatibleTrips — empty", heightDp = 1000)
@Composable
private fun CompatibleTripsScreenEmptyPreview() {
    PasabayanTheme {
        CompatibleTripsScreenContent(
            pkg = previewPackage(),
            state = CompatibleTripsUiState(packageRequestId = 42, hasLoaded = true),
            onClose = {}, onRetry = {}, onRequestTrip = {},
        )
    }
}

@Preview(showBackground = true, name = "CompatibleTrips — loading")
@Composable
private fun CompatibleTripsScreenLoadingPreview() {
    PasabayanTheme {
        CompatibleTripsScreenContent(
            pkg = previewPackage(),
            state = CompatibleTripsUiState(packageRequestId = 42, isLoading = true),
            onClose = {}, onRetry = {}, onRequestTrip = {},
        )
    }
}

@Preview(showBackground = true, name = "CompatibleTrips — error")
@Composable
private fun CompatibleTripsScreenErrorPreview() {
    PasabayanTheme {
        CompatibleTripsScreenContent(
            pkg = previewPackage(),
            state = CompatibleTripsUiState(
                packageRequestId = 42,
                errorMessage = "Network unavailable",
                hasLoaded = true,
            ),
            onClose = {}, onRetry = {}, onRequestTrip = {},
        )
    }
}
