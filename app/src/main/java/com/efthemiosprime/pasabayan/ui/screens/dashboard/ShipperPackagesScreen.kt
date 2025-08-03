package com.efthemiosprime.pasabayan.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.efthemiosprime.pasabayan.data.model.PackageRequest
import com.efthemiosprime.pasabayan.data.model.PackageRequestStatus
import com.efthemiosprime.pasabayan.presentation.viewmodel.PackageViewModel
import com.efthemiosprime.pasabayan.ui.shared.ScreenContainer
import com.efthemiosprime.pasabayan.ui.screens.dashboard.components.PackageCard
import com.efthemiosprime.pasabayan.ui.screens.packagedetail.PackageDetailScreen
import com.efthemiosprime.pasabayan.ui.screens.findcarriers.FindCarriersScreen
import com.efthemiosprime.pasabayan.ui.screens.match.ViewMatchScreen

/**
 * My Packages Screen - matches the carrier "My Trips" design from iOS
 * Provides comprehensive package management for shippers
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipperPackagesScreen(
    packageViewModel: PackageViewModel = viewModel(),
    onNavigateToCreate: () -> Unit = {},
    onNavigateToDetails: (PackageRequest) -> Unit = {}
) {
    // Collect state
    val myPackages by packageViewModel.myPackageRequests.collectAsState()
    val filteredPackages by packageViewModel.filteredPackages.collectAsState()
    val isLoading by packageViewModel.isLoading.collectAsState()
    val selectedFilter by packageViewModel.selectedFilter.collectAsState()
    val errorMessage by packageViewModel.errorMessage.collectAsState()
    
    // State for navigation to package detail screen
    var selectedPackageForDetails by remember { mutableStateOf<PackageRequest?>(null) }
    
    // State for navigation to find carriers screen
    var selectedPackageForCarriers by remember { mutableStateOf<PackageRequest?>(null) }
    
    // State for navigation to view match screen
    var selectedPackageForMatches by remember { mutableStateOf<PackageRequest?>(null) }
    
    // Show package detail screen if a package is selected
    selectedPackageForDetails?.let { packageRequest ->
        PackageDetailScreen(
            packageRequest = packageRequest,
            onNavigateBack = { 
                selectedPackageForDetails = null
                // Refresh packages list after coming back from details
                packageViewModel.loadPackageRequests()
            }
        )
        return
    }
    
    // Show find carriers screen if a package is selected
    selectedPackageForCarriers?.let { packageRequest ->
        println("🔧 DEBUG: Showing FindCarriersScreen for package ID: ${packageRequest.id}")
        FindCarriersScreen(
            packageRequest = packageRequest,
            onNavigateBack = {
                println("🔧 DEBUG: Navigating back from FindCarriersScreen")
                selectedPackageForCarriers = null
                // Refresh packages to update compatible trips count
                packageViewModel.loadPackageRequests()
            }
        )
        return
    }
    
    // Show view match screen if a package is selected
    selectedPackageForMatches?.let { packageRequest ->
        println("🔧 DEBUG: Showing ViewMatchScreen for package ID: ${packageRequest.id}")
        ViewMatchScreen(
            packageRequest = packageRequest,
            onNavigateBack = {
                println("🔧 DEBUG: Navigating back from ViewMatchScreen")
                selectedPackageForMatches = null
                // Refresh packages to update match status
                packageViewModel.loadPackageRequests()
            }
        )
        return
    }
    
    // Load data when screen appears
    LaunchedEffect(Unit) {
        packageViewModel.loadPackageRequests()
    }
    
    ScreenContainer {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("My Packages") },
                    actions = {
                        // Debug button (for development)
                        TextButton(onClick = { debugPackageFilters(packageViewModel) }) {
                            Text("Debug", style = MaterialTheme.typography.labelSmall)
                        }
                        
                        // Create package FAB matching iOS "+" button
                        IconButton(onClick = onNavigateToCreate) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create Package",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Filter Section (matches iOS filter tabs)
                PackageFilterRow(
                    packages = myPackages,
                    selectedFilter = selectedFilter,
                    onFilterSelected = packageViewModel::setFilter
                )
                
                HorizontalDivider()
                
                // Content Section
                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator()
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Loading packages...")
                                }
                            }
                        }
                        
                        errorMessage != null -> {
                            val currentError = errorMessage // Store in local variable to avoid smart cast issues
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "Error: $currentError",
                                        color = MaterialTheme.colorScheme.error,
                                        textAlign = TextAlign.Center
                                    )
                                    
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Button(onClick = { packageViewModel.loadPackageRequests() }) {
                                            Text("Retry")
                                        }
                                        
                                        OutlinedButton(onClick = { packageViewModel.debugServerConnection() }) {
                                            Text("Debug")
                                        }
                                    }
                                    
                                    if (currentError?.contains("500") == true) {
                                        Text(
                                            text = "💡 HTTP 500 means your server has an internal error.\nCheck your server logs for the /api/packages endpoint.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                        
                        filteredPackages.isEmpty() -> {
                            EmptyPackagesState(
                                selectedFilter = selectedFilter,
                                onCreateClick = onNavigateToCreate
                            )
                        }
                        
                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                                items(filteredPackages) { packageRequest ->
                    PackageCard(
                        packageRequest = packageRequest,
                        onDetailsClick = { 
                            selectedPackageForDetails = packageRequest
                        },
                        onFindCarriersClick = { 
                            println("🔧 DEBUG: Find Carriers clicked for package ID: ${packageRequest.id}")
                            selectedPackageForCarriers = packageRequest
                        },
                        onViewMatchClick = {
                            println("🔧 DEBUG: View Match clicked for package ID: ${packageRequest.id}")
                            selectedPackageForMatches = packageRequest
                        }
                    )
                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Debug function to help troubleshoot filtering issues
 */
