package com.efthemiosprime.pasabayan.features.chat.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
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
    onReceiptUploadClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start
    val deliveryLabel = deliveryLabel(message = message, isOwnMessage = isOwnMessage)
    val deliveryIcon = deliveryIcon(message = message, isOwnMessage = isOwnMessage)
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
    ) {
        if (message.isSystemMessage) {
            Text(
                text = message.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PasabayanSpacing.sm),
            )
        } else {
            PCard {
                if (message.isDeleted) {
                    Text(
                        text = stringResource(R.string.chat_message_deleted_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    Text(
                        text = message.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    renderSpecialMessageContent(
                        message = message,
                        onReceiptUploadClick = onReceiptUploadClick,
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = PasabayanSpacing.sm),
            ) {
                Icon(
                    imageVector = deliveryIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = deliveryLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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

@Composable
private fun renderSpecialMessageContent(
    message: MessageItem,
    onReceiptUploadClick: () -> Unit,
) {
    when (message.metadata?.type) {
        "receipt_upload_prompt" -> {
            Text(
                text = stringResource(R.string.chat_message_receipt_prompt),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = PasabayanSpacing.xs),
            )
            androidx.compose.material3.TextButton(onClick = onReceiptUploadClick) {
                Text(text = stringResource(R.string.chat_message_receipt_prompt_action))
            }
        }

        "service_list_item" -> {
            val listItem = message.metadata.listItem
            Text(
                text = stringResource(R.string.chat_message_service_list_item_label),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = PasabayanSpacing.xs),
            )
            if (listItem != null) {
                Text(
                    text = stringResource(R.string.chat_message_service_item, listItem.item),
                    style = MaterialTheme.typography.bodySmall,
                )
                listItem.quantity?.let {
                    Text(
                        text = stringResource(R.string.chat_message_service_quantity, it),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (!listItem.notes.isNullOrBlank()) {
                    Text(
                        text = stringResource(R.string.chat_message_service_notes, listItem.notes),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                listItem.noteUrls.forEach { url ->
                    Text(
                        text = stringResource(R.string.chat_message_service_note_url, url),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun deliveryLabel(message: MessageItem, isOwnMessage: Boolean): String {
    if (isOwnMessage) {
        return when (message.deliveryStatus) {
            "read" -> stringResource(R.string.chat_message_status_read)
            "delivered" -> stringResource(R.string.chat_message_status_delivered)
            else -> stringResource(R.string.chat_message_status_sent)
        }
    }
    return if (message.isRead || message.deliveryStatus == "read") {
        stringResource(R.string.chat_message_status_read_by_you)
    } else {
        stringResource(R.string.chat_message_status_unread)
    }
}

private fun deliveryIcon(
    message: MessageItem,
    isOwnMessage: Boolean,
): androidx.compose.ui.graphics.vector.ImageVector {
    if (isOwnMessage) {
        return when (message.deliveryStatus) {
            "read" -> Icons.Filled.DoneAll
            "delivered" -> Icons.Filled.CheckCircle
            else -> Icons.Filled.Check
        }
    }
    return if (message.isRead || message.deliveryStatus == "read") {
        Icons.Filled.CheckCircle
    } else {
        Icons.Filled.RadioButtonUnchecked
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
            onReceiptUploadClick = {},
        )
    }
}

