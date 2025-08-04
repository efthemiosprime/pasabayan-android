package com.efthemiosprime.pasabayan.ui.screens.carrier

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.data.model.PackageRequest
import com.efthemiosprime.pasabayan.data.model.Trip
import com.efthemiosprime.pasabayan.data.repository.TripRepositoryImpl
import com.efthemiosprime.pasabayan.data.repository.DeliveryMatchRepositoryImpl
import com.efthemiosprime.pasabayan.ui.shared.cards.PCardStandard
import com.efthemiosprime.pasabayan.ui.theme.PasabayanDesignSystem
import kotlinx.coroutines.launch

/**
 * Full-screen Request to Carry screen - iOS-style sheet presentation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestToCarryScreen(
    packageRequest: PackageRequest,

    onNavigateBack: () -> Unit,
    onRequestSent: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // State management
    var carrierTrips by remember { mutableStateOf<List<Trip>>(emptyList()) }
    var selectedTrip by remember { mutableStateOf<Trip?>(null) }
    var proposedPrice by remember { mutableStateOf("") }
    var carrierMessage by remember { mutableStateOf("") }
    var isRequesting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Initialize repositories
    val tripRepository = remember { 
        TripRepositoryImpl.create(context)
    }
    val matchRepository = remember { 
        DeliveryMatchRepositoryImpl.create(context)
    }
    
    // Load carrier trips on screen open
    LaunchedEffect(Unit) {
        scope.launch {
            tripRepository.getTripsForCarrier(1).collect { result ->
                result.fold(
                    onSuccess = { trips -> 
                        carrierTrips = trips
                        isLoading = false
                    },
                    onFailure = { error ->
                        errorMessage = "Failed to load trips: ${error.message}"
                        isLoading = false
                    }
                )
            }
        }
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Request to Carry",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    TextButton(
                        onClick = onNavigateBack
                    ) {
                        Text(
                            text = "Cancel",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(PasabayanDesignSystem.Spacing.lg)
                ) {
                    val buttonEnabled = !isRequesting && selectedTrip != null && proposedPrice.isNotBlank()
                    
                    Button(
                        onClick = {
                            scope.launch {
                                if (selectedTrip != null && proposedPrice.isNotBlank()) {
                                    isRequesting = true
                                    errorMessage = null
                                    
                                    val price = proposedPrice.toDoubleOrNull()
                                    if (price != null) {
                                        // Use repository directly (iOS parity achieved at API level)
                                        matchRepository.requestToCarryPackage(
                                            packageId = packageRequest.id,
                                            tripId = selectedTrip!!.id,
                                            proposedPrice = price,
                                            message = if (carrierMessage.isBlank()) null else carrierMessage
                                        ).fold(
                                            onSuccess = { match ->
                                                // Success - navigate back and refresh
                                                onRequestSent()
                                            },
                                            onFailure = { error ->
                                                errorMessage = error.message
                                                isRequesting = false
                                            }
                                        )
                                    } else {
                                        errorMessage = "Please enter a valid price"
                                        isRequesting = false
                                    }
                                }
                            }
                        },
                        enabled = buttonEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (isRequesting) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Text(
                                    text = "Sending Request...",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        } else {
                            Text(
                                text = "Send Request to Carry",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    
                    // Error message
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(PasabayanDesignSystem.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.lg)
        ) {
            // Package Details Section
            item {
                PackageDetailsSection(packageRequest = packageRequest)
            }
            
            // Select Your Trip Section
            item {
                SelectTripSection(
                    availableTrips = carrierTrips,
                    selectedTrip = selectedTrip,
                    isLoading = isLoading,
                    onTripSelected = { selectedTrip = it }
                )
            }
            
            // Your Pricing Proposal Section
            item {
                PricingProposalSection(
                    proposedPrice = proposedPrice,
                    onPriceChanged = { proposedPrice = it },
                    isEnabled = !isRequesting
                )
            }
            
            // Message to Shipper Section
            item {
                MessageSection(
                    carrierMessage = carrierMessage,
                    onMessageChanged = { carrierMessage = it },
                    isEnabled = !isRequesting
                )
            }
            
            // Bottom spacing for the button
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun PackageDetailsSection(packageRequest: PackageRequest) {
    PCardStandard {
        Column(
            verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
        ) {
            Text(
                text = "Package Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            DetailRow(
                label = "Route:",
                value = "${packageRequest.pickupLocation} → ${packageRequest.deliveryLocation}"
            )
            DetailRow(
                label = "Weight:",
                value = "${packageRequest.packageWeight ?: "N/A"} kg"
            )
            DetailRow(
                label = "Shipper's Budget:",
                value = "CAD $${packageRequest.maxBudget ?: "N/A"}",
                valueColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SelectTripSection(
    availableTrips: List<Trip>,
    selectedTrip: Trip?,
    isLoading: Boolean,
    onTripSelected: (Trip) -> Unit
) {
    PCardStandard {
        Text(
            text = "Select Your Trip",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = PasabayanDesignSystem.Spacing.md)
        )
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            availableTrips.isEmpty() -> {
                NoEligibleTripsMessage()
            }
            
            else -> {
                LazyColumn(
                    modifier = Modifier.height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableTrips) { trip ->
                        TripSelectionCard(
                            trip = trip,
                            isSelected = selectedTrip?.id == trip.id,
                            onSelected = { onTripSelected(trip) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoEligibleTripsMessage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(PasabayanDesignSystem.Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
    ) {
        // Icon
        Surface(
            modifier = Modifier.size(60.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = "No trips",
                    modifier = Modifier.size(30.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Title
        Text(
            text = "No Eligible Trips",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        // Description
        Text(
            text = "You don't have any active trips that can accommodate this package. Create a new trip or ensure your existing trips have sufficient capacity.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun TripSelectionCard(
    trip: Trip,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelected() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        }
    ) {
        Row(
            modifier = Modifier.padding(PasabayanDesignSystem.Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = trip.route,
                    style = MaterialTheme.typography.titleSmall,
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

@Composable
private fun PricingProposalSection(
    proposedPrice: String,
    onPriceChanged: (String) -> Unit,
    isEnabled: Boolean
) {
    PCardStandard {
        Column(
            verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
        ) {
            Text(
                text = "Your Pricing Proposal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = proposedPrice,
                onValueChange = onPriceChanged,
                label = { Text("Proposed Price (CAD $)") },
                placeholder = { Text("0.00") },
                prefix = { Text("$") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                enabled = isEnabled,
                singleLine = true
            )
        }
    }
}

@Composable
private fun MessageSection(
    carrierMessage: String,
    onMessageChanged: (String) -> Unit,
    isEnabled: Boolean
) {
    PCardStandard {
        Column(
            verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
        ) {
            Text(
                text = "Message to Shipper (Optional)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = carrierMessage,
                onValueChange = { newMessage ->
                    if (newMessage.length <= 500) {
                        onMessageChanged(newMessage)
                    }
                },
                placeholder = { 
                    Text("Add a personal message to introduce yourself or explain why you're the right carrier for this package...")
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isEnabled,
                minLines = 3,
                maxLines = 5,
                supportingText = {
                    Text(
                        text = "${carrierMessage.length}/500 characters",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            )
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    icon: String? = null,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (icon != null) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = valueColor
            )
        }
    }
}