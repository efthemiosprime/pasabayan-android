package com.efthemiosprime.pasabayan.features.chat.model

data class ConversationSummary(
    val id: Int,
    val matchId: Int?,
    val status: String,
    val statusDisplay: String,
    val userRole: String?,
    val otherParticipant: Participant,
    val matchInfo: MatchInfo?,
    val unreadCount: Int,
    val lastMessage: LastMessage?,
    val lastMessageAt: String?,
    val createdAt: String?,
) {
    val hasUnreadMessages: Boolean
        get() = unreadCount > 0
}

data class Participant(
    val id: Int,
    val name: String,
    val avatar: String?,
    val verificationLevel: String?,
)

data class MatchInfo(
    val route: String?,
    val packageDescription: String?,
    val tripRoute: String?,
    val departureDate: String?,
)

data class LastMessage(
    val id: Int,
    val message: String,
    val messageType: String,
    val senderName: String?,
    val createdAt: String?,
    /** Server-side flag for the last-message preview in the conversations list. */
    val isSystemMessage: Boolean = false,
)

