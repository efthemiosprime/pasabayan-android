package com.efthemiosprime.pasabayan.ui.screens.carrier.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.efthemiosprime.pasabayan.data.model.DeliveryMatch
import com.efthemiosprime.pasabayan.data.model.MatchStatus
import com.efthemiosprime.pasabayan.ui.components.MatchStatusBadge
import com.efthemiosprime.pasabayan.ui.shared.cards.PCardStandard
import com.efthemiosprime.pasabayan.ui.theme.PasabayanDesignSystem

/**
 * Booking Request Detail Sheet - Mirrors iOS BookingRequestDetailView  
 * Shows comprehensive booking details with accept/decline actions
 * Follows PCard standards for consistent card styling throughout
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingRequestDetailSheet(
    request: DeliveryMatch,
    onDismiss: () -> Unit,
    onAccept: (DeliveryMatch) -> Unit,
    onDecline: (DeliveryMatch) -> Unit,
    onCancel: (DeliveryMatch) -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(PasabayanDesignSystem.Spacing.lg),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top App Bar
                TopAppBar(
                    title = { 
                        Text(
                            "Booking Details",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(PasabayanDesignSystem.Spacing.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.lg)
                ) {
                    // Header Card - Booking Status and Price
                    item {
                        BookingHeaderCard(request = request)
                    }
                    
                    // Package Information Card
                    request.packageRequest?.let { packageRequest ->
                        item {
                            PackageInformationCard(packageRequest = packageRequest)
                        }
                    }
                    
                    // Trip Information Card
                    request.carrierTrip?.let { trip ->
                        item {
                            TripInformationCard(trip = trip)
                        }
                    }
                    
                    // Shipper Information Card
                    request.shipper?.let { shipper ->
                        item {
                            ShipperInformationCard(shipper = shipper)
                        }
                    }
                    
                    // Action Buttons Section
                    item {
                        BookingActionSection(
                            request = request,
                            onAccept = onAccept,
                            onDecline = onDecline,
                            onCancel = onCancel
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingHeaderCard(
    request: DeliveryMatch,
    modifier: Modifier = Modifier
) {
    PCardStandard(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.lg)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Booking Request",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Request ID: ${request.id}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                MatchStatusBadge(status = request.status)
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Agreed Price",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "CAD $${String.format("%.2f", request.agreedPrice)}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun PackageInformationCard(
    packageRequest: com.efthemiosprime.pasabayan.data.model.PackageRequest,
    modifier: Modifier = Modifier
) {
    PCardStandard(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.sm)
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Package Information",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            HorizontalDivider()
            
            // Package details
            DetailRow(label = "Title", value = packageRequest.title)
            packageRequest.description?.let { description ->
                DetailRow(label = "Description", value = description)
            }
            DetailRow(label = "Weight", value = "${packageRequest.packageWeight ?: 0.0} kg")
            DetailRow(label = "Size", value = packageRequest.packageSize.toString())
            packageRequest.packageValue?.let { value ->
                DetailRow(label = "Value", value = "CAD $${String.format("%.2f", value)}")
            }
            
            HorizontalDivider()
            
            // Location details
            DetailRow(label = "Pickup Location", value = packageRequest.pickupLocation)
            DetailRow(label = "Delivery Location", value = packageRequest.deliveryLocation)
            
            HorizontalDivider()
            
            // Dates
            DetailRow(label = "Preferred Pickup", value = packageRequest.preferredPickupDate ?: "Not specified")
            DetailRow(label = "Preferred Delivery", value = packageRequest.preferredDeliveryDate ?: "Not specified")
        }
    }
}

@Composable
private fun TripInformationCard(
    trip: com.efthemiosprime.pasabayan.data.model.Trip,
    modifier: Modifier = Modifier
) {
    PCardStandard(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.sm)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Trip Information",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            HorizontalDivider()
            
            DetailRow(label = "Route", value = "${trip.originCity} → ${trip.destinationCity}")
            DetailRow(label = "Departure", value = trip.departureDate)
            trip.arrivalDate?.let { arrival ->
                DetailRow(label = "Arrival", value = arrival)
            }
            DetailRow(label = "Transportation", value = trip.transportationMethod.toString())
            DetailRow(label = "Available Weight", value = "${trip.availableWeightKg} kg")
            DetailRow(label = "Available Space", value = "${trip.availableSpaceLiters ?: 0} L")
            DetailRow(label = "Price per kg", value = "CAD $${String.format("%.2f", trip.pricePerKg)}")
        }
    }
}

@Composable
private fun ShipperInformationCard(
    shipper: com.efthemiosprime.pasabayan.data.model.User,
    modifier: Modifier = Modifier
) {
    PCardStandard(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.sm)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Shipper Information",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            HorizontalDivider()
            
            DetailRow(label = "Name", value = shipper.name)
            DetailRow(label = "Email", value = shipper.email)
            shipper.phone?.let { phone ->
                DetailRow(label = "Phone", value = phone)
            }
        }
    }
}

@Composable
private fun BookingActionSection(
    request: DeliveryMatch,
    onAccept: (DeliveryMatch) -> Unit,
    onDecline: (DeliveryMatch) -> Unit,
    onCancel: (DeliveryMatch) -> Unit,
    modifier: Modifier = Modifier
) {
    PCardStandard(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
        ) {
            Text(
                text = "Actions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            when (request.status) {
                MatchStatus.PENDING -> {
                    // Accept and Decline buttons for pending requests
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
                    ) {
                        OutlinedButton(
                            onClick = { onDecline(request) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Decline")
                        }
                        
                        Button(
                            onClick = { onAccept(request) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Accept")
                        }
                    }
                }
                
                MatchStatus.CONFIRMED, MatchStatus.PICKED_UP, MatchStatus.IN_TRANSIT -> {
                    // Cancel button for active bookings
                    Button(
                        onClick = { onCancel(request) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Cancel Booking")
                    }
                }
                
                else -> {
                    // No actions available for other statuses
                    Text(
                        text = "No actions available for this booking status.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.6f)
        )
    }
}