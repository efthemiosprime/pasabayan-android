package com.efthemiosprime.pasabayan.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.StateFlow
import com.efthemiosprime.pasabayan.data.model.UserRole

/**
 * DashboardViewModel - Functional composition of role-specific ViewModels
 * Following functional programming patterns with pure composition
 */
class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    
    // Composition of specialized ViewModels
    private val _roleViewModel = RoleViewModel()
    private val _shipperViewModel = ShipperViewModel(application)
    private val _carrierViewModel = CarrierViewModel()
    
    // Public access to role state
    val currentRole: StateFlow<UserRole> = _roleViewModel.currentRole
    
    // Public access to specialized ViewModels
    val shipperViewModel: ShipperViewModel = _shipperViewModel
    val carrierViewModel: CarrierViewModel = _carrierViewModel
    val roleViewModel: RoleViewModel = _roleViewModel
} 