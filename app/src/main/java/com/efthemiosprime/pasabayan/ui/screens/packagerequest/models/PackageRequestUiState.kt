package com.efthemiosprime.pasabayan.ui.screens.packagerequest.models

import com.efthemiosprime.pasabayan.data.model.PackageSize
import com.efthemiosprime.pasabayan.data.model.PackageType
import com.efthemiosprime.pasabayan.data.model.UrgencyLevel

/**
 * Package Request UI State - Immutable state management
 * Following functional programming patterns with all properties as val
 * Updated to match iOS implementation behavior
 */
data class PackageRequestUiState(
    // Package Details
    val packageDescription: String = "",
    val weight: String = "",
    val packageValue: String = "",
    val maxBudget: String = "",
    val packageSize: PackageSize = PackageSize.MEDIUM,
    val packageType: PackageType = PackageType.ELECTRONICS,
    val urgencyLevel: UrgencyLevel = UrgencyLevel.NORMAL,
    val isFragile: Boolean = false,
    val specialInstructions: String = "",
    
    // Package Dimensions (Optional - defaults will be provided)
    val packageLength: String = "",
    val packageWidth: String = "",
    val packageHeight: String = "",
    
    // Pickup Information (country auto-detected from city)
    val pickupAddress: String = "",
    val pickupCity: String = "",
    val preferredPickupDate: String = "",
    val preferredPickupTime: String = "",
    val pickupDateFlexible: Boolean = true,
    
    // Delivery Information (country auto-detected from city)
    val deliveryAddress: String = "",
    val deliveryCity: String = "",
    val preferredDeliveryDate: String = "",
    val preferredDeliveryTime: String = "",
    
    // UI State
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) 