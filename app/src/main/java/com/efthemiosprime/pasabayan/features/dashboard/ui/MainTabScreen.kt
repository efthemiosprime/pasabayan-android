package com.efthemiosprime.pasabayan.features.dashboard.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalShipping
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
                "explore" -> ExploreStubContent(state.currentRole)
                "matches" -> StubTabContent(
                    icon = Icons.Outlined.SwapHoriz,
                    title = stringResource(R.string.dashboard_stub_matches_title),
                    description = stringResource(R.string.dashboard_stub_matches_description),
                )
                "my_trips" -> com.efthemiosprime.pasabayan.features.trips.ui.CarrierMyTripsScreen(
                    onViewTripDetails = { /* TODO: navigate to trip details */ },
                    onCreateTrip = { /* TODO: navigate to trip creation */ },
                )
                "packages" -> com.efthemiosprime.pasabayan.features.packages.ui.PackageListScreen(
                    onViewPackageDetails = { /* TODO: navigate to package details */ },
                    onCreatePackage = { /* TODO: navigate to package creation */ },
                )
                "messages" -> StubTabContent(
                    icon = Icons.Outlined.Chat,
                    title = stringResource(R.string.dashboard_stub_messages_title),
                    description = stringResource(R.string.dashboard_stub_messages_description),
                )
                "profile" -> ProfileStubContent(onLogout = onLogout)
                else -> {}
            }
        }
    }
}

@Composable
private fun ExploreStubContent(role: UserRole) {
    val (title, description) = when (role) {
        UserRole.CARRIER -> Pair(
            stringResource(R.string.dashboard_stub_explore_carrier_title),
            stringResource(R.string.dashboard_stub_explore_carrier_description),
        )
        UserRole.SHIPPER -> Pair(
            stringResource(R.string.dashboard_stub_explore_shipper_title),
            stringResource(R.string.dashboard_stub_explore_shipper_description),
        )
    }
    PEmptyState(
        icon = Icons.Outlined.Explore,
        title = title,
        description = description,
        modifier = Modifier.padding(
            com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing.lg,
        ),
    )
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

@Composable
private fun ProfileStubContent(onLogout: () -> Unit) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing.lg),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(
            com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing.lg,
        ),
    ) {
        PEmptyState(
            icon = Icons.Outlined.Person,
            title = stringResource(R.string.dashboard_stub_profile_title),
            description = stringResource(R.string.dashboard_stub_profile_description),
        )
        com.efthemiosprime.pasabayan.core.designsystem.component.PButton(
            text = stringResource(R.string.auth_sign_out),
            onClick = onLogout,
            style = com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle.Secondary,
        )
    }
}
