package com.efthemiosprime.pasabayan.features.payments.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.features.payments.model.Transaction
import com.efthemiosprime.pasabayan.features.payments.services.PaymentRepository
import com.efthemiosprime.pasabayan.features.payments.services.StripeConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PaymentFlowStatus {
    IDLE,
    SHEET_READY,
    PAYMENT_CANCELED,
    PAYMENT_SUCCESS,
    ALREADY_PAID,
}

data class PaymentUiState(
    val isProcessing: Boolean = false,
    val errorMessage: String? = null,
    val paymentSuccess: Boolean = false,
    val transaction: Transaction? = null,
    val infoMessage: String? = null,
    val isSheetReady: Boolean = false,
    val statusMessage: String? = null,
    val flowStatus: PaymentFlowStatus = PaymentFlowStatus.IDLE,
    val clientSecret: String? = null,
    val customerId: String? = null,
    val ephemeralKey: String? = null,
    val publicKey: String? = null,
    val stripeCurrencyCode: String = "CAD",
    val stripeIsSandbox: Boolean = true,
)

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val stripeConfigRepository: StripeConfigRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    init {
        loadStripeConfig()
    }

    fun createPayment(
        deliveryMatchId: Int,
        amount: Double,
        currency: String = "cad",
        transactionStatus: String? = null,
    ) {
        if (isPaidTransactionStatus(transactionStatus)) {
            _uiState.update {
                it.copy(
                    isProcessing = false,
                    isSheetReady = false,
                    flowStatus = PaymentFlowStatus.ALREADY_PAID,
                    statusMessage = "already_paid",
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isProcessing = true,
                    errorMessage = null,
                    paymentSuccess = false,
                    transaction = null,
                    infoMessage = null,
                    isSheetReady = false,
                    statusMessage = null,
                    flowStatus = PaymentFlowStatus.IDLE,
                )
            }
            paymentRepository.createPayment(deliveryMatchId, amount, currency).fold(
                onSuccess = { response ->
                    val resolvedClientSecret = response.clientSecret ?: response.data?.clientSecret
                    if (resolvedClientSecret.isNullOrBlank()) {
                        _uiState.update {
                            it.copy(
                                isProcessing = false,
                                errorMessage = "Payment initialization failed",
                                clientSecret = null,
                                customerId = null,
                                ephemeralKey = null,
                                publicKey = null,
                                isSheetReady = false,
                            )
                        }
                        return@fold
                    }
                    val isMock = isMockClientSecret(resolvedClientSecret)
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            clientSecret = resolvedClientSecret,
                            customerId = response.customerId,
                            ephemeralKey = response.ephemeralKey,
                            publicKey = response.publicKey,
                            infoMessage = response.message,
                            isSheetReady = !isMock,
                            flowStatus = if (isMock) PaymentFlowStatus.PAYMENT_SUCCESS else PaymentFlowStatus.SHEET_READY,
                            statusMessage = if (isMock) "mock_payment_ready" else "sheet_ready",
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            errorMessage = e.message ?: "Payment failed",
                            clientSecret = null,
                            customerId = null,
                            ephemeralKey = null,
                            publicKey = null,
                            isSheetReady = false,
                            flowStatus = PaymentFlowStatus.IDLE,
                        )
                    }
                },
            )
        }
    }

    fun confirmCapture(deliveryMatchId: Int) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isProcessing = true,
                    errorMessage = null,
                    paymentSuccess = false,
                    statusMessage = null,
                )
            }
            paymentRepository.confirmCapture(deliveryMatchId).fold(
                onSuccess = { tx ->
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            paymentSuccess = true,
                            transaction = tx,
                            flowStatus = PaymentFlowStatus.PAYMENT_SUCCESS,
                            statusMessage = "payment_success",
                            isSheetReady = false,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            errorMessage = e.message ?: "Confirmation failed",
                            isSheetReady = false,
                        )
                    }
                },
            )
        }
    }

    fun onPaymentSheetCanceled() {
        _uiState.update {
            it.copy(
                isProcessing = false,
                errorMessage = null,
                flowStatus = PaymentFlowStatus.PAYMENT_CANCELED,
                statusMessage = "payment_canceled",
            )
        }
    }

    fun onPaymentSheetFailed(message: String?) {
        _uiState.update {
            it.copy(
                isProcessing = false,
                errorMessage = message ?: "Payment failed",
                isSheetReady = false,
                flowStatus = PaymentFlowStatus.IDLE,
                statusMessage = null,
            )
        }
    }

    fun isPaidTransactionStatus(status: String?): Boolean {
        if (status == null) return false
        val normalized = status.trim().lowercase()
        return normalized == "completed" || normalized == "captured"
    }

    fun isMockClientSecret(secret: String): Boolean = secret.startsWith("mock_pi_")

    private fun loadStripeConfig() {
        viewModelScope.launch {
            stripeConfigRepository.fetchConfig().onSuccess { config ->
                _uiState.update {
                    it.copy(
                        stripeCurrencyCode = config.currency.uppercase(),
                        stripeIsSandbox = config.isSandbox,
                    )
                }
            }
        }
    }

    fun clearState() {
        val current = _uiState.value
        _uiState.update {
            PaymentUiState(
                stripeCurrencyCode = current.stripeCurrencyCode,
                stripeIsSandbox = current.stripeIsSandbox,
            )
        }
    }
}
