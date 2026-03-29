package com.efthemiosprime.pasabayan.features.payments.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.core.domain.`enum`.RefundReason
import com.efthemiosprime.pasabayan.core.network.payments.RefundRequestDataJson
import com.efthemiosprime.pasabayan.features.payments.services.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RefundUiState(
    val isProcessing: Boolean = false,
    val errorMessage: String? = null,
    val refundSuccess: Boolean = false,
    val refundRequest: RefundRequestDataJson? = null,
    val selectedReason: RefundReason? = null,
    val customReason: String = "",
    val additionalDetails: String = "",
    val isPartialRefund: Boolean = false,
    val partialAmount: String = "",
) {
    val effectiveReason: String
        get() = when (selectedReason) {
            RefundReason.OTHER -> customReason
            null -> ""
            else -> selectedReason.displayText
        }

    val isValidRequest: Boolean
        get() = effectiveReason.length >= MINIMUM_REASON_LENGTH

    val reasonValidationMessage: String?
        get() = if (effectiveReason.length in 1 until MINIMUM_REASON_LENGTH) {
            "Reason must be at least $MINIMUM_REASON_LENGTH characters"
        } else null

    val refundAmount: Double?
        get() = if (isPartialRefund) partialAmount.toDoubleOrNull() else null

    companion object {
        const val MINIMUM_REASON_LENGTH = 10
        const val MAXIMUM_REASON_LENGTH = 500
    }
}

@HiltViewModel
class RefundViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RefundUiState())
    val uiState: StateFlow<RefundUiState> = _uiState.asStateFlow()

    fun selectReason(reason: RefundReason) {
        _uiState.update { it.copy(selectedReason = reason) }
    }

    fun setCustomReason(reason: String) {
        _uiState.update { it.copy(customReason = reason) }
    }

    fun setAdditionalDetails(details: String) {
        _uiState.update { it.copy(additionalDetails = details) }
    }

    fun setPartialRefund(isPartial: Boolean) {
        _uiState.update { it.copy(isPartialRefund = isPartial) }
    }

    fun setPartialAmount(amount: String) {
        _uiState.update { it.copy(partialAmount = amount) }
    }

    fun submitRefundRequest(transactionId: Int) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }
            paymentRepository.requestRefund(
                transactionId = transactionId,
                amount = state.refundAmount,
                reason = state.effectiveReason,
                description = state.additionalDetails.ifBlank { null },
            ).fold(
                onSuccess = { data ->
                    _uiState.update {
                        it.copy(isProcessing = false, refundSuccess = true, refundRequest = data)
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isProcessing = false, errorMessage = e.message ?: "Refund request failed")
                    }
                },
            )
        }
    }

    fun reset() {
        _uiState.update { RefundUiState() }
    }
}
