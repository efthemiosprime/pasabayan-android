package com.efthemiosprime.pasabayan.features.payments.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.features.payments.services.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TippingUiState(
    val selectedTipAmount: Double = 0.0,
    val customTipAmount: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showSuccess: Boolean = false,
    val showPaymentSheet: Boolean = false,
    val clientSecret: String? = null,
) {
    val effectiveTipAmount: Double
        get() = customTipAmount.toDoubleOrNull() ?: selectedTipAmount

    val isValidTip: Boolean
        get() = effectiveTipAmount in MIN_TIP..MAX_TIP

    companion object {
        const val MIN_TIP = 1.0
        const val MAX_TIP = 500.0
        val PRESET_TIPS = listOf(2.0, 5.0, 10.0, 15.0)
    }
}

@HiltViewModel
class TippingViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TippingUiState())
    val uiState: StateFlow<TippingUiState> = _uiState.asStateFlow()

    fun selectPresetTip(amount: Double) {
        _uiState.update { it.copy(selectedTipAmount = amount, customTipAmount = "") }
    }

    fun clearPresetTip() {
        _uiState.update { it.copy(selectedTipAmount = 0.0) }
    }

    fun setCustomTipAmount(amount: String) {
        _uiState.update { it.copy(customTipAmount = amount, selectedTipAmount = 0.0) }
    }

    fun addTip(transactionId: Int) {
        val amount = _uiState.value.effectiveTipAmount
        if (!_uiState.value.isValidTip) {
            _uiState.update { it.copy(errorMessage = "Tip amount must be between 1 and 500") }
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    showSuccess = false,
                    showPaymentSheet = false,
                    clientSecret = null,
                )
            }
            paymentRepository.addTip(transactionId, amount).fold(
                onSuccess = { response ->
                    if (response.clientSecret != null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                showPaymentSheet = true,
                                clientSecret = response.clientSecret,
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false, showSuccess = true) }
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.message ?: "Failed to add tip")
                    }
                },
            )
        }
    }

    fun reset() {
        _uiState.update { TippingUiState() }
    }
}
