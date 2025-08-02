package com.efthemiosprime.pasabayan.ui.screens.match

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.efthemiosprime.pasabayan.data.model.DeliveryMatch
import com.efthemiosprime.pasabayan.data.model.MatchStatus
import com.efthemiosprime.pasabayan.data.model.PackageRequest
import com.efthemiosprime.pasabayan.presentation.viewmodel.MatchViewModel
import com.efthemiosprime.pasabayan.ui.shared.cards.PCard
import com.efthemiosprime.pasabayan.ui.shared.cards.PCardElevation
import com.efthemiosprime.pasabayan.ui.shared.cards.PCardPadding
import com.efthemiosprime.pasabayan.ui.theme.PasabayanDesignSystem
import java.text.SimpleDateFormat
import java.util.*

/**
 * View Match Screen - Mirrors iOS ViewMatchScreen
 * Shows delivery match details, tracking, and shipper actions
 * Follows iOS design patterns and functionality
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewMatchScreen(
    packageRequest: PackageRequest,
    onNavigateBack: () -> Unit,
    matchViewModel: MatchViewModel = viewModel()
) {
    val shipperMatches by matchViewModel.shipperMatches.collectAsState()
    val isLoading by matchViewModel.isLoading.collectAsState()
    val errorMessage by matchViewModel.errorMessage.collectAsState()
    
    // Find the match for this package
    val deliveryMatch = shipperMatches.find { match ->
        match.packageRequestId == packageRequest.id
    }
    
    LaunchedEffect(packageRequest.id) {
        matchViewModel.loadShipperMatches()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Delivery Match",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(PasabayanDesignSystem.Spacing.screenPadding),
            verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.sectionSpacing)
        ) {
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (deliveryMatch != null) {
                // Match Status Card
                item {
                    MatchStatusCard(deliveryMatch = deliveryMatch)
                }
                
                // Package Details Card
                item {
                    PackageDetailsCard(packageRequest = packageRequest)
                }
                
                // Carrier Details Card
                item {
                    CarrierDetailsCard(deliveryMatch = deliveryMatch)
                }
                
                // Tracking Timeline Card
                item {
                    TrackingTimelineCard(deliveryMatch = deliveryMatch)
                }
                
                // Match Actions (if applicable)
                if (deliveryMatch.status == MatchStatus.PENDING) {
                    item {
                        MatchActionsCard(
                            onCancelMatch = {
                                matchViewModel.cancelMatch(deliveryMatch.id)
                            }
                        )
                    }
                }
            } else {
                item {
                    NoMatchFoundCard()
                }
            }
            
            // Error handling
            errorMessage?.let { error ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

/**
 * Match Status Chip - Custom status chip for match status
 */
