package com.efthemiosprime.pasabayan.ui.screens.dashboard.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.data.model.UserRole
import com.efthemiosprime.pasabayan.presentation.viewmodel.AuthViewModel
import com.efthemiosprime.pasabayan.presentation.viewmodel.CarrierViewModel
import com.efthemiosprime.pasabayan.presentation.viewmodel.RoleViewModel
import com.efthemiosprime.pasabayan.ui.screens.analytics.AnalyticsScreen
import com.efthemiosprime.pasabayan.ui.screens.dashboard.BrowseTabContent
import com.efthemiosprime.pasabayan.ui.screens.dashboard.PackagesOrTripsTabContent
import com.efthemiosprime.pasabayan.ui.screens.profile.ProfileScreen
import com.efthemiosprime.pasabayan.ui.screens.dashboard.components.TabNavigationLayout
import com.efthemiosprime.pasabayan.ui.screens.dashboard.components.TabNavigationConfig
import com.efthemiosprime.pasabayan.ui.screens.dashboard.components.TabItem
import com.efthemiosprime.pasabayan.ui.screens.dashboard.home.CarrierHomeContent

/**
 * Carrier Dashboard Content - Mirrors iOS CarrierDashboard (70 lines)
 * Tab navigation layout for carrier role with "More" tab system
 * Visible: Home, Analytics, Trips, Matches, More
 * Hidden under More: Earnings, Profile
 */
@Composable
fun CarrierDashboardContent(
    viewModel: CarrierViewModel,
    authViewModel: AuthViewModel,
    roleViewModel: RoleViewModel
) {
    val config = TabNavigationConfig(
        visibleTabs = listOf(
            TabItem("Home", painterResource(id = R.drawable.home_24)) { CarrierHomeContent(viewModel, authViewModel, roleViewModel) },
            TabItem("Analytics", painterResource(id = R.drawable.analysis)) { AnalyticsScreen() },
            TabItem("Trips", painterResource(id = R.drawable.traveling_24)) { PackagesOrTripsTabContent(currentRole = UserRole.CARRIER) },
            TabItem("Matches", painterResource(id = R.drawable.browse)) { BrowseTabContent(currentRole = UserRole.CARRIER, authViewModel = authViewModel) }
        ),
        moreTabs = listOf(
            TabItem("Earnings", painterResource(id = R.drawable.revenue)) { EarningsTabContent() },
            TabItem("Profile", painterResource(id = R.drawable.user)) { ProfileScreen(authViewModel, roleViewModel) }
        )
    )
    
    TabNavigationLayout(
        config = config,
        accentColor = Color(0xFF4CAF50) // Green for carriers
    )
}

@Composable
private fun EarningsTabContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Earnings Coming Soon",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

 