private fun debugPackageFilters(viewModel: PackageViewModel) {
    println("🔧 DEBUG: Package Filter Debug Info")
    println("   📦 Total packages: ${viewModel.packageRequests.value.size}")
    println("   👤 My packages: ${viewModel.myPackageRequests.value.size}")
    println("   🔄 Loading: ${viewModel.isLoading.value}")
    println("   ❌ Error: ${viewModel.errorMessage.value ?: "none"}")
    println("   🎯 Selected filter: ${viewModel.selectedFilter.value?.displayName ?: "All"}")
    println("   📊 Filtered packages: ${viewModel.filteredPackages.value.size}")
    
    if (viewModel.myPackageRequests.value.isNotEmpty()) {
        println("   👤 My package details:")
        viewModel.myPackageRequests.value.forEachIndexed { index, pkg ->
            println("      ${index + 1}. ID: ${pkg.id}, Status: ${pkg.status.displayName}, Title: ${pkg.title}")
        }
    } else {
        println("   ❌ No packages found for current user")
    }
}

@Composable
fun PackageFilterRow(
    packages: List<PackageRequest>,
    selectedFilter: PackageRequestStatus?,
    onFilterSelected: (PackageRequestStatus?) -> Unit
) {
    // Calculate filter counts
    val filterOptions = remember(packages) {
        listOf(
            FilterOption("All", packages.size, null),
            FilterOption(
                "Open", 
                packages.count { it.status in listOf(PackageRequestStatus.OPEN, PackageRequestStatus.PENDING_REQUEST) },
                PackageRequestStatus.OPEN
            ),
            FilterOption("Matched", packages.count { it.status == PackageRequestStatus.MATCHED }, PackageRequestStatus.MATCHED),
            FilterOption("Delivered", packages.count { it.status == PackageRequestStatus.DELIVERED }, PackageRequestStatus.DELIVERED)
        )
    }
    
    LazyRow(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(filterOptions) { option ->
            FilterChip(
                selected = selectedFilter == option.status,
                onClick = { onFilterSelected(option.status) },
                label = { Text("${option.title} (${option.count})") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

private data class FilterOption(
    val title: String,
    val count: Int,
    val status: PackageRequestStatus?
)

@Composable
fun EmptyPackagesState(
    selectedFilter: PackageRequestStatus?,
    onCreateClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Inbox,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = if (selectedFilter == null) {
                    "No packages yet"
                } else {
                    "No ${selectedFilter.displayName.lowercase()} packages"
                },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = if (selectedFilter == null) {
                    "Create your first package request to start shipping with carriers."
                } else {
                    "No packages found with ${selectedFilter.displayName.lowercase()} status."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            if (selectedFilter == null) {
                Button(onClick = onCreateClick) {
                    Text("Create Package")
                }
            }
        }
    }
}

 