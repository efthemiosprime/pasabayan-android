package com.efthemiosprime.pasabayan.features.chat.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.features.chat.model.MessageItem
import com.efthemiosprime.pasabayan.features.chat.model.MessageMetadata
import com.efthemiosprime.pasabayan.features.chat.model.Sender

@Composable
fun ChatMessageBubble(
    message: MessageItem,
    isOwnMessage: Boolean,
    isFailed: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
    ) {
        PCard {
            if (message.isDeleted) {
                Text(
                    text = stringResource(R.string.chat_message_deleted_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else if (message.messageType == "system") {
                Text(
                    text = message.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                Text(
                    text = message.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                val metadataType = message.metadata?.type
                if (metadataType == "receipt_upload_prompt") {
                    Text(
                        text = stringResource(R.string.chat_message_receipt_prompt),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                if (metadataType == "service_list_item") {
                    Text(
                        text = stringResource(R.string.chat_message_service_list_item_label),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
        if (isFailed) {
            Text(
                text = stringResource(R.string.chat_message_failed_retry),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = PasabayanSpacing.sm),
            )
            androidx.compose.material3.TextButton(onClick = onRetry) {
                Text(text = stringResource(R.string.chat_message_retry))
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ChatMessageBubblePreview() {
    PasabayanTheme {
        ChatMessageBubble(
            message = MessageItem(
                id = 1,
                message = "Hello",
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
                    listItem = null,
                ),
            ),
            isOwnMessage = true,
            isFailed = false,
            onRetry = {},
        )
    }
}

