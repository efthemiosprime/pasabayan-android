package com.efthemiosprime.pasabayan.features.chat.services

import com.efthemiosprime.pasabayan.features.chat.model.ConversationSummary
import com.efthemiosprime.pasabayan.features.chat.model.MessageItem
import com.efthemiosprime.pasabayan.features.chat.model.ReverbConfig

interface ChatRepository {

    suspend fun loadBroadcastingConfig(): Result<ReverbConfig>

    suspend fun loadConversations(
        role: String? = null,
        status: String? = null,
        unreadOnly: Boolean? = null,
        page: Int = 1,
        perPage: Int = DEFAULT_PER_PAGE,
    ): Result<ConversationsPage>

    suspend fun loadConversationDetail(conversationId: Int): Result<ConversationSummary>

    suspend fun loadMessages(conversationId: Int, page: Int = 1): Result<MessagesPage>

    suspend fun sendMessage(
        conversationId: Int,
        message: String,
        type: String = "text",
    ): Result<MessageItem>

    suspend fun deleteMessage(messageId: Int): Result<String?>

    suspend fun markConversationRead(conversationId: Int): Result<Unit>

    suspend fun markMessageRead(messageId: Int): Result<Unit>

    suspend fun getMessageStatus(messageId: Int): Result<MessageDeliveryStatus>

    suspend fun authenticateChannel(
        config: ReverbConfig,
        channelName: String,
        socketId: String,
    ): Result<String>

    companion object {
        const val DEFAULT_PER_PAGE: Int = 15
    }
}

data class ConversationsPage(
    val conversations: List<ConversationSummary>,
    val currentPage: Int,
    val lastPage: Int,
    val total: Int,
) {
    val hasMore: Boolean get() = currentPage < lastPage
}

data class MessagesPage(
    val messages: List<MessageItem>,
    val currentPage: Int,
    val lastPage: Int,
)

data class MessageDeliveryStatus(
    val deliveryStatus: String?,
    val readReceipts: Map<String, String>,
)

