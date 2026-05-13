package com.efthemiosprime.pasabayan.features.dashboard.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PChip
import com.efthemiosprime.pasabayan.core.designsystem.component.PCircularProgress
import com.efthemiosprime.pasabayan.core.designsystem.component.PDivider
import com.efthemiosprime.pasabayan.core.designsystem.component.PEmptyState
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField
import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole
import com.efthemiosprime.pasabayan.core.session.AuthUser
import com.efthemiosprime.pasabayan.features.bookings.ui.ShipperMatchCreationSheet
import com.efthemiosprime.pasabayan.features.dashboard.components.ShipperExploreTripCard
import com.efthemiosprime.pasabayan.core.domain.model.UserSummary
import com.efthemiosprime.pasabayan.features.packages.viewmodel.PackageViewModel
import com.efthemiosprime.pasabayan.features.profile.ui.UserProfilePopover
import com.efthemiosprime.pasabayan.features.shipper.components.NearbyCarriersSection
import com.efthemiosprime.pasabayan.features.shipper.components.RecentSearchesSection
import com.efthemiosprime.pasabayan.features.shipper.model.ShipperExploreSectionVisibility
import com.efthemiosprime.pasabayan.features.shipper.viewmodel.NearbyCarriersViewModel
import com.efthemiosprime.pasabayan.features.trips.ui.TripDetailsScreen
import com.efthemiosprime.pasabayan.features.trips.viewmodel.BrowseTripsViewModel
import com.efthemiosprime.pasabayan.features.trips.model.PopularRoute
import com.efthemiosprime.pasabayan.features.trips.model.Trip
import com.efthemiosprime.pasabayan.features.verification.model.VerifyPhoneReason
import kotlinx.coroutines.flow.distinctUntilChanged

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
    onOpenTripFilter: () -> Unit = {},
    onPhoneVerificationRequired: (VerifyPhoneReason) -> Unit = {},
    /** Empty-state CTA: route to the Packages tab so the shipper can create a package request. */
    onNavigateToPackages: () -> Unit = {},
    browseTripsViewModel: BrowseTripsViewModel = hiltViewModel(),
    packageViewModel: PackageViewModel = hiltViewModel(),
    nearbyCarriersViewModel: NearbyCarriersViewModel = hiltViewModel(),
    recentSearchesViewModel: com.efthemiosprime.pasabayan.features.shipper.viewmodel.ShipperRecentSearchesViewModel =
        hiltViewModel(),
) {
    val state by browseTripsViewModel.uiState.collectAsStateWithLifecycle()
    val packageState by packageViewModel.uiState.collectAsStateWithLifecycle()
    val nearbyCarriersState by nearbyCarriersViewModel.uiState.collectAsStateWithLifecycle()
    val recentSearches by recentSearchesViewModel.entries.collectAsStateWithLifecycle()
    var detailTrip by remember { mutableStateOf<Trip?>(null) }
    var requestBookTrip by remember { mutableStateOf<Trip?>(null) }
    // Profile popover state — non-null when the user tapped a trip card's carrier header
    // (or, future, a nearby-carrier chip). iOS parity: UserProfilePopover.swift.
    var profileSheetCarrier by remember { mutableStateOf<UserSummary?>(null) }

    LaunchedEffect(Unit) {
        browseTripsViewModel.loadAvailableTrips()
        browseTripsViewModel.loadPopularRoutes()
        nearbyCarriersViewModel.loadNearbyCarriersIfNeeded()
    }

    val listState = rememberLazyListState()
    // Auto-paginate when the user scrolls within 3 items of the end.
    // Mirrors iOS `BrowseTripsView` infinite-scroll trigger.
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val total = layoutInfo.totalItemsCount
            if (total == 0) return@derivedStateOf false
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            lastVisible >= total - 3
        }
    }
    LaunchedEffect(listState) {
        snapshotFlow { shouldLoadMore }
            .distinctUntilChanged()
            .collect { atEnd ->
                if (atEnd) browseTripsViewModel.loadMoreTrips()
            }
    }

    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { browseTripsViewModel.loadAvailableTrips(reset = true) },
        modifier = modifier.fillMaxSize(),
    ) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(PasabayanSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
    ) {
        item("find-carriers-header") {
            Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
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
            }
        }
        item("search-row") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                POutlinedTextField(
                    value = state.filter.searchText,
                    onValueChange = { browseTripsViewModel.updateSearchText(it) },
                    label = { Text(stringResource(R.string.dashboard_shipper_search_placeholder)) },
                    modifier = Modifier.weight(1f),
                    trailingIcon = {
                        IconButton(onClick = { browseTripsViewModel.applyFilterAndFetch() }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = PasabayanColors.PrimaryBlack,
                            )
                        }
                    },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        imeAction = androidx.compose.ui.text.input.ImeAction.Search,
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onSearch = { browseTripsViewModel.applyFilterAndFetch() },
                    ),
                )
                IconButton(onClick = onOpenTripFilter) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = stringResource(R.string.trips_filter_open),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
        item("search-divider") { PDivider() }

        // Top Carriers vs Recent Searches — iOS renders them as siblings: Top Carriers wins
        // when there's at least one carrier with completed deliveries; otherwise Recent
        // Searches surfaces if the user has saved any. Visibility logic mirrors iOS
        // `ShipperExploreSectionVisibility.shouldShow{TopCarriers,RecentSearches}`.
        val topCarriers = nearbyCarriersState.carriersWithCompletedDeliveries
        val showTopCarriers = ShipperExploreSectionVisibility.shouldShowTopCarriers(
            searchText = state.filter.searchText,
            selectedPopularDestination = null,
            carriersWithCompletedTripsCount = topCarriers.size,
        )
        val showRecentSearches = ShipperExploreSectionVisibility.shouldShowRecentSearches(
            searchText = state.filter.searchText,
            selectedPopularDestination = null,
            carriersWithCompletedTripsCount = topCarriers.size,
            recentSearchesCount = recentSearches.size,
        )
        when {
            showTopCarriers -> {
                item("nearby-carriers-section") {
                    NearbyCarriersSection(
                        carriers = topCarriers,
                        onCarrierTap = { carrier ->
                            // iOS parity: ShipperHomeContent sets `selectedCarrierForProfile`
                            // and renders the same UserProfilePopover used by trip-card carrier
                            // headers. NearbyCarrier carries id/name/avatar; rating + verification
                            // are fetched fresh by the popover's VM.
                            profileSheetCarrier = UserSummary(
                                id = carrier.id,
                                name = carrier.name,
                                avatar = carrier.avatar,
                            )
                        },
                    )
                }
                item("nearby-carriers-divider") { PDivider() }
            }
            showRecentSearches -> {
                item("recent-searches-section") {
                    RecentSearchesSection(
                        entries = recentSearches,
                        onEntryTap = { entry ->
                            browseTripsViewModel.updateSearchText(entry.displayName)
                            browseTripsViewModel.updateDestination(entry.displayName)
                            browseTripsViewModel.applyFilterAndFetch()
                        },
                    )
                }
                item("recent-searches-divider") { PDivider() }
            }
        }

        if (state.popularRoutes.isNotEmpty()) {
            item("popular-routes-section") {
                PopularRoutesChipRow(
                    routes = state.popularRoutes.take(5),
                    // iOS parity: tapping a popular route persists it as a recent search
                    // so it surfaces in the Recent fallback later. Free-form-search-text
                    // saves are deferred — see the Shipper feature row in
                    // android-spec/IMPLEMENTATION-STATUS.md.
                    onRouteTap = { route ->
                        recentSearchesViewModel.save(
                            city = route.destinationCity,
                            country = route.destinationCountry ?: "",
                        )
                    },
                )
            }
            item("popular-routes-divider") { PDivider() }
        }

        when {
            state.isLoading && state.availableTrips.isEmpty() -> {
                item("loading") {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        PCircularProgress()
                    }
                }
            }
            state.hasLoadedTrips && state.availableTrips.isEmpty() -> {
                item("empty") {
                    ShipperBrowseEmptyState(onCreatePackage = onNavigateToPackages)
                }
            }
            else -> {
                items(state.availableTrips, key = { it.id }) { trip ->
                    ShipperExploreTripCard(
                        trip = trip,
                        onViewDetails = {
                            onViewTripDetails(trip.id)
                            detailTrip = trip
                        },
                        onRequestBook = {
                            if (!user.phoneVerified) {
                                onPhoneVerificationRequired(VerifyPhoneReason.BookTrip)
                            } else {
                                requestBookTrip = trip
                            }
                        },
                        onOpenCarrierProfile = trip.carrier?.let { carrier ->
                            { profileSheetCarrier = carrier }
                        },
                    )
                }
                // Trailing footer drives the auto-paginate signal (visible to
                // the layoutInfo) and surfaces in-flight / end-of-list state.
                item("pagination-footer") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = PasabayanSpacing.md),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (state.isLoadingMore) PCircularProgress()
                    }
                }
            }
        }
    }
    } // end PullToRefreshBox

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
                    if (!user.phoneVerified) {
                        detailTrip = null
                        onPhoneVerificationRequired(VerifyPhoneReason.BookTrip)
                    } else {
                        detailTrip = null
                        requestBookTrip = selectedTrip
                    }
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

    profileSheetCarrier?.let { carrier ->
        UserProfilePopover(
            userId = carrier.id,
            userName = carrier.name,
            userAvatar = carrier.avatar,
            verificationLevel = carrier.verificationLevel,
            initialRating = carrier.ratingValue,
            initialTotalRatings = carrier.totalRatings,
            userRole = UserRole.CARRIER,
            memberSince = carrier.memberSinceLabel,
            currentUserId = user.id.toInt(),
            currentUserRole = UserRole.SHIPPER,
            onDismiss = { profileSheetCarrier = null },
        )
    }
}

@Composable
private fun PopularRoutesChipRow(
    routes: List<PopularRoute>,
    onRouteTap: (PopularRoute) -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.trips_popular_routes_title),
            style = PasabayanTextStyles.Heading.h6,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {
            for (route in routes) {
                PChip(
                    label = stringResource(
                        R.string.trips_popular_routes_path,
                        route.originCity,
                        route.destinationCity,
                    ),
                    secondary = stringResource(
                        R.string.trips_popular_routes_count,
                        route.packageCount,
                    ),
                    onClick = { onRouteTap(route) },
                )
            }
        }
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
