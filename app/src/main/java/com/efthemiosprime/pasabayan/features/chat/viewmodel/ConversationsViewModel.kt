package com.efthemiosprime.pasabayan.features.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.features.chat.model.ConversationSummary
import com.efthemiosprime.pasabayan.features.chat.services.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConversationsUiState(
    val conversations: List<ConversationSummary> = emptyList(),
    val isLoading: Boolean = false,
    val alertMessage: String? = null,
    val allUnreadCount: Int = 0,
)

@HiltViewModel
class ConversationsViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversationsUiState())
    val uiState: StateFlow<ConversationsUiState> = _uiState.asStateFlow()

    fun loadConversations(role: String? = null, status: String? = null, unreadOnly: Boolean? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, alertMessage = null) }
            chatRepository.loadConversations(role = role, status = status, unreadOnly = unreadOnly)
                .onSuccess { conversations ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            conversations = conversations,
                            allUnreadCount = conversations.sumOf { conversation -> conversation.unreadCount },
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, alertMessage = error.message) }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(alertMessage = null) }
    }

    fun markConversationOpened(conversationId: Int) {
        _uiState.update { state ->
            val updated = state.conversations.map { conversation ->
                if (conversation.id == conversationId) {
                    conversation.copy(unreadCount = 0)
                } else {
                    conversation
                }
            }
            state.copy(
                conversations = updated,
                allUnreadCount = updated.sumOf { it.unreadCount },
            )
        }
    }
}

