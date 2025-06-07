package com.efthemiosprime.pasabayan.ui.screens.dashboard.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.efthemiosprime.pasabayan.presentation.viewmodel.ShipperViewModel
import com.efthemiosprime.pasabayan.presentation.viewmodel.AuthViewModel
import com.efthemiosprime.pasabayan.presentation.viewmodel.RoleViewModel
import com.efthemiosprime.pasabayan.ui.screens.dashboard.components.UserHeaderCard
import com.efthemiosprime.pasabayan.ui.screens.dashboard.components.ShipperStatsGrid
import com.efthemiosprime.pasabayan.ui.screens.dashboard.components.RecentActivitySection
import com.efthemiosprime.pasabayan.ui.common.EmptyStateData

/**
 * Shipper Home Content - Mirrors iOS ShipperHomeTab (40 lines)
 * Focused layout with header, stats, and recent activity
 */
@Composable
fun ShipperHomeContent(
    viewModel: ShipperViewModel,
    authViewModel: AuthViewModel,
    roleViewModel: RoleViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            UserHeaderCard(
                welcomeMessage = "Welcome back!",
                userName = currentUser?.name ?: "User",
                userAvatar = currentUser?.avatar,
                roleViewModel = roleViewModel
            )
        }
        
        item {
            ShipperStatsGrid(packageViewModel = viewModel.packageViewModel)
        }
        
        item {
            RecentActivitySection(
                title = "Recent Package Requests",
                packageRequests = uiState.recentPackages,
                emptyStateConfig = EmptyStateData(
                    icon = Icons.Default.Inventory,
                    title = "No packages yet",
                    description = "Start by creating a package request or browse available trips."
                )
            )
        }
    }
} 