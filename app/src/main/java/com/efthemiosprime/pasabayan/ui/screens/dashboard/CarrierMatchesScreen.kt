package com.efthemiosprime.pasabayan.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.efthemiosprime.pasabayan.data.model.DeliveryMatch
import com.efthemiosprime.pasabayan.presentation.viewmodel.MatchViewModel
import com.efthemiosprime.pasabayan.ui.components.MatchCard
import com.efthemiosprime.pasabayan.ui.shared.ScreenContainer
import com.efthemiosprime.pasabayan.ui.shared.EmptyStateView
import com.efthemiosprime.pasabayan.ui.theme.PasabayanDesignSystem

/**
 * Carrier Matches Screen - Shows all delivery matches for carriers
 * Mirrors iOS behavior for carrier match management
 */
@Composable
fun CarrierMatchesScreen(
    matchViewModel: MatchViewModel = viewModel()
) {
    val carrierMatches by matchViewModel.carrierMatches.collectAsState()
    val isLoading by matchViewModel.isLoading.collectAsState()
    val errorMessage by matchViewModel.errorMessage.collectAsState()
    
    // Load carrier matches when screen appears
    LaunchedEffect(Unit) {
        matchViewModel.loadCarrierMatches()
    }
    
    ScreenContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(PasabayanDesignSystem.Spacing.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.sectionSpacing)
        ) {
            Text(
                text = "My Matches",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            
            when {
                isLoading -> {
                    LoadingState()
                }
                errorMessage != null -> {
                    ErrorState(
                        errorMessage = errorMessage ?: "Unknown error",
                        onRetry = { matchViewModel.loadCarrierMatches() }
                    )
                }
                carrierMatches.isEmpty() -> {
                    EmptyMatchesState()
                }
                else -> {
                    MatchesList(matches = carrierMatches, onMatchAction = { match, action ->
                        when (action) {
                            MatchAction.CONFIRM -> matchViewModel.confirmMatch(match.id)
                            MatchAction.PICKUP -> matchViewModel.pickupMatch(match.id, "")
                            MatchAction.DELIVER -> matchViewModel.deliverMatch(match.id, "")
                            MatchAction.CANCEL -> matchViewModel.cancelMatch(match.id)
                        }
                    })
                }
            }
        }
    }
}

enum class MatchAction {
    CONFIRM, PICKUP, DELIVER, CANCEL
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading matches...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorState(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Error Loading Matches",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Try Again")
        }
    }
}

@Composable
private fun EmptyMatchesState() {
    EmptyStateView(
        icon = Icons.Default.Assignment,
        title = "No Matches Yet",
        description = "You don't have any delivery matches yet. Matches will appear here when shippers book your trips."
    )
}

@Composable
private fun MatchesList(
    matches: List<DeliveryMatch>,
    onMatchAction: (DeliveryMatch, MatchAction) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(matches) { match ->
            MatchCard(
                match = match,
                onAction = { action -> onMatchAction(match, action) }
            )
        }
    }
}