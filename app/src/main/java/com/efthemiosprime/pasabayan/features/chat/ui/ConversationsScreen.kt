package com.efthemiosprime.pasabayan.features.chat.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PCircularProgress
import com.efthemiosprime.pasabayan.features.chat.components.ConversationRow
import com.efthemiosprime.pasabayan.features.chat.model.ConversationSummary
import com.efthemiosprime.pasabayan.features.chat.model.LastMessage
import com.efthemiosprime.pasabayan.features.chat.model.Participant
import com.efthemiosprime.pasabayan.features.chat.viewmodel.ConversationsUiState
import com.efthemiosprime.pasabayan.features.chat.viewmodel.ConversationsViewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * Conversations list — iOS parity with `ConversationsView.swift`. The backend returns
 * every conversation the user participates in; each [ConversationSummary.userRole]
 * carries which role the user was acting in for that thread (rendered as a per-row
 * badge in [ConversationRow]). No client-side role / status / unread filters — iOS
 * does not have them and they were artificially hiding conversations on Android.
 *
 * Mirrors iOS `ChatViewModel.loadNextConversationsPage()`: when the user scrolls
 * within 3 rows of the loaded set, the next page is requested automatically.
 */
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
        onLoadMore = viewModel::loadNextConversationsPage,
        onRetryLoadMore = viewModel::retryLoadMoreConversations,
        modifier = modifier,
    )
}

@Composable
fun ConversationsContent(
    state: ConversationsUiState,
    onOpenConversation: (ConversationSummary) -> Unit,
    onLoadMore: () -> Unit,
    onRetryLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        !state.alertMessage.isNullOrBlank() && state.conversations.isEmpty() && !state.isLoading ->
            Text(
                text = state.alertMessage,
                modifier = modifier
                    .fillMaxSize()
                    .padding(PasabayanSpacing.lg),
            )

        state.conversations.isEmpty() && !state.isLoading ->
            Text(
                text = stringResource(R.string.chat_conversations_empty),
                modifier = modifier
                    .fillMaxSize()
                    .padding(PasabayanSpacing.lg),
            )

        else -> ConversationsList(
            state = state,
            onOpenConversation = onOpenConversation,
            onLoadMore = onLoadMore,
            onRetryLoadMore = onRetryLoadMore,
            modifier = modifier,
        )
    }
}

@Composable
private fun ConversationsList(
    state: ConversationsUiState,
    onOpenConversation: (ConversationSummary) -> Unit,
    onLoadMore: () -> Unit,
    onRetryLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                ?: return@derivedStateOf false
            val total = listState.layoutInfo.totalItemsCount
            state.hasMore && !state.isLoadingMore && state.loadMoreError == null && last >= total - 3
        }
    }
    LaunchedEffect(listState) {
        snapshotFlow { shouldLoadMore }.collectLatest { if (it) onLoadMore() }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(PasabayanSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
    ) {
        items(state.conversations, key = { it.id }) { conversation ->
            ConversationRow(
                conversation = conversation,
                onClick = { onOpenConversation(conversation) },
            )
        }
        if (state.isLoadingMore) {
            item(key = "load-more-spinner") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = PasabayanSpacing.sm),
                    contentAlignment = Alignment.Center,
                ) {
                    PCircularProgress()
                }
            }
        }
        if (state.loadMoreError != null) {
            item(key = "load-more-error") {
                LoadMoreErrorRow(
                    message = state.loadMoreError,
                    onRetry = onRetryLoadMore,
                )
            }
        }
    }
}

@Composable
private fun LoadMoreErrorRow(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = PasabayanSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.chat_conversations_load_more_error),
            style = PasabayanTextStyles.Body.small,
            color = MaterialTheme.colorScheme.error,
        )
        Text(
            text = message,
            style = PasabayanTextStyles.Caption.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        PButton(
            text = stringResource(R.string.chat_conversations_load_more_retry),
            onClick = onRetry,
        )
    }
}

private fun sampleConversation(id: Int, unread: Int = 0) = ConversationSummary(
    id = id,
    matchId = id * 10,
    status = "active",
    statusDisplay = "Active",
    userRole = "shipper",
    otherParticipant = Participant(id + 100, "Carrier $id", null, "verified"),
    matchInfo = null,
    unreadCount = unread,
    lastMessage = LastMessage(
        id = id,
        message = "Hi there",
        messageType = "text",
        senderName = "Carrier $id",
        createdAt = null,
    ),
    lastMessageAt = null,
    createdAt = null,
)

@Preview(showBackground = true, name = "Conversations — list")
@Preview(showBackground = true, name = "Conversations — list (dark)", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ConversationsContentPreview() {
    PasabayanTheme {
        ConversationsContent(
            state = ConversationsUiState(
                conversations = listOf(sampleConversation(1, unread = 1), sampleConversation(2)),
                currentPage = 1,
                lastPage = 3,
            ),
            onOpenConversation = {},
            onLoadMore = {},
            onRetryLoadMore = {},
        )
    }
}

@Preview(showBackground = true, name = "Conversations — loading more")
@Preview(showBackground = true, name = "Conversations — loading more (dark)", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ConversationsContentLoadingMorePreview() {
    PasabayanTheme {
        ConversationsContent(
            state = ConversationsUiState(
                conversations = (1..3).map { sampleConversation(it) },
                currentPage = 1,
                lastPage = 4,
                isLoadingMore = true,
            ),
            onOpenConversation = {},
            onLoadMore = {},
            onRetryLoadMore = {},
        )
    }
}

@Preview(showBackground = true, name = "Conversations — load more error")
@Preview(showBackground = true, name = "Conversations — load more error (dark)", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ConversationsContentLoadMoreErrorPreview() {
    PasabayanTheme {
        ConversationsContent(
            state = ConversationsUiState(
                conversations = (1..3).map { sampleConversation(it) },
                currentPage = 1,
                lastPage = 4,
                loadMoreError = "Network unavailable",
            ),
            onOpenConversation = {},
            onLoadMore = {},
            onRetryLoadMore = {},
        )
    }
}

@Preview(showBackground = true, name = "Conversations — empty")
@Composable
private fun ConversationsContentEmptyPreview() {
    PasabayanTheme {
        ConversationsContent(
            state = ConversationsUiState(),
            onOpenConversation = {},
            onLoadMore = {},
            onRetryLoadMore = {},
        )
    }
}
