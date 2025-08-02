package com.efthemiosprime.pasabayan.ui.screens.dashboard.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.data.model.PackageRequest
import com.efthemiosprime.pasabayan.data.model.PackageRequestStatus
import com.efthemiosprime.pasabayan.data.model.PackageSize
import com.efthemiosprime.pasabayan.ui.shared.cards.PCardStandard
import com.efthemiosprime.pasabayan.ui.theme.PasabayanDesignSystem

@Composable
fun PackageCard(
    packageRequest: PackageRequest,
    onDetailsClick: () -> Unit,
    onFindCarriersClick: () -> Unit,
    onViewMatchClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    PCardStandard(
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.lg)
        ) {
            // Header Row - Icon + Title + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.sm)
                ) {
                    Icon(
                        imageVector = getPackageTypeIcon(packageRequest.packageSize),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = packageRequest.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                StatusBadge(status = packageRequest.status)
            }
            
            // Route Information - Pickup → Delivery
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LocationInfo(
                    label = "Pickup",
                    location = packageRequest.pickupLocation,
                    modifier = Modifier.weight(1f)
                )
                
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "to",
                    modifier = Modifier.padding(horizontal = PasabayanDesignSystem.Spacing.sm)
                )
                
                LocationInfo(
                    label = "Delivery",
                    location = packageRequest.deliveryLocation,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Package Details Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetailInfo(
                    label = "Weight",
                    value = "${packageRequest.packageWeight ?: 0.0} kg"
                )
                DetailInfo(
                    label = "Type",
                    value = packageRequest.packageSize.displayName
                )
                DetailInfo(
                    label = "Value",
                    value = packageRequest.packageValue?.let { "CAD $${String.format("%.2f", it)}" } ?: "N/A"
                )
            }
            
            // Action Buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.sm)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
                ) {
                    OutlinedButton(
                        onClick = onDetailsClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("View Details")
                    }
                    
                    if (packageRequest.status in listOf(PackageRequestStatus.PENDING, PackageRequestStatus.OPEN)) {
                        Button(
                            onClick = {
                                println("🔧 DEBUG: Find Carriers button clicked! Package ID: ${packageRequest.id}, Status: ${packageRequest.status}")
                                onFindCarriersClick()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Find Carriers")
                        }
                    } else {
                        println("🔧 DEBUG: Find Carriers button NOT shown - Package status: ${packageRequest.status}")
                    }
                }
                
                // View match button when there are compatible trips
                packageRequest.compatibleTripsCount?.let { count ->
                    if (count > 0) {
                        Button(
                            onClick = onViewMatchClick,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text("View $count ${if (count == 1) "Match" else "Matches"}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationInfo(
    label: String,
    location: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = location,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DetailInfo(
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StatusBadge(status: PackageRequestStatus) {
    val backgroundColor = when (status) {
        PackageRequestStatus.OPEN -> MaterialTheme.colorScheme.primary
        PackageRequestStatus.PENDING -> Color(0xFFFF9800)
        PackageRequestStatus.MATCHED -> Color(0xFF9C27B0)
        PackageRequestStatus.BOOKED -> Color(0xFF9C27B0)
        PackageRequestStatus.IN_TRANSIT -> Color(0xFF4CAF50)
        PackageRequestStatus.DELIVERED -> Color(0xFF2196F3)
        PackageRequestStatus.CANCELLED -> Color(0xFFF44336)
    }
    
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(PasabayanDesignSystem.CornerRadius.card),
        modifier = Modifier.padding(PasabayanDesignSystem.Spacing.xs)
    ) {
        Text(
            text = "${status.icon} ${status.displayName}",
            modifier = Modifier.padding(
                horizontal = PasabayanDesignSystem.Spacing.sm,
                vertical = PasabayanDesignSystem.Spacing.xs
            ),
            color = Color.White,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

// Helper function to get package type icon
private fun getPackageTypeIcon(packageSize: PackageSize): ImageVector {
    return when (packageSize) {
        PackageSize.SMALL -> Icons.Default.LocalShipping
        PackageSize.MEDIUM -> Icons.Default.Inventory
        PackageSize.LARGE -> Icons.Default.LocalShipping
        PackageSize.EXTRA_LARGE -> Icons.Default.LocalShipping
    }
} 