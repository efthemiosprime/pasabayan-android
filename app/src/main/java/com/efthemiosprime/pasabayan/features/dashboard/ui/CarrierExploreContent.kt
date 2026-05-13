package com.efthemiosprime.pasabayan.features.dashboard.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PCircularProgress
import com.efthemiosprime.pasabayan.core.designsystem.component.PDivider
import com.efthemiosprime.pasabayan.core.designsystem.component.PEmptyState
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField
import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole
import com.efthemiosprime.pasabayan.core.session.AuthUser
import com.efthemiosprime.pasabayan.core.domain.model.UserSummary
import com.efthemiosprime.pasabayan.features.packages.components.CarrierExplorePackageCard
import com.efthemiosprime.pasabayan.features.packages.components.NearbyFallbackBanner
import com.efthemiosprime.pasabayan.features.packages.model.PackageBrowseFilter
import com.efthemiosprime.pasabayan.features.packages.ui.PackageFilterSheet
import com.efthemiosprime.pasabayan.features.packages.viewmodel.PackageViewModel
import com.efthemiosprime.pasabayan.features.dashboard.model.CarrierExploreContentMode
import com.efthemiosprime.pasabayan.features.profile.ui.UserProfilePopover
import com.efthemiosprime.pasabayan.features.trips.model.CarrierExploreDropdownState
import com.efthemiosprime.pasabayan.features.trips.model.PopularRoute
import com.efthemiosprime.pasabayan.features.trips.viewmodel.BrowseTripsViewModel
import com.efthemiosprime.pasabayan.features.trips.viewmodel.RouteActivitySummaryViewModel
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Carrier Explore tab — browse available packages with iOS-parity
 * infinite scroll, filter sheet, and `nearby` envelope flag capture.
 * Mirrors iOS `CarrierBrowsePackagesView`.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CarrierExploreContent(
    user: AuthUser,
    onSwitchRole: () -> Unit,
    modifier: Modifier = Modifier,
    onViewPackageDetails: (packageId: Int) -> Unit = {},
    onRequestToCarry: (packageId: Int) -> Unit = {},
    /** Empty-state CTA: route to the My Trips tab so the carrier can post a trip. */
    onNavigateToMyTrips: () -> Unit = {},
    packageViewModel: PackageViewModel = hiltViewModel(),
    routeActivityViewModel: RouteActivitySummaryViewModel = hiltViewModel(),
    /** Activity-scoped — the same instance ShipperExploreContent uses, so the popular-routes
     * fetch is shared across role-switches. iOS parity with `popularRoutesViewModel`. */
    browseTripsViewModel: BrowseTripsViewModel = hiltViewModel(),
) {
    val state by packageViewModel.uiState.collectAsStateWithLifecycle()
    val routeActivityState by routeActivityViewModel.uiState.collectAsStateWithLifecycle()
    val browseTripsState by browseTripsViewModel.uiState.collectAsStateWithLifecycle()
    var showFilterSheet by remember { mutableStateOf(false) }
    // Profile popover state — set when the user taps a package card's shipper header.
    var profileSheetShipper by remember { mutableStateOf<UserSummary?>(null) }
    // Search-field focus drives the Recent/Popular dropdown visibility per iOS
    // `CarrierExploreDropdownState.shouldShowDropdown(focused, empty, recent, popular)`.
    var isSearchFocused by remember { mutableStateOf(false) }
    // What the main content area shows — iOS parity with `CarrierExploreContentMode`.
    // Default is Nearby (the package list); dropdown taps + popular-route taps switch
    // the mode. "Back to Nearby" returns to the default and clears any search filter.
    var contentMode by remember {
        mutableStateOf<CarrierExploreContentMode>(CarrierExploreContentMode.Nearby)
    }
    val returnToNearby: () -> Unit = {
        contentMode = CarrierExploreContentMode.Nearby
        if (state.availablePackagesFilter.searchText.isNotBlank()) {
            packageViewModel.setBrowseFilter(
                state.availablePackagesFilter.copy(searchText = ""),
            )
            packageViewModel.applyBrowseFilter()
        }
    }

    LaunchedEffect(Unit) {
        packageViewModel.applyBrowseFilter()
        routeActivityViewModel.loadSummary()
        browseTripsViewModel.loadPopularRoutes()
    }

    val listState = rememberLazyListState()
    // Auto-paginate: when the user scrolls within 3 items of the end, trigger
    // the next page. Mirrors iOS `onAppear { viewModel.loadNextPage() }` on
    // the trailing PaginationFooter.
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
                if (atEnd) packageViewModel.loadMoreAvailablePackages()
            }
    }

    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
        isRefreshing = state.isLoadingAvailablePackages,
        onRefresh = { packageViewModel.applyBrowseFilter() },
        modifier = modifier.fillMaxSize(),
    ) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(PasabayanSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
    ) {
        item("find-packages-header") {
            Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
                Text(
                    text = stringResource(R.string.dashboard_carrier_find_packages),
                    style = PasabayanTextStyles.Heading.h4,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.dashboard_carrier_find_packages_subtitle),
                    style = PasabayanTextStyles.Body.small,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item("search-row") {
            SearchAndFilterRow(
                searchText = state.availablePackagesFilter.searchText,
                onSearchTextChange = { newText ->
                    packageViewModel.setBrowseFilter(state.availablePackagesFilter.copy(searchText = newText))
                },
                onSubmitSearch = { packageViewModel.applyBrowseFilter() },
                onOpenFilters = { showFilterSheet = true },
                onFocusChange = { isSearchFocused = it },
            )
        }

        // iOS parity: Recent / Popular dropdown surfaces only while the search field
        // is focused, empty, and at least one section qualifies. Logic in
        // `CarrierExploreDropdownState`; tested in `CarrierExploreDropdownStateTest`.
        run {
            val newPackagesNearHome = routeActivityState.summary?.newPackagesThisWeekNearHome ?: 0
            val popularRoutesCount = browseTripsState.popularRoutes.size
            val showRecent = CarrierExploreDropdownState.showRecentOption(newPackagesNearHome)
            val showPopular = CarrierExploreDropdownState.showPopularOption(popularRoutesCount)
            val showDropdown = CarrierExploreDropdownState.shouldShowDropdown(
                isSearchFocused = isSearchFocused,
                isSearchEmpty = state.availablePackagesFilter.searchText.isBlank(),
                showRecentOption = showRecent,
                showPopularOption = showPopular,
            )
            if (showDropdown) {
                item("search-dropdown") {
                    CarrierExploreSearchDropdown(
                        showRecent = showRecent,
                        showPopular = showPopular,
                        // iOS parity: tapping a dropdown row swaps the main content
                        // area to the matching mode and unfocuses the search field.
                        onSelectRecent = {
                            contentMode = CarrierExploreContentMode.Recent
                            isSearchFocused = false
                        },
                        onSelectPopular = {
                            contentMode = CarrierExploreContentMode.PopularList
                            isSearchFocused = false
                        },
                    )
                }
            }
        }

        routeActivityState.summary?.let { summary ->
            item("route-activity") {
                RouteActivityCard(summary = summary)
            }
            item("route-activity-divider") { PDivider() }
        }

        // Server-driven proximity fallback: the `/packages/available` envelope
        // carries a `nearby` flag set to `false` when no proximity-matched
        // packages were found and the response fell back to non-proximity
        // results. iOS captures the same flag but never wired the UI; we do.
        if (state.availablePackagesNearby == false) {
            item("nearby-fallback-banner") { NearbyFallbackBanner() }
        }

        // iOS parity: main content area branches on `exploreContentMode`. Nearby +
        // PopularDestination + Search all render the package list (PopularDestination
        // adds a "Showing packages for X" header). PopularList renders the popular
        // routes list. Recent ships as a "coming soon" placeholder until the backend
        // contract for the Recent feed is confirmed (tracked in IMPLEMENTATION-STATUS).
        when (val mode = contentMode) {
            CarrierExploreContentMode.PopularList -> {
                item("popular-list-back") {
                    BackToNearbyRow(onClick = returnToNearby)
                }
                item("popular-list-title") {
                    Text(
                        text = stringResource(R.string.dashboard_carrier_explore_popular_list_title),
                        style = PasabayanTextStyles.Heading.h6,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                items(browseTripsState.popularRoutes, key = { "${it.originCity}-${it.destinationCity}" }) { route ->
                    PopularRouteRow(
                        route = route,
                        onClick = {
                            contentMode = CarrierExploreContentMode
                                .PopularDestination(route.destinationCity)
                            packageViewModel.setBrowseFilter(
                                state.availablePackagesFilter.copy(searchText = route.destinationCity),
                            )
                            packageViewModel.applyBrowseFilter()
                        },
                    )
                }
            }
            CarrierExploreContentMode.Recent -> {
                item("recent-back") {
                    BackToNearbyRow(onClick = returnToNearby)
                }
                item("recent-coming-soon") {
                    PCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.dashboard_carrier_explore_recent_coming_soon),
                            style = PasabayanTextStyles.Body.medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(PasabayanSpacing.md),
                        )
                    }
                }
            }
            else -> {
                if (mode is CarrierExploreContentMode.PopularDestination) {
                    item("destination-back") {
                        BackToNearbyRow(onClick = returnToNearby)
                    }
                    item("destination-header") {
                        Text(
                            text = stringResource(
                                R.string.dashboard_carrier_explore_showing_destination,
                                mode.displayName,
                            ),
                            style = PasabayanTextStyles.Heading.h6,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                when {
                    state.isLoadingAvailablePackages && state.availablePackages.isEmpty() -> {
                        item("loading") {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                PCircularProgress()
                            }
                        }
                    }
                    state.hasLoadedAvailablePackages && state.visibleAvailablePackages.isEmpty() -> {
                        item("empty") {
                            CarrierBrowseEmptyState(onPostTrip = onNavigateToMyTrips)
                        }
                    }
                    else -> {
                        items(state.visibleAvailablePackages, key = { it.effectiveId }) { available ->
                            CarrierExplorePackageCard(
                                pkg = available,
                                onViewDetails = { onViewPackageDetails(available.effectiveId) },
                                onRequestToCarry = { onRequestToCarry(available.effectiveId) },
                                onOpenShipperProfile = available.shipper?.let { shipper ->
                                    { profileSheetShipper = shipper }
                                },
                            )
                        }
                        // Footer: spinner, retry, or end-of-list.
                        item("pagination-footer") {
                            PaginationFooter(
                                isLoadingMore = state.availablePackagesIsLoadingMore,
                                loadMoreError = state.availablePackagesLoadMoreError,
                                hasMore = state.availablePackagesHasMore,
                                onRetry = { packageViewModel.loadMoreAvailablePackages() },
                            )
                        }
                    }
                }
            }
        }
    }
    } // end PullToRefreshBox

    if (showFilterSheet) {
        PackageFilterSheet(
            filter = state.availablePackagesFilter,
            onDismiss = { showFilterSheet = false },
            onUrgencyChange = { urgency ->
                packageViewModel.setBrowseFilter(state.availablePackagesFilter.copy(urgency = urgency))
            },
            onPackageTypeChange = { type ->
                packageViewModel.setBrowseFilter(state.availablePackagesFilter.copy(packageType = type))
            },
            onMaxWeightChange = { weight ->
                packageViewModel.setBrowseFilter(state.availablePackagesFilter.copy(maxWeight = weight))
            },
            onMaxPriceChange = { price ->
                packageViewModel.setBrowseFilter(state.availablePackagesFilter.copy(maxPrice = price))
            },
            onApply = {
                packageViewModel.applyBrowseFilter()
                showFilterSheet = false
            },
            onClear = {
                packageViewModel.clearBrowseFilter()
                showFilterSheet = false
            },
        )
    }

    profileSheetShipper?.let { shipper ->
        UserProfilePopover(
            userId = shipper.id,
            userName = shipper.name,
            userAvatar = shipper.avatar,
            verificationLevel = shipper.verificationLevel,
            initialRating = shipper.ratingValue,
            initialTotalRatings = shipper.totalRatings,
            userRole = UserRole.SHIPPER,
            memberSince = shipper.memberSinceLabel,
            currentUserId = user.id.toInt(),
            currentUserRole = UserRole.CARRIER,
            onDismiss = { profileSheetShipper = null },
        )
    }
}

