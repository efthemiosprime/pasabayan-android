package com.efthemiosprime.pasabayan.ui.screens.dashboard.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.efthemiosprime.pasabayan.presentation.viewmodel.CarrierViewModel
import com.efthemiosprime.pasabayan.presentation.viewmodel.AuthViewModel
import com.efthemiosprime.pasabayan.presentation.viewmodel.RoleViewModel
import com.efthemiosprime.pasabayan.ui.screens.dashboard.components.UserHeaderCard
import com.efthemiosprime.pasabayan.ui.screens.dashboard.components.CarrierStatusCard
import com.efthemiosprime.pasabayan.ui.screens.dashboard.components.CarrierStatsGrid
import com.efthemiosprime.pasabayan.ui.screens.dashboard.components.RecentTripsSection
import com.efthemiosprime.pasabayan.ui.screens.carrier.CarrierRequestsScreen
import com.efthemiosprime.pasabayan.ui.common.EmptyStateData

/**
 * Carrier Home Content - Mirrors iOS CarrierHomeTab (50 lines)
 * Focused layout with header, status, stats, and recent trips
 */
@Composable
fun CarrierHomeContent(
    viewModel: CarrierViewModel,
    authViewModel: AuthViewModel,
    roleViewModel: RoleViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    
    // State for showing booking requests screen
    var showBookingRequests by remember { mutableStateOf(false) }
    
    if (showBookingRequests) {
        CarrierRequestsScreen(onNavigateBack = { showBookingRequests = false })
    } else {
        LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            UserHeaderCard(
                welcomeMessage = "Welcome back, Carrier!",
                userName = currentUser?.name ?: "User",
                userAvatar = currentUser?.avatar,
                roleViewModel = roleViewModel
            )
        }
        
        item {
            CarrierStatusCard(viewModel = viewModel)
        }
        
        item {
            CarrierStatsGrid(
                carrierViewModel = viewModel,
                onBookingRequestsClick = { showBookingRequests = true }
            )
        }
        
        item {
            RecentTripsSection(
                trips = uiState.recentTrips,
                emptyStateConfig = EmptyStateData(
                    icon = Icons.Default.DirectionsCar,
                    title = "No trips yet",
                    description = "Create your first trip to start accepting package delivery requests."
                )
            )
        }
    }
    }
} 