package com.efthemiosprime.pasabayan.features.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.features.chat.model.ConversationSummary
import com.efthemiosprime.pasabayan.features.chat.services.ChatRepository
import com.efthemiosprime.pasabayan.features.chat.services.ConversationsPage
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
    val currentPage: Int = 0,
    val lastPage: Int = 1,
    val isLoadingMore: Boolean = false,
    val loadMoreError: String? = null,
) {
    val hasMore: Boolean get() = currentPage in 1 until lastPage
}

@HiltViewModel
class ConversationsViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversationsUiState())
    val uiState: StateFlow<ConversationsUiState> = _uiState.asStateFlow()

    // Filters from the most recent loadConversations() call. Reused on
    // loadNextConversationsPage() so pagination respects the active query —
    // iOS parity with ChatViewModel.currentConversationFilters.
    private var currentRole: String? = null
    private var currentStatus: String? = null
    private var currentUnreadOnly: Boolean? = null

    fun loadConversations(role: String? = null, status: String? = null, unreadOnly: Boolean? = null) {
        currentRole = role
        currentStatus = status
        currentUnreadOnly = unreadOnly
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    alertMessage = null,
                    loadMoreError = null,
                )
            }
            chatRepository.loadConversations(
                role = role,
                status = status,
                unreadOnly = unreadOnly,
                page = 1,
                perPage = ChatRepository.DEFAULT_PER_PAGE,
            )
                .onSuccess { page ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            conversations = page.conversations,
                            allUnreadCount = page.conversations.sumOf { conversation -> conversation.unreadCount },
                            currentPage = page.currentPage,
                            lastPage = page.lastPage,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, alertMessage = error.message) }
                }
        }
    }

    /// Fetch the next page and append. Dedup by id — a newer message on an
    /// earlier-page conversation can re-surface here when the list is sorted
    /// by last-message-at; without dedup LazyColumn keys would collide.
    fun loadNextConversationsPage() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.hasMore) return
        val nextPage = state.currentPage + 1

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true, loadMoreError = null) }
            chatRepository.loadConversations(
                role = currentRole,
                status = currentStatus,
                unreadOnly = currentUnreadOnly,
                page = nextPage,
                perPage = ChatRepository.DEFAULT_PER_PAGE,
            )
                .onSuccess { page ->
                    _uiState.update { existing ->
                        val merged = (existing.conversations + page.conversations)
                            .distinctBy { it.id }
                        existing.copy(
                            isLoadingMore = false,
                            conversations = merged,
                            allUnreadCount = merged.sumOf { it.unreadCount },
                            currentPage = page.currentPage,
                            lastPage = page.lastPage,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoadingMore = false, loadMoreError = error.message) }
                }
        }
    }

    fun retryLoadMoreConversations() {
        if (_uiState.value.isLoadingMore) return
        _uiState.update { it.copy(loadMoreError = null) }
        loadNextConversationsPage()
    }

    fun clearError() {
        _uiState.update { it.copy(alertMessage = null, loadMoreError = null) }
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
