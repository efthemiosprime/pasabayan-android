package com.efthemiosprime.pasabayan.features.chat.model

import com.efthemiosprime.pasabayan.core.network.chat.AttachmentJson
import com.efthemiosprime.pasabayan.core.network.chat.ConversationSummaryJson
import com.efthemiosprime.pasabayan.core.network.chat.LastMessageJson
import com.efthemiosprime.pasabayan.core.network.chat.ListItemJson
import com.efthemiosprime.pasabayan.core.network.chat.MatchInfoJson
import com.efthemiosprime.pasabayan.core.network.chat.MessageItemJson
import com.efthemiosprime.pasabayan.core.network.chat.MessageMetadataJson
import com.efthemiosprime.pasabayan.core.network.chat.ParticipantJson
import com.efthemiosprime.pasabayan.core.network.chat.ReverbConfigJson
import com.efthemiosprime.pasabayan.core.network.chat.SenderJson

fun ConversationSummaryJson.toDomain(): ConversationSummary = ConversationSummary(
    id = id,
    matchId = matchId,
    status = status,
    statusDisplay = statusDisplay,
    userRole = userRole,
    otherParticipant = otherParticipant?.toDomain() ?: Participant(
        id = 0,
        name = "",
        avatar = null,
        verificationLevel = null,
    ),
    matchInfo = matchInfo?.toDomain(),
    unreadCount = unreadCount,
    lastMessage = lastMessage?.toDomain(),
    lastMessageAt = lastMessageAt,
    createdAt = createdAt,
)

fun ParticipantJson.toDomain(): Participant = Participant(
    id = id,
    name = name,
    avatar = avatar,
    verificationLevel = verificationLevel,
)

fun MatchInfoJson.toDomain(): MatchInfo = MatchInfo(
    route = route,
    packageDescription = packageDescription,
    tripRoute = tripRoute,
    departureDate = departureDate,
)

fun LastMessageJson.toDomain(): LastMessage = LastMessage(
    id = id,
    message = message,
    messageType = messageType,
    senderName = senderName,
    createdAt = createdAt,
)

fun MessageItemJson.toDomain(): MessageItem = MessageItem(
    id = id,
    message = message,
    messageType = messageType,
    sender = sender?.toDomain(),
    isRead = isRead,
    createdAt = createdAt,
    formattedMessage = formattedMessage,
    messageTypeDisplay = messageTypeDisplay,
    readAt = readAt,
    readReceipts = readReceipts,
    deliveryStatus = deliveryStatus,
    deliveredAt = deliveredAt,
    attachments = attachments.map { it.toDomain() },
    canEdit = canEdit,
    canDelete = canDelete,
    isDeleted = isDeleted,
    deletedAt = deletedAt,
    metadata = metadata?.toDomain(),
)

fun SenderJson.toDomain(): Sender = Sender(id = id, name = name, avatar = avatar)

fun AttachmentJson.toDomain(): Attachment = Attachment(id = id, url = url, type = type)

fun MessageMetadataJson.toDomain(): MessageMetadata = MessageMetadata(
    type = type,
    listItemIndex = listItemIndex,
    listItemTotal = listItemTotal,
    listItem = listItem?.toDomain(),
)

fun ListItemJson.toDomain(): ListItem = ListItem(
    item = item,
    quantity = quantity,
    notes = notes,
    noteUrls = noteUrls,
)

fun ReverbConfigJson.toDomain(): ReverbConfig = ReverbConfig(
    key = key,
    host = host,
    port = port,
    scheme = scheme,
)

