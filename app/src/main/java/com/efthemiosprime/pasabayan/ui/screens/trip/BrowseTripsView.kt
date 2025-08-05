package com.efthemiosprime.pasabayan.ui.screens.trip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.efthemiosprime.pasabayan.data.model.*
import com.efthemiosprime.pasabayan.presentation.viewmodel.BrowseTripsViewModel
import com.efthemiosprime.pasabayan.presentation.viewmodel.PackageViewModel
import com.efthemiosprime.pasabayan.presentation.viewmodel.AuthViewModel
import com.efthemiosprime.pasabayan.ui.shared.EmptyStateView
import com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme
import com.efthemiosprime.pasabayan.ui.components.TripCard
import com.efthemiosprime.pasabayan.ui.screens.booking.MatchCreationScreen

/**
 * BrowseTripsView - Exact Mirror of iOS Implementation
 * 
 * Features:
 * - Search Bar with clear functionality
 * - Transportation Method Filter Controls
 * - Trips List with TripCard components
 * - Direct Booking functionality
 * - Loading/Error/Empty states
 * - Pull to refresh
 * - Filter sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseTripsView(
    packageViewModel: PackageViewModel,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: BrowseTripsViewModel = viewModel { BrowseTripsViewModel(context) }
    
    // Collect states
    val filteredTrips by viewModel.filteredTrips.collectAsState()
    val loadingState by viewModel.loadingState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterSheetState by viewModel.filterSheetState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    // Match Creation State
    var selectedTrip by remember { mutableStateOf<Trip?>(null) }
    var showingMatchCreation by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        println("🔍 BrowseTripsView: LaunchedEffect triggered")
        viewModel.loadAvailableTrips()
        packageViewModel.loadPackageRequests()
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search Bar
        SearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = viewModel::updateSearchQuery,
            onClearSearch = viewModel::clearSearch,
            onFilterClick = viewModel::showFilterSheet
        )
        
        // Filter Controls
        FilterControls(
            filters = filterSheetState.filters,
            onFilterChange = { filters ->
                viewModel.updateTempFilters(filters)
                viewModel.applyFilters()
            },
            onFilterClick = viewModel::showFilterSheet
        )
        
        // Trips List
        TripsList(
            trips = filteredTrips,
            isLoading = loadingState.isLoading,
            errorMessage = errorMessage,
            onTripBookClick = { trip ->
                selectedTrip = trip
                showingMatchCreation = true
            },
            onRefresh = viewModel::loadAvailableTrips
        )
    }
    
    // Filter Sheet
    if (filterSheetState.isVisible) {
        FilterSheet(
            filters = filterSheetState.tempFilters,
            onFiltersChange = viewModel::updateTempFilters,
            onApplyFilters = {
                viewModel.applyFilters()
                viewModel.hideFilterSheet()
            },
            onClearFilters = {
                viewModel.clearFilters()
                viewModel.hideFilterSheet()
            },
            onDismiss = viewModel::hideFilterSheet
        )
    }
    
    // Match Creation Screen - iOS-style package selection + price + send
    if (showingMatchCreation && selectedTrip != null) {
        MatchCreationScreen(
            trip = selectedTrip!!,
            onNavigateBack = { 
                showingMatchCreation = false
                selectedTrip = null
            },
            onMatchCreated = {
                showingMatchCreation = false
                selectedTrip = null
                viewModel.loadAvailableTrips() // Refresh trips
                packageViewModel.loadPackageRequests() // Refresh packages
            }
        )
    }
}

/**
 * Search Bar Component - Mirrors iOS searchBar
 */
@Composable
private fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Search origin, destination...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = onClearSearch) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
        
        OutlinedButton(
            onClick = onFilterClick,
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filter",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Filter")
        }
    }
}

/**
 * Filter Controls Component - Mirrors iOS filterControls
 */
