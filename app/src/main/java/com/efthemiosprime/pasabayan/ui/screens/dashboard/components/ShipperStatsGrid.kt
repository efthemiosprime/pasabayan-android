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
import com.efthemiosprime.pasabayan.presentation.viewmodel.PackageViewModel
import com.efthemiosprime.pasabayan.ui.components.cards.StatItem

/**
 * Shipper Stats Grid - Following Global Card Standards
 * Pure stats display for shipper metrics using flat StatItem components
 * Uses StatItem to prevent nested shadows when used inside parent containers
 */
@Composable
fun ShipperStatsGrid(
    packageViewModel: PackageViewModel,
    modifier: Modifier = Modifier
) {
    val packages by packageViewModel.myPackageRequests.collectAsState()
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
            
            StatItem( // Using StatItem (flat) for consistent card standards
                title = "Total Requests",
                value = packages.size.toString(),
                icon = Icons.Default.Inventory,
                color = Color(0xFF9C27B0),
                modifier = Modifier.weight(1f)
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