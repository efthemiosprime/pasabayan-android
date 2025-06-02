package com.efthemiosprime.pasabayan.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.data.model.*
import com.efthemiosprime.pasabayan.presentation.viewmodel.*
import com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme
import com.efthemiosprime.pasabayan.ui.components.*
import kotlinx.coroutines.launch

/**
 * Main dashboard screen that exactly mirrors iOS DashboardView
 * Provides role-based navigation for Shippers and Carriers
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    authViewModel: AuthViewModel
) {
    val roleViewModel: RoleViewModel = viewModel { RoleViewModel() }
    val packageViewModel: PackageViewModel = viewModel { PackageViewModel() }
    val carrierViewModel: CarrierViewModel = viewModel { CarrierViewModel() }
    
    val currentRole by roleViewModel.currentRole.collectAsState()
    
    when (currentRole) {
        UserRole.SHIPPER -> ShipperDashboard(
            authViewModel = authViewModel,
            packageViewModel = packageViewModel,
            roleViewModel = roleViewModel
        )
        UserRole.CARRIER -> CarrierDashboard(
            authViewModel = authViewModel,
            carrierViewModel = carrierViewModel,
            roleViewModel = roleViewModel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShipperDashboard(
    authViewModel: AuthViewModel,
    packageViewModel: PackageViewModel,
    roleViewModel: RoleViewModel
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
            0 -> ShipperHomeTab(
                modifier = Modifier.padding(innerPadding),
                authViewModel = authViewModel,
                packageViewModel = packageViewModel,
                roleViewModel = roleViewModel
            )
            1 -> AnalyticsView(modifier = Modifier.padding(innerPadding))
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
            4 -> MoreTabView(
                modifier = Modifier.padding(innerPadding),
                moreTabs = moreTabs,
                selectedMoreItem = selectedMoreItem,
                onMoreItemSelected = { selectedMoreItem = it },
                authViewModel = authViewModel,
                packageViewModel = packageViewModel,
                roleViewModel = roleViewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CarrierDashboard(
    authViewModel: AuthViewModel,
    carrierViewModel: CarrierViewModel,
    roleViewModel: RoleViewModel
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedMoreItem by remember { mutableIntStateOf(-1) }
    
    val allTabs = listOf(
        TabItem("Home", painterResource(id = R.drawable.home_24)),
        TabItem("Analytics", painterResource(id = R.drawable.analysis)),
        TabItem("Trips", painterResource(id = R.drawable.traveling_24)),
        TabItem("Matches", painterResource(id = R.drawable.browse)),
        TabItem("Earnings", painterResource(id = R.drawable.revenue)),
        TabItem("Profile", painterResource(id = R.drawable.user))
    )
    
    // Split tabs: first 4 visible, rest in "More"
    val visibleTabs = allTabs.take(4)
    val moreTabs = allTabs.drop(4)
    
    // Create navigation tabs (4 visible + More button)
    val navigationTabs = visibleTabs + TabItem("More", painterResource(id = R.drawable.menu_bar))
    
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = Color.Green
            ) {
                navigationTabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        icon = {
                            if (index == 3 && tab.title == "Matches") { // Matches tab
                                BadgedBox(
                                    badge = {
                                        val activeBookings by carrierViewModel.activeBookings.collectAsState()
                                        if (activeBookings.isNotEmpty()) {
                                            Badge { 
                                                Text("${activeBookings.size}")
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
                            selectedIconColor = Color.Green,
                            selectedTextColor = Color.Green
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        when (selectedTabIndex) {
            0 -> CarrierHomeTab(
                modifier = Modifier.padding(innerPadding),
                authViewModel = authViewModel,
                carrierViewModel = carrierViewModel,
                roleViewModel = roleViewModel
            )
            1 -> AnalyticsView(modifier = Modifier.padding(innerPadding))
            2 -> CarrierTripsView(modifier = Modifier.padding(innerPadding))
            3 -> CarrierBookingsView(modifier = Modifier.padding(innerPadding))
            4 -> MoreTabView(
                modifier = Modifier.padding(innerPadding),
                moreTabs = moreTabs,
                selectedMoreItem = selectedMoreItem,
                onMoreItemSelected = { selectedMoreItem = it },
                authViewModel = authViewModel,
                carrierViewModel = carrierViewModel,
                roleViewModel = roleViewModel
            )
        }
    }
}

/**
 * Shipper Home Tab - Exact iOS implementation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShipperHomeTab(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    packageViewModel: PackageViewModel,
    roleViewModel: RoleViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val myPackages by packageViewModel.myPackageRequests.collectAsState()
    val scope = rememberCoroutineScope()
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            // Role Switcher Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            AsyncImage(
                                model = currentUser?.avatar,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                                fallback = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery)
                            )
                            
                            VerificationBadgeIcon()
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
                            Text(
                                text = "Welcome back!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Text(
                                text = currentUser?.name ?: "User",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    
                    // Role Switcher
                    RoleSwitcherView(roleViewModel = roleViewModel)
                }
            }
        }
        
        item {
            // Quick Stats Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        title = "Active Packages",
                        value = "${packageViewModel.getMatchedRequests().size + packageViewModel.getBookedRequests().size + packageViewModel.getInTransitRequests().size}",
                        icon = Icons.Default.LocalShipping,
                        color = Color.Blue,
                        modifier = Modifier.weight(1f)
                    )
                    
                    StatCard(
                        title = "Delivered",
                        value = "${packageViewModel.getDeliveredRequests().size}",
                        icon = Icons.Default.CheckCircle,
                        color = Color.Green,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        title = "Pending",
                        value = "${packageViewModel.getPendingRequests().size}",
                        icon = Icons.Default.Schedule,
                        color = Color(0xFFFFA726),
                        modifier = Modifier.weight(1f)
                    )
                    
                    StatCard(
                        title = "Total Requests",
                        value = "${myPackages.size}",
                        icon = Icons.Default.Inventory,
                        color = Color(0xFF9C27B0),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        item {
            // Recent Activity Section
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Recent Package Requests",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                
                if (myPackages.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.Inventory2,
                        title = "No packages yet",
                        description = "Start by creating a package request or browse available trips."
                    )
                } else {
                    myPackages.take(3).forEach { packageRequest ->
                        PackageRequestCard(packageRequest = packageRequest)
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

/**
 * Carrier Home Tab - Exact iOS implementation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CarrierHomeTab(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    carrierViewModel: CarrierViewModel,
    roleViewModel: RoleViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val trips by carrierViewModel.trips.collectAsState()
    val totalEarnings by carrierViewModel.totalEarnings.collectAsState()
    val activeBookings by carrierViewModel.activeBookings.collectAsState()
    val averageRatingText = carrierViewModel.averageRatingText
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        scope.launch {
            carrierViewModel.loadCarrierProfile()
            carrierViewModel.loadCarrierStats()
            carrierViewModel.loadTrips()
            carrierViewModel.loadActiveBookings()
        }
    }
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            // Role Switcher Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White) // Set your desired background color here


            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            AsyncImage(
                                model = currentUser?.avatar,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                                fallback = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery)
                            )
                            
                            VerificationBadgeIcon()
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
                            Text(
                                text = "Welcome back, Carrier!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Text(
                                text = currentUser?.name ?: "User",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    
                    // Role Switcher
                    RoleSwitcherView(roleViewModel = roleViewModel)
                }
            }
        }
        
        item {
            // Carrier Status Card
            CarrierStatusCard(carrierViewModel = carrierViewModel)
        }
        
        item {
            // Carrier Stats Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        title = "Active Trips",
                        value = "${trips.filter { it.status == TripStatus.PLANNED }.size}",
                        icon = Icons.Default.DriveEta,
                        color = Color.Green,
                        modifier = Modifier.weight(1f)
                    )
                    
                    StatCard(
                        title = "Total Earnings",
                        value = totalEarnings,
                        icon = Icons.Default.AttachMoney,
                        color = Color.Blue,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        title = "Active Matches",
                        value = "${activeBookings.size}",
                        icon = Icons.Default.Description,
                        color = Color(0xFFFFA726),
                        modifier = Modifier.weight(1f)
                    )
                    
                    StatCard(
                        title = "Rating",
                        value = averageRatingText,
                        icon = Icons.Default.Star,
                        color = Color(0xFFFFD54F),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

/**
 * More Tab View - iOS-style additional items screen
 */
