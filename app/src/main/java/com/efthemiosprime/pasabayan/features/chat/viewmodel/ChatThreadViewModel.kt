package com.efthemiosprime.pasabayan.features.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efthemiosprime.pasabayan.features.chat.model.MessageItem
import com.efthemiosprime.pasabayan.features.chat.services.ChatMergeLogic
import com.efthemiosprime.pasabayan.features.chat.services.ChatRepository
import com.efthemiosprime.pasabayan.features.chat.services.RealtimeChatService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatThreadUiState(
    val conversationId: Int? = null,
    val messages: List<MessageItem> = emptyList(),
    val isLoading: Boolean = false,
    val alertMessage: String? = null,
    val isComposerEnabled: Boolean = true,
    val nextPage: Int? = 2,
    val isPaging: Boolean = false,
    val failedMessageTempIds: Set<Int> = emptySet(),
)

@HiltViewModel
class ChatThreadViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val realtimeChatService: RealtimeChatService,
    private val chatMergeLogic: ChatMergeLogic,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatThreadUiState())
    val uiState: StateFlow<ChatThreadUiState> = _uiState.asStateFlow()

    private val markMessageAsReadRequestedIds = linkedSetOf<Int>()
    private val tempIdToText = mutableMapOf<Int, String>()
    private var nextTempId = -1
    private var pollJob: Job? = null
    private var latestPollApplyMs = 0L
    private var lastMessageId: Int = 0

    init {
        observeRealtimeMessages()
    }

    fun openConversation(conversationId: Int, status: String) {
        _uiState.update {
            it.copy(
                conversationId = conversationId,
                messages = emptyList(),
                isLoading = true,
                alertMessage = null,
                isComposerEnabled = status == "active",
                nextPage = 2,
                failedMessageTempIds = emptySet(),
            )
        }

        viewModelScope.launch {
            chatRepository.markConversationRead(conversationId)
            chatRepository.loadBroadcastingConfig()
                .onSuccess { config ->
                    realtimeChatService.connect(config)
                    realtimeChatService.subscribe(conversationId)
                }
            loadFirstPage()
            startPollingFallback(conversationId)
        }
    }

    fun closeConversation() {
        pollJob?.cancel()
        _uiState.value.conversationId?.let { realtimeChatService.unsubscribe(it) }
        _uiState.update { ChatThreadUiState() }
        lastMessageId = 0
    }

    fun sendMessage(text: String, type: String = "text") {
        val conversationId = _uiState.value.conversationId ?: return
        if (!_uiState.value.isComposerEnabled) return
        val tempId = nextTempId--
        tempIdToText[tempId] = text

        val optimistic = MessageItem(
            id = tempId,
            message = text,
            messageType = type,
            sender = null,
            isRead = false,
            createdAt = "",
            formattedMessage = null,
            messageTypeDisplay = null,
            readAt = null,
            readReceipts = emptyMap(),
            deliveryStatus = "sent",
            deliveredAt = null,
            attachments = emptyList(),
            canEdit = false,
            canDelete = true,
            isDeleted = false,
            deletedAt = null,
            metadata = null,
        )
        _uiState.update { it.copy(messages = (it.messages + optimistic).sortedBy { msg -> msg.id }) }

        viewModelScope.launch {
            chatRepository.sendMessage(conversationId, text, type)
                .onSuccess { sent ->
                    tempIdToText.remove(tempId)
                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages.map { msg ->
                                if (msg.id == tempId) sent else msg
                            }.sortedBy { msg -> msg.id },
                            failedMessageTempIds = state.failedMessageTempIds - tempId,
                        )
                    }
                    lastMessageId = maxOf(lastMessageId, sent.id)
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            failedMessageTempIds = it.failedMessageTempIds + tempId,
                        )
                    }
                }
        }
    }

    fun retrySend(tempId: Int) {
        val text = tempIdToText[tempId] ?: return
        _uiState.update { it.copy(failedMessageTempIds = it.failedMessageTempIds - tempId) }
        sendMessage(text)
    }

    fun isFailed(message: MessageItem): Boolean = message.id in _uiState.value.failedMessageTempIds

    fun loadMoreMessages() {
        val conversationId = _uiState.value.conversationId ?: return
        val page = _uiState.value.nextPage ?: return
        if (_uiState.value.isPaging) return
        _uiState.update { it.copy(isPaging = true) }
        viewModelScope.launch {
            chatRepository.loadMessages(conversationId = conversationId, page = page)
                .onSuccess { pageData ->
                    _uiState.update { state ->
                        val prepended = (pageData.messages + state.messages)
                            .associateBy { it.id }
                            .values
                            .sortedBy { it.id }
                        state.copy(
                            messages = prepended,
                            isPaging = false,
                            nextPage = if (pageData.currentPage < pageData.lastPage) {
                                pageData.currentPage + 1
                            } else {
                                null
                            },
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isPaging = false, alertMessage = error.message) }
                }
        }
    }

    fun markMessageRead(messageId: Int) {
        if (!markMessageAsReadRequestedIds.add(messageId)) return
        viewModelScope.launch {
            chatRepository.markMessageRead(messageId)
        }
    }

    fun deleteMessage(messageId: Int) {
        viewModelScope.launch {
            chatRepository.deleteMessage(messageId)
                .onSuccess { deletedAt ->
                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages.map { message ->
                                if (message.id == messageId) {
                                    message.copy(
                                        isDeleted = true,
                                        deletedAt = deletedAt,
                                        message = "",
                                    )
                                } else {
                                    message
                                }
                            },
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(alertMessage = null) }
    }

    private suspend fun loadFirstPage() {
        val conversationId = _uiState.value.conversationId ?: return
        chatRepository.loadMessages(conversationId = conversationId, page = 1)
            .onSuccess { pageData ->
                _uiState.update {
                    it.copy(
                        messages = pageData.messages.sortedBy { message -> message.id },
                        isLoading = false,
                        nextPage = if (pageData.currentPage < pageData.lastPage) pageData.currentPage + 1 else null,
                    )
                }
                lastMessageId = _uiState.value.messages.maxOfOrNull { it.id } ?: 0
            }
            .onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        alertMessage = error.message,
                        isComposerEnabled = !isComposerDisableError(error.message),
                    )
                }
            }
    }

    private fun observeRealtimeMessages() {
        viewModelScope.launch {
            realtimeChatService.incomingMessages.collect { incoming ->
                val merge = chatMergeLogic.merge(_uiState.value.messages, listOf(incoming))
                _uiState.update { it.copy(messages = merge.messages) }
                lastMessageId = merge.lastMessageId
            }
        }
    }

    private fun startPollingFallback(conversationId: Int) {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            realtimeChatService.pollingMessages(conversationId).collect { polledMessages ->
                val now = System.currentTimeMillis()
                if (_uiState.value.messages.size > 200 && now - latestPollApplyMs < 1_000L) {
                    return@collect
                }
                val incoming = polledMessages.filter { it.id > lastMessageId }
                val merge = chatMergeLogic.merge(_uiState.value.messages, incoming)
                _uiState.update { it.copy(messages = merge.messages) }
                lastMessageId = merge.lastMessageId
                latestPollApplyMs = now
            }
        }
    }

    private fun isComposerDisableError(message: String?): Boolean {
        val text = message.orEmpty().lowercase()
        return text.contains("401") || text.contains("404") || text.contains("closed")
    }
}

