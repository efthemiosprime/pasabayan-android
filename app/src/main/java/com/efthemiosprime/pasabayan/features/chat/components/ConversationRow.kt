package com.efthemiosprime.pasabayan.features.chat.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.features.chat.model.ConversationSummary
import com.efthemiosprime.pasabayan.features.chat.model.LastMessage
import com.efthemiosprime.pasabayan.features.chat.model.MatchInfo
import com.efthemiosprime.pasabayan.features.chat.model.Participant
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ConversationRow(
    conversation: ConversationSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
            verticalAlignment = Alignment.Top,
        ) {
            AvatarBadge(name = conversation.otherParticipant.name)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = conversation.otherParticipant.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (!conversation.otherParticipant.verificationLevel.isNullOrBlank()) {
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = stringResource(R.string.chat_conversation_verified),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Text(
                        text = formatTimestamp(conversation.lastMessageAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = conversation.matchInfo?.route ?: stringResource(R.string.chat_conversations_unknown_route),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (!conversation.statusDisplay.isBlank()) {
                    Text(
                        text = conversation.statusDisplay,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                if (!conversation.matchInfo?.packageDescription.isNullOrBlank()) {
                    Text(
                        text = conversation.matchInfo?.packageDescription.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = conversation.lastMessage?.message
                        ?: stringResource(R.string.chat_conversations_no_messages_yet),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (conversation.unreadCount > 0) {
                Badge(modifier = Modifier.padding(start = PasabayanSpacing.sm)) {
                    Text(conversation.unreadCount.toString())
                }
            }
        }
    }
}

@Composable
private fun AvatarBadge(name: String) {
    Box(
        modifier = Modifier
            .size(PasabayanSpacing.xxxl)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials(name),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

private fun initials(name: String): String {
    val pieces = name.split(" ").filter { it.isNotBlank() }
    if (pieces.isEmpty()) return "?"
    val first = pieces.first().first().uppercase()
    val second = pieces.drop(1).firstOrNull()?.first()?.uppercase() ?: ""
    return first + second
}

private fun formatTimestamp(raw: String?): String {
    if (raw.isNullOrBlank()) {
        return ""
    }
    return runCatching {
        val parsed = Instant.parse(raw)
        DateTimeFormatter.ofPattern("MMM d, HH:mm")
            .withZone(ZoneId.systemDefault())
            .format(parsed)
    }.getOrElse { raw }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ConversationRowPreview() {
    PasabayanTheme {
        ConversationRow(
            conversation = ConversationSummary(
                id = 1,
                matchId = 10,
                status = "active",
                statusDisplay = "Active",
                userRole = "shipper",
                otherParticipant = Participant(
                    id = 2,
                    name = "Carrier One",
                    avatar = null,
                    verificationLevel = "verified",
                ),
                matchInfo = MatchInfo(
                    route = "Toronto -> Vancouver",
                    packageDescription = "Laptop",
                    tripRoute = "YYZ-YVR",
                    departureDate = "2026-04-24",
                ),
                unreadCount = 2,
                lastMessage = LastMessage(
                    id = 3,
                    message = "On my way",
                    messageType = "text",
                    senderName = "Carrier One",
                    createdAt = "2026-04-24T10:00:00Z",
                ),
                lastMessageAt = "2026-04-24T10:00:00Z",
                createdAt = "2026-04-20T10:00:00Z",
            ),
            onClick = {},
        )
    }
}

