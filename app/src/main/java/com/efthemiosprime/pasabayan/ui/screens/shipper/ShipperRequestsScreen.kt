package com.efthemiosprime.pasabayan.ui.screens.shipper

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.efthemiosprime.pasabayan.data.model.DeliveryMatch
import com.efthemiosprime.pasabayan.data.model.MatchStatus
import com.efthemiosprime.pasabayan.presentation.viewmodel.MatchViewModel
import com.efthemiosprime.pasabayan.ui.screens.shipper.components.ShipperAcceptDeclineDialog
import com.efthemiosprime.pasabayan.ui.screens.shipper.components.CarrierRequestCard
import com.efthemiosprime.pasabayan.ui.screens.shipper.components.ActiveMatchCard
import com.efthemiosprime.pasabayan.ui.screens.shipper.components.CarrierRequestDetailSheet
import com.efthemiosprime.pasabayan.ui.shared.ScreenContainer
import com.efthemiosprime.pasabayan.ui.shared.EmptyStateView
import com.efthemiosprime.pasabayan.ui.theme.PasabayanDesignSystem

/**
 * Shipper Requests Screen - Mirrors iOS carrier request handling for shippers
 * Complete carrier request workflow with sectioned lists:
 * - "Carrier Requests" section for new carrier offers
 * - "Active Matches" section for accepted/in-progress deliveries
 * - Quick accept/decline actions
 * - Detailed carrier request review functionality
 * - Real-time status updates
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipperRequestsScreen(
    matchViewModel: MatchViewModel = viewModel(),
    onNavigateBack: (() -> Unit)? = null
) {
    val viewModel = matchViewModel
    
    val shipperMatches by viewModel.shipperMatches.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    // State for accept/decline dialogs
    var selectedRequest by remember { mutableStateOf<DeliveryMatch?>(null) }
    var showingAcceptDialog by remember { mutableStateOf(false) }
    var showingDeclineDialog by remember { mutableStateOf(false) }
    var showingAlert by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }
    
    // Computed properties matching iOS implementation - only carrier requests
    val carrierRequests = remember(shipperMatches) {
        shipperMatches.filter { it.status == MatchStatus.CARRIER_REQUESTED }
    }
    
    // Load shipper matches when screen appears
    LaunchedEffect(Unit) {
        viewModel.loadShipperMatches()
    }
    
    ScreenContainer {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            "Carrier Requests",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        if (onNavigateBack != null) {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = { viewModel.loadShipperMatches() }
                        ) {
                            Text("Refresh")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { paddingValues ->
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
                carrierRequests.isEmpty() -> {
                    EmptyRequestsState()
                }
                else -> {
                    CarrierRequestsList(
                        carrierRequests = carrierRequests,
                        onAccept = { request ->
                            selectedRequest = request
                            showingAcceptDialog = true
                        },
                        onDecline = { request ->
                            selectedRequest = request
                            showingDeclineDialog = true
                        },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
    
    // Removed detail sheet to match iOS simplicity
    
    // Accept Dialog - Shows message input for accepting carrier request
    if (showingAcceptDialog && selectedRequest != null) {
        ShipperAcceptDeclineDialog(
            request = selectedRequest!!,
            isAcceptMode = true,
            onAccept = { message ->
                showingAcceptDialog = false
                viewModel.acceptCarrierRequest(selectedRequest!!.id, message)
                alertMessage = "Carrier request accepted! Chat has been opened for communication."
                showingAlert = true
                selectedRequest = null
            },
            onDecline = { _, _ -> /* Not used in accept mode */ },
            onDismiss = { 
                showingAcceptDialog = false
                selectedRequest = null
            }
        )
    }
    
    // Decline Dialog - Shows reason selection and message input for declining
    if (showingDeclineDialog && selectedRequest != null) {
        ShipperAcceptDeclineDialog(
            request = selectedRequest!!,
            isAcceptMode = false,
            onAccept = { _ -> /* Not used in decline mode */ },
            onDecline = { reason, message ->
                showingDeclineDialog = false
                viewModel.declineCarrierRequest(selectedRequest!!.id, reason, message)
                alertMessage = "Carrier request declined."
                showingAlert = true
                selectedRequest = null
            },
            onDismiss = { 
                showingDeclineDialog = false
                selectedRequest = null
            }
        )
    }
    
    // Alert Dialog - Shows action completion messages
    if (showingAlert) {
        AlertDialog(
            onDismissRequest = { showingAlert = false },
            title = { Text("Action Complete") },
            text = { Text(alertMessage) },
            confirmButton = {
                TextButton(onClick = { showingAlert = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Loading carrier requests...",
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
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun EmptyRequestsState() {
    EmptyStateView(
        icon = Icons.Default.LocalShipping,
        title = "No Carrier Requests",
        description = "You haven't received any carrier requests yet. Carriers will be able to request to carry your packages."
    )
}

@Composable
private fun CarrierRequestsList(
    carrierRequests: List<DeliveryMatch>,
    onAccept: (DeliveryMatch) -> Unit,
    onDecline: (DeliveryMatch) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(PasabayanDesignSystem.Spacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
    ) {
        items(carrierRequests) { request ->
            CarrierRequestCard(
                request = request,
                onAccept = { onAccept(request) },
                onDecline = { onDecline(request) }
            )
        }
    }
}

