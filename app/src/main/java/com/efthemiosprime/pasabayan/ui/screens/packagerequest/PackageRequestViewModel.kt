package com.efthemiosprime.pasabayan.ui.screens.packagerequest

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.efthemiosprime.pasabayan.data.model.PackageSize
import com.efthemiosprime.pasabayan.data.model.CreatePackageRequest
import com.efthemiosprime.pasabayan.data.repository.PackageRepositoryImpl
import com.efthemiosprime.pasabayan.ui.screens.packagerequest.models.PackageRequestUiState
import com.efthemiosprime.pasabayan.ui.screens.packagerequest.models.PackageRequestEvent
import com.efthemiosprime.pasabayan.ui.screens.packagerequest.utils.PackageRequestValidator
import com.efthemiosprime.pasabayan.data.model.PackageDimensions
import com.efthemiosprime.pasabayan.data.model.PackageType
import com.efthemiosprime.pasabayan.data.model.UrgencyLevel

/**
 * PackageRequestViewModel - Functional state management for package requests
 * Following functional programming patterns with immutable state and pure functions
 * Now uses actual PackageRepository for real API calls
 */
class PackageRequestViewModel(application: Application) : AndroidViewModel(application) {
    
    // Repository for API calls
    private val packageRepository = PackageRepositoryImpl.create(getApplication())
    
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
    
    fun updatePackageType(type: PackageType) {
        _uiState.update { it.copy(packageType = type) }
    }
    
    fun updateUrgencyLevel(level: UrgencyLevel) {
        _uiState.update { it.copy(urgencyLevel = level) }
    }
    
    fun updatePackageLength(value: String) {
        _uiState.update { it.copy(packageLength = value) }
    }
    
    fun updatePackageWidth(value: String) {
        _uiState.update { it.copy(packageWidth = value) }
    }
    
    fun updatePackageHeight(value: String) {
        _uiState.update { it.copy(packageHeight = value) }
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
                val request = buildCreatePackageRequest(currentState, weightValue)
                
                println("🚀 Creating package with real API: ${request.title}")
                println("📦 Package details: ${request.pickupAddress}, ${request.pickupCity} → ${request.deliveryAddress}, ${request.deliveryCity}")
                
                // Use actual repository to create package
                packageRepository.createPackageRequest(request)
                    .onSuccess { createdPackage ->
                        println("🎉 Package created successfully!")
                        println("   📦 Package ID: ${createdPackage.id}")
                        println("   📦 Package Title: ${createdPackage.title}")
                        println("   📦 Package Status: ${createdPackage.status}")
                        
                        _uiState.update { it.copy(isLoading = false) }
                        clearForm()
                        sendEvent(PackageRequestEvent.ShowSuccess("Package request created successfully!"))
                        sendEvent(PackageRequestEvent.NavigateBack)
                    }
                    .onFailure { exception ->
                        println("❌ Package creation failed: ${exception.message}")
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                errorMessage = exception.message ?: "Unknown error occurred"
                            ) 
                        }
                        sendEvent(PackageRequestEvent.ShowError(exception.message ?: "Failed to create package. Please try again."))
                    }
                
            } catch (e: Exception) {
                println("❌ Exception during package creation: ${e.message}")
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
        // Provide default dimensions like iOS (30x20x15 cm) if not specified
        val dimensions = if (state.packageLength.isNotBlank() && 
                            state.packageWidth.isNotBlank() && 
                            state.packageHeight.isNotBlank()) {
            PackageDimensions(
                length = state.packageLength.toIntOrNull() ?: 30,
                width = state.packageWidth.toIntOrNull() ?: 20,
                height = state.packageHeight.toIntOrNull() ?: 15
            )
        } else {
            // Default dimensions matching iOS implementation
            PackageDimensions(
                length = 30,  // 30 cm default length
                width = 20,   // 20 cm default width  
                height = 15   // 15 cm default height
            )
        }
        
        return CreatePackageRequest(
            title = state.packageDescription,
            description = state.specialInstructions.takeIf { it.isNotBlank() },
            pickupAddress = state.pickupAddress,
            pickupCity = state.pickupCity,
            pickupCountry = detectCountry(state.pickupCity),
            deliveryAddress = state.deliveryAddress,
            deliveryCity = state.deliveryCity,
            deliveryCountry = detectCountry(state.deliveryCity),
            pickupCoordinates = null, // TODO: Add coordinate lookup
            deliveryCoordinates = null, // TODO: Add coordinate lookup
            preferredPickupDate = state.preferredPickupDate,
            preferredPickupTime = state.preferredPickupTime.takeIf { it.isNotBlank() },
            preferredDeliveryDate = state.preferredDeliveryDate.takeIf { it.isNotBlank() },
            packageSize = state.packageSize,
            packageWeight = weightValue,
            packageDimensions = dimensions,
            packageType = state.packageType.value,
            packageValue = PackageRequestValidator.validateAmount(state.packageValue),
            isFragile = state.isFragile,
            urgencyLevel = state.urgencyLevel.value,
            specialInstructions = state.specialInstructions.takeIf { it.isNotBlank() },
            maxBudget = PackageRequestValidator.validateAmount(state.maxBudget),
            pickupDateFlexible = state.pickupDateFlexible
        )
    }
    
    /**
     * Smart country detection based on city names - matching iOS implementation
     */
    private fun detectCountry(city: String): String {
        val cityLower = city.lowercase()
        
        // Canadian cities
        val canadianCities = setOf("montreal", "toronto", "vancouver", "ottawa", "calgary", 
                                  "edmonton", "winnipeg", "quebec city", "hamilton", "kitchener")
        if (canadianCities.any { cityLower.contains(it) }) {
            return "Canada"
        }
        
        // Philippine cities  
        val philippineCities = setOf("manila", "makati", "quezon city", "cebu", "davao", "pasig", 
                                   "taguig", "paranaque", "las pinas", "muntinlupa", "marikina", 
                                   "antipolo", "pasay", "caloocan", "mandaluyong", "san juan")
        if (philippineCities.any { cityLower.contains(it) } || 
            cityLower.contains("makati") || cityLower.contains("manila")) {
            return "Philippines"
        }
        
        // Default to Philippines for now (matching iOS behavior)
        return "Philippines"
    }
} 