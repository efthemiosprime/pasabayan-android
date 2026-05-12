package com.efthemiosprime.pasabayan.features.chat.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonSize
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PModalBottomSheet
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField
import com.efthemiosprime.pasabayan.core.designsystem.component.PScaffold
import com.efthemiosprime.pasabayan.core.designsystem.component.PTopBar
import com.efthemiosprime.pasabayan.features.chat.components.ChatMessageBubble
import com.efthemiosprime.pasabayan.features.chat.model.MessageItem
import com.efthemiosprime.pasabayan.features.chat.model.Sender
import com.efthemiosprime.pasabayan.features.chat.viewmodel.ChatThreadUiState
import com.efthemiosprime.pasabayan.features.chat.viewmodel.ChatThreadViewModel
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatThreadScreen(
    conversationId: Int,
    currentUserId: Long,
    status: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatThreadViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    var composerText by remember { mutableStateOf("") }
    var showReceiptUploadSheet by remember { mutableStateOf(false) }

    LaunchedEffect(conversationId) {
        viewModel.openConversation(conversationId, status)
    }

    ChatThreadContent(
        state = state,
        composerText = composerText,
        onComposerTextChange = { composerText = it },
        onSend = {
            if (composerText.isNotBlank()) {
                viewModel.sendMessage(composerText)
                composerText = ""
            }
        },
        onRetrySend = viewModel::retrySend,
        onBack = {
            viewModel.closeConversation()
            onBack()
        },
        onLoadMore = viewModel::loadMoreMessages,
        onDeleteMessage = viewModel::deleteMessage,
        isFailed = viewModel::isFailed,
        onMessageVisible = viewModel::markMessageRead,
        onReceiptUploadClick = { showReceiptUploadSheet = true },
        currentUserId = currentUserId,
        modifier = modifier,
    )
    if (showReceiptUploadSheet) {
        PModalBottomSheet(onDismissRequest = { showReceiptUploadSheet = false }) {
            ChatReceiptUploadSheet(
                onUploadFromCamera = {
                    showReceiptUploadSheet = false
                    viewModel.refreshConversation()
                },
                onUploadFromGallery = {
                    showReceiptUploadSheet = false
                    viewModel.refreshConversation()
                },
                onClose = { showReceiptUploadSheet = false },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatThreadContent(
    state: ChatThreadUiState,
    composerText: String,
    onComposerTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onRetrySend: (Int) -> Unit,
    onBack: () -> Unit,
    onLoadMore: () -> Unit,
    onDeleteMessage: (Int) -> Unit,
    isFailed: (MessageItem) -> Boolean,
    onMessageVisible: (Int) -> Unit,
    onReceiptUploadClick: () -> Unit,
    currentUserId: Long,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(listState, state.nextPage, state.isPaging) {
        snapshotFlow { listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0 }
            .distinctUntilChanged()
            .collect { isAtTop ->
                if (isAtTop && state.nextPage != null && !state.isPaging) {
                    onLoadMore()
                }
            }
    }

    PScaffold(
        modifier = modifier,
        topBar = {
            PTopBar(
                title = stringResource(R.string.chat_thread_title),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.chat_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = PasabayanSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
            ) {
                items(state.messages, key = { it.id }) { message ->
                    // Prefer the server-authoritative `sender.isMe`; fall back to
                    // optimistic-id / sender-id / canDelete heuristics for temp
                    // messages and older payloads that didn't carry the flag.
                    val isOwnMessage = message.sender?.isMe == true ||
                        message.id < 0 ||
                        message.sender?.id?.toLong() == currentUserId ||
                        message.canDelete
                    ChatMessageBubble(
                        message = message,
                        isOwnMessage = isOwnMessage,
                        isFailed = isFailed(message),
                        onRetry = { onRetrySend(message.id) },
                        onReceiptUploadClick = onReceiptUploadClick,
                    )
                    if (!isOwnMessage && !message.isSystemMessage && !message.isRead) {
                        LaunchedEffect(message.id) {
                            onMessageVisible(message.id)
                        }
                    }
                    if (isOwnMessage && !message.isDeleted) {
                        PButton(
                            text = stringResource(R.string.chat_delete_message),
                            onClick = { onDeleteMessage(message.id) },
                            style = PButtonStyle.Tertiary,
                            size = PButtonSize.Small,
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(PasabayanSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
            ) {
                POutlinedTextField(
                    value = composerText,
                    onValueChange = onComposerTextChange,
                    modifier = Modifier.weight(1f),
                    label = { Text(text = stringResource(R.string.chat_compose_label)) },
                    enabled = state.isComposerEnabled,
                )
                IconButton(
                    onClick = onSend,
                    enabled = state.isComposerEnabled && composerText.isNotBlank(),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(R.string.chat_send),
                    )
                }
            }
            if (!state.isComposerEnabled) {
                Text(
                    text = stringResource(R.string.chat_composer_disabled),
                    modifier = Modifier.padding(
                        horizontal = PasabayanSpacing.md,
                        vertical = PasabayanSpacing.sm,
                    ),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ChatThreadContentPreview() {
    PasabayanTheme {
        ChatThreadContent(
            state = ChatThreadUiState(
                conversationId = 10,
                messages = listOf(
                    MessageItem(
                        id = 1,
                        message = "Hello",
                        messageType = "text",
                        sender = Sender(1, "Me", null),
                        isRead = false,
                        createdAt = "",
                        formattedMessage = null,
                        messageTypeDisplay = null,
                        readAt = null,
                        readReceipts = emptyMap(),
                        deliveryStatus = "sent",
                        deliveredAt = null,
                        attachments = emptyList(),
                        canEdit = false,
                        canDelete = true,
                        isDeleted = false,
                        deletedAt = null,
                        metadata = null,
                    ),
                ),
            ),
            composerText = "Draft message",
            onComposerTextChange = {},
            onSend = {},
            onRetrySend = {},
            onBack = {},
            onLoadMore = {},
            onDeleteMessage = {},
            isFailed = { false },
            onMessageVisible = {},
            onReceiptUploadClick = {},
            currentUserId = 1L,
        )
    }
}

