package com.efthemiosprime.pasabayan.core.network.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
data class ConversationsResponseJson(
    /**
     * Live API shape — Laravel paginator under `data`. iOS reads `$0.data.data`
     * to drill into the nested array; Android matches via
     * [PaginatedConversationsJson.data].
     */
    val data: PaginatedConversationsJson? = null,
    /**
     * Legacy / test-fixture flat-array shape. Some older fixtures (and any
     * realtime helper that wraps a list under `conversations`) emit this;
     * we keep the fallback wired so it doesn't silently drop them.
     */
    val conversations: List<ConversationSummaryJson>? = null,
) {
    fun conversationsOrEmpty(): List<ConversationSummaryJson> =
        when {
            !conversations.isNullOrEmpty() -> conversations
            data != null -> data.data
            else -> emptyList()
        }
}

/**
 * Laravel paginator block returned under `data` from `GET /chat/conversations`.
 * Mirrors `PaginatedMessagesJson` (under `messages`) and the analogous
 * envelopes used by trips / packages browse.
 */
@Serializable
data class PaginatedConversationsJson(
    val data: List<ConversationSummaryJson> = emptyList(),
    @SerialName("current_page") val currentPage: Int = 1,
    @SerialName("last_page") val lastPage: Int = 1,
    @SerialName("per_page") val perPage: Int = 15,
    val total: Int = 0,
)

@Serializable
data class ConversationDetailResponseJson(
    val data: ConversationDetailJson? = null,
    val conversation: ConversationDetailJson? = null,
) {
    fun conversationOrNull(): ConversationDetailJson? = conversation ?: data
}

@Serializable
data class MessagesResponseJson(
    val data: List<MessageItemJson>? = null,
    @SerialName("current_page") val currentPage: Int? = null,
    @SerialName("last_page") val lastPage: Int? = null,
    val messages: PaginatedMessagesJson? = null,
) {
    fun messagesOrEmpty(): List<MessageItemJson> = messages?.data ?: data.orEmpty()
    fun resolvedCurrentPage(): Int = messages?.currentPage ?: currentPage ?: 1
    fun resolvedLastPage(): Int = messages?.lastPage ?: lastPage ?: 1
}

@Serializable
data class SendMessageResponseJson(
    val message: JsonElement? = null,
    @SerialName("chat_message") val chatMessage: MessageItemJson? = null,
) {
    fun messageOrNull(): MessageItemJson? {
        if (chatMessage != null) return chatMessage
        val payload = message as? JsonObject ?: return null
        return runCatching { tolerantJson.decodeFromJsonElement(MessageItemJson.serializer(), payload) }.getOrNull()
    }

    private companion object {
        val tolerantJson = Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
        }
    }
}

@Serializable
data class DeleteMessageResponseJson(
    val success: Boolean = false,
    @SerialName("deleted_at") val deletedAt: String? = null,
    val data: DeleteMessageDataJson? = null,
) {
    fun deletedAtOrNull(): String? = deletedAt ?: data?.deletedAt
}

@Serializable
data class GenericSuccessResponseJson(
    val success: Boolean = false,
    val message: String? = null,
)

@Serializable
data class PaginatedMessagesJson(
    val data: List<MessageItemJson> = emptyList(),
    @SerialName("current_page") val currentPage: Int = 1,
    @SerialName("last_page") val lastPage: Int = 1,
)

@Serializable
data class DeleteMessageDataJson(
    @SerialName("deleted_at") val deletedAt: String? = null,
)

@Serializable
data class MessageStatusResponseJson(
    val data: MessageStatusJson? = null,
)

@Serializable
data class MessageStatusJson(
    @SerialName("delivery_status") val deliveryStatus: String? = null,
    @SerialName("read_receipts") val readReceipts: Map<String, String> = emptyMap(),
)

@Serializable
data class BroadcastingConfigResponseJson(
    val success: Boolean = false,
    val reverb: ReverbConfigJson? = null,
)

@Serializable
data class ReverbConfigJson(
    val key: String = "pasabayan",
    val host: String = "",
    val port: Int? = null,
    val scheme: String? = null,
)

@Serializable
data class ChannelAuthResponseJson(
    val auth: String = "",
)

