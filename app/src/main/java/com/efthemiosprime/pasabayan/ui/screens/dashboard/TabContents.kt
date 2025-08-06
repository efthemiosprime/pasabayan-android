package com.efthemiosprime.pasabayan.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.FilterChip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.DatePickerState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.efthemiosprime.pasabayan.data.model.User
import com.efthemiosprime.pasabayan.data.model.UserRole
import com.efthemiosprime.pasabayan.data.model.Trip
import com.efthemiosprime.pasabayan.data.model.TripStatus
import com.efthemiosprime.pasabayan.data.model.TransportationMethod
import com.efthemiosprime.pasabayan.data.model.PackageRequest
import com.efthemiosprime.pasabayan.data.model.PackageRequestStatus
import com.efthemiosprime.pasabayan.data.model.PackageSize
import com.efthemiosprime.pasabayan.data.repository.TripRepositoryImpl
import com.efthemiosprime.pasabayan.presentation.viewmodel.AuthViewModel
import com.efthemiosprime.pasabayan.ui.components.TripCard
import com.efthemiosprime.pasabayan.ui.components.packages.PackageRequestCard
import com.efthemiosprime.pasabayan.ui.screens.carrier.TripCreationScreen
import com.efthemiosprime.pasabayan.ui.screens.trip.TripDetailScreen
import java.text.SimpleDateFormat
import java.util.*

/**
 * Home tab content that mirrors iOS ShipperHomeTab/CarrierHomeTab
 */
