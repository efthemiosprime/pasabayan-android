package com.efthemiosprime.pasabayan.features.payments.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.features.payments.model.PaymentMethodDisplay
import com.efthemiosprime.pasabayan.features.payments.services.PaymentMethodsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaymentMethodsUiState(
    val paymentMethods: List<PaymentMethodDisplay> = emptyList(),
    val defaultPaymentMethodId: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

@HiltViewModel
class PaymentMethodsViewModel @Inject constructor(
    private val paymentMethodsRepository: PaymentMethodsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentMethodsUiState())
    val uiState: StateFlow<PaymentMethodsUiState> = _uiState.asStateFlow()

    fun loadPaymentMethods() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            paymentMethodsRepository.loadPaymentMethods().fold(
                onSuccess = { methods ->
                    _uiState.update { it.copy(paymentMethods = methods, isLoading = false) }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.message ?: "Failed to load payment methods")
                    }
                },
            )
        }
    }

    fun loadDefaultPaymentMethod() {
        viewModelScope.launch {
            paymentMethodsRepository.loadDefaultPaymentMethod().fold(
                onSuccess = { id ->
                    _uiState.update { it.copy(defaultPaymentMethodId = id) }
                },
                onFailure = { /* silent */ },
            )
        }
    }

    fun removePaymentMethod(methodId: String) {
        viewModelScope.launch {
            paymentMethodsRepository.removePaymentMethod(methodId).fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            paymentMethods = state.paymentMethods.filter { it.id != methodId },
                            successMessage = "Payment method removed",
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(errorMessage = e.message ?: "Failed to remove") }
                },
            )
        }
    }

    fun setDefaultPaymentMethod(methodId: String) {
        viewModelScope.launch {
            paymentMethodsRepository.setDefaultPaymentMethod(methodId).fold(
                onSuccess = {
                    _uiState.update { it.copy(defaultPaymentMethodId = methodId) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(errorMessage = e.message ?: "Failed to set default") }
                },
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
