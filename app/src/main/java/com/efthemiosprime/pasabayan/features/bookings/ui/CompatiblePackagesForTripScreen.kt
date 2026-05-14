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
import androidx.compose.material.icons.filled.Inventory2
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
import com.efthemiosprime.pasabayan.features.bookings.components.CompatiblePackageCard
import com.efthemiosprime.pasabayan.features.bookings.model.nested.CarrierTripInfo
import com.efthemiosprime.pasabayan.features.bookings.viewmodel.CompatiblePackagesUiState
import com.efthemiosprime.pasabayan.features.bookings.viewmodel.CompatiblePackagesViewModel
import com.efthemiosprime.pasabayan.features.packages.model.PackageRequest

/**
 * Carrier-side screen showing packages compatible with a specific trip.
 * iOS parity: `Features/Bookings/Views/Carrier/CompatiblePackagesForTripView.swift`.
 *
 * Backed by `GET /trips/{tripId}/compatible-packages`. The estimated earnings
 * per package are computed by the caller's trip pricing rules and surfaced on
 * each card; this screen does not own pricing logic.
 */
@Composable
fun CompatiblePackagesForTripScreen(
    trip: CarrierTripInfo,
    onClose: () -> Unit,
    onRequestToCarry: (PackageRequest) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CompatiblePackagesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(trip.id) { viewModel.loadCompatiblePackages(trip.id) }
    CompatiblePackagesForTripScreenContent(
        trip = trip,
        state = state,
        onClose = onClose,
        onRetry = viewModel::refresh,
        onRequestToCarry = onRequestToCarry,
        modifier = modifier,
    )
}

/**
 * Stateless layer for previews and tests. The screen estimates carrier
 * earnings by `weight × pricePerKg` for per-kg trips, `flatTripPrice` for
 * flat-pricing trips, and `null` otherwise. Mirrors iOS
 * `trip.estimatedPrice(forWeight:)` fallback chain.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CompatiblePackagesForTripScreenContent(
    trip: CarrierTripInfo,
    state: CompatiblePackagesUiState,
    onClose: () -> Unit,
    onRetry: () -> Unit,
    onRequestToCarry: (PackageRequest) -> Unit,
    modifier: Modifier = Modifier,
) {
    PScaffold(
        modifier = modifier,
        topBar = {
            PTopBar(
                title = stringResource(R.string.bookings_compatible_packages_title),
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.bookings_compatible_packages_close),
                        )
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.isLoading && state.packages.isEmpty() -> LoadingState(padding)
            state.errorMessage != null && state.packages.isEmpty() ->
                ErrorState(message = state.errorMessage, onRetry = onRetry, padding = padding)
            state.packages.isEmpty() && state.hasLoaded ->
                EmptyState(trip = trip, padding = padding)
            else -> ContentList(
                trip = trip,
                packages = state.packages,
                onRequestToCarry = onRequestToCarry,
                padding = padding,
            )
        }
    }
}

@Composable
private fun ContentList(
    trip: CarrierTripInfo,
    packages: List<PackageRequest>,
    onRequestToCarry: (PackageRequest) -> Unit,
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
        TripSummaryHeader(trip = trip)
        packages.forEach { pkg ->
            CompatiblePackageCard(
                pkg = pkg,
                estimatedEarnings = estimateEarnings(trip = trip, weightKg = pkg.packageWeightKg),
                onRequestToCarry = { onRequestToCarry(pkg) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun TripSummaryHeader(trip: CarrierTripInfo) {
    PCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            PRouteSection(
                origin = trip.originCity,
                destination = trip.destinationCity,
            )
            trip.availableWeightKg?.let { weight ->
                Text(
                    text = stringResource(R.string.bookings_compatible_packages_weight_kg, weight),
                    style = PasabayanTextStyles.Caption.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                text = stringResource(R.string.bookings_compatible_packages_finding),
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
            text = stringResource(R.string.bookings_compatible_packages_error_title),
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
            text = stringResource(R.string.bookings_compatible_packages_try_again),
            onClick = onRetry,
        )
    }
}

@Composable
private fun EmptyState(trip: CarrierTripInfo, padding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(PasabayanSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TripSummaryHeader(trip = trip)
        Icon(
            imageVector = Icons.Default.Inventory2,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(72.dp),
        )
        Text(
            text = stringResource(R.string.bookings_compatible_packages_empty_title),
            style = PasabayanTextStyles.Heading.h5,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.bookings_compatible_packages_empty_description),
            style = PasabayanTextStyles.Body.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Mirrors iOS `Trip.estimatedPrice(forWeight:)` — flat price for land-transport
 * trips (or trips with `pricing_type=flat`), per-kg × weight otherwise. Returns
 * `null` when neither input is meaningful, so the card hides the earnings row.
 */
