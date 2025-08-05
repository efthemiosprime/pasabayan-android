package com.efthemiosprime.pasabayan.ui.screens.dashboard.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.data.model.PackageRequest
import com.efthemiosprime.pasabayan.data.model.UserRole
import com.efthemiosprime.pasabayan.data.repository.PackageRepositoryImpl
import com.efthemiosprime.pasabayan.presentation.viewmodel.AuthViewModel
import com.efthemiosprime.pasabayan.presentation.viewmodel.RoleViewModel
import com.efthemiosprime.pasabayan.presentation.viewmodel.ShipperViewModel

import com.efthemiosprime.pasabayan.ui.screens.dashboard.CreateTabContent
import com.efthemiosprime.pasabayan.ui.screens.dashboard.ShipperPackagesScreen
import com.efthemiosprime.pasabayan.ui.screens.packagerequest.DeliveryRequestScreen
import com.efthemiosprime.pasabayan.ui.screens.packagerequest.PackageRequestViewModel
import com.efthemiosprime.pasabayan.ui.screens.profile.ProfileScreen
import com.efthemiosprime.pasabayan.ui.screens.dashboard.components.TabNavigationLayout
import com.efthemiosprime.pasabayan.ui.screens.dashboard.components.TabNavigationConfig
import com.efthemiosprime.pasabayan.ui.screens.dashboard.components.TabItem
import com.efthemiosprime.pasabayan.ui.screens.dashboard.home.ShipperHomeContent
import com.efthemiosprime.pasabayan.ui.screens.trip.BrowseTripsView

/**
 * Shipper Dashboard Content - Mirrors iOS ShipperDashboard (70 lines)
 * Tab navigation layout for shipper role with "More" tab system
 * Visible: Home, Packages, Browse (trips compatible with packages), More
 * Hidden under More: Create, Profile
 */
@Composable
fun ShipperDashboardContent(
    viewModel: ShipperViewModel,
    authViewModel: AuthViewModel,
    roleViewModel: RoleViewModel
) {
    val context = LocalContext.current
    
    // State for navigation to create package screen
    var showCreatePackageScreen by remember { mutableStateOf(false) }
    
    // State for initial selected tab (0 = Home, 1 = Packages, 2 = Browse)
    var initialSelectedTab by remember { mutableIntStateOf(0) }
    
    if (showCreatePackageScreen) {
        // Create PackageRequestViewModel with Application parameter
        val packageRequestViewModel: PackageRequestViewModel = viewModel { 
            PackageRequestViewModel(context.applicationContext as android.app.Application) 
        }
        
        // Show full-screen package creation
        DeliveryRequestScreen(
            viewModel = packageRequestViewModel,
            onNavigateBack = { 
                showCreatePackageScreen = false
                // Refresh packages list after coming back from creation
                viewModel.packageViewModel.loadPackageRequests()
            },
            onNavigateBackWithSuccess = { createdPackage, message ->
                showCreatePackageScreen = false
                // Switch to Packages tab (index 1) to show the new package
                initialSelectedTab = 1
                // Refresh packages list to show the new package
                viewModel.packageViewModel.loadPackageRequests()
                // Log successful creation for debugging
                println("🎉 Package created successfully: ${createdPackage.title} (ID: ${createdPackage.id})")
                println("📦 Message: $message")
            }
        )
    } else {
        // Show normal dashboard tabs
        val config = TabNavigationConfig(
            visibleTabs = listOf(
                TabItem("Home", painterResource(id = R.drawable.home_24)) { 
                    ShipperHomeContent(viewModel, authViewModel, roleViewModel) 
                },
                TabItem("Packages", painterResource(id = R.drawable.traveling_24)) { 
                    ShipperPackagesScreen(
                        packageViewModel = viewModel.packageViewModel,
                        onNavigateToCreate = { 
                            showCreatePackageScreen = true 
                        },
                        onNavigateToDetails = { packageRequest ->
                            // Navigation handled internally by ShipperPackagesScreen
                        }
                    ) 
                },
                TabItem("Browse", painterResource(id = R.drawable.browse)) { 
                    BrowseTripsView(packageViewModel = viewModel.packageViewModel, authViewModel = authViewModel) 
                }
            ),
            moreTabs = listOf(
                TabItem("Create", Icons.Default.Add) { 
                    CreateTabContent(currentRole = UserRole.SHIPPER) 
                },
                TabItem("Profile", painterResource(id = R.drawable.user)) { 
                    ProfileScreen(authViewModel, roleViewModel) 
                }
            )
        )
        
        TabNavigationLayout(
            config = config,
            accentColor = MaterialTheme.colorScheme.primary,
            initialSelectedTabIndex = initialSelectedTab
        )
    }
}

 