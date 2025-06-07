package com.efthemiosprime.pasabayan.data.model.analytics

/**
 * UI state for analytics features
 */
sealed class AnalyticsUiState {
    object Loading : AnalyticsUiState()
    
    data class Success(
        val carrierStats: CarrierHistoricalStats? = null,
        val shipperStats: ShipperDetailedStats? = null
    ) : AnalyticsUiState()
    
    data class Error(val message: String) : AnalyticsUiState()
} 