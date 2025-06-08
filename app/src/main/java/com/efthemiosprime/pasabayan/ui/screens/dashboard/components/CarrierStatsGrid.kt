package com.efthemiosprime.pasabayan.ui.screens.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import com.efthemiosprime.pasabayan.presentation.viewmodel.CarrierViewModel
import com.efthemiosprime.pasabayan.ui.components.cards.StatItem
import com.efthemiosprime.pasabayan.data.model.TripStatus

/**
 * Carrier Stats Grid - Following Global Card Standards
 * Pure stats display for carrier metrics using flat StatItem components
 * Uses StatItem to prevent nested shadows when used inside parent containers
 */
@Composable
fun CarrierStatsGrid(
    carrierViewModel: CarrierViewModel,
    modifier: Modifier = Modifier
) {
    val carrierState by carrierViewModel.state.collectAsState()
    val primaryColor = MaterialTheme.colorScheme.primary
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // First row of stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatItem( // Using StatItem (flat) for consistent card standards
                title = "Active Trips",
                value = (carrierState.trips.data?.count { it.tripStatus == TripStatus.ACTIVE } ?: 0).toString(),
                icon = Icons.Default.DirectionsCar,
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
            
            StatItem( // Using StatItem (flat) for consistent card standards
                title = "Total Earnings",
                value = carrierState.profile.totalEarnings,
                icon = Icons.Default.AttachMoney,
                color = primaryColor,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Second row of stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatItem( // Using StatItem (flat) for consistent card standards
                title = "Active Matches",
                value = (carrierState.bookings.data?.size ?: 0).toString(),
                icon = Icons.Default.Assignment,
                color = Color(0xFFFF9800),
                modifier = Modifier.weight(1f)
            )
            
            StatItem( // Using StatItem (flat) for consistent card standards
                title = "Rating",
                value = carrierState.averageRatingText,
                icon = Icons.Default.Star,
                color = Color(0xFFFFC107),
                modifier = Modifier.weight(1f)
            )
        }
    }
} 