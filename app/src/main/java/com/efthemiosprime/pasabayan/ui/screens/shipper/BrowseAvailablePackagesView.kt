package com.efthemiosprime.pasabayan.ui.screens.shipper

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.data.model.PackageRequest
import com.efthemiosprime.pasabayan.presentation.viewmodel.PackageViewModel
import com.efthemiosprime.pasabayan.ui.shared.cards.PCardStandard
import com.efthemiosprime.pasabayan.ui.theme.PasabayanDesignSystem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Browse Available Packages View for Shippers
 * Mirrors iOS functionality where shippers can browse available package requests
 * from other shippers (package sharing/consolidation opportunities)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseAvailablePackagesView(
    packageViewModel: PackageViewModel,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    
    // Collect available packages state from PackageViewModel
    val availablePackages by packageViewModel.availablePackages.collectAsStateWithLifecycle()
    val isLoadingAvailable by packageViewModel.isLoadingAvailable.collectAsStateWithLifecycle()
    val availableErrorMessage by packageViewModel.availableErrorMessage.collectAsStateWithLifecycle()
    
    // Local state for search
    var searchQuery by remember { mutableStateOf("") }
    
    // Filter packages based on search
    val filteredPackages = remember(availablePackages, searchQuery) {
        availablePackages.filter { packageRequest ->
            if (searchQuery.isBlank()) true
            else {
                packageRequest.title.contains(searchQuery, ignoreCase = true) ||
                packageRequest.pickupLocation.contains(searchQuery, ignoreCase = true) ||
                packageRequest.deliveryLocation.contains(searchQuery, ignoreCase = true) ||
                packageRequest.description?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }
    
    // Load available packages on first composition
    LaunchedEffect(Unit) {
        packageViewModel.loadAvailablePackages()
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(PasabayanDesignSystem.Spacing.lg)
    ) {
        // Title
        Text(
            text = "Browse Available Packages",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = PasabayanDesignSystem.Spacing.lg)
        )
        
        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search by city, description, or package type...") },
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
            isLoadingAvailable -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            availableErrorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error loading available packages",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = availableErrorMessage ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(PasabayanDesignSystem.Spacing.lg))
                        Button(
                            onClick = { 
                                packageViewModel.loadAvailablePackages()
                            }
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            
            filteredPackages.isEmpty() -> {
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
                            text = if (searchQuery.isBlank()) "No available packages" else "No packages match your search",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (searchQuery.isNotBlank()) {
                            Text(
                                text = "Try adjusting your search terms",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.lg)
                ) {
                    items(filteredPackages, key = { it.id }) { packageRequest ->
                        AvailablePackageCard(
                            packageRequest = packageRequest,
                            onRequestToShare = {
                                scope.launch {
                                    // TODO: Implement package sharing/collaboration functionality
                                    // This could involve requesting to join a package shipment
                                    // or coordinating shared delivery with another shipper
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AvailablePackageCard(
    packageRequest: PackageRequest,
    onRequestToShare: () -> Unit,
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
                                    text = "Standard",
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
                text = "Available Package",
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
            
            // Request to Share Button
            Button(
                onClick = onRequestToShare,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Request to Share",
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