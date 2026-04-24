package com.efthemiosprime.pasabayan.features.chat.services

import com.efthemiosprime.pasabayan.features.chat.model.MessageItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatMergeLogic @Inject constructor() {

    fun merge(current: List<MessageItem>, incoming: List<MessageItem>): MergeResult {
        if (incoming.isEmpty()) {
            return MergeResult(messages = current, lastMessageId = current.maxOfOrNull { it.id } ?: 0)
        }

        val currentMax = current.maxOfOrNull { it.id } ?: Int.MIN_VALUE
        val newById = incoming.filter { it.id > currentMax && it.id > 0 }
        val allIncomingAreNew = newById.size == incoming.size

        val merged = if (allIncomingAreNew) {
            (current + newById).sortedBy { it.id }
        } else {
            mergeSlowPath(current, incoming)
        }

        return MergeResult(
            messages = merged,
            lastMessageId = merged.maxOfOrNull { it.id } ?: 0,
        )
    }

    private fun mergeSlowPath(current: List<MessageItem>, incoming: List<MessageItem>): List<MessageItem> {
        val byId = LinkedHashMap<Int, MessageItem>(current.size + incoming.size)
        current.forEach { byId[it.id] = it }
        incoming.forEach { incomingMessage ->
            val existing = byId[incomingMessage.id]
            byId[incomingMessage.id] = if (existing == null) {
                incomingMessage
            } else {
                existing.copy(
                    isRead = incomingMessage.isRead,
                    readAt = incomingMessage.readAt,
                    readReceipts = incomingMessage.readReceipts,
                    deliveryStatus = incomingMessage.deliveryStatus,
                    deliveredAt = incomingMessage.deliveredAt,
                    isDeleted = incomingMessage.isDeleted,
                    deletedAt = incomingMessage.deletedAt,
                    metadata = incomingMessage.metadata ?: existing.metadata,
                    attachments = if (incomingMessage.attachments.isEmpty()) {
                        existing.attachments
                    } else {
                        incomingMessage.attachments
                    },
                    message = incomingMessage.message.ifBlank { existing.message },
                )
            }
        }
        return byId.values.sortedBy { it.id }
    }
}

data class MergeResult(
    val messages: List<MessageItem>,
    val lastMessageId: Int,
)

