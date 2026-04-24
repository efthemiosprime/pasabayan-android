package com.efthemiosprime.pasabayan.features.chat.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsScreen(
    currentRole: UserRole,
    onOpenConversation: (ConversationSummary) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConversationsViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val roleFilter = when (currentRole) {
        UserRole.SHIPPER -> "shipper"
        UserRole.CARRIER -> "carrier"
    }
    val (statusFilter, setStatusFilter) = remember { mutableStateOf<String?>(null) }
    val (unreadOnly, setUnreadOnly) = remember { mutableStateOf(false) }

    LaunchedEffect(roleFilter, statusFilter, unreadOnly) {
        viewModel.loadConversations(
            role = roleFilter,
            status = statusFilter,
            unreadOnly = if (unreadOnly) true else null,
        )
    }
    ConversationsContent(
        state = state,
        statusFilter = statusFilter,
        unreadOnly = unreadOnly,
        onStatusFilterChange = setStatusFilter,
        onUnreadOnlyChange = setUnreadOnly,
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
    statusFilter: String?,
    unreadOnly: Boolean,
    onStatusFilterChange: (String?) -> Unit,
    onUnreadOnlyChange: (Boolean) -> Unit,
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
            item {
                FilterBar(
                    statusFilter = statusFilter,
                    unreadOnly = unreadOnly,
                    onStatusFilterChange = onStatusFilterChange,
                    onUnreadOnlyChange = onUnreadOnlyChange,
                )
            }
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
            statusFilter = null,
            unreadOnly = false,
            onStatusFilterChange = {},
            onUnreadOnlyChange = {},
            onOpenConversation = {},
        )
    }
}

@Composable
private fun FilterBar(
    statusFilter: String?,
    unreadOnly: Boolean,
    onStatusFilterChange: (String?) -> Unit,
    onUnreadOnlyChange: (Boolean) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
        FilterChip(
            selected = statusFilter == null,
            onClick = { onStatusFilterChange(null) },
            label = { Text(stringResource(R.string.chat_filters_all_statuses)) },
        )
        FilterChip(
            selected = statusFilter == "active",
            onClick = { onStatusFilterChange("active") },
            label = { Text(stringResource(R.string.chat_filters_status_active)) },
        )
        FilterChip(
            selected = statusFilter == "closed",
            onClick = { onStatusFilterChange("closed") },
            label = { Text(stringResource(R.string.chat_filters_status_closed)) },
        )
        FilterChip(
            selected = unreadOnly,
            onClick = { onUnreadOnlyChange(!unreadOnly) },
            label = { Text(stringResource(R.string.chat_filters_unread_only)) },
        )
    }
}

