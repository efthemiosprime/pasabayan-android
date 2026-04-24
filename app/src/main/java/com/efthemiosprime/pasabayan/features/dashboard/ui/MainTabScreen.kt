package com.efthemiosprime.pasabayan.features.dashboard.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.component.PEmptyState
import com.efthemiosprime.pasabayan.core.designsystem.component.PScaffold
import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole
import com.efthemiosprime.pasabayan.core.session.AuthUser
import com.efthemiosprime.pasabayan.features.dashboard.components.DashboardTopBar
import com.efthemiosprime.pasabayan.features.dashboard.components.PasabayanBottomBar
import com.efthemiosprime.pasabayan.features.dashboard.model.MainTabs
import com.efthemiosprime.pasabayan.features.dashboard.model.DashboardSheetRoute
import com.efthemiosprime.pasabayan.features.dashboard.viewmodel.DashboardViewModel
import com.efthemiosprime.pasabayan.features.packages.components.CreatePackageOptionsSheet
import com.efthemiosprime.pasabayan.features.packages.ui.EditPackageSheet
import com.efthemiosprime.pasabayan.features.packages.ui.PackageErrandRequestScreen
import com.efthemiosprime.pasabayan.features.packages.ui.PackageDetailScreen
import com.efthemiosprime.pasabayan.features.packages.ui.PackageRequestScreen
import com.efthemiosprime.pasabayan.features.packages.viewmodel.PackageCreationAssistViewModel
import com.efthemiosprime.pasabayan.features.packages.viewmodel.PackageViewModel
import com.efthemiosprime.pasabayan.features.trips.model.Trip
import com.efthemiosprime.pasabayan.features.trips.ui.TripFilterSheet
import com.efthemiosprime.pasabayan.features.trips.ui.CreateTripFromPackageScreen
import com.efthemiosprime.pasabayan.features.trips.ui.EditTripSheet
import com.efthemiosprime.pasabayan.features.trips.ui.TripDetailsScreen
import com.efthemiosprime.pasabayan.features.trips.ui.TripCreationScreen
import com.efthemiosprime.pasabayan.features.trips.viewmodel.BrowseTripsViewModel
import com.efthemiosprime.pasabayan.features.trips.viewmodel.CarrierTripsViewModel
import com.efthemiosprime.pasabayan.features.trips.viewmodel.CarrierPreferencesFormViewModel
import com.efthemiosprime.pasabayan.features.trips.viewmodel.TripCreationSavedRoutesViewModel
import com.efthemiosprime.pasabayan.features.trips.viewmodel.TripsLocalStateViewModel

