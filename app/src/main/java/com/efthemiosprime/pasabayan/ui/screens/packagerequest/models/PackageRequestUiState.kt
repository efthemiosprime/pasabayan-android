package com.efthemiosprime.pasabayan.ui.screens.packagerequest.models

import com.efthemiosprime.pasabayan.data.model.PackageSize

/**
 * Package Request UI State - Immutable state management
 * Following functional programming patterns with all properties as val
 */
data class PackageRequestUiState(
    // Package Details
    val packageDescription: String = "",
    val weight: String = "",
    val packageValue: String = "",
    val maxBudget: String = "",
    val packageSize: PackageSize = PackageSize.MEDIUM,
    val isFragile: Boolean = false,
    val specialInstructions: String = "",
    
    // Pickup Information
    val pickupAddress: String = "",
    val pickupCity: String = "",
    val preferredPickupDate: String = "",
    val preferredPickupTime: String = "",
    val pickupDateFlexible: Boolean = true,
    
    // Delivery Information
    val deliveryAddress: String = "",
    val deliveryCity: String = "",
    val preferredDeliveryDate: String = "",
    val preferredDeliveryTime: String = "",
    
    // UI State
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) 