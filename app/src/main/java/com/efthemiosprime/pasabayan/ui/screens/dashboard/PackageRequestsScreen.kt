package com.efthemiosprime.pasabayan.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.efthemiosprime.pasabayan.data.model.PackageRequest
import com.efthemiosprime.pasabayan.presentation.viewmodel.PackageViewModel
import com.efthemiosprime.pasabayan.ui.shared.ScreenContainer
import com.efthemiosprime.pasabayan.ui.screens.dashboard.components.PackageCard
import com.efthemiosprime.pasabayan.ui.shared.EmptyStateView
import com.efthemiosprime.pasabayan.ui.theme.PasabayanDesignSystem

/**
 * Package Requests Screen - Shows all package requests for carriers to view
 * Mirrors iOS behavior for carrier package request browsing
 */
@Composable
fun PackageRequestsScreen(
    packageViewModel: PackageViewModel = viewModel()
) {
    val packageRequests by packageViewModel.packageRequests.collectAsState()
    val isLoading by packageViewModel.isLoading.collectAsState()
    val errorMessage by packageViewModel.errorMessage.collectAsState()
    
    // Load package requests when screen appears
    LaunchedEffect(Unit) {
        packageViewModel.loadPackageRequests()
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
                text = "Package Requests",
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
                        onRetry = { packageViewModel.loadPackageRequests() }
                    )
                }
                packageRequests.isEmpty() -> {
                    EmptyRequestsState()
                }
                else -> {
                    RequestsList(requests = packageRequests)
                }
            }
        }
    }
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
                text = "Loading requests...",
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
            text = "Error Loading Requests",
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
private fun EmptyRequestsState() {
    EmptyStateView(
        icon = Icons.Default.Inventory,
        title = "No Package Requests",
        description = "There are currently no package requests available. Check back later for new delivery opportunities."
    )
}

@Composable
private fun RequestsList(requests: List<PackageRequest>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(requests) { request ->
            PackageCard(
                packageRequest = request,
                onDetailsClick = {
                    // TODO: Navigate to package details for carriers
                },
                onFindCarriersClick = {
                    // TODO: Handle finding carriers for package request
                },
                onViewMatchClick = {
                    // TODO: Handle view match for carriers
                }
            )
        }
    }
}