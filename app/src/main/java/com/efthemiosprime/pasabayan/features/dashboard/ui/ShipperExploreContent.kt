package com.efthemiosprime.pasabayan.features.dashboard.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanRadius
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PCircularProgress
import com.efthemiosprime.pasabayan.core.designsystem.component.PDivider
import com.efthemiosprime.pasabayan.core.designsystem.component.PEmptyState
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField
import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole
import com.efthemiosprime.pasabayan.core.session.AuthUser
import com.efthemiosprime.pasabayan.features.bookings.ui.ShipperMatchCreationSheet
import com.efthemiosprime.pasabayan.features.dashboard.components.ShipperExploreTripCard
import com.efthemiosprime.pasabayan.features.dashboard.components.UserHeaderCard
import com.efthemiosprime.pasabayan.features.packages.viewmodel.PackageViewModel
import com.efthemiosprime.pasabayan.features.trips.ui.TripDetailsScreen
import com.efthemiosprime.pasabayan.features.trips.model.Trip
import com.efthemiosprime.pasabayan.features.trips.viewmodel.BrowseTripsViewModel

/**
 * Shipper Explore tab — browse available trips / carriers.
 * Parity with iOS `ShipperHomeContent.swift` → `ShipperDashboardView`.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ShipperExploreContent(
    user: AuthUser,
    onSwitchRole: () -> Unit,
    modifier: Modifier = Modifier,
    onViewTripDetails: (tripId: Int) -> Unit = {},
    browseTripsViewModel: BrowseTripsViewModel = hiltViewModel(),
    packageViewModel: PackageViewModel = hiltViewModel(),
) {
    val state by browseTripsViewModel.uiState.collectAsStateWithLifecycle()
    val packageState by packageViewModel.uiState.collectAsStateWithLifecycle()
    var detailTrip by remember { mutableStateOf<Trip?>(null) }
    var requestBookTrip by remember { mutableStateOf<Trip?>(null) }

    LaunchedEffect(Unit) {
        browseTripsViewModel.loadAvailableTrips()
        browseTripsViewModel.loadPopularRoutes()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(PasabayanSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
    ) {
        // User header
        UserHeaderCard(
            userName = user.name,
            currentRole = UserRole.SHIPPER,
            verificationLevel = null,
            avatarUrl = user.avatar,
            onSwitchRole = onSwitchRole,
        )

        // Find Carriers header
        Text(
            text = stringResource(R.string.dashboard_shipper_find_carriers),
            style = PasabayanTextStyles.Heading.h4,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.dashboard_shipper_find_carriers_subtitle),
            style = PasabayanTextStyles.Body.small,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Search field
        POutlinedTextField(
            value = state.filter.searchText,
            onValueChange = { browseTripsViewModel.updateSearchText(it) },
            label = { Text(stringResource(R.string.dashboard_shipper_search_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { browseTripsViewModel.applyFilterAndFetch() }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = PasabayanColors.PrimaryBlack,
                    )
                }
            },
        )

        PDivider()

        if (state.popularRoutes.isNotEmpty()) {
            Text(
                text = stringResource(R.string.trips_popular_routes_title),
                style = PasabayanTextStyles.Body.medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            state.popularRoutes.take(5).forEach { route ->
                PCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(PasabayanSpacing.sm),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = route.displayName,
                                style = PasabayanTextStyles.Body.medium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            route.tripCount?.let { count ->
                                Text(
                                    text = stringResource(R.string.trips_popular_routes_count, count),
                                    style = PasabayanTextStyles.Caption.regular,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Text(
                            text = route.routeType.name.lowercase(),
                            style = PasabayanTextStyles.Caption.large,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
            PDivider()
        }

        // Browse content
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    PCircularProgress()
                }
            }
            state.hasLoadedTrips && state.availableTrips.isEmpty() -> {
                ShipperBrowseEmptyState(
                    onCreatePackage = { /* TODO: switch to Packages tab */ },
                )
            }
            else -> {
                state.availableTrips.forEach { trip ->
                    ShipperExploreTripCard(
                        trip = trip,
                        onViewDetails = {
                            onViewTripDetails(trip.id)
                            detailTrip = trip
                        },
                        onRequestBook = { requestBookTrip = trip },
                    )
                }
                if (state.hasMore) {
                    Spacer(modifier = Modifier.size(PasabayanSpacing.xs))
                    PButton(
                        text = stringResource(R.string.trips_browse_load_more),
                        onClick = { browseTripsViewModel.loadMoreTrips() },
                        style = PButtonStyle.Secondary,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (state.isLoadingMore) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        PCircularProgress()
                    }
                }
            }
        }
    }

    detailTrip?.let { selectedTrip ->
        com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet(
            onDismissRequest = { detailTrip = null },
        ) {
            TripDetailsScreen(
                trip = selectedTrip,
                isCarrier = false,
                onEdit = {},
                onCancel = {},
                onBack = { detailTrip = null },
                onRequestBook = {
                    detailTrip = null
                    requestBookTrip = selectedTrip
                },
            )
        }
    }

    requestBookTrip?.let { selectedTrip ->
        LaunchedEffect(selectedTrip.id) {
            packageViewModel.loadPackages()
            packageViewModel.clearTripRequestState()
        }
        ShipperMatchCreationSheet(
            trip = selectedTrip,
            pendingPackages = packageViewModel.getPendingRequests(),
            isSubmitting = packageState.isSubmittingTripRequest,
            requestErrorMessage = packageState.tripRequestErrorMessage,
            requestSuccessMessage = packageState.tripRequestSuccessMessage,
            onSubmit = { packageId, offeredPrice, message ->
                packageViewModel.requestTripForPackage(
                    packageId = packageId,
                    tripId = selectedTrip.id,
                    offeredPrice = offeredPrice,
                    message = message,
                ) { result ->
                    if (result.isSuccess) {
                        browseTripsViewModel.loadAvailableTrips()
                        packageViewModel.refreshPackages()
                    }
                }
            },
            onSuccessDone = {
                requestBookTrip = null
                packageViewModel.clearTripRequestState()
            },
            onDismiss = {
                requestBookTrip = null
                packageViewModel.clearTripRequestState()
            },
        )
    }
}

