package com.efthemiosprime.pasabayan.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.data.model.DeliveryMatch
import com.efthemiosprime.pasabayan.data.model.MatchStatus
import com.efthemiosprime.pasabayan.ui.screens.dashboard.MatchAction
import com.efthemiosprime.pasabayan.ui.shared.cards.PCardStandard
import com.efthemiosprime.pasabayan.ui.theme.PasabayanDesignSystem
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * MatchCard - Displays delivery match information for carriers
 * Mirrors iOS MatchCard with carrier-specific actions and styling
 * Follows PCard standards for consistent elevation, padding, and styling
 */
@Composable
fun MatchCard(
    match: DeliveryMatch,
    modifier: Modifier = Modifier,
    onAction: (MatchAction) -> Unit = {}
) {
    PCardStandard(
        modifier = modifier
    ) {
        // Card content follows PCard standards - no need to specify padding as it's handled by PCardStandard
        
        // Header with status and price
        MatchHeaderRow(match = match)
        
        Spacer(modifier = Modifier.height(PasabayanDesignSystem.Spacing.md))
        
        // Package and trip details
        MatchDetailsSection(match = match)
        
        Spacer(modifier = Modifier.height(PasabayanDesignSystem.Spacing.md))
        
        // Status-specific actions
        MatchActionsRow(match = match, onAction = onAction)
    }
}

@Composable
private fun MatchHeaderRow(match: DeliveryMatch) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status chip
        MatchStatusChip(status = match.status)
        
        // Price
        Text(
            text = "$${String.format("%.2f", match.agreedPrice)}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun MatchStatusChip(status: MatchStatus) {
    val (backgroundColor, textColor) = when (status) {
        MatchStatus.PENDING -> Color(0xFFFF9800) to Color.White
        MatchStatus.CONFIRMED -> Color(0xFF2196F3) to Color.White
        MatchStatus.PICKED_UP -> Color(0xFF9C27B0) to Color.White
        MatchStatus.IN_TRANSIT -> Color(0xFF00BCD4) to Color.White
        MatchStatus.DELIVERED -> Color(0xFF4CAF50) to Color.White
        MatchStatus.CANCELLED -> Color(0xFFF44336) to Color.White
        MatchStatus.CARRIER_REQUESTED -> Color(0xFF03A9F4) to Color.White
        MatchStatus.SHIPPER_REQUESTED -> Color(0xFF9C27B0) to Color.White
        MatchStatus.SHIPPER_ACCEPTED -> Color(0xFF4CAF50) to Color.White
        MatchStatus.SHIPPER_DECLINED -> Color(0xFFF44336) to Color.White
        MatchStatus.CARRIER_ACCEPTED -> Color(0xFF4CAF50) to Color.White
        MatchStatus.CARRIER_DECLINED -> Color(0xFFF44336) to Color.White
    }
    
    Surface(
        modifier = Modifier.wrapContentSize(),
        shape = RoundedCornerShape(PasabayanDesignSystem.CornerRadius.lg),
        color = backgroundColor
    ) {
        Text(
            text = status.description,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(
                horizontal = PasabayanDesignSystem.Spacing.md,
                vertical = PasabayanDesignSystem.Spacing.xs
            )
        )
    }
}

@Composable
private fun MatchDetailsSection(match: DeliveryMatch) {
    Column(
        verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.sm)
    ) {
        // Package information
        match.packageRequest?.let { packageRequest ->
            MatchDetailRow(
                icon = Icons.Default.Inventory,
                label = "Package",
                value = packageRequest.description ?: "No description"
            )
            MatchDetailRow(
                icon = Icons.Default.Scale,
                label = "Weight",
                value = "${packageRequest.packageWeight ?: 0.0} kg"
            )
        }
        
        // Trip information
        match.carrierTrip?.let { trip ->
            MatchDetailRow(
                icon = Icons.Default.Route,
                label = "Route",
                value = "${trip.originCity} → ${trip.destinationCity}"
            )
            MatchDetailRow(
                icon = Icons.Default.Schedule,
                label = "Departure",
                value = formatDate(trip.departureDate)
            )
        }
        
        // Shipper information
        match.shipper?.let { shipper ->
            MatchDetailRow(
                icon = Icons.Default.Person,
                label = "Shipper",
                value = shipper.name
            )
        }
        
        // Tracking information
        if (match.pickupConfirmationCode != null) {
            MatchDetailRow(
                icon = Icons.Default.QrCode,
                label = "Pickup Code",
                value = match.pickupConfirmationCode
            )
        }
    }
}

@Composable
private fun MatchDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.iconTextSpacing)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(PasabayanDesignSystem.Spacing.lg)
        )
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(PasabayanDesignSystem.Spacing.xxxxxl)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun MatchActionsRow(
    match: DeliveryMatch,
    onAction: (MatchAction) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.sm)
    ) {
        when (match.status) {
            MatchStatus.PENDING -> {
                OutlinedButton(
                    onClick = { onAction(MatchAction.CANCEL) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Decline")
                }
                Button(
                    onClick = { onAction(MatchAction.CONFIRM) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Accept")
                }
            }
            MatchStatus.CARRIER_REQUESTED -> {
                OutlinedButton(
                    onClick = { onAction(MatchAction.CANCEL) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Decline Request")
                }
                Button(
                    onClick = { onAction(MatchAction.CONFIRM) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Accept Request")
                }
            }
            MatchStatus.CONFIRMED -> {
                Button(
                    onClick = { onAction(MatchAction.PICKUP) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Mark as Picked Up")
                }
            }
            MatchStatus.PICKED_UP, MatchStatus.IN_TRANSIT -> {
                Button(
                    onClick = { onAction(MatchAction.DELIVER) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Mark as Delivered")
                }
            }
            MatchStatus.DELIVERED -> {
                Button(
                    onClick = { },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Completed")
                }
            }
            MatchStatus.CANCELLED -> {
                // No actions for cancelled matches
            }
            MatchStatus.SHIPPER_REQUESTED -> {
                Text(
                    text = "Waiting for Carrier Response",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            MatchStatus.SHIPPER_ACCEPTED -> {
                Text(
                    text = "Shipper Accepted - Waiting for Confirmation",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            MatchStatus.SHIPPER_DECLINED -> {
                Text(
                    text = "Shipper Declined",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            MatchStatus.CARRIER_ACCEPTED -> {
                Text(
                    text = "Carrier Accepted - Ready for Pickup",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            MatchStatus.CARRIER_DECLINED -> {
                Text(
                    text = "Carrier Declined",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}