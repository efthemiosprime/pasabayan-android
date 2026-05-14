package com.efthemiosprime.pasabayan.features.chat.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.features.chat.model.ConversationSummary
import com.efthemiosprime.pasabayan.features.chat.model.LastMessage
import com.efthemiosprime.pasabayan.features.chat.model.ListItem
import com.efthemiosprime.pasabayan.features.chat.model.MessageItem
import com.efthemiosprime.pasabayan.features.chat.model.MessageMetadata
import com.efthemiosprime.pasabayan.features.chat.model.Participant
import com.efthemiosprime.pasabayan.features.chat.model.Sender
import com.efthemiosprime.pasabayan.features.chat.viewmodel.ChatThreadUiState
import com.efthemiosprime.pasabayan.features.chat.viewmodel.ConversationsUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatScreensTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun conversationsContent_showsParticipantName() {
        composeRule.setContent {
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
                                otherParticipant = Participant(2, "Carrier One", null, null),
                                matchInfo = null,
                                unreadCount = 1,
                                lastMessage = LastMessage(1, "hello", "text", "Carrier One", null),
                                lastMessageAt = null,
                                createdAt = null,
                            ),
                        ),
                    ),
                    onOpenConversation = {},
                    onLoadMore = {},
                    onRetryLoadMore = {},
                )
            }
        }

        composeRule.onNodeWithText("Carrier One").assertIsDisplayed()
    }

    @Test
    fun chatThreadContent_showsMessageAndComposeField() {
        composeRule.setContent {
            PasabayanTheme {
                ChatThreadContent(
                    state = ChatThreadUiState(
                        conversationId = 9,
                        messages = listOf(
                            MessageItem(
                                id = 1,
                                message = "Hello there",
                                messageType = "text",
                                sender = Sender(1, "User", null),
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
                    composerText = "",
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

        composeRule.onNodeWithText("Hello there").assertIsDisplayed()
    }

    @Test
    fun chatThreadContent_showsServiceListDetails() {
        composeRule.setContent {
            PasabayanTheme {
                ChatThreadContent(
                    state = ChatThreadUiState(
                        conversationId = 9,
                        messages = listOf(
                            MessageItem(
                                id = 10,
                                message = "List item",
                                messageType = "text",
                                sender = Sender(1, "User", null),
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
                                metadata = MessageMetadata(
                                    type = "service_list_item",
                                    listItemIndex = 1,
                                    listItemTotal = 2,
                                    listItem = ListItem(
                                        item = "Milk",
                                        quantity = 2,
                                        notes = "Low fat",
                                        noteUrls = listOf("https://example.com/milk"),
                                    ),
                                ),
                            ),
                        ),
                    ),
                    composerText = "",
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

        composeRule.onNodeWithText("Quantity: 2").assertIsDisplayed()
    }

    @Test
    fun chatThreadContent_showsReceiptUploadAction() {
        composeRule.setContent {
            PasabayanTheme {
                ChatThreadContent(
                    state = ChatThreadUiState(
                        conversationId = 9,
                        messages = listOf(
                            MessageItem(
                                id = 11,
                                message = "Upload please",
                                messageType = "text",
                                sender = Sender(1, "User", null),
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
                                metadata = MessageMetadata(
                                    type = "receipt_upload_prompt",
                                    listItemIndex = null,
                                    listItemTotal = null,
                                    listItem = null,
                                ),
                            ),
                        ),
                    ),
                    composerText = "",
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

        composeRule.onNodeWithText("Upload receipt").assertIsDisplayed()
    }
}

