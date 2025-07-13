package com.efthemiosprime.pasabayan.ui.screens.packagedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.efthemiosprime.pasabayan.data.model.*
import com.efthemiosprime.pasabayan.ui.components.status.StatusChip
import com.efthemiosprime.pasabayan.ui.shared.ScreenContainer
import com.efthemiosprime.pasabayan.ui.shared.cards.PCardStandard
import com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme
import com.efthemiosprime.pasabayan.ui.theme.PasabayanDesignSystem
import java.text.SimpleDateFormat
import java.util.*

/**
 * Package Detail Screen - Mirrors iOS Package Detail View
 * Shows comprehensive package information with timeline tracking
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackageDetailScreen(
    packageRequest: PackageRequest,
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    ScreenContainer {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            text = "Package Details",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        // Status Badge (matching iOS "Electronics" badge)
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = packageRequest.packageSize.displayName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                        
                        // Done Button
                        TextButton(onClick = onNavigateBack) {
                            Text(
                                text = "Done",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = PasabayanDesignSystem.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.xxl),
                contentPadding = PaddingValues(vertical = PasabayanDesignSystem.Spacing.lg)
            ) {
                // Package Details Section
                item {
                    PackageDetailsSection(packageRequest = packageRequest)
                }
                
                // Locations Section
                item {
                    LocationsSection(packageRequest = packageRequest)
                }
                
                // Timeline Section
                item {
                    TimelineSection(packageRequest = packageRequest)
                }
            }
        }
    }
}

/**
 * Package Details Section
 */
@Composable
private fun PackageDetailsSection(
    packageRequest: PackageRequest,
    modifier: Modifier = Modifier
) {
    PCardStandard(
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.lg)
        ) {
            Text(
                text = "Package Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            // Description
            PackageDetailRow(
                label = "Description",
                value = packageRequest.title,
                valueStyle = MaterialTheme.typography.bodyLarge
            )
            
            // Weight
            PackageDetailRow(
                label = "Weight",
                value = "${packageRequest.packageWeight ?: 0.0} kg",
                valueStyle = MaterialTheme.typography.bodyLarge
            )
            
            // Type
            PackageDetailRow(
                label = "Type",
                value = packageRequest.packageSize.displayName,
                valueStyle = MaterialTheme.typography.bodyLarge
            )
            
            // Value (if provided)
            packageRequest.packageValue?.let { value ->
                PackageDetailRow(
                    label = "Value",
                    value = "CAD ${String.format("%.2f", value)}",
                    valueStyle = MaterialTheme.typography.bodyLarge
                )
            }
            
            // Special Instructions (if any)
            packageRequest.specialInstructions?.let { instructions ->
                PackageDetailRow(
                    label = "Special Instructions",
                    value = instructions,
                    valueStyle = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

/**
 * Locations Section
 */
@Composable
private fun LocationsSection(
    packageRequest: PackageRequest,
    modifier: Modifier = Modifier
) {
    PCardStandard(
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.xl)
        ) {
            Text(
                text = "Locations",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            // Pickup Location
            LocationItem(
                icon = Icons.Default.LocationOn,
                iconColor = MaterialTheme.colorScheme.primary,
                title = "Pickup Location",
                address = packageRequest.pickupLocation,
                subtitle = "Contact Info • Available on booking"
            )
            
            // Delivery Location
            LocationItem(
                icon = Icons.Default.Place,
                iconColor = Color(0xFF4CAF50),
                title = "Delivery Location",
                address = packageRequest.deliveryLocation,
                subtitle = "Contact Info • Available on booking"
            )
        }
    }
}

/**
 * Timeline Section
 */
@Composable
private fun TimelineSection(
    packageRequest: PackageRequest,
    modifier: Modifier = Modifier
) {
    PCardStandard(
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.xl)
        ) {
            Text(
                text = "Timeline",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            // Timeline items based on status
            val timelineItems = getTimelineItems(packageRequest)
            
            timelineItems.forEachIndexed { index, item ->
                TimelineItem(
                    title = item.title,
                    timestamp = item.timestamp,
                    isCompleted = item.isCompleted,
                    isLast = index == timelineItems.size - 1
                )
            }
        }
    }
}

/**
 * Package Detail Row Component
 */
@Composable
private fun PackageDetailRow(
    label: String,
    value: String,
    valueStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.xs)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = valueStyle,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Location Item Component
 */
@Composable
private fun LocationItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    address: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.lg)
    ) {
        // Icon
        Surface(
            color = iconColor,
            shape = CircleShape,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier
                    .size(24.dp)
                    .padding(PasabayanDesignSystem.Spacing.sm)
            )
        }
        
        // Location details
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.xs)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = address,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Timeline Item Component
 */
