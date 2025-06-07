package com.efthemiosprime.pasabayan.ui.screens.dashboard

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.presentation.viewmodel.AuthViewModel
import com.efthemiosprime.pasabayan.presentation.viewmodel.PackageViewModel
import com.efthemiosprime.pasabayan.presentation.viewmodel.RoleViewModel

import com.efthemiosprime.pasabayan.ui.components.packages.PackageListView
import com.efthemiosprime.pasabayan.ui.screens.analytics.AnalyticsScreen

import com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme

/**
 * Shipper Dashboard Screen component
 * Provides shipper-specific navigation and content tabs
 * Matches iOS shipper dashboard layout and functionality
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipperDashboard(
    authViewModel: AuthViewModel,
    packageViewModel: PackageViewModel,
    roleViewModel: RoleViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedMoreItem by remember { mutableIntStateOf(-1) }
    
    val allTabs = listOf(
        TabItem("Home", painterResource(id = R.drawable.home_24)),
        TabItem("Analytics", painterResource(id = R.drawable.analysis)),
        TabItem("Browse", painterResource(id = R.drawable.browse)),
        TabItem("Packages", painterResource(id = R.drawable.carton_24)),
        TabItem("Create", painterResource(id = R.drawable.add)),
        TabItem("Profile", painterResource(id = R.drawable.user))
    )
    
    // Split tabs: first 4 visible, rest in "More"
    val visibleTabs = allTabs.take(4)
    val moreTabs = allTabs.drop(4)
    
    // Create navigation tabs (4 visible + More button)
    val navigationTabs = visibleTabs + TabItem("More", painterResource(id = R.drawable.menu_bar))
    
    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = Color.Blue
            ) {
                navigationTabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        icon = {
                            if (index == 3 && tab.title == "Packages") { // My Packages tab
                                BadgedBox(
                                    badge = {
                                        val myPackages by packageViewModel.myPackageRequests.collectAsState()
                                        if (myPackages.isNotEmpty()) {
                                            Badge { 
                                                Text("${myPackages.size}")
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        painter = tab.icon,
                                        contentDescription = tab.title
                                    )
                                }
                            } else {
                                Icon(
                                    painter = tab.icon,
                                    contentDescription = tab.title
                                )
                            }
                        },
                        label = { Text(tab.title) },
                        selected = selectedTabIndex == index,
                        onClick = { 
                            selectedTabIndex = index
                            if (index == 4) { // More tab
                                selectedMoreItem = -1 // Reset more selection
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Blue,
                            selectedTextColor = Color.Blue
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        when (selectedTabIndex) {
            0 -> Text("Shipper Home Tab") // ShipperHomeTab placeholder
            1 -> AnalyticsScreen(modifier = Modifier.padding(innerPadding))
            2 -> PackageListView(
                modifier = Modifier.padding(innerPadding),
                title = "Available Packages",
                packages = packageViewModel.packageRequests.collectAsState().value
            )
            3 -> PackageListView(
                modifier = Modifier.padding(innerPadding),
                title = "Packages",
                packages = packageViewModel.myPackageRequests.collectAsState().value
            )
            4 -> Text("More Tab View") // MoreTabView placeholder
        }
    }
}



// MARK: - Previews
@Preview("Shipper Dashboard Screen")
@Composable
fun ShipperDashboardScreenPreview() {
    PasabayanTheme {
        // Note: Preview shows static UI only - actual functionality requires ViewModels
        Text("Shipper Dashboard Preview")
    }
} 