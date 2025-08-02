package com.efthemiosprime.pasabayan.ui.screens.carrier

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.efthemiosprime.pasabayan.data.model.PackageRequest
import com.efthemiosprime.pasabayan.data.model.PackageSize
import com.efthemiosprime.pasabayan.data.model.Trip
import com.efthemiosprime.pasabayan.data.repository.TripRepositoryImpl
import com.efthemiosprime.pasabayan.data.repository.DeliveryMatchRepositoryImpl
import com.efthemiosprime.pasabayan.data.service.APIService
import com.efthemiosprime.pasabayan.data.service.AuthService
import com.efthemiosprime.pasabayan.presentation.viewmodel.PackageViewModel
import com.efthemiosprime.pasabayan.ui.shared.cards.PCardStandard
import com.efthemiosprime.pasabayan.ui.theme.PasabayanDesignSystem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Browse Packages View for Carriers
 * Mirrors iOS functionality where carriers can browse available package requests
 * and request to carry them
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowsePackagesView(
    packageViewModel: PackageViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Collect package requests state
    val packageRequests by packageViewModel.packageRequests.collectAsStateWithLifecycle()
    val isLoading by packageViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by packageViewModel.errorMessage.collectAsStateWithLifecycle()
    
    // Search state
    var searchQuery by remember { mutableStateOf("") }
    
    // Request to carry dialog state
    var showRequestDialog by remember { mutableStateOf(false) }
    var selectedPackage by remember { mutableStateOf<PackageRequest?>(null) }
    var carrierTrips by remember { mutableStateOf<List<Trip>>(emptyList()) }
    var selectedTrip by remember { mutableStateOf<Trip?>(null) }
    var proposedPrice by remember { mutableStateOf("") }
    var isRequesting by remember { mutableStateOf(false) }
    var requestErrorMessage by remember { mutableStateOf<String?>(null) }
    
    // Initialize repositories
    val tripRepository = remember { 
        TripRepositoryImpl.create(context)
    }
    val matchRepository = remember { 
        DeliveryMatchRepositoryImpl.create(context)
    }
    
    // Filter packages based on search
    val filteredPackages = remember(packageRequests, searchQuery) {
        packageRequests.filter { packageRequest ->
            if (searchQuery.isBlank()) true
            else {
                packageRequest.title.contains(searchQuery, ignoreCase = true) ||
                packageRequest.pickupLocation.contains(searchQuery, ignoreCase = true) ||
                packageRequest.deliveryLocation.contains(searchQuery, ignoreCase = true) ||
                packageRequest.description?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }
    
    // Load packages on first composition
    LaunchedEffect(Unit) {
        packageViewModel.loadPackageRequests()
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(PasabayanDesignSystem.Spacing.lg)
    ) {
        // Title
        Text(
            text = "Browse Packages",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = PasabayanDesignSystem.Spacing.lg)
        )
        
        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search by city or description...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = PasabayanDesignSystem.Spacing.lg),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = true
        )
        
        // Content
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error loading packages",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = errorMessage ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(PasabayanDesignSystem.Spacing.lg))
                        Button(
                            onClick = { packageViewModel.loadPackageRequests() }
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            
            (filteredPackages as List<PackageRequest>).isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(PasabayanDesignSystem.Spacing.lg))
                        Text(
                            text = if (searchQuery.isBlank()) "No packages available" else "No packages match your search",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.lg)
                ) {
                    items(filteredPackages, key = { it.id }) { packageRequest ->
                        BrowsePackageCard(
                            packageRequest = packageRequest,
                            onRequestToCarry = {
                                selectedPackage = packageRequest
                                scope.launch {
                                    // Load carrier trips when dialog opens
                                    tripRepository.getTripsForCarrier(1).collect { result ->
                                        result.fold(
                                            onSuccess = { trips -> 
                                                carrierTrips = trips
                                                showRequestDialog = true
                                            },
                                            onFailure = { error ->
                                                requestErrorMessage = "Failed to load trips: ${error.message}"
                                            }
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
        
        // Request to Carry Dialog
        if (showRequestDialog && selectedPackage != null) {
            RequestToCarryDialog(
                packageRequest = selectedPackage!!,
                availableTrips = carrierTrips,
                selectedTrip = selectedTrip,
                proposedPrice = proposedPrice,
                isRequesting = isRequesting,
                errorMessage = requestErrorMessage,
                onTripSelected = { selectedTrip = it },
                onPriceChanged = { proposedPrice = it },
                onConfirm = {
                    scope.launch {
                        if (selectedTrip != null && proposedPrice.isNotBlank()) {
                            isRequesting = true
                            requestErrorMessage = null
                            
                            val price = proposedPrice.toDoubleOrNull()
                            if (price != null) {
                                matchRepository.requestToCarryPackage(
                                    packageId = selectedPackage!!.id,
                                    tripId = selectedTrip!!.id,
                                    proposedPrice = price,
                                    message = null
                                ).fold(
                                    onSuccess = { match ->
                                        // Success - close dialog and refresh
                                        showRequestDialog = false
                                        selectedPackage = null
                                        selectedTrip = null
                                        proposedPrice = ""
                                        packageViewModel.loadAvailablePackages()
                                    },
                                    onFailure = { error ->
                                        requestErrorMessage = error.message
                                    }
                                )
                            } else {
                                requestErrorMessage = "Please enter a valid price"
                            }
                            isRequesting = false
                        }
                    }
                },
                onDismiss = {
                    showRequestDialog = false
                    selectedPackage = null
                    selectedTrip = null
                    proposedPrice = ""
                    requestErrorMessage = null
                }
            )
        }
    }
}

@Composable
private fun BrowsePackageCard(
    packageRequest: PackageRequest,
    onRequestToCarry: () -> Unit,
    modifier: Modifier = Modifier
) {
    PCardStandard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(PasabayanDesignSystem.Spacing.lg)
        ) {
            // Header with package type and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = getPackageIcon(packageRequest.title),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(PasabayanDesignSystem.Spacing.sm))
                    Text(
                        text = packageRequest.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                // Package status badges
                Row {
                    if (packageRequest.isFragile) {
                        Surface(
                            color = Color(0xFFFF9800).copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.padding(end = PasabayanDesignSystem.Spacing.sm)
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = PasabayanDesignSystem.Spacing.sm,
                                    vertical = 2.dp
                                ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = Color(0xFFFF9800)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Fragile",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFF9800)
                                )
                            }
                        }
                    } else {
                        Surface(
                            color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = PasabayanDesignSystem.Spacing.sm,
                                    vertical = 2.dp
                                ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = Color(0xFF4CAF50)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Normal",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(PasabayanDesignSystem.Spacing.sm))
            
            Text(
                text = "Package Request",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(PasabayanDesignSystem.Spacing.lg))
            
            // From -> To
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "From",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = packageRequest.pickupLocation,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "to",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "To",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = packageRequest.deliveryLocation,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(PasabayanDesignSystem.Spacing.lg))
            
            // Weight and Budget
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    Text(
                        text = "Weight:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(PasabayanDesignSystem.Spacing.sm))
                    Text(
                        text = "${packageRequest.packageWeight ?: "N/A"} kg",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Text(
                    text = "CAD $${packageRequest.maxBudget ?: "0.00"}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
            }
            
            // Description
            if (!packageRequest.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(PasabayanDesignSystem.Spacing.sm))
                Text(
                    text = "Description:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = packageRequest.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(PasabayanDesignSystem.Spacing.lg))
            
            // Dates
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Pickup Date",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatDate(packageRequest.preferredPickupDate),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Delivery Needed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatDate(packageRequest.preferredDeliveryDate ?: ""),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(PasabayanDesignSystem.Spacing.lg))
            
            // Request to Carry Button
            Button(
                onClick = onRequestToCarry,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text(
                    text = "Request to Carry",
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun getPackageIcon(title: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when {
        title.contains("Electronics", ignoreCase = true) -> Icons.Default.Computer
        title.contains("Document", ignoreCase = true) -> Icons.Default.Description
        title.contains("Food", ignoreCase = true) -> Icons.Default.Restaurant
        title.contains("Clothing", ignoreCase = true) -> Icons.Default.Checkroom
        title.contains("Medicine", ignoreCase = true) -> Icons.Default.LocalPharmacy
        else -> Icons.Default.Inventory
    }
}

private fun formatDate(dateString: String): String {
    if (dateString.isBlank()) return "N/A"
    
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}

/**
 * Request to Carry Dialog - allows carrier to select trip and enter proposed price
 */
@Composable
private fun RequestToCarryDialog(
    packageRequest: PackageRequest,
    availableTrips: List<Trip>,
    selectedTrip: Trip?,
    proposedPrice: String,
    isRequesting: Boolean,
    errorMessage: String?,
    onTripSelected: (Trip) -> Unit,
    onPriceChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Request to Carry Package",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Package info
                Text(
                    text = packageRequest.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${packageRequest.pickupLocation} → ${packageRequest.deliveryLocation}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Weight: ${packageRequest.packageWeight ?: "N/A"}kg • Budget: $${packageRequest.maxBudget ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Divider()
                
                // Trip selection
                Text(
                    text = "Select Your Trip:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                if (availableTrips.isEmpty()) {
                    Text(
                        text = "No available trips found. Create a trip first.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.height(120.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableTrips) { trip ->
                            val isSelected = selectedTrip?.id == trip.id
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        println("🔧 DEBUG: Trip selected - ID: ${trip.id}, Route: ${trip.route}")
                                        onTripSelected(trip) 
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
                                ),
                                border = if (isSelected) {
                                    BorderStroke(
                                        2.dp, 
                                        MaterialTheme.colorScheme.primary
                                    )
                                } else null
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = trip.route,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "${trip.departureDate} • Capacity: ${trip.availableWeightKg}kg",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Price input
                Text(
                    text = "Proposed Price:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                OutlinedTextField(
                    value = proposedPrice,
                    onValueChange = onPriceChanged,
                    label = { Text("Enter price (CDN)") },
                    prefix = { Text("$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isRequesting
                )
                
                // Error message
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            val buttonEnabled = !isRequesting && selectedTrip != null && proposedPrice.isNotBlank()
            println("🔧 DEBUG: Button state - isRequesting: $isRequesting, selectedTrip: ${selectedTrip?.id}, proposedPrice: '$proposedPrice', enabled: $buttonEnabled")
            Button(
                onClick = onConfirm,
                enabled = buttonEnabled
            ) {
                if (isRequesting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Send Request")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isRequesting
            ) {
                Text("Cancel")
            }
        }
    )
}