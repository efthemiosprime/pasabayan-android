package com.efthemiosprime.pasabayan.ui.screens.carrier

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.efthemiosprime.pasabayan.data.model.DeliveryMatch
import com.efthemiosprime.pasabayan.data.model.MatchStatus
import com.efthemiosprime.pasabayan.presentation.viewmodel.MatchViewModel
import com.efthemiosprime.pasabayan.ui.screens.carrier.components.BookingRequestCard
import com.efthemiosprime.pasabayan.ui.screens.carrier.components.ActiveBookingCard
import com.efthemiosprime.pasabayan.ui.screens.carrier.components.BookingRequestDetailSheet
import com.efthemiosprime.pasabayan.ui.shared.ScreenContainer
import com.efthemiosprime.pasabayan.ui.shared.EmptyStateView
import com.efthemiosprime.pasabayan.ui.theme.PasabayanDesignSystem

/**
 * Carrier Requests Screen - Mirrors iOS CarrierRequestsView (810 lines)
 * Complete booking request workflow with sectioned lists:
 * - "Pending Requests" section for new booking requests  
 * - "Active Bookings" section for confirmed/in-progress deliveries
 * - Quick accept/decline actions
 * - Detailed booking review functionality
 * - Real-time status updates
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarrierRequestsScreen(
    matchViewModel: MatchViewModel = viewModel(),
    onNavigateBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val viewModel = matchViewModel ?: viewModel { MatchViewModel(context.applicationContext as android.app.Application) }
    
    val carrierMatches by viewModel.carrierMatches.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    // State for detail sheet
    var selectedRequest by remember { mutableStateOf<DeliveryMatch?>(null) }
    var showingRequestDetail by remember { mutableStateOf(false) }
    var showingAlert by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }
    
    // Computed properties matching iOS implementation
    val pendingRequests = remember(carrierMatches) {
        carrierMatches.filter { it.status == MatchStatus.PENDING }
    }
    
    val activeBookings = remember(carrierMatches) {
        carrierMatches.filter { 
            it.status == MatchStatus.CONFIRMED || 
            it.status == MatchStatus.PICKED_UP || 
            it.status == MatchStatus.IN_TRANSIT 
        }
    }
    
    val allCarrierMatches = remember(carrierMatches) {
        carrierMatches.filter { 
            it.status != MatchStatus.CANCELLED && 
            it.status != MatchStatus.DELIVERED 
        }
    }
    
    // Load carrier matches when screen appears
    LaunchedEffect(Unit) {
        viewModel.loadCarrierMatches()
    }
    
    ScreenContainer {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top App Bar
            TopAppBar(
                title = { 
                    Text(
                        "Booking Requests",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.loadCarrierMatches() }
                    ) {
                        Text("Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
            
            when {
                isLoading -> {
                    LoadingState()
                }
                errorMessage != null -> {
                    ErrorState(
                        errorMessage = errorMessage ?: "Unknown error",
                        onRetry = { viewModel.loadCarrierMatches() }
                    )
                }
                allCarrierMatches.isEmpty() -> {
                    EmptyRequestsState()
                }
                else -> {
                    RequestsAndBookingsList(
                        pendingRequests = pendingRequests,
                        activeBookings = activeBookings,
                        onRequestTap = { request ->
                            selectedRequest = request
                            showingRequestDetail = true
                        },
                        onQuickAccept = { request ->
                            handleAcceptRequest(request, viewModel) { message ->
                                alertMessage = message
                                showingAlert = true
                            }
                        }
                    )
                }
            }
        }
    }
    
    // Detail Sheet
    if (showingRequestDetail && selectedRequest != null) {
        BookingRequestDetailSheet(
            request = selectedRequest!!,
            onDismiss = { showingRequestDetail = false },
            onAccept = { request ->
                showingRequestDetail = false
                handleAcceptRequest(request, viewModel) { message ->
                    alertMessage = message
                    showingAlert = true
                }
            },
            onDecline = { request ->
                showingRequestDetail = false
                handleDeclineRequest(request, viewModel) { message ->
                    alertMessage = message
                    showingAlert = true
                }
            },
            onCancel = { booking ->
                showingRequestDetail = false
                handleCancelBooking(booking, viewModel) { message ->
                    alertMessage = message
                    showingAlert = true
                }
            }
        )
    }
    
    // Alert Dialog
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
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading booking requests...",
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
        modifier = Modifier
            .fillMaxSize()
            .padding(PasabayanDesignSystem.Spacing.screenPadding)
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
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
        icon = Icons.Default.Mail,
        title = "No Booking Requests",
        description = "When shippers request to book your trips, they'll appear here."
    )
}

@Composable
private fun RequestsAndBookingsList(
    pendingRequests: List<DeliveryMatch>,
    activeBookings: List<DeliveryMatch>,
    onRequestTap: (DeliveryMatch) -> Unit,
    onQuickAccept: (DeliveryMatch) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(PasabayanDesignSystem.Spacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(PasabayanDesignSystem.Spacing.md)
    ) {
        // Pending Requests Section
        if (pendingRequests.isNotEmpty()) {
            item {
                Text(
                    text = "Pending Requests (${pendingRequests.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(pendingRequests) { request ->
                BookingRequestCard(
                    request = request,
                    onTap = { onRequestTap(request) },
                    onQuickAccept = { onQuickAccept(request) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
        
        // Active Bookings Section
        if (activeBookings.isNotEmpty()) {
            item {
                Text(
                    text = "Active Bookings (${activeBookings.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(activeBookings) { booking ->
                ActiveBookingCard(
                    booking = booking,
                    onTap = { onRequestTap(booking) }
                )
            }
        }
    }
}

// MARK: - Action Handlers (mirroring iOS implementation)

private fun handleAcceptRequest(
    request: DeliveryMatch,
    viewModel: MatchViewModel,
    onShowAlert: (String) -> Unit
) {
    viewModel.confirmMatch(request.id)
    onShowAlert("Booking request accepted! The shipper has been notified.")
}

private fun handleDeclineRequest(
    request: DeliveryMatch,
    viewModel: MatchViewModel,
    onShowAlert: (String) -> Unit
) {
    viewModel.cancelMatch(request.id)
    onShowAlert("Booking request declined.")
}

private fun handleCancelBooking(
    booking: DeliveryMatch,
    viewModel: MatchViewModel,
    onShowAlert: (String) -> Unit
) {
    viewModel.cancelMatch(booking.id)
    onShowAlert("Booking cancelled. The shipper has been notified.")
}