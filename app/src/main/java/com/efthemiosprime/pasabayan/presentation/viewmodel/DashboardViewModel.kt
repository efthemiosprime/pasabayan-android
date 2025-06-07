package com.efthemiosprime.pasabayan.presentation.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import com.efthemiosprime.pasabayan.data.model.UserRole

/**
 * DashboardViewModel - Functional composition of role-specific ViewModels
 * Following functional programming patterns with pure composition
 */
class DashboardViewModel : ViewModel() {
    
    // Composition of specialized ViewModels
    private val _roleViewModel = RoleViewModel()
    private val _shipperViewModel = ShipperViewModel()
    private val _carrierViewModel = CarrierViewModel()
    
    // Public access to role state
    val currentRole: StateFlow<UserRole> = _roleViewModel.currentRole
    
    // Public access to specialized ViewModels
    val shipperViewModel: ShipperViewModel = _shipperViewModel
    val carrierViewModel: CarrierViewModel = _carrierViewModel
    val roleViewModel: RoleViewModel = _roleViewModel
} 