@Composable
private fun MatchStatusChip(
    status: MatchStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (status) {
        MatchStatus.PENDING -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        MatchStatus.CONFIRMED -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        MatchStatus.PICKED_UP, MatchStatus.IN_TRANSIT -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        MatchStatus.DELIVERED -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        MatchStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor
    ) {
        Text(
            text = status.description.uppercase(),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

/**
 * Match Status Card - Shows current status and progress
 * Mirrors iOS match status display
 */
@Composable
private fun MatchStatusCard(deliveryMatch: DeliveryMatch) {
    PCard(
        modifier = Modifier.fillMaxWidth(),
        padding = PCardPadding.Medium,
        elevation = PCardElevation.Medium
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Match Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                MatchStatusChip(status = deliveryMatch.status)
            }
            
            // Price Information
            Text(
                text = "Agreed Price: $${String.format("%.2f", deliveryMatch.agreedPrice)}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            
            // Match ID
            Text(
                text = "Match ID: #${deliveryMatch.id}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Package Details Card - Shows package information
 */
@Composable
private fun PackageDetailsCard(packageRequest: PackageRequest) {
    PCard(
        modifier = Modifier.fillMaxWidth(),
        padding = PCardPadding.Medium,
        elevation = PCardElevation.Medium
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Package Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = packageRequest.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            
            packageRequest.description?.let { description ->
                if (description.isNotBlank()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Route Information
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = "From: ${packageRequest.pickupLocation}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "To: ${packageRequest.deliveryLocation}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

/**
 * Carrier Details Card - Shows carrier information
 */
@Composable
private fun CarrierDetailsCard(deliveryMatch: DeliveryMatch) {
    PCard(
        modifier = Modifier.fillMaxWidth(),
        padding = PCardPadding.Medium,
        elevation = PCardElevation.Medium
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Carrier Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Column {
                    Text(
                        text = deliveryMatch.carrier?.name ?: "Carrier",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    
                    deliveryMatch.carrier?.email?.let { email ->
                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Contact option
            if (deliveryMatch.status in listOf(MatchStatus.CONFIRMED, MatchStatus.PICKED_UP, MatchStatus.IN_TRANSIT)) {
                OutlinedButton(
                    onClick = { /* TODO: Implement contact carrier */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Contact Carrier")
                }
            }
        }
    }
}

/**
 * Tracking Timeline Card - Shows delivery progress
 * Mirrors iOS tracking timeline functionality
 */
@Composable
private fun TrackingTimelineCard(deliveryMatch: DeliveryMatch) {
    PCard(
        modifier = Modifier.fillMaxWidth(),
        padding = PCardPadding.Medium,
        elevation = PCardElevation.Medium
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Tracking Timeline",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Timeline items
            val timelineItems = buildTrackingTimeline(deliveryMatch)
            
            timelineItems.forEach { item ->
                TimelineItemView(
                    title = item.title,
                    timestamp = item.timestamp,
                    isCompleted = item.isCompleted,
                    isActive = item.isActive
                )
            }
        }
    }
}

/**
 * Timeline Item View
 */
@Composable
private fun TimelineItemView(
    title: String,
    timestamp: String?,
    isCompleted: Boolean,
    isActive: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Status indicator
        Box(
            modifier = Modifier.size(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                )
            } else if (isActive) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        )
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            MaterialTheme.colorScheme.outlineVariant,
                            shape = CircleShape
                        )
                )
            }
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = if (isCompleted || isActive) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            timestamp?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Match Actions Card - For pending matches
 */
@Composable
private fun MatchActionsCard(
    onCancelMatch: () -> Unit
) {
    PCard(
        modifier = Modifier.fillMaxWidth(),
        padding = PCardPadding.Medium,
        elevation = PCardElevation.Medium
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            OutlinedButton(
                onClick = onCancelMatch,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Cancel Match")
            }
        }
    }
}

/**
 * No Match Found Card - When no match exists for the package
 */
@Composable
private fun NoMatchFoundCard() {
    PCard(
        modifier = Modifier.fillMaxWidth(),
        padding = PCardPadding.Medium,
        elevation = PCardElevation.Medium
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "No Match Found",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "This package hasn't been matched with a carrier yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { /* TODO: Navigate to find carriers */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Find Carriers")
            }
        }
    }
}

/**
 * Data class for tracking timeline items
 */
private data class TimelineItem(
    val title: String,
    val timestamp: String?,
    val isCompleted: Boolean,
    val isActive: Boolean
)

/**
 * Build tracking timeline based on match status
 * Mirrors iOS tracking logic
 */
private fun buildTrackingTimeline(deliveryMatch: DeliveryMatch): List<TimelineItem> {
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
    
    return listOf(
        TimelineItem(
            title = "Match Created",
            timestamp = formatDate(deliveryMatch.createdAt, dateFormatter),
            isCompleted = true,
            isActive = false
        ),
        TimelineItem(
            title = "Match Confirmed",
            timestamp = deliveryMatch.confirmedAt?.let { formatDate(it, dateFormatter) },
            isCompleted = deliveryMatch.status != MatchStatus.PENDING,
            isActive = deliveryMatch.status == MatchStatus.CONFIRMED
        ),
        TimelineItem(
            title = "Package Picked Up",
            timestamp = deliveryMatch.pickedUpAt?.let { formatDate(it, dateFormatter) },
            isCompleted = deliveryMatch.status in listOf(MatchStatus.PICKED_UP, MatchStatus.IN_TRANSIT, MatchStatus.DELIVERED),
            isActive = deliveryMatch.status == MatchStatus.PICKED_UP
        ),
        TimelineItem(
            title = "In Transit",
            timestamp = null, // Usually same as pickup time
            isCompleted = deliveryMatch.status in listOf(MatchStatus.IN_TRANSIT, MatchStatus.DELIVERED),
            isActive = deliveryMatch.status == MatchStatus.IN_TRANSIT
        ),
        TimelineItem(
            title = "Delivered",
            timestamp = deliveryMatch.deliveredAt?.let { formatDate(it, dateFormatter) },
            isCompleted = deliveryMatch.status == MatchStatus.DELIVERED,
            isActive = deliveryMatch.status == MatchStatus.DELIVERED
        )
    )
}

/**
 * Format date string for display
 */
private fun formatDate(dateString: String, formatter: SimpleDateFormat): String {
    return try {
        val inputFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val date = inputFormatter.parse(dateString)
        date?.let { formatter.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}