@Composable
private fun SearchAndFilterRow(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onSubmitSearch: () -> Unit,
    onOpenFilters: () -> Unit,
    onFocusChange: (Boolean) -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        POutlinedTextField(
            value = searchText,
            onValueChange = onSearchTextChange,
            label = { Text(stringResource(R.string.dashboard_carrier_search_placeholder)) },
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { onFocusChange(it.isFocused) },
            trailingIcon = {
                IconButton(onClick = onSubmitSearch) {
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
                onSearch = { onSubmitSearch() },
            ),
        )
        IconButton(onClick = onOpenFilters) {
            Icon(
                imageVector = Icons.Outlined.Tune,
                contentDescription = stringResource(R.string.dashboard_carrier_filters_open),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

/**
 * Recent / Popular dropdown shown under the carrier-explore search field. iOS parity
 * with `CarrierExploreDropdownOverlayView` in `CarrierHomeContent.swift` — two
 * tappable rows, each gated by [showRecent] / [showPopular] computed via
 * [CarrierExploreDropdownState].
 */
@Composable
private fun CarrierExploreSearchDropdown(
    showRecent: Boolean,
    showPopular: Boolean,
    onSelectRecent: () -> Unit,
    onSelectPopular: () -> Unit,
) {
    PCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (showRecent) {
                DropdownOptionRow(
                    icon = Icons.Filled.History,
                    label = stringResource(R.string.dashboard_carrier_explore_recent),
                    onClick = onSelectRecent,
                )
            }
            if (showRecent && showPopular) {
                PDivider()
            }
            if (showPopular) {
                DropdownOptionRow(
                    icon = Icons.Filled.LocalFireDepartment,
                    label = stringResource(R.string.dashboard_carrier_explore_popular),
                    onClick = onSelectPopular,
                )
            }
        }
    }
}

@Composable
private fun DropdownOptionRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = PasabayanSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = label,
            style = PasabayanTextStyles.Body.small,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * "← Back to Nearby" affordance shown above non-default content modes. Tapping
 * returns the explore tab to its default Nearby package list. iOS parity with
 * the back-link in `CarrierPopularRouteListView` and the recent/destination headers.
 */
@Composable
private fun BackToNearbyRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = PasabayanSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = stringResource(R.string.dashboard_carrier_explore_back_to_nearby),
            style = PasabayanTextStyles.Body.small,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * Single row in the popular-routes list (carrier-explore `.popularList` mode).
 * Mirrors iOS `CarrierPopularRouteListView` row layout — origin → destination
 * + package count, full-width tappable card.
 */
@Composable
private fun PopularRouteRow(
    route: PopularRoute,
    onClick: () -> Unit,
) {
    PCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PasabayanSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {
            Icon(
                imageVector = Icons.Filled.LocalFireDepartment,
                contentDescription = null,
                tint = PasabayanColors.BadgeGold,
                modifier = Modifier.size(20.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(
                        R.string.trips_popular_routes_path,
                        route.originCity,
                        route.destinationCity,
                    ),
                    style = PasabayanTextStyles.Body.medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.trips_popular_routes_count, route.packageCount),
                    style = PasabayanTextStyles.Caption.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun RouteActivityCard(
    summary: com.efthemiosprime.pasabayan.features.trips.model.RouteActivitySummary,
) {
    PCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PasabayanSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
        ) {
            Text(
                text = stringResource(R.string.trips_route_activity_title),
                style = PasabayanTextStyles.Body.medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.trips_route_activity_total_trips_count, summary.totalTrips),
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.trips_route_activity_active_trips_count, summary.activeTrips),
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.trips_route_activity_completed_trips_count, summary.completedTrips),
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val earningsText = summary.totalEarnings?.let { earnings ->
                val currency = summary.currency ?: "CAD"
                stringResource(R.string.trips_route_activity_total_earnings, currency, earnings)
            } ?: stringResource(R.string.trips_route_activity_total_earnings_unavailable)
            Text(
                text = earningsText,
                style = PasabayanTextStyles.Body.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PaginationFooter(
    isLoadingMore: Boolean,
    loadMoreError: String?,
    hasMore: Boolean,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = PasabayanSpacing.md),
        contentAlignment = Alignment.Center,
    ) {
        when {
            isLoadingMore -> PCircularProgress()
            loadMoreError != null -> {
                IconButton(onClick = onRetry) {
                    Text(
                        text = stringResource(R.string.dashboard_carrier_load_more_retry),
                        style = PasabayanTextStyles.Caption.regular,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            !hasMore -> {
                // End-of-list — no marker today; iOS shows `endOfList` empty state.
            }
        }
    }
}

/**
 * Carrier empty state — iOS parity: CTA + tips section.
 */
@Composable
private fun CarrierBrowseEmptyState(
    onPostTrip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val blueGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF3B82F6), Color(0xFF2F6FE6)),
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
    ) {
        PEmptyState(
            icon = Icons.Outlined.Inventory2,
            title = stringResource(R.string.dashboard_carrier_no_packages_title),
            description = stringResource(R.string.dashboard_carrier_no_packages_description),
        )

        androidx.compose.material3.Button(
            onClick = onPostTrip,
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
                    .background(blueGradient, RoundedCornerShape(14.dp))
                    .padding(vertical = PasabayanSpacing.md),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.dashboard_carrier_create_trip_cta),
                    style = PasabayanTextStyles.Body.medium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        PDivider()

        TipRow(
            color = Color(0xFF3B82F6),
            title = stringResource(R.string.dashboard_carrier_tip_demand_title),
            body = stringResource(R.string.dashboard_carrier_tip_demand_body),
        )
        TipRow(
            color = Color(0xFFF59E0B),
            title = stringResource(R.string.dashboard_carrier_tip_earning_title),
            body = stringResource(R.string.dashboard_carrier_tip_earning_body),
        )
        TipRow(
            color = Color(0xFF9EC5FF),
            title = stringResource(R.string.dashboard_carrier_tip_proactive_title),
            body = stringResource(R.string.dashboard_carrier_tip_proactive_body),
        )

        PCard {
            Row(
                horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFF2A8CFF),
                )
                Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs)) {
                    Text(
                        text = stringResource(R.string.dashboard_carrier_card_tip_title),
                        style = PasabayanTextStyles.Body.medium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.dashboard_carrier_card_tip_body),
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

@Preview(showBackground = true, name = "CarrierExplore — light", heightDp = 1200)
@Preview(showBackground = true, name = "CarrierExplore — dark", heightDp = 1200, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CarrierExplorePreview() {
    PasabayanTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(PasabayanSpacing.screenPadding),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
        ) {
            Text(
                text = stringResource(R.string.dashboard_carrier_find_packages),
                style = PasabayanTextStyles.Heading.h4,
            )
            Text(
                text = stringResource(R.string.dashboard_carrier_find_packages_subtitle),
                style = PasabayanTextStyles.Body.small,
            )
            CarrierBrowseEmptyState(onPostTrip = {})
        }
    }
}
