package com.efthemiosprime.pasabayan.features.chat.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PScaffold
import com.efthemiosprime.pasabayan.core.designsystem.component.PTopBar
import com.efthemiosprime.pasabayan.features.chat.components.ConversationRow
import com.efthemiosprime.pasabayan.features.chat.model.ConversationSummary
import com.efthemiosprime.pasabayan.features.chat.model.LastMessage
import com.efthemiosprime.pasabayan.features.chat.model.Participant
import com.efthemiosprime.pasabayan.features.chat.viewmodel.ConversationsUiState
import com.efthemiosprime.pasabayan.features.chat.viewmodel.ConversationsViewModel

/**
 * Conversations list — iOS parity with `ConversationsView.swift`. The backend returns
 * every conversation the user participates in; each [ConversationSummary.userRole]
 * carries which role the user was acting in for that thread (rendered as a per-row
 * badge in [ConversationRow]). No client-side role / status / unread filters — iOS
 * does not have them and they were artificially hiding conversations on Android.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsScreen(
    onOpenConversation: (ConversationSummary) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConversationsViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(Unit) {
        viewModel.loadConversations()
    }

    ConversationsContent(
        state = state,
        onOpenConversation = {
            viewModel.markConversationOpened(it.id)
            onOpenConversation(it)
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsContent(
    state: ConversationsUiState,
    onOpenConversation: (ConversationSummary) -> Unit,
    modifier: Modifier = Modifier,
) {
    PScaffold(
        modifier = modifier,
        topBar = {
            PTopBar(title = stringResource(R.string.chat_conversations_title))
        },
    ) { innerPadding ->
        if (!state.alertMessage.isNullOrBlank() && state.conversations.isEmpty() && !state.isLoading) {
            Text(
                text = state.alertMessage,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(PasabayanSpacing.lg),
            )
            return@PScaffold
        }

        if (state.conversations.isEmpty() && !state.isLoading) {
            Text(
                text = stringResource(R.string.chat_conversations_empty),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(PasabayanSpacing.lg),
            )
            return@PScaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(PasabayanSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
        ) {
            items(state.conversations, key = { it.id }) { conversation ->
                ConversationRow(
                    conversation = conversation,
                    onClick = { onOpenConversation(conversation) },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ConversationsContentPreview() {
    PasabayanTheme {
        ConversationsContent(
            state = ConversationsUiState(
                conversations = listOf(
                    ConversationSummary(
                        id = 1,
                        matchId = 10,
                        status = "active",
                        statusDisplay = "Active",
                        userRole = "shipper",
                        otherParticipant = Participant(2, "Carrier One", null, "verified"),
                        matchInfo = null,
                        unreadCount = 1,
                        lastMessage = LastMessage(
                            id = 1,
                            message = "Hi there",
                            messageType = "text",
                            senderName = "Carrier One",
                            createdAt = null,
                        ),
                        lastMessageAt = null,
                        createdAt = null,
                    ),
                ),
            ),
            onOpenConversation = {},
        )
    }
}

