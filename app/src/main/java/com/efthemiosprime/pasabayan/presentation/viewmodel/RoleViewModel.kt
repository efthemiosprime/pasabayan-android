package com.efthemiosprime.pasabayan.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.efthemiosprime.pasabayan.data.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * RoleViewModel manages user role switching between Shipper and Carrier
 * Mirrors iOS RoleViewModel structure
 */
class RoleViewModel : ViewModel() {
    
    private val _currentRole = MutableStateFlow(UserRole.SHIPPER)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()
    
    /**
     * Switch between Shipper and Carrier roles
     */
    fun switchRole(role: UserRole) {
        _currentRole.value = role
    }
    
    /**
     * Toggle between roles
     */
    fun toggleRole() {
        _currentRole.value = when (_currentRole.value) {
            UserRole.SHIPPER -> UserRole.CARRIER
            UserRole.CARRIER -> UserRole.SHIPPER
        }
    }
} 