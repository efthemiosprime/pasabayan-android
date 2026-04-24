package com.efthemiosprime.pasabayan.features.chat.services

import com.efthemiosprime.pasabayan.features.chat.model.MessageItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatMergeLogicTest {

    private val mergeLogic = ChatMergeLogic()

    @Test
    fun `merge fast path appends newer messages`() {
        val current = listOf(message(1, "first"), message(2, "second"))
        val incoming = listOf(message(3, "third"), message(4, "fourth"))

        val result = mergeLogic.merge(current, incoming)

        assertEquals(listOf(1, 2, 3, 4), result.messages.map { it.id })
        assertEquals(4, result.lastMessageId)
    }

    @Test
    fun `merge slow path refreshes delivery status and dedupes by id`() {
        val current = listOf(
            message(id = 1, text = "hello", deliveryStatus = "sent", isRead = false),
            message(id = 2, text = "world", deliveryStatus = "sent", isRead = false),
        )
        val incoming = listOf(
            message(id = 2, text = "world", deliveryStatus = "read", isRead = true),
            message(id = 3, text = "new", deliveryStatus = "delivered", isRead = false),
        )

        val result = mergeLogic.merge(current, incoming)

        assertEquals(3, result.messages.size)
        val updated = result.messages.first { it.id == 2 }
        assertEquals("read", updated.deliveryStatus)
        assertTrue(updated.isRead)
        assertEquals(3, result.lastMessageId)
    }

    private fun message(
        id: Int,
        text: String,
        deliveryStatus: String = "sent",
        isRead: Boolean = false,
    ) = MessageItem(
        id = id,
        message = text,
        messageType = "text",
        sender = null,
        isRead = isRead,
        createdAt = "",
        formattedMessage = null,
        messageTypeDisplay = null,
        readAt = null,
        readReceipts = emptyMap(),
        deliveryStatus = deliveryStatus,
        deliveredAt = null,
        attachments = emptyList(),
        canEdit = false,
        canDelete = true,
        isDeleted = false,
        deletedAt = null,
        metadata = null,
    )
}

