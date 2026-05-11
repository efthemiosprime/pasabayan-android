package com.efthemiosprime.pasabayan.features.chat.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole
import com.efthemiosprime.pasabayan.features.chat.model.ConversationSummary

@Composable
fun MessagesTabScreen(
    currentRole: UserRole,
    currentUserId: Long,
    initialConversationId: Int? = null,
    onInitialConversationConsumed: () -> Unit = {},
) {
    var selectedConversation by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedStatus by rememberSaveable { mutableStateOf("active") }

    // iOS parity: when the host requests a specific conversation (e.g. user tapped the chat
    // pill inside trip details), open it directly. Status defaults to "active" since the host
    // doesn't always know the conversation's archive status.
    androidx.compose.runtime.LaunchedEffect(initialConversationId) {
        if (initialConversationId != null) {
            selectedConversation = initialConversationId
            selectedStatus = "active"
            onInitialConversationConsumed()
        }
    }

    if (selectedConversation == null) {
        ConversationsScreen(
            currentRole = currentRole,
            onOpenConversation = { conversation: ConversationSummary ->
                selectedConversation = conversation.id
                selectedStatus = conversation.status
            },
        )
    } else {
        ChatThreadScreen(
            conversationId = selectedConversation ?: return,
            currentUserId = currentUserId,
            status = selectedStatus,
            onBack = { selectedConversation = null },
        )
    }
}

