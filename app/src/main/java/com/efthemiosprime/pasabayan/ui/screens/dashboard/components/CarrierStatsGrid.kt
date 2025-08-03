package com.efthemiosprime.pasabayan.ui.screens.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import com.efthemiosprime.pasabayan.presentation.viewmodel.CarrierViewModel
import com.efthemiosprime.pasabayan.presentation.viewmodel.MatchViewModel
import com.efthemiosprime.pasabayan.ui.components.cards.StatItem
import com.efthemiosprime.pasabayan.data.model.TripStatus
import com.efthemiosprime.pasabayan.data.model.MatchStatus

/**
 * Carrier Stats Grid - Following Global Card Standards
 * Pure stats display for carrier metrics using flat StatItem components
 * Uses StatItem to prevent nested shadows when used inside parent containers
 */
@Composable
fun CarrierStatsGrid(
    carrierViewModel: CarrierViewModel,
    matchViewModel: MatchViewModel,
    modifier: Modifier = Modifier,
    onBookingRequestsClick: (() -> Unit)? = null
) {
    val carrierState by carrierViewModel.state.collectAsState()
    val carrierMatches by matchViewModel.carrierMatches.collectAsState()
    val primaryColor = MaterialTheme.colorScheme.primary
    
    // Load carrier matches when component appears - mirroring iOS behavior
    LaunchedEffect(Unit) {
        matchViewModel.loadCarrierMatches()
    }
    
    // Calculate booking requests count - filtering for pending requests like iOS
    val bookingRequestsCount = carrierMatches.count { match ->
        match.status == MatchStatus.PENDING || match.status == MatchStatus.SHIPPER_REQUESTED
    }
    
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
                value = carrierState.profile.activeTrips.toString(),
                icon = Icons.Default.DirectionsCar,
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
            
            StatItem( // Using StatItem (flat) for consistent card standards
                title = "Booking Requests",
                value = bookingRequestsCount.toString(),
                icon = Icons.AutoMirrored.Filled.Assignment,
                color = Color(0xFF9C27B0), // Purple color matching the image
                modifier = Modifier.weight(1f),
                onClick = onBookingRequestsClick
            )
        }
        
        // Second row of stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatItem( // Using StatItem (flat) for consistent card standards
                title = "Total Earnings",
                value = carrierState.profile.totalEarnings,
                icon = Icons.Default.AttachMoney,
                color = primaryColor,
                modifier = Modifier.weight(1f)
            )
            
            StatItem( // Using StatItem (flat) for consistent card standards
                title = "Active Matches",
                value = carrierState.profile.totalMatches.toString(),
                icon = Icons.AutoMirrored.Filled.Assignment,
                color = Color(0xFFFF9800),
                modifier = Modifier.weight(1f)
            )
        }
        
        // Third row of stats  
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatItem( // Using StatItem (flat) for consistent card standards
                title = "Rating",
                value = carrierState.averageRatingText,
                icon = Icons.Default.Star,
                color = Color(0xFFFFC107),
                modifier = Modifier.weight(1f)
            )
            
            // Empty spacer to maintain layout balance
            Spacer(modifier = Modifier.weight(1f))
        }
    }
} 