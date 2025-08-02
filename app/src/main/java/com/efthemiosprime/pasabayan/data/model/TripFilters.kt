package com.efthemiosprime.pasabayan.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * Trip Sort Options - Mirrors iOS TripSortOption
 */
@Serializable
enum class TripSortOption {
    @SerialName("departure_date")
    DEPARTURE_DATE,
    @SerialName("price_per_kg")
    PRICE_PER_KG,
    @SerialName("available_capacity")
    AVAILABLE_CAPACITY,
    @SerialName("distance")
    DISTANCE;

    val displayName: String
        get() = when (this) {
            DEPARTURE_DATE -> "Departure Date"
            PRICE_PER_KG -> "Price per Kg"
            AVAILABLE_CAPACITY -> "Available Capacity"
            DISTANCE -> "Distance"
        }
}

/**
 * Sort Order Options - Mirrors iOS SortOrder
 */
@Serializable
enum class SortOrder {
    @SerialName("asc")
    ASCENDING,
    @SerialName("desc")
    DESCENDING;

    val displayName: String
        get() = when (this) {
            ASCENDING -> "Ascending"
            DESCENDING -> "Descending"
        }
}

/**
 * Trip Filter Configuration - Exactly Mirrors iOS TripFilter
 * Used for filtering available trips in the browse trips screen
 */
@Serializable
data class TripFilters(
    val searchText: String = "",
    val origin: String = "",
    val destination: String = "",
    val departureDate: String? = null, // ISO date format
    val minWeightCapacity: Double? = null,
    val maxWeightCapacity: Double? = null,
    val minSpaceCapacity: Double? = null,
    val maxSpaceCapacity: Double? = null,
    val minPricePerKg: Double? = null,
    val maxPricePerKg: Double? = null,
    val transportationMethod: TransportationMethod? = null,
    val sortBy: TripSortOption = TripSortOption.DEPARTURE_DATE,
    val sortOrder: SortOrder = SortOrder.ASCENDING
) {
    /**
     * Check if any filters are applied
     */
    fun hasActiveFilters(): Boolean {
        return searchText.isNotEmpty() ||
               origin.isNotEmpty() ||
               destination.isNotEmpty() ||
               departureDate != null ||
               minWeightCapacity != null ||
               maxWeightCapacity != null ||
               minSpaceCapacity != null ||
               maxSpaceCapacity != null ||
               minPricePerKg != null ||
               maxPricePerKg != null ||
               transportationMethod != null ||
               sortBy != TripSortOption.DEPARTURE_DATE ||
               sortOrder != SortOrder.ASCENDING
    }
    
    /**
     * Get a user-friendly description of active filters
     */
    fun getActiveFiltersDescription(): String {
        val filters = mutableListOf<String>()
        
        if (searchText.isNotEmpty()) filters.add("Search: $searchText")
        if (origin.isNotEmpty()) filters.add("From: $origin")
        if (destination.isNotEmpty()) filters.add("To: $destination")
        departureDate?.let { filters.add("Date: $it") }
        
        if (minPricePerKg != null || maxPricePerKg != null) {
            val priceRange = when {
                minPricePerKg != null && maxPricePerKg != null -> "$${minPricePerKg} - $${maxPricePerKg}/kg"
                minPricePerKg != null -> "From $${minPricePerKg}/kg"
                maxPricePerKg != null -> "Up to $${maxPricePerKg}/kg"
                else -> ""
            }
            filters.add(priceRange)
        }
        
        transportationMethod?.let { filters.add("Vehicle: ${it.displayName}") }
        
        if (minWeightCapacity != null || maxWeightCapacity != null) {
            val weightRange = when {
                minWeightCapacity != null && maxWeightCapacity != null -> "${minWeightCapacity}-${maxWeightCapacity} kg"
                minWeightCapacity != null -> "Min ${minWeightCapacity} kg"
                maxWeightCapacity != null -> "Max ${maxWeightCapacity} kg"
                else -> ""
            }
            filters.add("Weight: $weightRange")
        }
        
        return when {
            filters.isEmpty() -> "No filters applied"
            filters.size == 1 -> filters.first()
            else -> "${filters.size} filters applied"
        }
    }
    
    companion object {
        /**
         * Create default filters instance
         */
        fun default() = TripFilters()
    }
}

/**
 * Filter Sheet State
 * Manages the UI state for the trip filter bottom sheet
 */
data class FilterSheetState(
    val isVisible: Boolean = false,
    val filters: TripFilters = TripFilters.default(),
    val tempFilters: TripFilters = TripFilters.default() // Temporary filters while editing
) {
    /**
     * Apply temporary filters to actual filters
     */
    fun applyTempFilters(): FilterSheetState {
        return copy(filters = tempFilters, isVisible = false)
    }
    
    /**
     * Reset temporary filters to current filters
     */
    fun resetTempFilters(): FilterSheetState {
        return copy(tempFilters = filters)
    }
    
    /**
     * Clear all filters
     */
    fun clearFilters(): FilterSheetState {
        return copy(
            filters = TripFilters.default(),
            tempFilters = TripFilters.default(),
            isVisible = false
        )
    }
} 