@Composable
private fun MoreTabView(
    modifier: Modifier = Modifier,
    moreTabs: List<TabItem>,
    selectedMoreItem: Int,
    onMoreItemSelected: (Int) -> Unit,
    authViewModel: AuthViewModel,
    packageViewModel: PackageViewModel? = null,
    carrierViewModel: CarrierViewModel? = null,
    roleViewModel: RoleViewModel
) {
    if (selectedMoreItem >= 0 && selectedMoreItem < moreTabs.size) {
        // Show selected item content
        when (moreTabs[selectedMoreItem].title) {
            "Create" -> CreatePackageView(modifier = modifier)
            "Earnings" -> CarrierEarningsView(modifier = modifier)
            "Profile" -> ProfileView(
                modifier = modifier,
                authViewModel = authViewModel,
                roleViewModel = roleViewModel
            )
            else -> {
                // Default placeholder for other items
                Box(
                    modifier = modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = moreTabs[selectedMoreItem].title,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
        }
    } else {
        // Show "More" items list
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "More",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            items(moreTabs.size) { index ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    onClick = { onMoreItemSelected(index) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White) // Set your desired background color here

                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            painter = moreTabs[index].icon,
                            contentDescription = moreTabs[index].title,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Text(
                            text = moreTabs[index].title,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Navigate",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// All supporting components follow...
data class TabItem(
    val title: String,
    val icon: Painter
)

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    PasabayanTheme {
        DashboardContent(
            user = null,
            onSignOut = { }
        )
    }
}

@Preview(showBackground = true, name = "Shipper Dashboard - Light")
@Composable
fun ShipperDashboardPreview() {
    PasabayanTheme {
        ShipperHomeTabContent(
            user = sampleUser,
            onSignOut = { }
        )
    }
}

@Preview(showBackground = true, name = "Carrier Dashboard - Light")
@Composable
fun CarrierDashboardPreview() {
    PasabayanTheme {
        CarrierHomeTabContent(
            user = sampleUser,
            onSignOut = { }
        )
    }
}

@Preview(showBackground = true, name = "Dashboard - Dark Theme")
@Composable
fun DashboardDarkPreview() {
    PasabayanTheme(darkTheme = true) {
        ShipperHomeTabContent(
            user = sampleUser,
            onSignOut = { }
        )
    }
}

@Preview(
    showBackground = true,
    name = "Dashboard - Tablet",
    widthDp = 1024,
    heightDp = 768
)
@Composable
fun DashboardTabletPreview() {
    PasabayanTheme {
        ShipperHomeTabContent(
            user = sampleUser,
            onSignOut = { }
        )
    }
}

// Sample data for previews
private val sampleUser = User(
    id = 1,
    name = "Maria Santos",
    email = "maria.santos@example.com",
    avatar = null,
    phone = "+63917123456",
    phoneVerified = true,
    profileCompleted = true,
    provider = "google",
    providerId = "123456789",
    emailVerifiedAt = "2024-01-01T00:00:00Z",
    createdAt = "2024-01-01T00:00:00Z",
    updatedAt = "2024-01-01T00:00:00Z",
    userTypes = listOf("shipper", "carrier"),
    isActiveCarrier = true,
    isActiveShipper = true,
    rating = 4.7,
    totalRatings = 42,
    verificationLevel = "verified"
)

// Placeholder composables for preview
@Composable
private fun ShipperHomeTabContent(
    user: User?,
    onSignOut: () -> Unit
) {
    Box(
        modifier = Modifier
                .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)

        ) {
            Text(
                text = "Shipper Dashboard",
                style = MaterialTheme.typography.headlineMedium
            )
            user?.let {
                Text(
                    text = "Welcome, ${it.name}!",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Rating: ${it.displayRating}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun CarrierHomeTabContent(
    user: User?,
    onSignOut: () -> Unit
) {
    Box(
        modifier = Modifier
                .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Carrier Dashboard",
                style = MaterialTheme.typography.headlineMedium
            )
            user?.let {
                Text(
                    text = "Welcome, ${it.name}!",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Rating: ${it.displayRating}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// Placeholder for the old preview function
@Composable
private fun DashboardContent(
    user: User?,
    onSignOut: () -> Unit
) {
    // Simple placeholder for preview
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Dashboard Preview")
    }
} 