internal fun estimateEarnings(trip: CarrierTripInfo, weightKg: Double?): Double? {
    val pricingType = trip.pricingType
    if (pricingType == "flat") return trip.flatTripPrice?.takeIf { it > 0 }
    if (pricingType == "per_kg" || pricingType == null) {
        val price = trip.pricePerKg ?: return trip.flatTripPrice?.takeIf { it > 0 }
        val weight = weightKg ?: return null
        if (price <= 0.0 || weight <= 0.0) return null
        return price * weight
    }
    return null
}

// -- Previews -----------------------------------------------------------------

private fun previewTrip(): CarrierTripInfo = CarrierTripInfo(
    id = 7,
    originCity = "Manila",
    destinationCity = "Cebu",
    departureDate = "2026-05-25",
    arrivalDate = "2026-05-26",
    transportationMethod = "flight",
    availableWeightKg = 20.0,
    pricePerKg = 7.50,
    flatTripPrice = null,
    pricingType = "per_kg",
)

private fun previewPackages(): List<PackageRequest> = listOf(
    PackageRequest(
        id = 642, shipperId = 5,
        pickupAddress = null, pickupCity = "Manila", pickupCountry = "PH",
        deliveryAddress = null, deliveryCity = "Cebu", deliveryCountry = "PH",
        packageWeightKg = 5.0, packageDimensions = null, packageType = null,
        fragile = false, packageValue = null,
        packageDescription = "Flat-screen TV in original packaging",
        urgencyLevel = null, maxPriceBudget = 200.0,
        pickupDatePreferred = "2026-05-24", pickupTimePreferred = null,
        pickupDateFlexible = true, deliveryDateNeeded = null,
        deliveryTimeNeeded = null, specialHandlingRequirements = null,
        requestStatus = null, createdAt = null, updatedAt = null,
        compatibleTripsCount = null, shipper = null, images = null,
        imagesProcessing = null, serviceType = null, shoppingList = null,
        storeName = null, storeAddress = null, receiptRequired = null,
    ),
)

@Preview(showBackground = true, name = "CompatiblePackages — light", heightDp = 1000)
@Preview(showBackground = true, name = "CompatiblePackages — dark", heightDp = 1000, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CompatiblePackagesForTripScreenPreview() {
    PasabayanTheme {
        CompatiblePackagesForTripScreenContent(
            trip = previewTrip(),
            state = CompatiblePackagesUiState(
                tripId = 7,
                packages = previewPackages(),
                hasLoaded = true,
            ),
            onClose = {},
            onRetry = {},
            onRequestToCarry = {},
        )
    }
}

@Preview(showBackground = true, name = "CompatiblePackages — empty", heightDp = 1000)
@Composable
private fun CompatiblePackagesForTripScreenEmptyPreview() {
    PasabayanTheme {
        CompatiblePackagesForTripScreenContent(
            trip = previewTrip(),
            state = CompatiblePackagesUiState(tripId = 7, hasLoaded = true),
            onClose = {},
            onRetry = {},
            onRequestToCarry = {},
        )
    }
}

@Preview(showBackground = true, name = "CompatiblePackages — loading")
@Composable
private fun CompatiblePackagesForTripScreenLoadingPreview() {
    PasabayanTheme {
        CompatiblePackagesForTripScreenContent(
            trip = previewTrip(),
            state = CompatiblePackagesUiState(tripId = 7, isLoading = true),
            onClose = {},
            onRetry = {},
            onRequestToCarry = {},
        )
    }
}

@Preview(showBackground = true, name = "CompatiblePackages — error")
@Composable
private fun CompatiblePackagesForTripScreenErrorPreview() {
    PasabayanTheme {
        CompatiblePackagesForTripScreenContent(
            trip = previewTrip(),
            state = CompatiblePackagesUiState(
                tripId = 7,
                errorMessage = "Network unavailable",
                hasLoaded = true,
            ),
            onClose = {},
            onRetry = {},
            onRequestToCarry = {},
        )
    }
}
