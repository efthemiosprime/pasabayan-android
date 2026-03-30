package com.efthemiosprime.pasabayan.features.dashboard.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.component.PEmptyState
import com.efthemiosprime.pasabayan.core.designsystem.component.PScaffold
import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole
import com.efthemiosprime.pasabayan.core.session.AuthUser
import com.efthemiosprime.pasabayan.features.dashboard.components.DashboardTopBar
import com.efthemiosprime.pasabayan.features.dashboard.components.PasabayanBottomBar
import com.efthemiosprime.pasabayan.features.dashboard.model.MainTabs
import com.efthemiosprime.pasabayan.features.dashboard.viewmodel.DashboardViewModel

/**
 * Main tabbed dashboard shell — replaces the Phase 1 placeholder.
 * 5 tabs per role, matching iOS tab order.
 */
@Composable
fun MainTabScreen(
    user: AuthUser,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val tabs = MainTabs.forRole(state.currentRole)

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
                    currentUserId = user.id.toInt(),
                    onAction = { action, matchId -> /* TODO: handle booking actions */ },
                )
                "my_trips" -> com.efthemiosprime.pasabayan.features.trips.ui.CarrierMyTripsScreen(
                    onViewTripDetails = { /* Handled by expandable card */ },
                    onCreateTrip = { /* TODO: navigate to trip creation */ },
                )
                "packages" -> com.efthemiosprime.pasabayan.features.packages.ui.PackageListScreen(
                    onViewPackageDetails = { /* Handled by expandable card */ },
                    onCreatePackage = { /* TODO: navigate to package creation */ },
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
}

@Composable
private fun StubTabContent(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
) {
    PEmptyState(
        icon = icon,
        title = title,
        description = description,
        modifier = Modifier.padding(
            com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing.lg,
        ),
    )
}

