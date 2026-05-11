package com.efthemiosprime.pasabayan.features.support.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.features.support.model.SupportCategory
import com.efthemiosprime.pasabayan.features.support.model.SupportPriority
import com.efthemiosprime.pasabayan.features.support.model.SupportTicket
import com.efthemiosprime.pasabayan.features.support.model.SupportTicketDraft
import com.efthemiosprime.pasabayan.features.support.services.SupportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SupportTicketUiState(
    val draft: SupportTicketDraft = SupportTicketDraft(),
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val submittedTicket: SupportTicket? = null,
)

@HiltViewModel
class SupportTicketViewModel @Inject constructor(
    private val repository: SupportRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SupportTicketUiState())
    val uiState: StateFlow<SupportTicketUiState> = _uiState.asStateFlow()

    fun selectCategory(category: SupportCategory) {
        update { it.copy(draft = it.draft.copy(category = category)) }
    }

    fun selectPriority(priority: SupportPriority) {
        update { it.copy(draft = it.draft.copy(priority = priority)) }
    }

    fun setSubject(value: String) {
        update {
            it.copy(
                draft = it.draft.copy(
                    subject = value.take(SupportTicketDraft.MAX_SUBJECT),
                ),
            )
        }
    }

    fun setEmail(value: String) {
        update { it.copy(draft = it.draft.copy(email = value.trim())) }
    }

    fun setDescription(value: String) {
        update {
            it.copy(
                draft = it.draft.copy(
                    description = value.take(SupportTicketDraft.MAX_DESCRIPTION),
                ),
            )
        }
    }

    fun addAttachment(uri: Uri) {
        update { state ->
            val current = state.draft.attachments
            if (uri in current || current.size >= SupportTicketDraft.MAX_ATTACHMENTS) state
            else state.copy(draft = state.draft.copy(attachments = current + uri))
        }
    }

    fun removeAttachment(uri: Uri) {
        update { state ->
            state.copy(draft = state.draft.copy(attachments = state.draft.attachments - uri))
        }
    }

    fun submit() {
        val state = _uiState.value
        val draft = state.draft
        val category = draft.category ?: return
        if (!draft.isSubmittable || state.isSubmitting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, submittedTicket = null) }
            repository.submit(
                category = category,
                subject = draft.subject.trim(),
                email = draft.email.trim(),
                priority = draft.priority,
                description = draft.description.trim(),
                attachments = draft.attachments,
            ).fold(
                onSuccess = { ticket ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            submittedTicket = ticket,
                            draft = SupportTicketDraft(),
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = e.message ?: "Could not submit ticket. Please try again.",
                        )
                    }
                },
            )
        }
    }

    fun consumeSubmittedTicket() {
        _uiState.update { it.copy(submittedTicket = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private inline fun update(transform: (SupportTicketUiState) -> SupportTicketUiState) {
        _uiState.update(transform)
    }
}