@Serializable
data class ConversationDetailJson(
    val id: Int = 0,
    @SerialName("match_id") val matchId: Int? = null,
    val status: String = "active",
    @SerialName("status_display") val statusDisplay: String = "",
    @SerialName("user_role") val userRole: String? = null,
    @SerialName("other_participant") val otherParticipant: ParticipantJson? = null,
    @SerialName("match_info") val matchInfo: MatchInfoJson? = null,
    @SerialName("unread_count") val unreadCount: Int = 0,
    @SerialName("last_message") val lastMessage: LastMessageJson? = null,
    @SerialName("last_message_at") val lastMessageAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
) {
    val hasUnreadMessages: Boolean
        get() = unreadCount > 0
}

@Serializable
data class ConversationSummaryJson(
    val id: Int = 0,
    @SerialName("match_id") val matchId: Int? = null,
    val status: String = "active",
    @SerialName("status_display") val statusDisplay: String = "",
    @SerialName("user_role") val userRole: String? = null,
    @SerialName("other_participant") val otherParticipant: ParticipantJson? = null,
    @SerialName("match_info") val matchInfo: MatchInfoJson? = null,
    @SerialName("unread_count") val unreadCount: Int = 0,
    @SerialName("last_message") val lastMessage: LastMessageJson? = null,
    @SerialName("last_message_at") val lastMessageAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
) {
    val hasUnreadMessages: Boolean
        get() = unreadCount > 0
}

@Serializable
data class ParticipantJson(
    val id: Int = 0,
    val name: String = "",
    val avatar: String? = null,
    @SerialName("verification_level") val verificationLevel: String? = null,
)

@Serializable
data class MatchInfoJson(
    val route: String? = null,
    @SerialName("package_description") val packageDescription: String? = null,
    @SerialName("trip_route") val tripRoute: String? = null,
    @SerialName("departure_date") val departureDate: String? = null,
)

@Serializable
data class LastMessageJson(
    val id: Int = 0,
    val message: String = "",
    @SerialName("message_type") val messageType: String = "text",
    @SerialName("sender_name") val senderName: String? = null,
    @SerialName("is_system_message") val isSystemMessage: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class MessageItemJson(
    val id: Int = 0,
    val message: String = "",
    @SerialName("message_type") val messageType: String = "text",
    val sender: SenderJson? = null,
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("formatted_message") val formattedMessage: String? = null,
    @SerialName("message_type_display") val messageTypeDisplay: String? = null,
    @SerialName("read_at") val readAt: String? = null,
    @SerialName("read_receipts") val readReceipts: Map<String, String> = emptyMap(),
    @SerialName("delivery_status") val deliveryStatus: String? = null,
    @SerialName("delivered_at") val deliveredAt: String? = null,
    val attachments: List<AttachmentJson> = emptyList(),
    @SerialName("can_edit") val canEdit: Boolean = false,
    @SerialName("can_delete") val canDelete: Boolean = false,
    @SerialName("is_deleted") val isDeleted: Boolean = false,
    @SerialName("deleted_at") val deletedAt: String? = null,
    val metadata: MessageMetadataJson? = null,
) {
    val isSystemMessage: Boolean
        get() = messageType == "system"

    val hasAttachments: Boolean
        get() = attachments.isNotEmpty()
}

@Serializable
data class SenderJson(
    val id: Int = 0,
    val name: String = "",
    val avatar: String? = null,
    /**
     * Server-authoritative "this message is from the current user" flag.
     * iOS reads this directly. Android UI used to compare `sender.id ==
     * currentUserId`; that still works as a fallback but `is_me` avoids
     * id-comparison edge cases (e.g. system messages with synthetic
     * `sender_id = 0`).
     */
    @SerialName("is_me") val isMe: Boolean = false,
)

@Serializable
data class AttachmentJson(
    val id: Int? = null,
    val url: String? = null,
    val type: String? = null,
)

@Serializable
data class MessageMetadataJson(
    val type: String? = null,
    @SerialName("list_item_index") val listItemIndex: Int? = null,
    @SerialName("list_item_total") val listItemTotal: Int? = null,
    @SerialName("list_item") val listItem: ListItemJson? = null,
)

@Serializable
data class ListItemJson(
    val item: String = "",
    val quantity: Int? = null,
    val notes: String? = null,
    @SerialName("note_urls") val noteUrls: List<String> = emptyList(),
)

@Serializable
data class SendMessageRequestJson(
    val message: String,
    val type: String = "text",
)
