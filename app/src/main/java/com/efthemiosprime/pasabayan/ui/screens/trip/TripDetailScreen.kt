package com.efthemiosprime.pasabayan.ui.screens.trip

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.data.model.Trip
import com.efthemiosprime.pasabayan.data.model.TripStatus
import com.efthemiosprime.pasabayan.ui.theme.PasabayanDesignSystem
import com.efthemiosprime.pasabayan.ui.shared.cards.PCardStandard

/**
 * Trip Detail Screen - Shows comprehensive trip information for carriers
 * Includes trip details, status, and action buttons including Cancel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    trip: Trip,
    onNavigateBack: () -> Unit,
    onCancelTrip: (Trip) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showCancelDialog by remember { mutableStateOf(false) }
    
    // Debug logging
    println("🔧 DEBUG: TripDetailScreen rendered for trip ID: ${trip.id}, Status: ${trip.tripStatus}")
    println("🔧 DEBUG: Should show cancel button: ${trip.tripStatus in listOf(TripStatus.PLANNING, TripStatus.SCHEDULED)}")

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(
                    text = "Trip Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(PasabayanDesignSystem.Spacing.lg)
        ) {
            // Trip Route
            PCardStandard(
                modifier = Modifier.fillMaxWidth()
            ) {
                    Text(
                        text = "Route",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(PasabayanDesignSystem.Spacing.md))
                    
                    TripDetailRow(
                        label = "From:",
                        value = trip.originLocation
                    )
                    TripDetailRow(
                        label = "To:",
                        value = trip.destinationLocation
                    )
            }

            Spacer(modifier = Modifier.height(PasabayanDesignSystem.Spacing.lg))

            // Schedule Information
            PCardStandard(
                modifier = Modifier.fillMaxWidth()
            ) {
                    Text(
                        text = "Schedule",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(PasabayanDesignSystem.Spacing.md))
                    
                    TripDetailRow(
                        label = "Departure:",
                        value = trip.formattedDepartureDate
                    )
                    TripDetailRow(
                        label = "Arrival:",
                        value = trip.formattedArrivalDate
                    )
            }

            Spacer(modifier = Modifier.height(PasabayanDesignSystem.Spacing.lg))

            // Capacity Information
            PCardStandard(
                modifier = Modifier.fillMaxWidth()
            ) {
                    Text(
                        text = "Capacity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(PasabayanDesignSystem.Spacing.md))
                    
                    TripDetailRow(
                        label = "Available Weight:",
                        value = "${trip.availableWeightKg} kg"
                    )
                    TripDetailRow(
                        label = "Available Volume:",
                        value = "${trip.availableSpaceLiters} L"
                    )
                    TripDetailRow(
                        label = "Price per kg:",
                        value = "$${trip.pricePerKg}"
                    )
            }

            Spacer(modifier = Modifier.height(PasabayanDesignSystem.Spacing.lg))

            // Trip Information
            PCardStandard(
                modifier = Modifier.fillMaxWidth()
            ) {
                    Text(
                        text = "Trip Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(PasabayanDesignSystem.Spacing.md))
                    
                    TripDetailRow(
                        label = "Status:",
                        value = trip.tripStatus.displayName
                    )
                    TripDetailRow(
                        label = "Transportation:",
                        value = trip.transportationMethod.displayName
                    )
                    
                    if (!trip.specialNotes.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(PasabayanDesignSystem.Spacing.md))
                        Text(
                            text = "Special Notes:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(PasabayanDesignSystem.Spacing.xs))
                        Text(
                            text = trip.specialNotes,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
            }

            Spacer(modifier = Modifier.height(PasabayanDesignSystem.Spacing.xl))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
            ) {
                // Cancel Trip Button (only show if trip can be cancelled)
                println("🔧 DEBUG: Checking cancel button condition for trip ${trip.id} with status ${trip.tripStatus}")
                if (trip.tripStatus in listOf(TripStatus.PLANNING, TripStatus.SCHEDULED)) {
                    println("🔧 DEBUG: ✅ RENDERING CANCEL BUTTON for trip ${trip.id}")
                } else {
                    println("🔧 DEBUG: ❌ NOT rendering cancel button for trip ${trip.id} - status: ${trip.tripStatus}")
                }
                if (trip.tripStatus in listOf(TripStatus.PLANNING, TripStatus.SCHEDULED)) {
                    Button(
                        onClick = { 
                            println("🔧 DEBUG: Cancel button clicked for trip ${trip.id}")
                            showCancelDialog = true 
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Cancel,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(PasabayanDesignSystem.Spacing.xs))
                        Text("Cancel Trip")
                    }
                }
            }

            // Add some bottom padding
            Spacer(modifier = Modifier.height(PasabayanDesignSystem.Spacing.xl))
        }
    }

    // Cancel Confirmation Dialog
    if (showCancelDialog) {
        println("🔧 DEBUG: Showing cancel confirmation dialog for trip ${trip.id}")
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = {
                Text("Cancel Trip")
            },
            text = {
                Text("Are you sure you want to cancel this trip? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        println("🔧 DEBUG: User confirmed trip cancellation for trip ${trip.id}")
                        showCancelDialog = false
                        onCancelTrip(trip)
                    }
                ) {
                    Text(
                        "Cancel Trip",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        println("🔧 DEBUG: User dismissed trip cancellation dialog for trip ${trip.id}")
                        showCancelDialog = false 
                    }
                ) {
                    Text("Keep Trip")
                }
            }
        )
    }
}

/**
 * Trip Detail Row helper component
 * Reused from DirectBookingSheet pattern
 */
@Composable
private fun TripDetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
    Spacer(modifier = Modifier.height(PasabayanDesignSystem.Spacing.sm))
}