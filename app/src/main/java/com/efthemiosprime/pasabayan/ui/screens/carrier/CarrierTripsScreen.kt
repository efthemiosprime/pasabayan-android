package com.efthemiosprime.pasabayan.ui.screens.carrier

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.efthemiosprime.pasabayan.data.model.TripStatus
import com.efthemiosprime.pasabayan.presentation.viewmodel.CarrierViewModel
import com.efthemiosprime.pasabayan.ui.shared.EmptyStateView
import com.efthemiosprime.pasabayan.ui.components.TripCard
import com.efthemiosprime.pasabayan.R

/**
 * Carrier Trips Screen - Exactly matching iOS CarrierTripsView implementation
 * Mirrors iOS CarrierTripsView.swift structure with filtering and trip management
 * Handles trip filtering, loading states, and empty states for carriers
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarrierTripsScreen(
    modifier: Modifier = Modifier,
    carrierViewModel: CarrierViewModel = viewModel()
) {
    val carrierState by carrierViewModel.state.collectAsState()
    val trips = carrierState.trips.data ?: emptyList()
    val isLoading = carrierState.isLoading
    var selectedFilter by remember { mutableStateOf<TripStatus?>(null) }
    val scope = rememberCoroutineScope()
    
    // Filter trips based on selected status
    val filteredTrips = remember(trips, selectedFilter) {
        if (selectedFilter == null) {
            trips
        } else {
            trips.filter { it.tripStatus == selectedFilter }
        }
    }
    
    LaunchedEffect(Unit) {
        scope.launch {
            carrierViewModel.loadTrips()
        }
    }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Filter Buttons (horizontal scrollable)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            // "All" filter button
            item {
                FilterChip(
                    onClick = { selectedFilter = null },
                    label = { Text(stringResource(R.string.trip_status_all)) },
                    selected = selectedFilter == null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color.Blue,
                        selectedLabelColor = Color.White
                    )
                )
            }
            
            // Status filter buttons
            items(TripStatus.allCases) { status ->
                val count = trips.count { it.tripStatus == status }
                FilterChip(
                    onClick = { selectedFilter = status },
                    label = { 
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(status.displayName)
                            if (count > 0) {
                                Text(
                                    text = "($count)",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    },
                    selected = selectedFilter == status,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color.Blue,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
        
        HorizontalDivider()
        
        // Trips List Content
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = stringResource(R.string.loading_trips),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (filteredTrips.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateView(
                    icon = Icons.Default.DirectionsCar,
                    title = if (selectedFilter == null) stringResource(R.string.no_trips_yet) else stringResource(R.string.no_trips_status, selectedFilter?.displayName?.lowercase() ?: ""),
                    description = if (selectedFilter == null) {
                        stringResource(R.string.create_first_trip)
                    } else {
                        stringResource(R.string.no_trips_found, selectedFilter?.displayName?.lowercase() ?: "")
                    }
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredTrips) { trip ->
                    TripCard(
                        trip = trip,
                        onTap = {
                            // Handle trip tap - could navigate to trip details
                            println("Tapped trip: ${trip.route}")
                        }
                    )
                }
            }
        }
    }
} 