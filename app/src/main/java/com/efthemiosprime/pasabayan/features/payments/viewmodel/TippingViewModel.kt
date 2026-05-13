package com.efthemiosprime.pasabayan.features.payments.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.features.payments.model.Transaction
import com.efthemiosprime.pasabayan.features.payments.model.toDomain
import com.efthemiosprime.pasabayan.features.payments.services.PaymentRepository
import com.efthemiosprime.pasabayan.features.payments.services.PaymentSheetConfigFactory
import com.stripe.android.paymentsheet.PaymentSheet
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TippingUiState(
    val selectedTipAmount: Double = 0.0,
    val customTipAmount: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showSuccess: Boolean = false,
    val showPaymentSheet: Boolean = false,
    val clientSecret: String? = null,
    val paymentSheetConfig: PaymentSheet.Configuration? = null,
    val paymentSheetReady: Boolean = false,
    val paymentSheetResult: PaymentSheetResultKind? = null,
    val updatedTransaction: Transaction? = null,
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
                    paymentSheetConfig = null,
                    paymentSheetReady = false,
                    paymentSheetResult = null,
                    updatedTransaction = null,
                )
            }
            paymentRepository.addTip(transactionId, amount).fold(
                onSuccess = { response ->
                    val updated = response.data?.toDomain()
                    val secret = response.clientSecret
                    if (secret != null) {
                        setupPaymentSheet(secret)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                showPaymentSheet = true,
                                updatedTransaction = updated,
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                showSuccess = true,
                                updatedTransaction = updated,
                            )
                        }
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

    /**
     * Builds a PaymentSheet config for the tip secret. Tip responses don't carry customer
     * credentials, so the factory returns the no-customer variant (no saved cards / Google Pay).
     */
    fun setupPaymentSheet(clientSecret: String) {
        val config = PaymentSheetConfigFactory.build(
            customerId = null,
            ephemeralKey = null,
            stripeIsSandbox = true,
            currencyCode = "CAD",
        )
        _uiState.update {
            it.copy(
                clientSecret = clientSecret,
                paymentSheetConfig = config,
                paymentSheetReady = true,
            )
        }
    }

    /** Translates Stripe's PaymentSheetResult into VM state. */
    fun handlePaymentResult(kind: PaymentSheetResultKind, message: String? = null) {
        when (kind) {
            PaymentSheetResultKind.COMPLETED -> _uiState.update {
                it.copy(
                    paymentSheetResult = kind,
                    showSuccess = true,
                    showPaymentSheet = false,
                    paymentSheetReady = false,
                    errorMessage = null,
                )
            }
            PaymentSheetResultKind.CANCELED -> _uiState.update {
                it.copy(
                    paymentSheetResult = kind,
                    showPaymentSheet = false,
                    paymentSheetReady = false,
                )
            }
            PaymentSheetResultKind.FAILED -> _uiState.update {
                it.copy(
                    paymentSheetResult = kind,
                    showPaymentSheet = false,
                    paymentSheetReady = false,
                    clientSecret = null,
                    paymentSheetConfig = null,
                    errorMessage = message ?: "Tip payment failed",
                )
            }
        }
    }

    fun reset() {
        _uiState.update { TippingUiState() }
    }
}
