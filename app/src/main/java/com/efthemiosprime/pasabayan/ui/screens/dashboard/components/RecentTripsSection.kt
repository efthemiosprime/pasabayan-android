package com.efthemiosprime.pasabayan.ui.screens.dashboard.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.data.model.Trip
import com.efthemiosprime.pasabayan.ui.components.TripCard
import com.efthemiosprime.pasabayan.ui.shared.EmptyStateView
import com.efthemiosprime.pasabayan.ui.common.EmptyStateData

/**
 * Recent Trips Section - Mirrors iOS RecentTripsSection (33 lines)
 * Pure component with immutable trip data and event handling
 */
@Composable
fun RecentTripsSection(
    trips: List<Trip>,
    emptyStateConfig: EmptyStateData,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Recent Trips",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (trips.isEmpty()) {
            EmptyStateView(
                icon = emptyStateConfig.icon,
                title = emptyStateConfig.title,
                description = emptyStateConfig.description
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                trips.forEach { trip ->
                    TripCard(
                        trip = trip,
                        onTap = { handleTripTap(trip) }
                    )
                }
            }
        }
    }
}

/**
 * Pure function for event handling
 */
private fun handleTripTap(trip: Trip) {
    println("Tapped trip: ${trip.route}")
} 