@Composable
fun HomeTabContent(
    modifier: Modifier = Modifier,
    user: User?,
    currentRole: UserRole,
    onSignOut: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome message
        Text(
            text = "Welcome back!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        user?.let {
            Text(
                text = it.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Current role: ${currentRole.name}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        // Quick stats placeholder
        Text(
            text = "Quick Stats Coming Soon...",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Button(onClick = onSignOut) {
            Text("Sign Out")
        }
    }
}

/**
 * Analytics tab content
 */
@Composable
fun AnalyticsTabContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Analytics Coming Soon",
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

/**
 * Browse tab content - loads real data from API
 */
@Composable
fun BrowseTabContent(
    modifier: Modifier = Modifier,
    currentRole: UserRole,
    authViewModel: AuthViewModel? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // State management
    var trips by remember { mutableStateOf<List<Trip>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Get current user for filtering
    val currentUser = authViewModel?.currentUser?.collectAsState()?.value
    
    // Load trips from API
    LaunchedEffect(Unit) {
        if (currentRole == UserRole.SHIPPER) {
            scope.launch {
                isLoading = true
                errorMessage = null
                try {
                    val tripRepository = TripRepositoryImpl.create(context)
                    tripRepository.getAvailableTrips().collect { result ->
                        result.fold(
                            onSuccess = { tripList ->
                                // Filter out trips that belong to the current user
                                // Only show trips from other carriers
                                trips = tripList.filter { trip ->
                                    currentUser?.id != trip.carrierId
                                }
                                isLoading = false
                            },
                            onFailure = { error ->
                                errorMessage = error.message ?: "Failed to load available trips"
                                isLoading = false
                            }
                        )
                    }
                } catch (e: Exception) {
                    errorMessage = e.message ?: "Failed to load available trips"
                    isLoading = false
                }
            }
        }
    }
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = when (currentRole) {
                    UserRole.SHIPPER -> "Available Trips"
                    UserRole.CARRIER -> "Available Package Requests"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        when (currentRole) {
            UserRole.SHIPPER -> {
                // Show loading state
                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator()
                                Text(
                                    text = "Loading available trips...",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
                
                // Show error state
                errorMessage?.let { error ->
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Error loading trips",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = error,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                // Show trips or empty state
                if (trips.isEmpty() && !isLoading && errorMessage == null) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "No available trips",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Check back later for trips from carriers",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    // Show available trips
                    items(trips) { trip ->
                        TripCard(
                            trip = trip,
                            onTap = { 
                                // TODO: Navigate to trip details or booking
                                println("📋 Shipper tapped available trip: ${trip.id}")
                            }
                        )
                    }
                }
            }
            UserRole.CARRIER -> {
                // Show available package requests for carriers
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "No package requests available",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Connect with real API to see package requests",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Packages or trips tab content
 */
@Composable
fun PackagesOrTripsTabContent(
    modifier: Modifier = Modifier,
    currentRole: UserRole
) {
    when (currentRole) {
        UserRole.CARRIER -> {
            CarrierTripsTabContent(modifier = modifier)
        }
        UserRole.SHIPPER -> {
            ShipperPackagesTabContent(modifier = modifier)
        }
    }
}

/**
 * Carrier trips tab content with + button and modal for trip creation
 */
@Composable
private fun CarrierTripsTabContent(
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf<TripStatus?>(null) }
    var showCreateTripScreen by remember { mutableStateOf(false) }
    var selectedTripForDetails: Trip? by remember { mutableStateOf(null) }
    val context = LocalContext.current
    val tripRepository = remember { TripRepositoryImpl.create(context) }
    var trips by remember { mutableStateOf<List<Trip>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    // Show TripCreationScreen when requested
    if (showCreateTripScreen) {
        TripCreationScreen(
            onNavigateBack = { 
                showCreateTripScreen = false
                // Refresh trips list after coming back from creation
                scope.launch {
                    isLoading = true
                    try {
                        tripRepository.getTripsForCarrier(1).collect { result ->
                            result.fold(
                                onSuccess = { tripList ->
                                    trips = tripList
                                    isLoading = false
                                },
                                onFailure = { error ->
                                    errorMessage = error.message ?: "Failed to refresh trips"
                                    isLoading = false
                                }
                            )
                        }
                    } catch (e: Exception) {
                        errorMessage = e.message ?: "Failed to refresh trips"
                        isLoading = false
                    }
                }
            },
            onNavigateToTripsTab = {
                // Close trip creation screen and refresh trips list
                showCreateTripScreen = false
                scope.launch {
                    isLoading = true
                    try {
                        tripRepository.getTripsForCarrier(1).collect { result ->
                            result.fold(
                                onSuccess = { tripList ->
                                    trips = tripList
                                    isLoading = false
                                },
                                onFailure = { error ->
                                    errorMessage = error.message ?: "Failed to refresh trips"
                                    isLoading = false
                                }
                            )
                        }
                    } catch (e: Exception) {
                        errorMessage = e.message ?: "Failed to refresh trips"
                        isLoading = false
                    }
                }
            }
        )
        return
    }
    
    // Show trip detail screen if a trip is selected
    selectedTripForDetails?.let { trip ->
        TripDetailScreen(
            trip = trip,
            onNavigateBack = { 
                selectedTripForDetails = null
                // Refresh trips list after coming back from details
                scope.launch {
                    isLoading = true
                    try {
                        tripRepository.getTripsForCarrier(1).collect { result ->
                            result.fold(
                                onSuccess = { tripList ->
                                    trips = tripList
                                    isLoading = false
                                },
                                onFailure = { error ->
                                    errorMessage = error.message ?: "Failed to refresh trips"
                                    isLoading = false
                                }
                            )
                        }
                    } catch (e: Exception) {
                        errorMessage = e.message ?: "Failed to refresh trips"
                        isLoading = false
                    }
                }
            },
            onCancelTrip = { tripToCancel ->
                println("🚫 Starting trip cancellation for ID: ${tripToCancel.id}")
                selectedTripForDetails = null
                scope.launch {
                    isLoading = true
                    errorMessage = null
                    try {
                        // Call the actual cancel trip API
                        tripRepository.cancelTrip(tripToCancel.id).collect { result ->
                            result.fold(
                                onSuccess = { cancelledTrip ->
                                    println("✅ Trip cancelled successfully: ${cancelledTrip.id}")
                                    // Update the trip in our local list
                                    trips = trips.map { trip ->
                                        if (trip.id == cancelledTrip.id) cancelledTrip else trip
                                    }
                                    isLoading = false
                                },
                                onFailure = { error ->
                                    println("❌ Trip cancellation failed: ${error.message}")
                                    errorMessage = "Failed to cancel trip: ${error.message}"
                                    isLoading = false
                                }
                            )
                        }
                    } catch (e: Exception) {
                        println("❌ Exception during trip cancellation: ${e.message}")
                        errorMessage = "Failed to cancel trip: ${e.message}"
                        isLoading = false
                    }
                }
            }
        )
        return
    }
    
    // Load trips from real API
    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                // Get trips for current carrier (using carrier ID 1 for now)
                tripRepository.getTripsForCarrier(1).collect { result ->
                    result.fold(
                        onSuccess = { tripList ->
                            trips = tripList
                            println("🔧 DEBUG: Loaded ${tripList.size} trips for carrier")
                            tripList.forEach { trip ->
                                println("🔧 DEBUG: Trip ID: ${trip.id}, Status: ${trip.tripStatus}, Route: ${trip.route}")
                                println("🔧 DEBUG: Cancel button would show for trip ${trip.id}: ${trip.tripStatus in listOf(TripStatus.PLANNING, TripStatus.SCHEDULED)}")
                            }
                            isLoading = false
                        },
                        onFailure = { error ->
                            errorMessage = error.message ?: "Failed to load trips"
                            isLoading = false
                        }
                    )
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to load trips"
                isLoading = false
            }
        }
    }
    
    val filteredTrips = remember(trips, selectedFilter) {
        if (selectedFilter == null) {
            trips
        } else {
            trips.filter { it.tripStatus == selectedFilter }
        }
    }
    
    // Function to refresh trips list
    val refreshTrips = {
        scope.launch {
            isLoading = true
            try {
                tripRepository.getTripsForCarrier(1).collect { result ->
                    result.fold(
                        onSuccess = { tripList ->
                            trips = tripList
                            isLoading = false
                        },
                        onFailure = { error ->
                            errorMessage = error.message ?: "Failed to refresh trips"
                            isLoading = false
                        }
                    )
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to refresh trips"
                isLoading = false
            }
        }
    }
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
            Text(
                    text = "My Trips",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
                )
                
                // + Button to create trip
                FloatingActionButton(
                    onClick = { showCreateTripScreen = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Trip"
                    )
                }
            }
        }
        
        // Show loading state
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Loading trips...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        
        // Show error message
        errorMessage?.let { error ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "❌ $error",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
        
        // Show filter chips only if we have trips
        if (trips.isNotEmpty() && !isLoading) {
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        item {
                            FilterChip(
                                onClick = { selectedFilter = null },
                                label = { Text("All") },
                                selected = selectedFilter == null
                            )
                        }
                        items(TripStatus.allCases) { status ->
                        val count = trips.count { it.tripStatus == status }
                            FilterChip(
                                onClick = { selectedFilter = status },
                                label = { 
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(status.displayName)
                                        if (count > 0) {
                                            Text(
                                                text = "($count)",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                },
                                selected = selectedFilter == status
                            )
                    }
                }
            }
        }
        
        // Show trips or empty state
        if (filteredTrips.isEmpty() && !isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (selectedFilter == null) "No trips yet" else "No ${selectedFilter!!.displayName.lowercase()} trips",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Tap the + button to create your first trip",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
                items(filteredTrips) { trip ->
                    TripCard(
                        trip = trip,
                        onTap = { 
                            println("🔧 DEBUG: Trip card clicked in CarrierTripsTabContent - ID: ${trip.id}, Route: ${trip.route}, Status: ${trip.tripStatus}")
                            println("🔧 DEBUG: Trip cancel button should show: ${trip.tripStatus in listOf(TripStatus.PLANNING, TripStatus.SCHEDULED)}")
                            selectedTripForDetails = trip
                        }
                    )
                }
            }
    }
}

/**
 * Shipper packages tab content (unchanged)
 */
@Composable
private fun ShipperPackagesTabContent(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "My Package Requests",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Show shipper's package requests
        val mockPackages = listOf(
            PackageRequest(
                id = 1,
                shipperId = 1,
                title = "Electronics Package",
                description = "Laptop and accessories",
                pickupLocation = "SM Mall of Asia",
                deliveryLocation = "Makati CBD",
                preferredPickupDate = "2024-01-15",
                packageSize = PackageSize.MEDIUM,
                packageWeight = 2.5,
                packageValue = 1080.0,
                isFragile = true,
                status = PackageRequestStatus.MATCHED,
                createdAt = "2024-01-14T10:00:00Z",
                updatedAt = "2024-01-14T11:00:00Z"
            ),
            PackageRequest(
                id = 2,
                shipperId = 1,
                title = "Gift Package",
                description = "Birthday gift for family",
                pickupLocation = "Quezon City",
                deliveryLocation = "Manila",
                preferredPickupDate = "2024-01-17",
                packageSize = PackageSize.LARGE,
                packageWeight = 5.0,
                packageValue = 192.0,
                status = PackageRequestStatus.DELIVERED,
                createdAt = "2024-01-13T15:00:00Z",
                updatedAt = "2024-01-17T18:30:00Z"
            ),
            PackageRequest(
                id = 3,
                shipperId = 1,
                title = "Documents",
                description = "Important business documents",
                pickupLocation = "BGC Taguig",
                deliveryLocation = "Ortigas Center",
                preferredPickupDate = "2024-01-16",
                packageSize = PackageSize.SMALL,
                packageWeight = 0.5,
                packageValue = 24.0,
                status = PackageRequestStatus.OPEN,
                createdAt = "2024-01-14T11:00:00Z",
                updatedAt = "2024-01-14T11:00:00Z"
            )
        )
        
        items(mockPackages) { packageRequest ->
            PackageRequestCard(
                packageRequest = packageRequest
            )
        }
    }
}

/**
 * Create tab content
 */
@Composable
fun CreateTabContent(
    modifier: Modifier = Modifier,
    currentRole: UserRole
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = when (currentRole) {
                UserRole.SHIPPER -> "Create Package Request"
                UserRole.CARRIER -> "Create Trip"
            },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        when (currentRole) {
            UserRole.SHIPPER -> {
                CreatePackageForm()
            }
            UserRole.CARRIER -> {
                CreateTripForm()
            }
        }
    }
}

@Composable
private fun CreatePackageForm() {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Quick Package Request",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        
        Text(
            text = "• Package Title: Electronics Package",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "• Pickup: SM Mall of Asia",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "• Delivery: Makati CBD",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "• Size: Medium (2.5kg)",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "• Value: $1,080",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Button(
            onClick = { /* Create package request */ },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Create Package Request")
        }
    }
}

@Composable
private fun CreateTripForm() {
    var isLoading by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val tripRepository = remember { TripRepositoryImpl.create(context) }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Quick Trip Creation",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        
        Text(
            text = "• Route: Manila → Cebu",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "• Method: Flight ✈️",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "• Departure: Tomorrow 10:00 AM",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "• Capacity: 15kg, 50L",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "• Price: $0.60/kg",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    errorMessage = null
                    
                    try {
                        // Create sample trip
                        val newTrip = Trip(
                            id = 0,
                            carrierId = 1,
                            originCity = "Manila",
                            originCountry = "Philippines",
                            originLat = 14.5995,
                            originLng = 120.9842,
                            destinationCity = "Cebu",
                            destinationCountry = "Philippines",
                            destinationLat = 10.3157,
                            destinationLng = 123.8854,
                                            departureDate = "", // Let server set the date (matching iOS)
                            arrivalDate = "", // Let server set the date (matching iOS)
                            availableWeightKg = 15.0,
                            availableSpaceLiters = 50.0,
                            pricePerKg = 0.60,
                            tripStatus = TripStatus.SCHEDULED,
                            transportationMethod = TransportationMethod.FLIGHT,
                            specialNotes = "Quick trip created from dashboard"
                        )
                        
                        tripRepository.createTrip(newTrip).collect { result ->
                            result.fold(
                                onSuccess = { createdTrip ->
                                    println("✅ Trip created successfully - ID: ${createdTrip.id}")
                                    showSuccess = true
                                    isLoading = false
                                },
                                onFailure = { error ->
                                    println("❌ Trip creation failed: ${error.message}")
                                    errorMessage = error.message ?: "Failed to create trip"
                                    isLoading = false
                                }
                            )
                        }
                    } catch (e: Exception) {
                        println("❌ Exception during trip creation: ${e.message}")
                        errorMessage = e.message ?: "Failed to create trip"
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.padding(top = 16.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (isLoading) "Creating..." else "Create Trip")
        }
        
        // Success message
        if (showSuccess) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
        Text(
                    text = "✅ Trip created successfully! Check 'My Trips' tab to see it.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
        
        // Error message
        errorMessage?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = "❌ $error",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
        
        Text(
            text = "Full trip creation form available in the next update!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Helper function to convert user date/time input to ISO format
private fun convertToISODateTime(dateStr: String, timeStr: String): String {
    return try {
        // Parse user-friendly date formats (e.g., "Jul 12, 2025" or "2025-07-12")
        val dateInput = dateStr.trim()
        val timeInput = timeStr.trim()
        
        // Convert date to standard format
        val standardDate = when {
            dateInput.contains(",") -> {
                // Handle "Jul 12, 2025" format
                val parts = dateInput.split(" ")
                if (parts.size >= 3) {
                    val monthMap = mapOf(
                        "Jan" to "01", "Feb" to "02", "Mar" to "03", "Apr" to "04",
                        "May" to "05", "Jun" to "06", "Jul" to "07", "Aug" to "08",
                        "Sep" to "09", "Oct" to "10", "Nov" to "11", "Dec" to "12"
                    )
                    val month = monthMap[parts[0]] ?: "01"
                    val day = parts[1].replace(",", "").padStart(2, '0')
                    val year = parts[2]
                    "$year-$month-$day"
                } else dateInput
            }
            dateInput.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) -> dateInput // Already in ISO format
            else -> {
                // Default to current date if parsing fails
                val calendar = Calendar.getInstance()
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            }
        }
        
        // Convert time to 24-hour format
        val standardTime = when {
            timeInput.contains("PM") -> {
                val timePart = timeInput.replace("PM", "").trim()
                val (hour, minute) = timePart.split(":").map { it.toIntOrNull() ?: 0 }
                val adjustedHour = if (hour == 12) 12 else hour + 12
                "${adjustedHour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
            }
            timeInput.contains("AM") -> {
                val timePart = timeInput.replace("AM", "").trim()
                val (hour, minute) = timePart.split(":").map { it.toIntOrNull() ?: 0 }
                val adjustedHour = if (hour == 12) 0 else hour
                "${adjustedHour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
            }
            timeInput.matches(Regex("\\d{2}:\\d{2}")) -> timeInput // Already 24-hour format
            else -> "10:00" // Default time
        }
        
        "${standardDate}T${standardTime}:00"
    } catch (e: Exception) {
        // Return a default ISO datetime if parsing fails
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1) // Default to tomorrow
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(calendar.time)
    }
}

/**
 * Profile tab content
 */
@Composable
fun ProfileTabContent(
    modifier: Modifier = Modifier,
    user: User?,
    onSignOut: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        user?.let {
            Text(text = "Name: ${it.name}")
            Text(text = "Email: ${it.email}")
            Text(text = "Rating: ${it.displayRating}")
            Text(text = "Verification: ${it.verificationLevel}")
        }
        
        Button(onClick = onSignOut) {
            Text("Sign Out")
        }
    }
}

// ===== PREVIEW FUNCTIONS =====
// User sample data
private val sampleUser = User(
    id = 1,
    name = "John Doe",
    email = "john.doe@example.com",
    phone = "+1234567890",
    rating = 4.5,
    verificationLevel = "verified",
    avatar = null,
    phoneVerified = true,
    profileCompleted = true,
    provider = "google",
    providerId = "123456789",
    emailVerifiedAt = "2024-01-01T00:00:00Z",
    createdAt = "2024-01-01T00:00:00Z",
    updatedAt = "2024-01-15T12:00:00Z",
    userTypes = listOf("shipper", "carrier"),
    isActiveCarrier = true,
    isActiveShipper = true,
    totalRatings = 42
)

/**
 * Home Tab Content Previews
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Home Tab - Shipper")
@Composable
fun HomeTabShipperPreview() {
    com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme {
        HomeTabContent(
            user = sampleUser,
            currentRole = UserRole.SHIPPER,
            onSignOut = { }
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Home Tab - Carrier")
@Composable
fun HomeTabCarrierPreview() {
    com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme {
        HomeTabContent(
            user = sampleUser,
            currentRole = UserRole.CARRIER,
            onSignOut = { }
        )
    }
}

/**
 * Analytics Tab Preview
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Analytics Tab")
@Composable
fun AnalyticsTabPreview() {
    com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme {
        AnalyticsTabContent()
    }
}

/**
 * Browse Tab Content Previews
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Browse Tab - Shipper")
@Composable
fun BrowseTabShipperPreview() {
    com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme {
        BrowseTabContent(currentRole = UserRole.SHIPPER, authViewModel = null)
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Browse Tab - Carrier")
@Composable
fun BrowseTabCarrierPreview() {
    com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme {
        BrowseTabContent(currentRole = UserRole.CARRIER, authViewModel = null)
    }
}

/**
 * Packages/Trips Tab Content Previews
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Packages Tab - Shipper")
@Composable
fun PackagesTabShipperPreview() {
    com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme {
        PackagesOrTripsTabContent(currentRole = UserRole.SHIPPER)
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Trips Tab - Carrier")
@Composable
fun TripsTabCarrierPreview() {
    com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme {
        PackagesOrTripsTabContent(currentRole = UserRole.CARRIER)
    }
}

/**
 * Create Tab Content Previews
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Create Tab - Shipper")
@Composable
fun CreateTabShipperPreview() {
    com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme {
        CreateTabContent(currentRole = UserRole.SHIPPER)
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Create Tab - Carrier")
@Composable
fun CreateTabCarrierPreview() {
    com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme {
        CreateTabContent(currentRole = UserRole.CARRIER)
    }
}

/**
 * Profile Tab Content Preview
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Profile Tab")
@Composable
fun ProfileTabPreview() {
    com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme {
        ProfileTabContent(
            user = sampleUser,
            onSignOut = { }
        )
    }
}

/**
 * Dark Theme Tab Content Previews
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Home Tab - Dark Theme")
@Composable
fun HomeTabDarkPreview() {
    com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme(darkTheme = true) {
        HomeTabContent(
            user = sampleUser,
            currentRole = UserRole.SHIPPER,
            onSignOut = { }
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Trips Tab - Dark Theme")
@Composable
fun TripsTabDarkPreview() {
    com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme(darkTheme = true) {
        PackagesOrTripsTabContent(currentRole = UserRole.CARRIER)
    }
} 