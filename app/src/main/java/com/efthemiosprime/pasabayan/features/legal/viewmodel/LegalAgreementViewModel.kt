package com.efthemiosprime.pasabayan.features.legal.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.features.legal.model.LegalDocument
import com.efthemiosprime.pasabayan.features.legal.model.LegalStatus
import com.efthemiosprime.pasabayan.features.legal.services.LegalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LegalAgreementUiState(
    val status: LegalStatus? = null,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val justAgreed: Boolean = false,
    val withdrawWarning: String? = null,
) {
    val hasPending: Boolean get() = (status?.pendingDocuments?.isNotEmpty() == true)
}

/**
 * Loads pending legal documents and submits agree / withdraw. iOS parity:
 * `LegalAgreementViewModel`.
 */
@HiltViewModel
class LegalAgreementViewModel @Inject constructor(
    private val repository: LegalRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LegalAgreementUiState())
    val uiState: StateFlow<LegalAgreementUiState> = _uiState.asStateFlow()

    fun loadStatus() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.fetchStatus().fold(
                onSuccess = { status ->
                    _uiState.update { it.copy(status = status, isLoading = false) }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.message ?: "Failed to load legal status")
                    }
                },
            )
        }
    }

    /**
     * Agree to all currently-pending documents.
     */
    fun agreeToAllPending(deviceId: String? = null) {
        val docs = _uiState.value.status?.pendingDocuments.orEmpty()
        if (docs.isEmpty()) return
        agree(docs.map { it.id }, deviceId)
    }

    fun agree(documentIds: List<Int>, deviceId: String? = null) {
        if (documentIds.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, justAgreed = false) }
            repository.agree(documentIds, deviceId).fold(
                onSuccess = { outcome ->
                    _uiState.update { state ->
                        state.copy(
                            isSubmitting = false,
                            justAgreed = outcome.allRequiredAgreed,
                            status = if (outcome.allRequiredAgreed) {
                                state.status?.copy(
                                    allAgreed = true,
                                    pendingDocuments = emptyList(),
                                    pendingCount = 0,
                                )
                            } else {
                                state.status?.copy(
                                    pendingDocuments = state.status.pendingDocuments
                                        .filterNot { it.id in documentIds },
                                    pendingCount = outcome.pendingRequiredCount,
                                )
                            },
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isSubmitting = false, errorMessage = e.message ?: "Failed to record agreement")
                    }
                },
            )
        }
    }

    fun withdraw(documentType: String) {
        if (documentType.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, withdrawWarning = null) }
            repository.withdraw(documentType).fold(
                onSuccess = { outcome ->
                    _uiState.update {
                        it.copy(isSubmitting = false, withdrawWarning = outcome.warning, justAgreed = false)
                    }
                    loadStatus()
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isSubmitting = false, errorMessage = e.message ?: "Failed to withdraw consent")
                    }
                },
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun consumeJustAgreed() {
        _uiState.update { it.copy(justAgreed = false) }
    }

    fun consumeWithdrawWarning() {
        _uiState.update { it.copy(withdrawWarning = null) }
    }

    /** Helper used by the UI when building the WebView list. */
    fun documentsByType(): Map<String, LegalDocument> =
        _uiState.value.status?.pendingDocuments.orEmpty().associateBy { it.type }
}
