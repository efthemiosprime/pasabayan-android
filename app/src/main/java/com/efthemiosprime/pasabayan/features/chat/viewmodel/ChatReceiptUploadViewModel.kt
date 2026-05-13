package com.efthemiosprime.pasabayan.features.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.features.bookings.model.MatchReceipt
import com.efthemiosprime.pasabayan.features.bookings.services.MatchReceiptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Orchestrates the in-chat receipt upload (carrier-side). The host
 * composable performs the actual Photo Picker call + URI → byte-array read;
 * this VM only handles compression + upload + state transitions so it stays
 * Context-free and JVM-testable.
 *
 * iOS parity: `ChatReceiptUploadSheet` state in `ConversationDetailView`.
 */
@HiltViewModel
class ChatReceiptUploadViewModel @Inject constructor(
    private val matchReceiptRepository: MatchReceiptRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatReceiptUploadState>(ChatReceiptUploadState.Idle)
    val uiState: StateFlow<ChatReceiptUploadState> = _uiState.asStateFlow()

    /**
     * Receipt fetched for inline display in the chat thread (shipper view).
     * Null when no receipt has been uploaded yet (server returned 404).
     * Auto-refreshed after a successful upload.
     */
    private val _receipt = MutableStateFlow<MatchReceipt?>(null)
    val receipt: StateFlow<MatchReceipt?> = _receipt.asStateFlow()

    fun uploadReceipt(matchId: Int, photoBytes: ByteArray) {
        _uiState.value = ChatReceiptUploadState.Uploading
        viewModelScope.launch {
            matchReceiptRepository.uploadReceipt(matchId, photoBytes).fold(
                onSuccess = {
                    _uiState.value = ChatReceiptUploadState.Success
                    loadReceipt(matchId)
                },
                onFailure = { e ->
                    _uiState.value = ChatReceiptUploadState.Error(
                        message = e.message ?: "Could not upload receipt",
                    )
                },
            )
        }
    }

    /**
     * Fetch the existing receipt for [matchId]. Surfaces silently — a missing
     * receipt (404) is normal (`MatchReceipt?` is null), and transient
     * failures shouldn't error a chat thread.
     */
    fun loadReceipt(matchId: Int) {
        viewModelScope.launch {
            matchReceiptRepository.fetchReceipt(matchId).fold(
                onSuccess = { _receipt.value = it },
                onFailure = { /* silent; surfaced via no inline card */ },
            )
        }
    }

    /** Return to Idle (upload state only — keeps any loaded receipt for display). */
    fun reset() {
        _uiState.value = ChatReceiptUploadState.Idle
    }
}
