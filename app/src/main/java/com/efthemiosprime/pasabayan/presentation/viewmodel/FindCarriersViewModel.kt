package com.efthemiosprime.pasabayan.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.efthemiosprime.pasabayan.data.model.Trip
import com.efthemiosprime.pasabayan.data.model.CompatibleTrip
import com.efthemiosprime.pasabayan.data.repository.PackageRepositoryImpl
// import com.efthemiosprime.pasabayan.data.repository.DeliveryMatchRepositoryImpl
import com.efthemiosprime.pasabayan.data.common.Result
import com.efthemiosprime.pasabayan.data.common.AppError

/**
 * Find Carriers ViewModel - Manages compatible trips and carrier selection
 * Follows MVVM pattern with reactive state management using StateFlow
 */
class FindCarriersViewModel(application: Application) : AndroidViewModel(application) {
    
    // Repository dependencies (manual DI following existing pattern)
    private val packageRepository: PackageRepositoryImpl by lazy {
        PackageRepositoryImpl.create(getApplication())
    }
    
    // No need for match repository - using package repository for trip requests
    
    // UI State
    private val _uiState = MutableStateFlow(FindCarriersUiState())
    val uiState: StateFlow<FindCarriersUiState> = _uiState.asStateFlow()
    
    /**
     * Load compatible trips for a package
     */
    fun loadCompatibleTrips(packageId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Get compatible trips from repository
                val result = packageRepository.getCompatibleTrips(packageId)
                
                result.fold(
                    onSuccess = { trips ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            compatibleTrips = trips,
                            error = null
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                )
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load carriers: ${exception.message}"
                )
            }
        }
    }
    
    /**
     * Request a trip for package - iOS parity implementation
     * Uses PackageViewModel.requestTripForPackage to match iOS exactly
     * Matches iOS packageViewModel.requestTripForPackage functionality
     */
    fun requestToCarry(packageId: Int, tripId: Int, offeredPrice: Double, message: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Create PackageViewModel to match iOS call structure exactly
                val packageViewModel = com.efthemiosprime.pasabayan.presentation.viewmodel.PackageViewModel(getApplication())
                
                // Call requestTripForPackage exactly like iOS does
                packageViewModel.requestTripForPackage(
                    packageId = packageId,
                    tripId = tripId,
                    offeredPrice = offeredPrice,
                    message = message
                ) { result ->
                    // Handle result exactly like iOS
                    result.fold(
                        onSuccess = { match ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                requestSent = true,
                                error = null
                            )
                            println("✅ Trip request sent successfully: Package $packageId -> Trip $tripId, Match ID: ${match.id}")
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = error.message
                            )
                            println("❌ Failed to send trip request: ${error.message}")
                        }
                    )
                }
                
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to send request: ${exception.message}"
                )
            }
        }
    }
    
    /**
     * Clear any error messages
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * UI State for Find Carriers screen
 */
data class FindCarriersUiState(
    val isLoading: Boolean = false,
    val compatibleTrips: List<CompatibleTrip> = emptyList(),
    val requestSent: Boolean = false,
    val error: String? = null
)