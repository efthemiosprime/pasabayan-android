package com.efthemiosprime.pasabayan.features.chat.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.efthemiosprime.pasabayan.features.chat.model.ConversationSummary

@Composable
fun MessagesTabScreen(
    currentUserId: Long,
    initialConversationId: Int? = null,
    onInitialConversationConsumed: () -> Unit = {},
) {
    var selectedConversation by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedStatus by rememberSaveable { mutableStateOf("active") }
    // Captured alongside the conversation id so ChatThreadScreen can gate the
    // receipt upload (carrier) and the shipper-side inline receipt card.
    // Stays null on push-deep-link entries that don't carry a ConversationSummary
    // — receipt UI is hidden in that case until the user re-opens via the list.
    var selectedMatchId by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedIsShipperViewer by rememberSaveable { mutableStateOf(false) }

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
            onOpenConversation = { conversation: ConversationSummary ->
                selectedConversation = conversation.id
                selectedStatus = conversation.status
                selectedMatchId = conversation.matchId
                selectedIsShipperViewer = conversation.userRole.equals("shipper", ignoreCase = true)
            },
        )
    } else {
        ChatThreadScreen(
            conversationId = selectedConversation ?: return,
            currentUserId = currentUserId,
            status = selectedStatus,
            onBack = { selectedConversation = null },
            matchId = selectedMatchId,
            isShipperViewer = selectedIsShipperViewer,
        )
    }
}

