package com.efthemiosprime.pasabayan.ui.screens.findcarriers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.efthemiosprime.pasabayan.data.model.PackageRequest
import com.efthemiosprime.pasabayan.data.model.CompatibleTrip
import com.efthemiosprime.pasabayan.presentation.viewmodel.FindCarriersViewModel
import com.efthemiosprime.pasabayan.ui.shared.cards.PCardStandard
import com.efthemiosprime.pasabayan.ui.shared.PButton
import com.efthemiosprime.pasabayan.ui.shared.ScreenContainer
import com.efthemiosprime.pasabayan.ui.common.ErrorMessage

/**
 * Find Carriers Screen - Browse available carriers for a package
 * Mirrors iOS CarrierSelectionView with trip browsing and carrier selection
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindCarriersScreen(
    packageRequest: PackageRequest,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: FindCarriersViewModel = viewModel { FindCarriersViewModel(context.applicationContext as android.app.Application) }
    val uiState by viewModel.uiState.collectAsState()
    
    // Load compatible trips when screen opens
    LaunchedEffect(packageRequest.id) {
        viewModel.loadCompatibleTrips(packageRequest.id)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Find Carriers",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        ScreenContainer {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Package summary card
                PackageSummaryCard(packageRequest = packageRequest)
                
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    
                    uiState.error != null -> {
                        ErrorMessage(
                            message = uiState.error ?: "Unknown error",
                            onRetry = { viewModel.loadCompatibleTrips(packageRequest.id) }
                        )
                    }
                    
                    uiState.compatibleTrips.isEmpty() -> {
                        EmptyCarriersState()
                    }
                    
                    else -> {
                        Text(
                            text = "Available Carriers (${uiState.compatibleTrips.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.compatibleTrips) { compatibleTrip ->
                                CarrierTripCard(
                                    compatibleTrip = compatibleTrip,
                                    onRequestToCarry = {
                                        viewModel.requestToCarry(packageRequest.id, compatibleTrip.id)
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

/**
 * Package summary card showing key details
 */
@Composable
private fun PackageSummaryCard(
    packageRequest: PackageRequest,
    modifier: Modifier = Modifier
) {
    PCardStandard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = packageRequest.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "From: ${packageRequest.pickupLocation}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "To: ${packageRequest.deliveryLocation}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${packageRequest.packageWeight ?: 0.0} kg",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = packageRequest.packageSize.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Carrier trip card showing trip details and request button
 */
@Composable
private fun CarrierTripCard(
    compatibleTrip: CompatibleTrip,
    onRequestToCarry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val trip = compatibleTrip.trip
    PCardStandard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Carrier info header
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Driver: ${trip.carrierId}", // TODO: Get actual carrier name
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "4.8", // TODO: Get actual rating
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₱${String.format("%.2f", compatibleTrip.estimatedPrice)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${String.format("%.0f", compatibleTrip.matchScore * 100)}% match",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Route information
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${trip.originCity} → ${trip.destinationCity}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Departure: ${trip.departureDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Capacity information
            Row(
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Available: ${trip.availableWeightKg} kg",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = trip.transportationMethod.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Request button
            PButton(
                text = "Request to Carry",
                onClick = onRequestToCarry,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Empty state when no carriers are available
 */
@Composable
private fun EmptyCarriersState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "🚚",
                style = MaterialTheme.typography.displayMedium
            )
            Text(
                text = "No Available Carriers",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "No carriers are currently available for this route.\nTry checking again later.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}