@Composable
private fun FilterControls(
    filters: TripFilters,
    onFilterChange: (TripFilters) -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 8.dp)
    ) {
        // Active Filters Count
        if (filters.hasActiveFilters()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Active Filters: ${filters.getActiveFiltersDescription()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                TextButton(
                    onClick = { onFilterChange(TripFilters.default()) }
                ) {
                    Text(
                        text = "Clear All",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        
        // Transportation Method Quick Filters
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(TransportationMethod.values()) { method ->
                FilterChip(
                    selected = filters.transportationMethod == method,
                    onClick = {
                        val newTransportationMethod = if (filters.transportationMethod == method) {
                            null
                        } else {
                            method
                        }
                        onFilterChange(filters.copy(transportationMethod = newTransportationMethod))
                    },
                    label = { Text(method.displayName) },
                    leadingIcon = {
                        Text(
                            text = method.icon,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                )
            }
        }
    }
}

/**
 * Trips List Component - Mirrors iOS tripsList
 */
@Composable
private fun TripsList(
    trips: List<Trip>,
    isLoading: Boolean,
    errorMessage: String?,
    onTripBookClick: (Trip) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Debug logging
    LaunchedEffect(isLoading, errorMessage, trips.size) {
        println("🔍 TripsList State: isLoading=$isLoading, errorMessage=$errorMessage, trips.size=${trips.size}")
        if (trips.isNotEmpty()) {
            println("🔍 First trip: ${trips.first().originCity} -> ${trips.first().destinationCity}")
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading trips...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Error",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Error Loading Trips",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRefresh) {
                        Text("Try Again")
                    }
                }
            }
            
            trips.isEmpty() -> {
                EmptyStateView(
                    icon = Icons.Default.DirectionsCar,
                    title = "No Trips Found",
                    description = "Try adjusting your filters or check back later for new trips."
                )
            }
            
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(trips) { trip ->
                        TripCard(
                            trip = trip,
                            onTap = { onTripBookClick(trip) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Filter Sheet Component - Exactly Mirrors iOS FilterSheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSheet(
    filters: TripFilters,
    onFiltersChange: (TripFilters) -> Unit,
    onApplyFilters: () -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    var tempFilters by remember { mutableStateOf(filters) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                
                Text(
                    text = "Filter Trips",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                
                TextButton(
                    onClick = {
                        onFiltersChange(tempFilters)
                        onApplyFilters()
                    }
                ) {
                    Text("Apply", fontWeight = FontWeight.SemiBold)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Route Filters
                item {
                    FilterSection(title = "Route") {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = tempFilters.origin,
                                onValueChange = { tempFilters = tempFilters.copy(origin = it) },
                                label = { Text("Origin city or country") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            OutlinedTextField(
                                value = tempFilters.destination,
                                onValueChange = { tempFilters = tempFilters.copy(destination = it) },
                                label = { Text("Destination city or country") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                
                // Date Filter
                item {
                    FilterSection(title = "Departure Date") {
                        var showDatePicker by remember { mutableStateOf(false) }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = { showDatePicker = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    tempFilters.departureDate ?: "Select Date"
                                )
                            }
                            
                            if (tempFilters.departureDate != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedButton(
                                    onClick = { tempFilters = tempFilters.copy(departureDate = null) }
                                ) {
                                    Text("Clear")
                                }
                            }
                        }
                        
                        // Date picker would be implemented here
                        // For now, simplified version
                    }
                }
                
                // Transportation Method
                item {
                    FilterSection(title = "Transportation Method") {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.height(120.dp)
                        ) {
                            items(TransportationMethod.values()) { method ->
                                FilterChip(
                                    selected = tempFilters.transportationMethod == method,
                                    onClick = {
                                        tempFilters = tempFilters.copy(
                                            transportationMethod = if (tempFilters.transportationMethod == method) null else method
                                        )
                                    },
                                    label = { Text(method.displayName) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
                
                // Capacity Filters
                item {
                    FilterSection(title = "Capacity Requirements") {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Weight Capacity
                            FilterRangeSection(
                                title = "Weight (kg)",
                                minValue = tempFilters.minWeightCapacity,
                                maxValue = tempFilters.maxWeightCapacity,
                                onMinChange = { tempFilters = tempFilters.copy(minWeightCapacity = it) },
                                onMaxChange = { tempFilters = tempFilters.copy(maxWeightCapacity = it) }
                            )
                            
                            // Space Capacity
                            FilterRangeSection(
                                title = "Space (liters)",
                                minValue = tempFilters.minSpaceCapacity,
                                maxValue = tempFilters.maxSpaceCapacity,
                                onMinChange = { tempFilters = tempFilters.copy(minSpaceCapacity = it) },
                                onMaxChange = { tempFilters = tempFilters.copy(maxSpaceCapacity = it) }
                            )
                        }
                    }
                }
                
                // Price Filter
                item {
                    FilterSection(title = "Price per kg") {
                        FilterRangeSection(
                            title = "Price Range ($)",
                            minValue = tempFilters.minPricePerKg,
                            maxValue = tempFilters.maxPricePerKg,
                            onMinChange = { tempFilters = tempFilters.copy(minPricePerKg = it) },
                            onMaxChange = { tempFilters = tempFilters.copy(maxPricePerKg = it) },
                            isCurrency = true
                        )
                    }
                }
                
                // Sorting
                item {
                    FilterSection(title = "Sort By") {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Sort By Options
                            Row(modifier = Modifier.fillMaxWidth()) {
                                TripSortOption.values().forEach { option ->
                                    FilterChip(
                                        selected = tempFilters.sortBy == option,
                                        onClick = { tempFilters = tempFilters.copy(sortBy = option) },
                                        label = { Text(option.displayName, style = MaterialTheme.typography.bodySmall) },
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                }
                            }
                            
                            // Sort Order
                            Row(modifier = Modifier.fillMaxWidth()) {
                                SortOrder.values().forEach { order ->
                                    FilterChip(
                                        selected = tempFilters.sortOrder == order,
                                        onClick = { tempFilters = tempFilters.copy(sortOrder = order) },
                                        label = { Text(order.displayName, style = MaterialTheme.typography.bodySmall) },
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Clear Filters Button
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedButton(
                        onClick = {
                            tempFilters = TripFilters.default()
                            onClearFilters()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear All Filters")
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
}

@Composable
private fun FilterRangeSection(
    title: String,
    minValue: Double?,
    maxValue: Double?,
    onMinChange: (Double?) -> Unit,
    onMaxChange: (Double?) -> Unit,
    isCurrency: Boolean = false
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = minValue?.toString() ?: "",
                onValueChange = { 
                    onMinChange(it.toDoubleOrNull())
                },
                label = { Text("Min") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
            
            Text(
                text = "to",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            OutlinedTextField(
                value = maxValue?.toString() ?: "",
                onValueChange = { 
                    onMaxChange(it.toDoubleOrNull())
                },
                label = { Text("Max") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

 