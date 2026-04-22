package com.efthemiosprime.pasabayan.features.dashboard.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Chat
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
import com.efthemiosprime.pasabayan.features.dashboard.viewmodel.DashboardViewModel
import com.efthemiosprime.pasabayan.features.packages.components.CreatePackageOptionsSheet
import com.efthemiosprime.pasabayan.features.packages.ui.PackageErrandRequestScreen
import com.efthemiosprime.pasabayan.features.packages.ui.PackageRequestScreen
import com.efthemiosprime.pasabayan.features.packages.viewmodel.PackageViewModel

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
    val packageUiState by packageViewModel.uiState.collectAsStateWithLifecycle()
    val tabs = MainTabs.forRole(state.currentRole)
    var showCreateOptionsSheet by remember { mutableStateOf(false) }
    var showPackageRequestSheet by remember { mutableStateOf(false) }
    var showErrandRequestSheet by remember { mutableStateOf(false) }

    LaunchedEffect(user) {
        viewModel.initializeRole(user)
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
                        )
                    UserRole.SHIPPER ->
                        ShipperExploreContent(
                            user = user,
                            onSwitchRole = { viewModel.switchRole() },
                        )
                }
                "matches" -> com.efthemiosprime.pasabayan.features.bookings.ui.MatchListScreen(
                    isCarrier = state.currentRole == UserRole.CARRIER,
                    onAction = { action, matchId -> /* TODO: handle booking actions */ },
                )
                "my_trips" -> com.efthemiosprime.pasabayan.features.trips.ui.CarrierMyTripsScreen(
                    onViewTripDetails = { /* Handled by expandable card */ },
                    onCreateTrip = { /* TODO: navigate to trip creation */ },
                )
                "packages" -> com.efthemiosprime.pasabayan.features.packages.ui.PackageListScreen(
                    onViewPackageDetails = { /* Handled by expandable card */ },
                    onCreatePackage = { showCreateOptionsSheet = true },
                    viewModel = packageViewModel,
                )
                "messages" -> StubTabContent(
                    icon = Icons.Outlined.Chat,
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
                    showPackageRequestSheet = true
                },
                onErrandService = {
                    showCreateOptionsSheet = false
                    showErrandRequestSheet = true
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
                            showPackageRequestSheet = false
                            packageViewModel.refreshPackages()
                        }
                    }
                },
                onCancel = { showPackageRequestSheet = false },
                isSubmitting = packageUiState.isSubmittingPackageRequest,
            )
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

