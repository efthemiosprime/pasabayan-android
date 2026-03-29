package com.efthemiosprime.pasabayan.features.payments.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.features.payments.model.Transaction
import com.efthemiosprime.pasabayan.features.payments.services.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaymentUiState(
    val isProcessing: Boolean = false,
    val errorMessage: String? = null,
    val paymentSuccess: Boolean = false,
    val transaction: Transaction? = null,
    val clientSecret: String? = null,
    val customerId: String? = null,
    val ephemeralKey: String? = null,
)

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    fun createPayment(deliveryMatchId: Int, amount: Double, currency: String = "cad") {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }
            paymentRepository.createPayment(deliveryMatchId, amount, currency).fold(
                onSuccess = { response ->
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            clientSecret = response.clientSecret ?: response.data?.clientSecret,
                            customerId = response.customerId,
                            ephemeralKey = response.ephemeralKey,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isProcessing = false, errorMessage = e.message ?: "Payment failed")
                    }
                },
            )
        }
    }

    fun confirmCapture(deliveryMatchId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }
            paymentRepository.confirmCapture(deliveryMatchId).fold(
                onSuccess = { tx ->
                    _uiState.update {
                        it.copy(isProcessing = false, paymentSuccess = true, transaction = tx)
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isProcessing = false, errorMessage = e.message ?: "Confirmation failed")
                    }
                },
            )
        }
    }

    fun isMockClientSecret(secret: String): Boolean = secret.startsWith("mock_pi_")

    fun clearState() {
        _uiState.update { PaymentUiState() }
    }
}
