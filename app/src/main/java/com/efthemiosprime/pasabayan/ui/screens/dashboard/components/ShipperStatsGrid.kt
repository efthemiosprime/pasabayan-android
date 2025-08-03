package com.efthemiosprime.pasabayan.ui.screens.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.MaterialTheme
import com.efthemiosprime.pasabayan.presentation.viewmodel.PackageViewModel
import com.efthemiosprime.pasabayan.presentation.viewmodel.MatchViewModel
import com.efthemiosprime.pasabayan.data.model.MatchStatus
import com.efthemiosprime.pasabayan.ui.components.cards.StatItem
import com.efthemiosprime.pasabayan.ui.screens.shipper.ShipperRequestsScreen

/**
 * Shipper Stats Grid - Following Global Card Standards
 * Pure stats display for shipper metrics using flat StatItem components
 * Uses StatItem to prevent nested shadows when used inside parent containers
 * UPDATED: Now matches iOS implementation with carrier requests instead of total packages
 */
@Composable
fun ShipperStatsGrid(
    packageViewModel: PackageViewModel,
    matchViewModel: MatchViewModel,
    modifier: Modifier = Modifier
) {
    // State for showing carrier requests screen - matching iOS behavior
    var showingCarrierRequests by remember { mutableStateOf(false) }
    val packages by packageViewModel.myPackageRequests.collectAsState()
    val shipperMatches by matchViewModel.shipperMatches.collectAsState()
    val primaryColor = MaterialTheme.colorScheme.primary
    
    // Count carrier requests (requests to carry from carriers) - matching iOS logic
    val carrierRequestsCount = shipperMatches.count { match ->
        match.status == MatchStatus.CARRIER_REQUESTED
    }
    
    // Debug logging to understand why count might be 0
    println("🔍 ShipperStatsGrid: Total shipper matches: ${shipperMatches.size}")
    println("🔍 ShipperStatsGrid: Carrier requests count: $carrierRequestsCount")
    println("🔍 ShipperStatsGrid: Match statuses: ${shipperMatches.map { it.status }}")
    if (shipperMatches.isNotEmpty()) {
        println("🔍 ShipperStatsGrid: First match: ${shipperMatches.first()}")
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
                title = "Active Packages",
                value = packages.count { it.status.name in listOf("MATCHED", "BOOKED", "IN_TRANSIT") }.toString(),
                icon = Icons.Default.LocalShipping,
                color = primaryColor,
                modifier = Modifier.weight(1f)
            )
            
            StatItem( // Using StatItem (flat) for consistent card standards
                title = "Delivered", 
                value = packages.count { it.status.name == "DELIVERED" }.toString(),
                icon = Icons.Default.CheckCircle,
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
        }
        
        // Second row of stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatItem( // Using StatItem (flat) for consistent card standards
                title = "Pending",
                value = packages.count { it.status.name == "PENDING" }.toString(), 
                icon = Icons.Default.Schedule,
                color = Color(0xFFFF9800),
                modifier = Modifier.weight(1f)
            )
            
            StatItem( // Using StatItem (flat) for consistent card standards - iOS: "Requests" 
                title = "Requests",
                value = carrierRequestsCount.toString(),
                icon = Icons.AutoMirrored.Filled.Assignment, // Matches iOS "doc.text.fill"
                color = Color(0xFF9C27B0), // Purple color matching iOS
                modifier = Modifier.weight(1f),
                onClick = {
                    showingCarrierRequests = true
                }
            )
        }
    }
    
    // Show ShipperRequestsScreen as a full-screen dialog - matching iOS sheet behavior
    if (showingCarrierRequests) {
        Dialog(
            onDismissRequest = { showingCarrierRequests = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            ShipperRequestsScreen(
                matchViewModel = matchViewModel,
                onNavigateBack = { showingCarrierRequests = false }
            )
        }
    }
}

/**
 * Immutable stat card configuration
 */
@androidx.compose.runtime.Immutable
private data class StatItemConfig(
    val title: String,
    val valueProvider: (List<com.efthemiosprime.pasabayan.data.model.PackageRequest>) -> String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: androidx.compose.ui.graphics.Color
) 