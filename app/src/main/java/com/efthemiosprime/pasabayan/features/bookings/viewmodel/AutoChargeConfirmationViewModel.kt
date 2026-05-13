package com.efthemiosprime.pasabayan.features.bookings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.features.bookings.services.BookingsRepository
import com.efthemiosprime.pasabayan.features.payments.services.PaymentMethodsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Orchestrates the "Confirm & Pay" sheet shown to a shipper on a
 * `MatchStatus.PENDING` match. Mirrors iOS `AutoChargeConfirmationViewModel`
 * including the post-confirm retry path when no default payment method
 * exists at confirm time.
 *
 * The VM does NOT launch Stripe `PaymentSheet` itself — when the user taps
 * "Add Payment Method", state moves to [AutoChargeConfirmationState.AddingPaymentMethod]
 * and the hosting composable launches PaymentSheet via the existing
 * `rememberPaymentSheet` pattern. The composable calls back into
 * [onPaymentMethodAdded] or [onPaymentMethodCancelled] when PaymentSheet
 * resolves.
 */
@HiltViewModel
class AutoChargeConfirmationViewModel @Inject constructor(
    private val bookingsRepository: BookingsRepository,
    private val paymentMethodsRepository: PaymentMethodsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AutoChargeConfirmationState>(AutoChargeConfirmationState.Idle)
    val uiState: StateFlow<AutoChargeConfirmationState> = _uiState.asStateFlow()

    private var matchId: Int? = null
    private var price: Double = 0.0

    fun prepareConfirmation(matchId: Int, price: Double) {
        this.matchId = matchId
        this.price = price
        _uiState.value = AutoChargeConfirmationState.CheckingPaymentMethod
        viewModelScope.launch { loadDefaultCard(afterConfirm = false) }
    }

    fun confirmMatch() {
        val id = matchId ?: return
        _uiState.value = AutoChargeConfirmationState.Confirming
        viewModelScope.launch {
            bookingsRepository.confirmMatch(id).fold(
                onSuccess = { result ->
                    val autoCharge = result.autoCharge
                    _uiState.value = when {
                        autoCharge == null -> AutoChargeConfirmationState.Success(autoChargeQueued = false)
                        !autoCharge.shipperHasDefaultPaymentMethod ->
                            AutoChargeConfirmationState.NeedsPaymentMethodAfterConfirm
                        else -> AutoChargeConfirmationState.Success(autoChargeQueued = autoCharge.queued)
                    }
                },
                onFailure = { e ->
                    _uiState.value = AutoChargeConfirmationState.Error(e.message ?: "Failed to confirm")
                },
            )
        }
    }

    fun startAddingPaymentMethod() {
        val afterConfirm = _uiState.value is AutoChargeConfirmationState.NeedsPaymentMethodAfterConfirm
        _uiState.value = AutoChargeConfirmationState.AddingPaymentMethod(afterConfirm = afterConfirm)
    }

    fun onPaymentMethodAdded() {
        val current = _uiState.value as? AutoChargeConfirmationState.AddingPaymentMethod ?: return
        _uiState.value = AutoChargeConfirmationState.CheckingPaymentMethod
        viewModelScope.launch { loadDefaultCard(afterConfirm = current.afterConfirm) }
    }

    fun onPaymentMethodCancelled() {
        val current = _uiState.value as? AutoChargeConfirmationState.AddingPaymentMethod ?: return
        _uiState.value = if (current.afterConfirm) {
            AutoChargeConfirmationState.NeedsPaymentMethodAfterConfirm
        } else {
            AutoChargeConfirmationState.NeedsPaymentMethod(price = price)
        }
    }

    fun retryConfirmation() {
        _uiState.value = AutoChargeConfirmationState.CheckingPaymentMethod
        viewModelScope.launch { loadDefaultCard(afterConfirm = false) }
    }

    fun dismiss() {
        matchId = null
        price = 0.0
        _uiState.value = AutoChargeConfirmationState.Idle
    }

    private suspend fun loadDefaultCard(afterConfirm: Boolean) {
        paymentMethodsRepository.loadPaymentMethods().fold(
            onSuccess = { methods ->
                val default = methods.firstOrNull { it.isDefault } ?: methods.firstOrNull()
                if (default != null) {
                    if (afterConfirm) {
                        retryAutoCharge()
                    } else {
                        _uiState.value = AutoChargeConfirmationState.ReadyToConfirm(
                            price = price,
                            cardLast4 = default.last4,
                        )
                    }
                } else {
                    _uiState.value = if (afterConfirm) {
                        AutoChargeConfirmationState.NeedsPaymentMethodAfterConfirm
                    } else {
                        AutoChargeConfirmationState.NeedsPaymentMethod(price = price)
                    }
                }
            },
            onFailure = { e ->
                _uiState.value = AutoChargeConfirmationState.Error(e.message ?: "Failed to load payment methods")
            },
        )
    }

    private suspend fun retryAutoCharge() {
        val id = matchId ?: return
        bookingsRepository.retryAutoCharge(id).fold(
            onSuccess = {
                _uiState.value = AutoChargeConfirmationState.Success(autoChargeQueued = true)
            },
            onFailure = { e ->
                _uiState.value = AutoChargeConfirmationState.Error(e.message ?: "Failed to retry auto-charge")
            },
        )
    }
}
