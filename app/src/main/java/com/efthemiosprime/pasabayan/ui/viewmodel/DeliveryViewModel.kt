package com.efthemiosprime.pasabayan.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.data.model.CreateDeliveryRequest
import com.efthemiosprime.pasabayan.data.model.DeliveryRequest
import com.efthemiosprime.pasabayan.data.model.DeliveryStatus
import com.efthemiosprime.pasabayan.data.service.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DeliveryUiState(
    val isLoading: Boolean = false,
    val deliveryRequests: List<DeliveryRequest> = emptyList(),
    val errorMessage: String? = null,
    val isCreatingRequest: Boolean = false,
    val createRequestSuccess: Boolean = false
)

class DeliveryViewModel : ViewModel() {
    private val apiService = ApiService.getInstance()
    
    private val _uiState = MutableStateFlow(DeliveryUiState())
    val uiState: StateFlow<DeliveryUiState> = _uiState.asStateFlow()

    init {
        loadDeliveryRequests()
    }

    fun loadDeliveryRequests() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val result = apiService.getDeliveryRequests()
                
                if (result.isSuccess) {
                    val requests = result.getOrThrow()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        deliveryRequests = requests,
                        errorMessage = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to load delivery requests"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "An error occurred"
                )
            }
        }
    }

    fun createDeliveryRequest(request: CreateDeliveryRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingRequest = true, errorMessage = null, createRequestSuccess = false)
            
            try {
                val result = apiService.createDeliveryRequest(request)
                
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isCreatingRequest = false,
                        createRequestSuccess = true,
                        errorMessage = null
                    )
                    // Reload the list to include the new request
                    loadDeliveryRequests()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isCreatingRequest = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to create delivery request"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCreatingRequest = false,
                    errorMessage = e.message ?: "An error occurred"
                )
            }
        }
    }

    fun updateDeliveryStatus(id: Int, status: DeliveryStatus) {
        viewModelScope.launch {
            try {
                val result = apiService.updateDeliveryRequestStatus(id, status)
                
                if (result.isSuccess) {
                    // Update the local list
                    val updatedRequest = result.getOrThrow()
                    val currentRequests = _uiState.value.deliveryRequests.toMutableList()
                    val index = currentRequests.indexOfFirst { it.id == id }
                    if (index != -1) {
                        currentRequests[index] = updatedRequest
                        _uiState.value = _uiState.value.copy(deliveryRequests = currentRequests)
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to update delivery status"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "An error occurred"
                )
            }
        }
    }

    fun acceptDeliveryRequest(id: Int) {
        viewModelScope.launch {
            try {
                val result = apiService.acceptDeliveryRequest(id)
                
                if (result.isSuccess) {
                    // Update the local list
                    val updatedRequest = result.getOrThrow()
                    val currentRequests = _uiState.value.deliveryRequests.toMutableList()
                    val index = currentRequests.indexOfFirst { it.id == id }
                    if (index != -1) {
                        currentRequests[index] = updatedRequest
                        _uiState.value = _uiState.value.copy(deliveryRequests = currentRequests)
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to accept delivery request"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "An error occurred"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearCreateSuccess() {
        _uiState.value = _uiState.value.copy(createRequestSuccess = false)
    }

    // Helper functions for filtering
    fun getPendingRequests(): List<DeliveryRequest> {
        return _uiState.value.deliveryRequests.filter { it.status == DeliveryStatus.PENDING }
    }

    fun getMyRequests(userId: Int): List<DeliveryRequest> {
        return _uiState.value.deliveryRequests.filter { it.userId == userId }
    }

    fun getMyDeliveries(driverId: Int): List<DeliveryRequest> {
        return _uiState.value.deliveryRequests.filter { it.driverId == driverId }
    }
} 