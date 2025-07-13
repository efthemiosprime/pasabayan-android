package com.efthemiosprime.pasabayan.ui.screens.carrier.models

import com.efthemiosprime.pasabayan.data.model.TransportationMethod

/**
 * Trip Creation UI State - Immutable state management
 * Following functional programming patterns with all properties as val
 */
data class TripCreationUiState(
    // Route Information
    val originCity: String = "",
    val destinationCity: String = "",
    val selectedTransportationMethod: TransportationMethod? = null,
    
    // Schedule
    val departureDate: String = "",
    val departureTime: String = "",
    val arrivalDate: String = "",
    val arrivalTime: String = "",
    
    // Available Capacity (Optional)
    val availableWeight: String = "",
    val availableSpace: String = "",
    val pricePerKg: String = "",
    
    // Additional Information
    val specialNotes: String = "",
    
    // UI State
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showTransportationDropdown: Boolean = false
) 