package com.efthemiosprime.pasabayan.ui.screens.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.presentation.viewmodel.AnalyticsViewModel
import com.efthemiosprime.pasabayan.presentation.viewmodel.AnalyticsState
import com.efthemiosprime.pasabayan.ui.components.analytics.*
import com.efthemiosprime.pasabayan.ui.common.ErrorMessage
import com.efthemiosprime.pasabayan.ui.shared.cards.PCard
import com.efthemiosprime.pasabayan.ui.shared.PButton

/**
 * Main analytics screen displaying carrier and shipper analytics
 */
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when {
            state.isLoading && state.carrierStats.data == null && state.shipperStats.data == null -> {
                // Initial loading state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            state.error != null && state.carrierStats.data == null && state.shipperStats.data == null -> {
                // Error state with no data
                ErrorMessage(
                    message = stringResource(R.string.error_loading_data),
                    onRetry = { viewModel.refreshData() }
                )
            }
            
            else -> {
                // Success state or loading with existing data
                AnalyticsContent(
                    state = state,
                    onRefresh = { viewModel.refreshData() }
                )
            }
        }
    }
}

/**
 * Analytics content display
 */
@Composable
private fun AnalyticsContent(
    state: AnalyticsState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Carrier Analytics Section
        state.carrierStats.data?.let { stats ->
            item {
                Text(
                    text = stringResource(R.string.carrier_performance),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                CarrierMetricsCard(stats = stats)
            }
            
            item {
                RouteAnalyticsCard(routes = stats.routeAnalytics)
            }
            
            item {
                PerformanceInsightsCard(insights = stats.performanceInsights)
            }
        }
        
        // Shipper Analytics Section
        state.shipperStats.data?.let { stats ->
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            item {
                Text(
                    text = stringResource(R.string.shipper_analytics),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                BudgetOverviewCard(budgetTracking = stats.budgetTracking)
            }
            
            item {
                PreferredCarriersCard(carriers = stats.preferredCarriers)
            }
            
            item {
                CostOptimizationCard(optimization = stats.costOptimization)
            }
        }
        
        // Loading indicator for refresh
        if (state.isRefreshing) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }
        
        // Error message for refresh errors
        state.error?.let { error ->
            item {
                PCard(
                    backgroundColor = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = "Error loading data",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = error.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    PButton(
                        text = "Retry",
                        onClick = onRefresh,
                        backgroundColor = MaterialTheme.colorScheme.error,
                        textColor = MaterialTheme.colorScheme.onError
                    )
                }
            }
        }
    }
}

 