/**
 * Shipper empty state — iOS parity: CTA + tips section.
 */
@Composable
private fun ShipperBrowseEmptyState(
    onCreatePackage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val purpleGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFAE47FF), Color(0xFF9047FF)),
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
    ) {
        // Empty state card
        PEmptyState(
            icon = Icons.Outlined.Explore,
            title = stringResource(R.string.dashboard_shipper_no_carriers_title),
            description = stringResource(R.string.dashboard_shipper_no_carriers_description),
        )

        // Purple gradient CTA
        androidx.compose.material3.Button(
            onClick = onCreatePackage,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
            ),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(purpleGradient, RoundedCornerShape(14.dp))
                    .padding(vertical = PasabayanSpacing.md),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.dashboard_shipper_create_package_cta),
                    style = PasabayanTextStyles.Body.medium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        PDivider()

        // Tips section
        TipRow(
            color = Color(0xFFA855F7),
            title = stringResource(R.string.dashboard_shipper_tip_demand_title),
            body = stringResource(R.string.dashboard_shipper_tip_demand_body),
        )
        TipRow(
            color = Color(0xFF00D26A),
            title = stringResource(R.string.dashboard_shipper_tip_peak_title),
            body = stringResource(R.string.dashboard_shipper_tip_peak_body),
        )
        TipRow(
            color = Color(0xFF2A8CFF),
            title = stringResource(R.string.dashboard_shipper_tip_flexible_title),
            body = stringResource(R.string.dashboard_shipper_tip_flexible_body),
        )

        // Sender tip card
        PCard {
            Row(
                horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFFAF5CFF),
                )
                Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
                    Text(
                        text = stringResource(R.string.dashboard_shipper_sender_tip_title),
                        style = PasabayanTextStyles.Body.medium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.dashboard_shipper_sender_tip_body),
                        style = PasabayanTextStyles.Body.small,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun TipRow(
    color: Color,
    title: String,
    body: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .padding(top = 6.dp)
                .background(color, CircleShape),
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = PasabayanTextStyles.Caption.large,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = body,
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true, name = "ShipperExplore — light", heightDp = 1200)
@Preview(showBackground = true, name = "ShipperExplore — dark", heightDp = 1200, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ShipperExplorePreview() {
    PasabayanTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(PasabayanSpacing.screenPadding),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
        ) {
            UserHeaderCard(
                userName = "Bong Suyat",
                currentRole = UserRole.SHIPPER,
                verificationLevel = "basic",
                avatarUrl = null,
                onSwitchRole = {},
            )
            Text(
                text = stringResource(R.string.dashboard_shipper_find_carriers),
                style = PasabayanTextStyles.Heading.h4,
            )
            Text(
                text = stringResource(R.string.dashboard_shipper_find_carriers_subtitle),
                style = PasabayanTextStyles.Body.small,
            )
            ShipperBrowseEmptyState(onCreatePackage = {})
        }
    }
}
