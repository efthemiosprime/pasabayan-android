package com.efthemiosprime.pasabayan.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.efthemiosprime.pasabayan.data.model.DeliveryMatch
import com.efthemiosprime.pasabayan.data.model.MatchStatus
import com.efthemiosprime.pasabayan.presentation.viewmodel.MatchViewModel
import com.efthemiosprime.pasabayan.ui.shared.cards.PCard
import com.efthemiosprime.pasabayan.ui.shared.cards.PCardElevation
import com.efthemiosprime.pasabayan.ui.shared.cards.PCardPadding
import com.efthemiosprime.pasabayan.ui.theme.PasabayanDesignSystem
import java.text.SimpleDateFormat
import java.util.*

/**
 * My Matches Screen - Displays shipper's delivery matches
 * Mirrors iOS MyMatchesScreen with match listing and status tracking
 * Follows Pasabayan design patterns
 */
@Composable
fun MyMatchesScreen(
    matchViewModel: MatchViewModel? = null,
    onMatchClick: (DeliveryMatch) -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel = matchViewModel ?: viewModel { MatchViewModel(context.applicationContext as android.app.Application) }
    val matches by viewModel.shipperMatches.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadShipperMatches()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(PasabayanDesignSystem.Spacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.sectionSpacing)
    ) {
        // Header
        Text(
            text = "My Delivery Matches",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        when {
            isLoading -> {
                LoadingState()
            }
            errorMessage != null -> {
                ErrorState(
                    errorMessage = errorMessage ?: "Unknown error",
                    onRetry = { viewModel.loadShipperMatches() }
                )
            }
            matches.isEmpty() -> {
                EmptyMatchesState()
            }
            else -> {
                MatchesList(
                    matches = matches,
                    onMatchClick = onMatchClick
                )
            }
        }
    }
}

/**
 * Loading state component
 */
@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Loading matches...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Error state component
 */
@Composable
private fun ErrorState(
    errorMessage: String,
    onRetry: () -> Unit
) {
    PCard(
        modifier = Modifier.fillMaxWidth(),
        padding = PCardPadding.Medium,
        elevation = PCardElevation.Medium,
        backgroundColor = MaterialTheme.colorScheme.errorContainer
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            
            Text(
                text = "Failed to load matches",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = "Retry",
                    color = MaterialTheme.colorScheme.onError
                )
            }
        }
    }
}

/**
 * Empty matches state component
 */
@Composable
private fun EmptyMatchesState() {
    PCard(
        modifier = Modifier.fillMaxWidth(),
        padding = PCardPadding.Large,
        elevation = PCardElevation.Medium
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Groups,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "No Matches Yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "Your delivery matches will appear here when carriers accept your package requests.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "💡 Create package requests to start getting matches!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Matches list component
 */
@Composable
private fun MatchesList(
    matches: List<DeliveryMatch>,
    onMatchClick: (DeliveryMatch) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(matches) { match ->
            MatchCard(
                match = match,
                onClick = { onMatchClick(match) }
            )
        }
    }
}

/**
 * Individual match card component
 * Mirrors iOS match card design
 */
@Composable
private fun MatchCard(
    match: DeliveryMatch,
    onClick: () -> Unit
) {
    PCard(
        modifier = Modifier
            .fillMaxWidth(),
        padding = PCardPadding.Medium,
        elevation = PCardElevation.Medium,
        onClick = onClick
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header with status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Match #${match.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                MatchStatusChip(status = match.status)
            }
            
            // Package info
            match.packageRequest?.let { packageRequest ->
                Text(
                    text = packageRequest.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "${packageRequest.pickupLocation} → ${packageRequest.deliveryLocation}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Price and carrier info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Price: $${String.format("%.2f", match.agreedPrice)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                match.carrier?.let { carrier ->
                    Text(
                        text = "Carrier: ${carrier.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Created date
            Text(
                text = "Created: ${formatDate(match.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Match status chip component
 */
@Composable
private fun MatchStatusChip(status: MatchStatus) {
    val (backgroundColor, textColor) = when (status) {
        MatchStatus.PENDING -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        MatchStatus.CONFIRMED -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        MatchStatus.PICKED_UP, MatchStatus.IN_TRANSIT -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        MatchStatus.DELIVERED -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        MatchStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        MatchStatus.CARRIER_REQUESTED -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        MatchStatus.SHIPPER_REQUESTED -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        MatchStatus.SHIPPER_ACCEPTED -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        MatchStatus.SHIPPER_DECLINED -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        MatchStatus.CARRIER_ACCEPTED -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        MatchStatus.CARRIER_DECLINED -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    }
    
    Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        color = backgroundColor
    ) {
        Text(
            text = status.description.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

/**
 * Format date for display
 */
private fun formatDate(dateString: String): String {
    return try {
        val inputFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = inputFormatter.parse(dateString)
        date?.let { outputFormatter.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}