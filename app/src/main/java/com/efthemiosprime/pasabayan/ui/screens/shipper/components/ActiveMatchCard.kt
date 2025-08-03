package com.efthemiosprime.pasabayan.ui.screens.shipper.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.data.model.DeliveryMatch
import com.efthemiosprime.pasabayan.data.model.MatchStatus
import com.efthemiosprime.pasabayan.ui.shared.cards.PCardStandard
import com.efthemiosprime.pasabayan.ui.theme.PasabayanDesignSystem
import java.text.SimpleDateFormat
import java.util.*

/**
 * Active Match Card - Shows active delivery matches for shippers
 * Mirrors iOS functionality for displaying ongoing deliveries
 */
@Composable
fun ActiveMatchCard(
    match: DeliveryMatch,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    PCardStandard(
        modifier = modifier.clickable { onTap() }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
        ) {
            // Header with carrier info and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Active Delivery",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Carrier: ${match.carrier?.name ?: "Unknown"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                StatusChip(status = match.status)
            }
            
            // Package information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory,
                    contentDescription = "Package",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = match.packageRequest?.description ?: "Package Details",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Route information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Route,
                    contentDescription = "Route",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "${match.carrierTrip?.originCity ?: "Unknown"} → ${match.carrierTrip?.destinationCity ?: "Unknown"}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Price and progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Agreed Price",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "CAD ${String.format("%.2f", match.agreedPrice)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Est. Delivery",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = match.carrierTrip?.arrivalDate?.let { 
                            SimpleDateFormat("MMM dd", Locale.getDefault()).format(it)
                        } ?: "TBD",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Progress indicator
            ProgressIndicator(status = match.status)
            
            // Last update timestamp
            match.updatedAt.let { timestamp ->
                Text(
                    text = "Last updated: ${SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(timestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatusChip(status: MatchStatus) {
    val (color, text) = when (status) {
        MatchStatus.SHIPPER_ACCEPTED -> MaterialTheme.colorScheme.primaryContainer to "Accepted"
        MatchStatus.PICKED_UP -> MaterialTheme.colorScheme.secondaryContainer to "Picked Up"
        MatchStatus.IN_TRANSIT -> MaterialTheme.colorScheme.tertiaryContainer to "In Transit"
        else -> MaterialTheme.colorScheme.surfaceVariant to status.name
    }
    
    Surface(
        shape = MaterialTheme.shapes.small,
        color = color,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun ProgressIndicator(status: MatchStatus) {
    val progress = when (status) {
        MatchStatus.SHIPPER_ACCEPTED -> 0.25f
        MatchStatus.PICKED_UP -> 0.5f
        MatchStatus.IN_TRANSIT -> 0.75f
        MatchStatus.DELIVERED -> 1f
        else -> 0f
    }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Delivery Progress",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}