package com.efthemiosprime.pasabayan.features.chat.model

data class MessageItem(
    val id: Int,
    val message: String,
    val messageType: String,
    val sender: Sender?,
    val isRead: Boolean,
    val createdAt: String,
    val formattedMessage: String?,
    val messageTypeDisplay: String?,
    val readAt: String?,
    val readReceipts: Map<String, String>,
    val deliveryStatus: String?,
    val deliveredAt: String?,
    val attachments: List<Attachment>,
    val canEdit: Boolean,
    val canDelete: Boolean,
    val isDeleted: Boolean,
    val deletedAt: String?,
    val metadata: MessageMetadata?,
) {
    val isSystemMessage: Boolean
        get() = messageType == "system"

    val hasAttachments: Boolean
        get() = attachments.isNotEmpty()
}

data class Sender(
    val id: Int,
    val name: String,
    val avatar: String?,
)

data class Attachment(
    val id: Int?,
    val url: String?,
    val type: String?,
)

data class MessageMetadata(
    val type: String?,
    val listItemIndex: Int?,
    val listItemTotal: Int?,
    val listItem: ListItem?,
)

data class ListItem(
    val item: String,
    val quantity: Int?,
    val notes: String?,
    val noteUrls: List<String>,
)

