package com.efthemiosprime.pasabayan.ui.screens.packagerequest.utils

import com.efthemiosprime.pasabayan.ui.screens.packagerequest.models.PackageRequestUiState

/**
 * Package Request Validator - Pure functions for validation logic
 * Following functional programming patterns with no side effects
 * Updated to match iOS validation behavior
 */
object PackageRequestValidator {
    
    /**
     * Pure function to validate complete form state
     * Package dimensions are now optional (iOS provides defaults)
     */
    fun isValid(state: PackageRequestUiState): Boolean {
        return state.packageDescription.isNotBlank() &&
                state.pickupAddress.isNotBlank() &&
                state.pickupCity.isNotBlank() &&
                state.deliveryAddress.isNotBlank() &&
                state.deliveryCity.isNotBlank() &&
                state.weight.isNotBlank() &&
                validateWeight(state.weight) != null &&
                state.preferredPickupDate.isNotBlank()
        // Note: Package dimensions are now optional - defaults will be provided
    }
    
    /**
     * Pure function to validate weight input
     */
    fun validateWeight(weight: String): Double? {
        return weight.toDoubleOrNull()?.takeIf { it > 0 }
    }
    
    /**
     * Pure function to validate dimension input (optional)
     */
    fun validateDimension(dimension: String): Int? {
        return if (dimension.isBlank()) null else dimension.toIntOrNull()?.takeIf { it > 0 }
    }
    
    /**
     * Pure function to validate monetary amount
     */
    fun validateAmount(amount: String): Double? {
        return if (amount.isBlank()) null else amount.toDoubleOrNull()?.takeIf { it >= 0 }
    }
    
    /**
     * Pure function to validate required text field
     */
    fun validateRequiredText(text: String): Boolean {
        return text.isNotBlank()
    }
    
    /**
     * Pure function to get validation error message
     * Updated to match iOS validation behavior
     */
    fun getValidationError(state: PackageRequestUiState): String? {
        return when {
            state.packageDescription.isBlank() -> "Package description is required"
            state.pickupAddress.isBlank() -> "Pickup address is required"
            state.pickupCity.isBlank() -> "Pickup city is required" 
            state.deliveryAddress.isBlank() -> "Delivery address is required"
            state.deliveryCity.isBlank() -> "Delivery city is required"
            state.weight.isBlank() -> "Weight is required"
            validateWeight(state.weight) == null -> "Please enter a valid weight (numbers only)"
            state.preferredPickupDate.isBlank() -> "Pickup date is required"
            // Note: Package dimensions validation removed - they're now optional with smart defaults
            else -> null
        }
    }
} 