@Composable
private fun TimelineItem(
    title: String,
    timestamp: String?,
    isCompleted: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.lg)
    ) {
        // Timeline indicator
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circle indicator
            Surface(
                color = if (isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline,
                shape = CircleShape,
                modifier = Modifier.size(16.dp)
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier
                            .size(12.dp)
                            .padding(2.dp)
                    )
                }
            }
            
            // Connecting line (if not last item)
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(PasabayanDesignSystem.Spacing.xxxl)
                        .background(
                            color = if (isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(1.dp)
                        )
                )
            }
        }
        
        // Timeline content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.xs)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isCompleted) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isCompleted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (timestamp != null && isCompleted) {
                Text(
                    text = timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Timeline Item Data Class
 */
private data class TimelineItemData(
    val title: String,
    val timestamp: String?,
    val isCompleted: Boolean
)

/**
 * Get timeline items based on package status
 */
private fun getTimelineItems(packageRequest: PackageRequest): List<TimelineItemData> {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val displayFormatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    
    return listOf(
        TimelineItemData(
            title = "Request Created",
            timestamp = try {
                val date = formatter.parse(packageRequest.createdAt)
                date?.let { displayFormatter.format(it) }
            } catch (e: Exception) {
                packageRequest.createdAt
            },
            isCompleted = true
        ),
        TimelineItemData(
            title = "Matched with Carrier",
            timestamp = null,
            isCompleted = packageRequest.status in listOf(
                PackageRequestStatus.MATCHED,
                PackageRequestStatus.BOOKED,
                PackageRequestStatus.IN_TRANSIT,
                PackageRequestStatus.DELIVERED
            )
        ),
        TimelineItemData(
            title = "Carrier Assigned",
            timestamp = null,
            isCompleted = packageRequest.status in listOf(
                PackageRequestStatus.BOOKED,
                PackageRequestStatus.IN_TRANSIT,
                PackageRequestStatus.DELIVERED
            )
        ),
        TimelineItemData(
            title = "Package Picked Up",
            timestamp = null,
            isCompleted = packageRequest.status in listOf(
                PackageRequestStatus.IN_TRANSIT,
                PackageRequestStatus.DELIVERED
            )
        ),
        TimelineItemData(
            title = "Package Delivered",
            timestamp = null,
            isCompleted = packageRequest.status == PackageRequestStatus.DELIVERED
        )
    )
}

/**
 * Preview
 */
@Preview(showBackground = true, name = "Package Detail Screen")
@Composable
fun PackageDetailScreenPreview() {
    PasabayanTheme {
        val samplePackage = PackageRequest(
            id = 1,
            shipperId = 1,
            title = "Electronic item",
            description = "Laptop and accessories",
            pickupLocation = "2680 goyer, Montreal",
            deliveryLocation = "102 LP Leviste, Makati",
            preferredPickupDate = "2025-07-15",
            packageSize = PackageSize.MEDIUM,
            packageWeight = 1.5,
            packageValue = 250.0,
            isFragile = false,
            specialInstructions = "Handle with care",
            status = PackageRequestStatus.MATCHED,
            createdAt = "2025-07-13T00:10:21.000000Z",
            updatedAt = "2025-07-13T00:10:21.000000Z"
        )
        
        PackageDetailScreen(
            packageRequest = samplePackage,
            onNavigateBack = {}
        )
    }
} 