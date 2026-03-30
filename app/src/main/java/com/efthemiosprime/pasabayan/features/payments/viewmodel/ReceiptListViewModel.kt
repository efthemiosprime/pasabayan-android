package com.efthemiosprime.pasabayan.features.payments.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.features.payments.model.PaymentReceipt
import com.efthemiosprime.pasabayan.features.payments.services.ReceiptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReceiptListUiState(
    val receipts: List<PaymentReceipt> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val hasMore: Boolean = false,
    val totalCount: Int = 0,
)

@HiltViewModel
class ReceiptListViewModel @Inject constructor(
    private val receiptRepository: ReceiptRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReceiptListUiState())
    val uiState: StateFlow<ReceiptListUiState> = _uiState.asStateFlow()
    private var currentPage = 1

    fun loadReceipts() {
        currentPage = 1
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isLoadingMore = false,
                    error = null,
                    receipts = emptyList(),
                    hasMore = false,
                    totalCount = 0,
                )
            }
            receiptRepository.fetchReceipts(page = 1).fold(
                onSuccess = { (receipts, hasMore, total) ->
                    _uiState.update {
                        it.copy(receipts = receipts, isLoading = false, hasMore = hasMore, totalCount = total)
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            receipts = emptyList(),
                            hasMore = false,
                            totalCount = 0,
                            error = e.message ?: "Failed to load",
                        )
                    }
                },
            )
        }
    }

    fun loadMore() {
        if (!_uiState.value.hasMore || _uiState.value.isLoadingMore) return
        currentPage++
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            receiptRepository.fetchReceipts(page = currentPage).fold(
                onSuccess = { (receipts, hasMore, total) ->
                    _uiState.update { state ->
                        state.copy(
                            receipts = state.receipts + receipts,
                            isLoadingMore = false,
                            hasMore = hasMore,
                            totalCount = total,
                        )
                    }
                },
                onFailure = { e ->
                    currentPage--
                    _uiState.update { it.copy(isLoadingMore = false, error = e.message) }
                },
            )
        }
    }

    fun refresh() = loadReceipts()
}