/**
 * Main tabbed dashboard shell — replaces the Phase 1 placeholder.
 * 5 tabs per role, matching iOS tab order.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MainTabScreen(
    user: AuthUser,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val packageViewModel: PackageViewModel = hiltViewModel()
    val packageCreationAssistViewModel: PackageCreationAssistViewModel = hiltViewModel()
    val packageCreationAssistState by packageCreationAssistViewModel.uiState.collectAsStateWithLifecycle()
    val browseTripsViewModel: BrowseTripsViewModel = hiltViewModel()
    val browseTripsState by browseTripsViewModel.uiState.collectAsStateWithLifecycle()
    val carrierPreferencesFormViewModel: CarrierPreferencesFormViewModel = hiltViewModel()
    val carrierTripsViewModel: CarrierTripsViewModel = hiltViewModel()
    val carrierTripsState by carrierTripsViewModel.uiState.collectAsStateWithLifecycle()
    val tripCreationSavedRoutesViewModel: TripCreationSavedRoutesViewModel = hiltViewModel()
    val tripCreationSavedRoutesState by tripCreationSavedRoutesViewModel.uiState.collectAsStateWithLifecycle()
    val tripsLocalStateViewModel: TripsLocalStateViewModel = hiltViewModel()
    val packageUiState by packageViewModel.uiState.collectAsStateWithLifecycle()
    val tabs = MainTabs.forRole(state.currentRole)
    var showCreateOptionsSheet by remember { mutableStateOf(false) }
    var showPackageRequestSheet by remember { mutableStateOf(false) }
    var showPackageDisclaimerGate by remember { mutableStateOf(false) }
    var showErrandRequestSheet by remember { mutableStateOf(false) }
    var openErrandAfterDisclaimer by remember { mutableStateOf(false) }
    var showTripCreationSheet by remember { mutableStateOf(false) }
    var showCarrierPreferencesGate by remember { mutableStateOf(false) }
    var selectedCarrierTripId by remember { mutableStateOf<Int?>(null) }
    val dismissActiveSheetRoute = { viewModel.dismissActiveSheetRoute() }
    val openTripFilterSheet = { viewModel.openTripFilterSheet() }
    val openCreateTripFromPackageSheet: (Int) -> Unit = { packageId ->
        viewModel.openCreateTripFromPackageSheet(packageId)
    }
    val openCarrierTripEditor: (Int) -> Unit = { tripId ->
        selectedCarrierTripId = null
        viewModel.openEditTripSheet(tripId)
    }
    val createTripFromPackageRouteActions = remember(viewModel, carrierTripsViewModel, packageViewModel) {
        CreateTripFromPackageRouteActions(
            dismissRoute = dismissActiveSheetRoute,
            refreshCarrierTrips = { carrierTripsViewModel.refreshTrips() },
            refreshPackages = { packageViewModel.refreshPackages() },
        )
    }

    LaunchedEffect(user) {
        viewModel.initializeRole(user)
        tripsLocalStateViewModel.retryCarrierDisclaimerPendingSync(user.id)
    }

    PScaffold(
        modifier = modifier,
        topBar = {
            DashboardTopBar(
                userName = user.name,
                currentRole = state.currentRole,
                onSwitchRole = { viewModel.switchRole() },
            )
        },
        bottomBar = {
            PasabayanBottomBar(
                tabs = tabs,
                selectedIndex = state.selectedTabIndex,
                onTabSelected = { viewModel.selectTab(it) },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            val currentTab = tabs.getOrNull(state.selectedTabIndex)
            when (currentTab?.route) {
                "explore" -> when (state.currentRole) {
                    UserRole.CARRIER ->
                        CarrierExploreContent(
                            user = user,
                            onSwitchRole = { viewModel.switchRole() },
                            onViewPackageDetails = { packageId ->
                                viewModel.openPackageDetailSheet(packageId)
                            },
                            packageViewModel = packageViewModel,
                        )
                    UserRole.SHIPPER ->
                        ShipperExploreContent(
                            user = user,
                            onSwitchRole = { viewModel.switchRole() },
                            onOpenTripFilter = openTripFilterSheet,
                            browseTripsViewModel = browseTripsViewModel,
                        )
                }
                "matches" -> com.efthemiosprime.pasabayan.features.bookings.ui.MatchListScreen(
                    isCarrier = state.currentRole == UserRole.CARRIER,
                    onAction = { action, matchId -> /* TODO: handle booking actions */ },
                )
                "my_trips" -> com.efthemiosprime.pasabayan.features.trips.ui.CarrierMyTripsScreen(
                    onViewTripDetails = { trip -> selectedCarrierTripId = trip.id },
                    onEditTrip = { trip -> openCarrierTripEditor(trip.id) },
                    onCreateTrip = {
                        tripCreationSavedRoutesViewModel.refreshSavedRoutes()
                        if (carrierPreferencesFormViewModel.isAcknowledged(user.id)) {
                            showTripCreationSheet = true
                        } else {
                            showCarrierPreferencesGate = true
                        }
                    },
                    viewModel = carrierTripsViewModel,
                )
                "packages" -> com.efthemiosprime.pasabayan.features.packages.ui.PackageListScreen(
                    onViewPackageDetails = { packageId ->
                        viewModel.openPackageDetailSheet(packageId)
                    },
                    onCreatePackage = {
                        packageCreationAssistViewModel.initialize(user.id)
                        showCreateOptionsSheet = true
                    },
                    onCreateTripFromPackage = openCreateTripFromPackageSheet,
                    onEditPackage = { packageId ->
                        viewModel.openEditPackageSheet(packageId)
                    },
                    onCancelPackage = { packageId ->
                        packageViewModel.cancelPackage(packageId)
                    },
                    viewModel = packageViewModel,
                )
                "messages" -> StubTabContent(
                    icon = Icons.AutoMirrored.Outlined.Chat,
                    title = stringResource(R.string.dashboard_stub_messages_title),
                    description = stringResource(R.string.dashboard_stub_messages_description),
                )
                "profile" -> com.efthemiosprime.pasabayan.features.payments.ui.PaymentsProfileScreen(
                    onLogout = onLogout,
                )
                else -> {}
            }
        }
    }

    if (showCreateOptionsSheet) {
        com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet(
            onDismissRequest = { showCreateOptionsSheet = false },
        ) {
            CreatePackageOptionsSheet(
                onClose = { showCreateOptionsSheet = false },
                onShipPackage = {
                    showCreateOptionsSheet = false
                    if (packageCreationAssistState.hasAcknowledgedDisclaimer) {
                        showPackageRequestSheet = true
                    } else {
                        openErrandAfterDisclaimer = false
                        showPackageDisclaimerGate = true
                    }
                },
                onErrandService = {
                    showCreateOptionsSheet = false
                    if (packageCreationAssistState.hasAcknowledgedDisclaimer) {
                        showErrandRequestSheet = true
                    } else {
                        openErrandAfterDisclaimer = true
                        showPackageDisclaimerGate = true
                    }
                },
            )
        }
    }

    if (showPackageRequestSheet) {
        com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet(
            onDismissRequest = { showPackageRequestSheet = false },
        ) {
            PackageRequestScreen(
                onSave = { payload, imageUris ->
                    packageViewModel.createPackageRequest(
                        payload = payload,
                        imageUris = imageUris,
                    ) { result ->
                        if (result.isSuccess) {
                            packageCreationAssistViewModel.onPackageCreated(payload)
                            showPackageRequestSheet = false
                            packageViewModel.refreshPackages()
                        }
                    }
                },
                onCancel = { showPackageRequestSheet = false },
                savedDescriptions = packageCreationAssistState.savedDescriptions,
                savedPickupTemplates = packageCreationAssistState.savedPickupTemplates,
                savedHandoffTemplates = packageCreationAssistState.savedHandoffTemplates,
                showTutorialOverlay = packageCreationAssistState.showTutorial,
                onDismissTutorial = { packageCreationAssistViewModel.dismissTutorial(user.id) },
                isSubmitting = packageUiState.isSubmittingPackageRequest,
            )
        }
    }

    if (showPackageDisclaimerGate) {
        com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet(
            onDismissRequest = { showPackageDisclaimerGate = false },
        ) {
            com.efthemiosprime.pasabayan.core.designsystem.component.PDetailSheetScaffold(
                title = stringResource(R.string.packages_disclaimer_title),
                closeContentDescription = stringResource(R.string.packages_create_close),
                onClose = { showPackageDisclaimerGate = false },
            ) {
                androidx.compose.material3.Text(
                    text = stringResource(R.string.packages_disclaimer_body),
                )
                com.efthemiosprime.pasabayan.core.designsystem.component.PButton(
                    text = stringResource(R.string.packages_disclaimer_acknowledge),
                    onClick = {
                        packageCreationAssistViewModel.acknowledgeDisclaimer(user.id)
                        showPackageDisclaimerGate = false
                        if (openErrandAfterDisclaimer) {
                            showErrandRequestSheet = true
                        } else {
                            showPackageRequestSheet = true
                        }
                        openErrandAfterDisclaimer = false
                    },
                )
            }
        }
    }

    if (showErrandRequestSheet) {
        com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet(
            onDismissRequest = { showErrandRequestSheet = false },
        ) {
            PackageErrandRequestScreen(
                onSave = { payload ->
                    packageViewModel.createServiceRequest(payload) { result ->
                        if (result.isSuccess) {
                            showErrandRequestSheet = false
                            packageViewModel.refreshPackages()
                        }
                    }
                },
                onCancel = { showErrandRequestSheet = false },
                isSubmitting = packageUiState.isSubmittingServiceRequest,
            )
        }
    }

    if (showCarrierPreferencesGate) {
        com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet(
            onDismissRequest = { showCarrierPreferencesGate = false },
        ) {
            com.efthemiosprime.pasabayan.core.designsystem.component.PDetailSheetScaffold(
                title = stringResource(R.string.trips_preferences_gate_title),
                closeContentDescription = stringResource(R.string.trips_detail_close),
                onClose = { showCarrierPreferencesGate = false },
            ) {
                androidx.compose.material3.Text(
                    text = stringResource(R.string.trips_preferences_gate_body),
                )
                com.efthemiosprime.pasabayan.core.designsystem.component.PButton(
                    text = stringResource(R.string.trips_preferences_gate_acknowledge),
                    onClick = {
                        carrierPreferencesFormViewModel.markAcknowledged(user.id)
                        tripCreationSavedRoutesViewModel.refreshSavedRoutes()
                        showCarrierPreferencesGate = false
                        showTripCreationSheet = true
                    },
                )
            }
        }
    }

    if (showTripCreationSheet) {
        com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet(
            onDismissRequest = { showTripCreationSheet = false },
        ) {
            TripCreationScreen(
                userId = user.id,
                savedRoutes = tripCreationSavedRoutesState.savedRoutes,
                onTripCreated = {
                    showTripCreationSheet = false
                    tripCreationSavedRoutesViewModel.refreshSavedRoutes()
                    carrierTripsViewModel.refreshTrips()
                },
                onCancel = { showTripCreationSheet = false },
            )
        }
    }

    if (state.activeSheetRoute is DashboardSheetRoute.TripFilter) {
        TripFilterSheet(
            filter = browseTripsState.filter,
            onDismiss = dismissActiveSheetRoute,
            onSearchChange = { browseTripsViewModel.updateSearchText(it) },
            onOriginChange = { browseTripsViewModel.updateOrigin(it) },
            onDestinationChange = { browseTripsViewModel.updateDestination(it) },
            onApply = {
                browseTripsViewModel.applyFilterAndFetch()
                dismissActiveSheetRoute()
            },
            onClear = {
                browseTripsViewModel.clearFilters()
                browseTripsViewModel.applyFilterAndFetch()
                dismissActiveSheetRoute()
            },
        )
    }

    val createFromPackageRoute = state.activeSheetRoute as? DashboardSheetRoute.CreateTripFromPackage
    createFromPackageRoute?.let { route ->
        com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet(
            onDismissRequest = createTripFromPackageRouteActions::onClose,
        ) {
            CreateTripFromPackageScreen(
                packageId = route.packageId,
                userId = user.id,
                onClose = createTripFromPackageRouteActions::onClose,
                onTripCreated = createTripFromPackageRouteActions::onTripCreated,
            )
        }
    }

    val packageDetailRoute = state.activeSheetRoute as? DashboardSheetRoute.PackageDetail
    packageDetailRoute?.let { route ->
        LaunchedEffect(route.packageId) {
            packageViewModel.loadPackageDetail(route.packageId)
        }
        com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet(
            onDismissRequest = {
                packageViewModel.clearPackageDetail()
                dismissActiveSheetRoute()
            },
        ) {
            val detail = packageUiState.selectedPackageDetail
            if (detail != null) {
                PackageDetailScreen(
                    pkg = detail,
                    onEdit = { viewModel.openEditPackageSheet(detail.id) },
                    onCancel = {
                        packageViewModel.cancelPackage(detail.id)
                        packageViewModel.clearPackageDetail()
                        dismissActiveSheetRoute()
                    },
                    onBack = {
                        packageViewModel.clearPackageDetail()
                        dismissActiveSheetRoute()
                    },
                )
            }
        }
    }

    val editPackageRoute = state.activeSheetRoute as? DashboardSheetRoute.EditPackage
    editPackageRoute?.let { route ->
        val detail = packageUiState.selectedPackageDetail
            ?: packageUiState.packageRequests.firstOrNull { it.id == route.packageId }
        detail?.let { pkg ->
            EditPackageSheet(
                pkg = pkg,
                onDismiss = dismissActiveSheetRoute,
                onSave = { request ->
                    packageViewModel.updatePackage(route.packageId, request)
                    packageViewModel.refreshPackages()
                    dismissActiveSheetRoute()
                },
            )
        }
    }

    val selectedCarrierTrip = selectedCarrierTripId?.let { id ->
        carrierTripsState.trips.firstOrNull { it.id == id }
    }
    selectedCarrierTrip?.let { trip ->
        com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet(
            onDismissRequest = { selectedCarrierTripId = null },
        ) {
            TripDetailsScreen(
                trip = trip,
                isCarrier = true,
                onEdit = { openCarrierTripEditor(trip.id) },
                onCancel = {
                    carrierTripsViewModel.cancelTrip(trip.id)
                    selectedCarrierTripId = null
                },
                onBack = { selectedCarrierTripId = null },
            )
        }
    }

    val editingCarrierTripId = (state.activeSheetRoute as? DashboardSheetRoute.EditTrip)?.tripId
    val editingCarrierTrip = editingCarrierTripId?.let { id ->
        carrierTripsState.trips.firstOrNull { it.id == id }
    }
    editingCarrierTrip?.let { trip ->
        EditTripSheet(
            trip = trip,
            onDismiss = dismissActiveSheetRoute,
            onSave = { availableWeightKg, notes ->
                carrierTripsViewModel.updateTripDetails(
                    tripId = trip.id,
                    availableWeightKg = availableWeightKg,
                    specialNotes = notes,
                )
                dismissActiveSheetRoute()
            },
        )
    }
}

@Composable
private fun StubTabContent(
    icon: ImageVector,
    title: String,
    description: String,
) {
    PEmptyState(
        icon = icon,
        title = title,
        description = description,
        modifier = Modifier.padding(PasabayanSpacing.lg),
    )
}

