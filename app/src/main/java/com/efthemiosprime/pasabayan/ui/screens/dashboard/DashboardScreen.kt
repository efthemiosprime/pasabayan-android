package com.efthemiosprime.pasabayan.ui.screens.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.efthemiosprime.pasabayan.data.model.UserRole
import com.efthemiosprime.pasabayan.presentation.viewmodel.AuthViewModel
import com.efthemiosprime.pasabayan.presentation.viewmodel.DashboardViewModel
import com.efthemiosprime.pasabayan.presentation.viewmodel.RoleViewModel
import com.efthemiosprime.pasabayan.ui.screens.dashboard.content.ShipperDashboardContent
import com.efthemiosprime.pasabayan.ui.screens.dashboard.content.CarrierDashboardContent

/**
 * Main Dashboard Logic - Mirrors iOS DashboardView (17 lines)
 * Pure composition-only component following functional patterns
 */
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val role by viewModel.currentRole.collectAsState()
    
    when (role) {
        UserRole.SHIPPER -> ShipperDashboardContent(
            viewModel = viewModel.shipperViewModel,
            authViewModel = authViewModel,
            roleViewModel = viewModel.roleViewModel
        )
        UserRole.CARRIER -> CarrierDashboardContent(
            viewModel = viewModel.carrierViewModel,
            authViewModel = authViewModel,
            roleViewModel = viewModel.roleViewModel
        )
    }
} 