package com.efthemiosprime.pasabayan.features.payments.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionRole
import com.efthemiosprime.pasabayan.core.domain.`enum`.TransactionStatus
import com.efthemiosprime.pasabayan.features.payments.model.Transaction
import com.efthemiosprime.pasabayan.features.payments.services.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TransactionDetailUiState(
    val transaction: Transaction? = null,
    val viewerRole: TransactionRole? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val cancelSuccess: Boolean = false,
) {
    private val isShipperViewer: Boolean get() = viewerRole == TransactionRole.SHIPPER
    private val isCarrierViewer: Boolean get() = viewerRole == TransactionRole.CARRIER

    private val statusIsPostPayment: Boolean
        get() = transaction?.transactionStatus.let {
            it == TransactionStatus.CAPTURED || it == TransactionStatus.COMPLETED
        }

    /** Carrier sees a card summarising how much they earned. */
    val showEarningsCard: Boolean
        get() = isCarrierViewer && transaction?.amounts != null

    /** Shipper sees a card breaking down what they paid. */
    val showAmountBreakdown: Boolean
        get() = isShipperViewer && transaction?.amounts != null

    /** Shipper can request a refund post-payment and only if one isn't already in flight. */
    val canRequestRefund: Boolean
        get() = isShipperViewer && statusIsPostPayment && transaction?.refund == null

    /** Shipper can add a tip after the delivery completes — but only once. */
    val canAddTip: Boolean
        get() = isShipperViewer &&
            transaction?.transactionStatus == TransactionStatus.COMPLETED &&
            transaction.tip == null

    /** Refund status visible to either side once a refund exists. */
    val showRefundStatusCard: Boolean
        get() = transaction?.refund != null

    /** Carrier viewer sees payout status when the server reports one. */
    val showPayoutStatusCard: Boolean
        get() = isCarrierViewer && transaction?.effectivePayoutStatus != null
}

@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionDetailUiState())
    val uiState: StateFlow<TransactionDetailUiState> = _uiState.asStateFlow()

    /** Sets the viewer role for the loaded transaction; computeds re-derive automatically. */
    fun setViewerRole(role: TransactionRole?) {
        _uiState.update { it.copy(viewerRole = role) }
    }

    fun loadTransaction(id: Int, viewerRole: TransactionRole? = null) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    cancelSuccess = false,
                    viewerRole = viewerRole ?: it.viewerRole,
                )
            }
            paymentRepository.getTransaction(id).fold(
                onSuccess = { tx ->
                    _uiState.update { it.copy(transaction = tx, isLoading = false) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Failed to load") }
                },
            )
        }
    }

    fun cancelTransaction(id: Int, reason: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, cancelSuccess = false) }
            paymentRepository.cancelTransaction(id, reason).fold(
                onSuccess = { tx ->
                    _uiState.update { it.copy(isLoading = false, cancelSuccess = true, transaction = tx) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Cancel failed") }
                },
            )
        }
    }
}
