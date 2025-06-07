package com.efthemiosprime.pasabayan.ui.screens.packagerequest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.efthemiosprime.pasabayan.data.model.PackageSize
import com.efthemiosprime.pasabayan.data.model.CreatePackageRequest
import com.efthemiosprime.pasabayan.ui.screens.packagerequest.models.PackageRequestUiState
import com.efthemiosprime.pasabayan.ui.screens.packagerequest.models.PackageRequestEvent
import com.efthemiosprime.pasabayan.ui.screens.packagerequest.utils.PackageRequestValidator

/**
 * PackageRequestViewModel - Functional state management for package requests
 * Following functional programming patterns with immutable state and pure functions
 */
class PackageRequestViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(PackageRequestUiState())
    val uiState: StateFlow<PackageRequestUiState> = _uiState.asStateFlow()
    
    private val _uiEvent = Channel<PackageRequestEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()
    
    // MARK: - Computed Properties (Pure Functions)
    val isFormValid: Boolean
        get() = PackageRequestValidator.isValid(uiState.value)
    
    val validationError: String?
        get() = PackageRequestValidator.getValidationError(uiState.value)
    
    // MARK: - Form Field Updates (Pure Functions)
    
    fun updatePackageDescription(value: String) {
        _uiState.update { it.copy(packageDescription = value) }
    }
    
    fun updateWeight(value: String) {
        _uiState.update { it.copy(weight = value) }
    }
    
    fun updatePackageValue(value: String) {
        _uiState.update { it.copy(packageValue = value) }
    }
    
    fun updateMaxBudget(value: String) {
        _uiState.update { it.copy(maxBudget = value) }
    }
    
    fun updatePackageSize(size: PackageSize) {
        _uiState.update { it.copy(packageSize = size) }
    }
    
    fun updateFragile(isFragile: Boolean) {
        _uiState.update { it.copy(isFragile = isFragile) }
    }
    
    fun updateSpecialInstructions(value: String) {
        _uiState.update { it.copy(specialInstructions = value) }
    }
    
    fun updatePickupAddress(value: String) {
        _uiState.update { it.copy(pickupAddress = value) }
    }
    
    fun updatePickupCity(value: String) {
        _uiState.update { it.copy(pickupCity = value) }
    }
    
    fun updatePreferredPickupDate(date: String) {
        _uiState.update { it.copy(preferredPickupDate = date) }
    }
    
    fun updatePreferredPickupTime(time: String) {
        _uiState.update { it.copy(preferredPickupTime = time) }
    }
    
    fun updatePickupDateFlexible(isFlexible: Boolean) {
        _uiState.update { it.copy(pickupDateFlexible = isFlexible) }
    }
    
    fun updateDeliveryAddress(value: String) {
        _uiState.update { it.copy(deliveryAddress = value) }
    }
    
    fun updateDeliveryCity(value: String) {
        _uiState.update { it.copy(deliveryCity = value) }
    }
    
    fun updatePreferredDeliveryDate(date: String) {
        _uiState.update { it.copy(preferredDeliveryDate = date) }
    }
    
    fun updatePreferredDeliveryTime(time: String) {
        _uiState.update { it.copy(preferredDeliveryTime = time) }
    }
    
    // MARK: - Business Logic
    
    fun createPackageRequest() {
        val currentState = uiState.value
        
        // Validate form
        val error = validationError
        if (error != null) {
            sendEvent(PackageRequestEvent.ShowError(error))
            return
        }
        
        // Validate weight specifically
        val weightValue = PackageRequestValidator.validateWeight(currentState.weight)
        if (weightValue == null) {
            sendEvent(PackageRequestEvent.ShowError("Please enter a valid weight"))
            return
        }
        
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        
        viewModelScope.launch {
            try {
                // Simulate API call
                delay(2000)
                
                val request = buildCreatePackageRequest(currentState, weightValue)
                
                // TODO: Replace with actual repository call
                // packageRepository.createPackageRequest(request)
                
                clearForm()
                sendEvent(PackageRequestEvent.ShowSuccess("Package request created successfully!"))
                sendEvent(PackageRequestEvent.NavigateBack)
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = e.message ?: "Unknown error occurred"
                    ) 
                }
                sendEvent(PackageRequestEvent.ShowError(e.message ?: "Unknown error occurred"))
            }
        }
    }
    
    fun clearForm() {
        _uiState.value = PackageRequestUiState()
        sendEvent(PackageRequestEvent.ClearForm)
    }
    
    // MARK: - Private Helper Functions
    
    private fun sendEvent(event: PackageRequestEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }
    
    private fun buildCreatePackageRequest(
        state: PackageRequestUiState,
        weightValue: Double
    ): CreatePackageRequest {
        return CreatePackageRequest(
            title = state.packageDescription,
            description = state.specialInstructions.takeIf { it.isNotBlank() },
            pickupLocation = "${state.pickupAddress}, ${state.pickupCity}",
            deliveryLocation = "${state.deliveryAddress}, ${state.deliveryCity}",
            pickupCoordinates = null, // TODO: Add coordinate lookup
            deliveryCoordinates = null, // TODO: Add coordinate lookup
            preferredPickupDate = state.preferredPickupDate,
            preferredPickupTime = state.preferredPickupTime.takeIf { it.isNotBlank() },
            preferredDeliveryDate = state.preferredDeliveryDate.takeIf { it.isNotBlank() },
            packageSize = state.packageSize,
            packageWeight = weightValue,
            packageValue = PackageRequestValidator.validateAmount(state.packageValue),
            isFragile = state.isFragile,
            specialInstructions = state.specialInstructions.takeIf { it.isNotBlank() },
            maxBudget = PackageRequestValidator.validateAmount(state.maxBudget)
        )
    }
} 