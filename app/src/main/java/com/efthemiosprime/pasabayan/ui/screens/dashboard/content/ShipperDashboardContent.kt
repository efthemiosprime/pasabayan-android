package com.efthemiosprime.pasabayan.ui.screens.dashboard.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.data.model.UserRole
import com.efthemiosprime.pasabayan.presentation.viewmodel.AuthViewModel
import com.efthemiosprime.pasabayan.presentation.viewmodel.RoleViewModel
import com.efthemiosprime.pasabayan.presentation.viewmodel.ShipperViewModel
import com.efthemiosprime.pasabayan.ui.screens.analytics.AnalyticsScreen
import com.efthemiosprime.pasabayan.ui.screens.dashboard.BrowseTabContent
import com.efthemiosprime.pasabayan.ui.screens.dashboard.CreateTabContent
import com.efthemiosprime.pasabayan.ui.screens.dashboard.PackagesOrTripsTabContent
import com.efthemiosprime.pasabayan.ui.screens.profile.ProfileScreen
import com.efthemiosprime.pasabayan.ui.screens.dashboard.components.TabNavigationLayout
import com.efthemiosprime.pasabayan.ui.screens.dashboard.components.TabNavigationConfig
import com.efthemiosprime.pasabayan.ui.screens.dashboard.components.TabItem
import com.efthemiosprime.pasabayan.ui.screens.dashboard.home.ShipperHomeContent

/**
 * Shipper Dashboard Content - Mirrors iOS ShipperDashboard (70 lines)
 * Tab navigation layout for shipper role with "More" tab system
 * Visible: Home, Analytics, Browse, Packages, More
 * Hidden under More: Create, Profile
 */
@Composable
fun ShipperDashboardContent(
    viewModel: ShipperViewModel,
    authViewModel: AuthViewModel,
    roleViewModel: RoleViewModel
) {
    val config = TabNavigationConfig(
        visibleTabs = listOf(
            TabItem("Home", painterResource(id = R.drawable.home_24)) { ShipperHomeContent(viewModel, authViewModel, roleViewModel) },
            TabItem("Analytics", painterResource(id = R.drawable.analysis)) { AnalyticsScreen() },
            TabItem("Browse", painterResource(id = R.drawable.browse)) { BrowseTabContent(currentRole = UserRole.SHIPPER) },
            TabItem("Packages", painterResource(id = R.drawable.traveling_24)) { PackagesOrTripsTabContent(currentRole = UserRole.SHIPPER) }
        ),
        moreTabs = listOf(
            TabItem("Create", Icons.Default.Add) { CreateTabContent(currentRole = UserRole.SHIPPER) },
            TabItem("Profile", painterResource(id = R.drawable.user)) { ProfileScreen(authViewModel, roleViewModel) }
        )
    )
    
    TabNavigationLayout(
        config = config,
        accentColor = MaterialTheme.colorScheme.primary
    )
}

 