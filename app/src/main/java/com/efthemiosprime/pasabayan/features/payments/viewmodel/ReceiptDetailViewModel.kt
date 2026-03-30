package com.efthemiosprime.pasabayan.features.payments.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.features.payments.services.ReceiptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReceiptDetailUiState(
    val receiptUrl: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class ReceiptDetailViewModel @Inject constructor(
    private val receiptRepository: ReceiptRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReceiptDetailUiState())
    val uiState: StateFlow<ReceiptDetailUiState> = _uiState.asStateFlow()

    fun loadReceipt(transactionId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            receiptRepository.fetchReceipt(transactionId).fold(
                onSuccess = { receipt ->
                    _uiState.update { it.copy(receiptUrl = receipt.receiptUrl, isLoading = false) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Failed to load") }
                },
            )
        }
    }
}
