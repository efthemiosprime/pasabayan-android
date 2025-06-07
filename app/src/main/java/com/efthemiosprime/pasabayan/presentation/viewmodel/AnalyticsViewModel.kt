package com.efthemiosprime.pasabayan.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.data.model.analytics.*
import com.efthemiosprime.pasabayan.data.repository.AnalyticsRepository
import com.efthemiosprime.pasabayan.data.repository.AnalyticsRepositoryImpl
import com.efthemiosprime.pasabayan.presentation.common.FunctionalViewModel
import com.efthemiosprime.pasabayan.presentation.common.UiState
import com.efthemiosprime.pasabayan.data.common.AppError
import kotlinx.coroutines.launch

/**
 * Analytics state following functional patterns
 */
data class AnalyticsState(
    val carrierStats: UiState<CarrierHistoricalStats> = UiState.idle(),
    val shipperStats: UiState<ShipperDetailedStats> = UiState.idle(),
    val isRefreshing: Boolean = false
) {
    val isLoading: Boolean get() = carrierStats.isLoading || shipperStats.isLoading
    val error: AppError? get() = carrierStats.error ?: shipperStats.error
}

/**
 * Analytics actions following functional patterns
 */
sealed class AnalyticsAction {
    object LoadAnalytics : AnalyticsAction()
    object LoadCarrierStats : AnalyticsAction()
    object LoadShipperStats : AnalyticsAction()
    object RefreshData : AnalyticsAction()
    data class CarrierStatsLoaded(val stats: CarrierHistoricalStats) : AnalyticsAction()
    data class ShipperStatsLoaded(val stats: ShipperDetailedStats) : AnalyticsAction()
    data class LoadingError(val error: AppError) : AnalyticsAction()
    object ClearError : AnalyticsAction()
}

/**
 * Analytics side effects
 */
sealed class AnalyticsEffect {
    data class ShowToast(val message: String) : AnalyticsEffect()
    object RefreshComplete : AnalyticsEffect()
}

/**
 * AnalyticsViewModel manages analytics data using functional patterns
 */
class AnalyticsViewModel(
    private val analyticsRepository: AnalyticsRepository = AnalyticsRepositoryImpl()
) : FunctionalViewModel<AnalyticsState, AnalyticsAction, AnalyticsEffect>(
    initialState = AnalyticsState()
) {
    
    init {
        dispatch(AnalyticsAction.LoadAnalytics)
    }
    
    // MARK: - Pure Reducer Function
    
    override fun reduce(currentState: AnalyticsState, action: AnalyticsAction): AnalyticsState = when (action) {
        is AnalyticsAction.LoadAnalytics -> currentState.copy(
            carrierStats = UiState.loading(currentState.carrierStats.data),
            shipperStats = UiState.loading(currentState.shipperStats.data)
        )
        
        is AnalyticsAction.LoadCarrierStats -> currentState.copy(
            carrierStats = UiState.loading(currentState.carrierStats.data)
        )
        
        is AnalyticsAction.LoadShipperStats -> currentState.copy(
            shipperStats = UiState.loading(currentState.shipperStats.data)
        )
        
        is AnalyticsAction.RefreshData -> currentState.copy(
            isRefreshing = true,
            carrierStats = UiState.loading(currentState.carrierStats.data),
            shipperStats = UiState.loading(currentState.shipperStats.data)
        )
        
        is AnalyticsAction.CarrierStatsLoaded -> currentState.copy(
            carrierStats = UiState.success(action.stats),
            isRefreshing = false
        )
        
        is AnalyticsAction.ShipperStatsLoaded -> currentState.copy(
            shipperStats = UiState.success(action.stats),
            isRefreshing = false
        )
        
        is AnalyticsAction.LoadingError -> currentState.copy(
            carrierStats = if (currentState.carrierStats.isLoading) 
                UiState.error(action.error, currentState.carrierStats.data) 
            else currentState.carrierStats,
            shipperStats = if (currentState.shipperStats.isLoading) 
                UiState.error(action.error, currentState.shipperStats.data) 
            else currentState.shipperStats,
            isRefreshing = false
        )
        
        is AnalyticsAction.ClearError -> currentState.copy(
            carrierStats = currentState.carrierStats.copy(error = null),
            shipperStats = currentState.shipperStats.copy(error = null)
        )
    }
    
    // MARK: - Side Effects Handler
    
    override suspend fun handleSideEffect(action: AnalyticsAction, currentState: AnalyticsState): AnalyticsEffect? = when (action) {
        is AnalyticsAction.LoadAnalytics -> {
            loadAnalyticsData()
            null
        }
        
        is AnalyticsAction.LoadCarrierStats -> {
            loadCarrierStatsInternal()
            null
        }
        
        is AnalyticsAction.LoadShipperStats -> {
            loadShipperStatsInternal()
            null
        }
        
        is AnalyticsAction.RefreshData -> {
            loadAnalyticsData()
            AnalyticsEffect.RefreshComplete
        }
        
        else -> null
    }
    
    // MARK: - Public API
    
    fun refreshData() {
        dispatch(AnalyticsAction.RefreshData)
    }
    
    fun clearError() {
        dispatch(AnalyticsAction.ClearError)
    }
    
    // MARK: - Private Methods
    
    private fun loadAnalyticsData() {
        viewModelScope.launch {
            try {
                // Load both carrier and shipper stats concurrently
                val carrierStats = analyticsRepository.getCarrierAnalytics()
                val shipperStats = analyticsRepository.getShipperAnalytics()
                
                dispatch(AnalyticsAction.CarrierStatsLoaded(carrierStats))
                dispatch(AnalyticsAction.ShipperStatsLoaded(shipperStats))
            } catch (e: Exception) {
                val error = AppError.NetworkError("Failed to load analytics data: ${e.message}")
                dispatch(AnalyticsAction.LoadingError(error))
            }
        }
    }
    
    private fun loadCarrierStatsInternal() {
        viewModelScope.launch {
            try {
                val stats = analyticsRepository.getCarrierAnalytics()
                dispatch(AnalyticsAction.CarrierStatsLoaded(stats))
            } catch (e: Exception) {
                val error = AppError.NetworkError("Failed to load carrier analytics: ${e.message}")
                dispatch(AnalyticsAction.LoadingError(error))
            }
        }
    }
    
    private fun loadShipperStatsInternal() {
        viewModelScope.launch {
            try {
                val stats = analyticsRepository.getShipperAnalytics()
                dispatch(AnalyticsAction.ShipperStatsLoaded(stats))
            } catch (e: Exception) {
                val error = AppError.NetworkError("Failed to load shipper analytics: ${e.message}")
                dispatch(AnalyticsAction.LoadingError(error))
            }
        }
    }
} 