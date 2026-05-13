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

/** Lifecycle of the "add new card" flow surfaced via [AddPaymentMethodSheet] (D2) + Stripe PaymentSheet. */
sealed interface AddCardFlowState {
    object Idle : AddCardFlowState
    object Preparing : AddCardFlowState
    object Ready : AddCardFlowState
    object Presenting : AddCardFlowState
    object Success : AddCardFlowState
    data class Failed(val message: String) : AddCardFlowState
}

data class PaymentMethodsUiState(
    val paymentMethods: List<PaymentMethodDisplay> = emptyList(),
    val defaultPaymentMethodId: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val addCardFlowState: AddCardFlowState = AddCardFlowState.Idle,
    val showAddCardSheet: Boolean = false,
    val setupIntentClientSecret: String? = null,
    val setupIntentCustomerId: String? = null,
    val setupIntentEphemeralKey: String? = null,
)

@HiltViewModel
class PaymentMethodsViewModel @Inject constructor(
    private val paymentMethodsRepository: PaymentMethodsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentMethodsUiState())
    val uiState: StateFlow<PaymentMethodsUiState> = _uiState.asStateFlow()

    fun loadPaymentMethods() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            paymentMethodsRepository.loadPaymentMethods().fold(
                onSuccess = { methods ->
                    val defaultId = paymentMethodsRepository.loadDefaultPaymentMethod().getOrNull()
                    val sorted = applyDefaultAndSort(methods, defaultId)
                    _uiState.update {
                        it.copy(
                            paymentMethods = sorted,
                            defaultPaymentMethodId = defaultId,
                            isLoading = false,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            paymentMethods = emptyList(),
                            defaultPaymentMethodId = null,
                            errorMessage = e.message ?: "Failed to load payment methods",
                        )
                    }
                },
            )
        }
    }

    fun loadDefaultPaymentMethod() {
        viewModelScope.launch {
            paymentMethodsRepository.loadDefaultPaymentMethod().fold(
                onSuccess = { id ->
                    _uiState.update {
                        it.copy(
                            defaultPaymentMethodId = id,
                            paymentMethods = applyDefaultAndSort(it.paymentMethods, id),
                        )
                    }
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
                            defaultPaymentMethodId = state.defaultPaymentMethodId.takeUnless { it == methodId },
                            successMessage = "Payment method removed",
                            errorMessage = null,
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
                    _uiState.update {
                        it.copy(
                            defaultPaymentMethodId = methodId,
                            paymentMethods = applyDefaultAndSort(it.paymentMethods, methodId),
                            successMessage = null,
                            errorMessage = null,
                        )
                    }
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

    // -- Add-card flow (B1) --
    // iOS mirror: PaymentMethodsViewModel.swift prepareAddPaymentMethod / showCardForm

    fun prepareAddPaymentMethod() {
        _uiState.update {
            it.copy(
                addCardFlowState = AddCardFlowState.Preparing,
                errorMessage = null,
            )
        }
        viewModelScope.launch {
            paymentMethodsRepository.createSetupIntent().fold(
                onSuccess = { data ->
                    _uiState.update {
                        it.copy(
                            addCardFlowState = AddCardFlowState.Ready,
                            setupIntentClientSecret = data.clientSecret.takeIf { s -> s.isNotEmpty() },
                            setupIntentCustomerId = data.customerId.takeIf { s -> s.isNotEmpty() },
                            setupIntentEphemeralKey = data.ephemeralKey.takeIf { s -> s.isNotEmpty() },
                        )
                    }
                },
                onFailure = { e ->
                    val msg = e.message ?: "Failed to set up payment method"
                    _uiState.update {
                        it.copy(
                            addCardFlowState = AddCardFlowState.Failed(msg),
                            errorMessage = msg,
                        )
                    }
                },
            )
        }
    }

    fun showAddCardSheet(show: Boolean) {
        _uiState.update { it.copy(showAddCardSheet = show) }
    }

    fun onAddCardSheetPresented() {
        _uiState.update { it.copy(addCardFlowState = AddCardFlowState.Presenting) }
    }

    fun onAddCardCompleted() {
        _uiState.update {
            it.copy(
                addCardFlowState = AddCardFlowState.Success,
                showAddCardSheet = false,
                setupIntentClientSecret = null,
                setupIntentCustomerId = null,
                setupIntentEphemeralKey = null,
                successMessage = "Payment method added",
                errorMessage = null,
            )
        }
        loadPaymentMethods()
    }

    fun onAddCardCanceled() {
        _uiState.update {
            it.copy(
                addCardFlowState = AddCardFlowState.Idle,
                showAddCardSheet = false,
                setupIntentClientSecret = null,
                setupIntentCustomerId = null,
                setupIntentEphemeralKey = null,
            )
        }
    }

    fun onAddCardFailed(message: String) {
        _uiState.update {
            it.copy(
                addCardFlowState = AddCardFlowState.Failed(message),
                errorMessage = message,
            )
        }
    }

    private fun applyDefaultAndSort(
        methods: List<PaymentMethodDisplay>,
        defaultId: String?,
    ): List<PaymentMethodDisplay> {
        val updated = methods.map { method ->
            method.copy(isDefault = defaultId != null && method.id == defaultId)
        }
        return updated.sortedByDescending { it.isDefault }
    }
}
