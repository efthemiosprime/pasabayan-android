package com.efthemiosprime.pasabayan.features.chat.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole
import com.efthemiosprime.pasabayan.features.chat.model.ConversationSummary

@Composable
fun MessagesTabScreen(currentRole: UserRole, currentUserId: Long) {
    var selectedConversation by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedStatus by rememberSaveable { mutableStateOf("active") }

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

