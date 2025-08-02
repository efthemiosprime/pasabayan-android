package com.efthemiosprime.pasabayan.ui.screens.booking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.efthemiosprime.pasabayan.data.model.DirectBookingData
import com.efthemiosprime.pasabayan.data.model.Trip
import com.efthemiosprime.pasabayan.ui.shared.cards.PCardStandard
import com.efthemiosprime.pasabayan.ui.theme.PasabayanDesignSystem
import java.text.SimpleDateFormat
import java.util.*

/**
 * BookingSuccessView - Exactly mirrors iOS BookingSuccessView (311 lines)
 * Complete booking confirmation screen with:
 * - Success header with checkmark icon
 * - Detailed booking information
 * - Pricing breakdown (if service fee exists)
 * - Next steps guidance
 * - Action buttons for navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingSuccessView(
    bookingData: DirectBookingData,
    trip: Trip,
    onDismiss: () -> Unit,
    onViewBookingDetails: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top bar - mirrors iOS navigation bar
                TopAppBar(
                    title = { 
                        Text(
                            text = "Booking Success",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    actions = {
                        TextButton(onClick = onDismiss) {
                            Text("Done")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Scrollable content
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = PasabayanDesignSystem.Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.xl)
                ) {
                    item { Spacer(modifier = Modifier.height(PasabayanDesignSystem.Spacing.lg)) }
                    
                    // Success Header - mirrors iOS success header
                    item {
                        SuccessHeader()
                    }
                    
                    // Booking Details Card - mirrors iOS booking details card
                    item {
                        BookingDetailsCard(
                            bookingData = bookingData,
                            trip = trip
                        )
                    }
                    
                    // Pricing Breakdown - mirrors iOS pricing breakdown (if service fee exists)
                    if (bookingData.serviceFee != null && bookingData.serviceFee > 0) {
                        item {
                            PricingBreakdownCard(bookingData = bookingData)
                        }
                    }
                    
                    // Next Steps - mirrors iOS next steps section
                    item {
                        NextStepsCard()
                    }
                    
                    // Action Buttons - mirrors iOS action buttons
                    item {
                        ActionButtonsSection(
                            onViewBookingDetails = onViewBookingDetails,
                            onDone = onDismiss
                        )
                    }
                    
                    item { Spacer(modifier = Modifier.height(PasabayanDesignSystem.Spacing.xl)) }
                }
            }
        }
    }
}

/**
 * Success Header - mirrors iOS success header with icon and text
 */
@Composable
private fun SuccessHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.lg)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Success",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Booking Confirmed!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Your trip has been successfully booked",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Booking Details Card - mirrors iOS booking details card
 */
@Composable
private fun BookingDetailsCard(
    bookingData: DirectBookingData,
    trip: Trip
) {
    PCardStandard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.lg)
        ) {
            Text(
                text = "Booking Details",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
            ) {
                BookingDetailRow(
                    title = "Booking Reference",
                    value = bookingData.bookingReference
                )
                
                BookingDetailRow(
                    title = "Trip Route",
                    value = "${trip.originCity} → ${trip.destinationCity}"
                )
                
                BookingDetailRow(
                    title = "Departure",
                    value = formatDate(trip.departureDate)
                )
                
                bookingData.estimatedPickupTime?.let { pickupTime ->
                    BookingDetailRow(
                        title = "Estimated Pickup",
                        value = formatDateString(pickupTime)
                    )
                }
                
                bookingData.estimatedDeliveryTime?.let { deliveryTime ->
                    BookingDetailRow(
                        title = "Estimated Delivery",
                        value = formatDateString(deliveryTime)
                    )
                }
                
                BookingDetailRow(
                    title = "Total Amount",
                    value = String.format("CAD $%.2f", bookingData.totalAmount),
                    valueColor = MaterialTheme.colorScheme.primary
                )
                
                BookingDetailRow(
                    title = "Status",
                    value = bookingData.status.replaceFirstChar { 
                        if (it.isLowerCase()) it.titlecase() else it.toString() 
                    },
                    valueColor = statusColor(bookingData.status)
                )
            }
        }
    }
}

/**
 * Booking Detail Row - mirrors iOS BookingDetailRow
 */
@Composable
private fun BookingDetailRow(
    title: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

/**
 * Pricing Breakdown Card - mirrors iOS pricing breakdown
 */
@Composable
private fun PricingBreakdownCard(bookingData: DirectBookingData) {
    PCardStandard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
        ) {
            Text(
                text = "Pricing Breakdown",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.sm)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Agreed Price",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = String.format("CAD $%.2f", bookingData.priceAgreed),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Service Fee",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = String.format("CAD $%.2f", bookingData.serviceFee ?: 0.0),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                HorizontalDivider()
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total Amount",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = String.format("CAD $%.2f", bookingData.totalAmount),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * Next Steps Card - mirrors iOS next steps section
 */
@Composable
private fun NextStepsCard() {
    PCardStandard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
        ) {
            Text(
                text = "Next Steps",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.sm)
            ) {
                NextStepItem(
                    step = "1",
                    title = "Carrier Confirmation",
                    description = "Wait for the carrier to confirm your booking"
                )
                
                NextStepItem(
                    step = "2",
                    title = "Pickup Coordination",
                    description = "The carrier will contact you for pickup details"
                )
                
                NextStepItem(
                    step = "3",
                    title = "Track Your Shipment",
                    description = "Monitor your booking status in the app"
                )
            }
        }
    }
}

/**
 * Next Step Item - mirrors iOS NextStepItem
 */
@Composable
private fun NextStepItem(
    step: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(24.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = step,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Action Buttons Section - mirrors iOS action buttons
 */
@Composable
private fun ActionButtonsSection(
    onViewBookingDetails: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
    ) {
        Button(
            onClick = {
                onViewBookingDetails()
                onDone() // Navigate and dismiss
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "View Booking Details",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        
        TextButton(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Done",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Status color helper - mirrors iOS statusColor
 */
private fun statusColor(status: String): Color {
    return when (status.lowercase()) {
        "confirmed", "active" -> Color(0xFF4CAF50) // Green
        "pending" -> Color(0xFFFF9800) // Orange
        "cancelled" -> Color(0xFFF44336) // Red
        else -> Color.Unspecified
    }
}

/**
 * Date formatting helper - mirrors iOS formatDate
 */
private fun formatDate(date: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
        val parsedDate = inputFormat.parse(date)
        outputFormat.format(parsedDate ?: Date())
    } catch (e: Exception) {
        date
    }
}

/**
 * Date string formatting helper - mirrors iOS formatDateString
 */
private fun formatDateString(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
        val parsedDate = inputFormat.parse(dateString)
        outputFormat.format(parsedDate ?: Date())
    } catch (e: Exception) {
        dateString
    }
}