package com.efthemiosprime.pasabayan.features.notifications.services

/**
 * Navigation events emitted by [NotificationRouter] in response to a push tap or an in-app
 * notification-card tap. The Compose host observes the [NotificationRouter.events] flow and
 * navigates accordingly. Mirrors the iOS `NotificationCenter` events
 * `.notificationRouted` / `.navigateFromNotification`.
 */
sealed interface NavigationEvent {
    data class OpenMatch(val matchId: Int) : NavigationEvent
    data object OpenMatchesTab : NavigationEvent
    data class OpenConversation(val conversationId: Int) : NavigationEvent
    data object OpenConversations : NavigationEvent
    data object OpenTransactions : NavigationEvent
    data class OpenTransactionDetail(val transactionId: Int) : NavigationEvent
    data object OpenPaymentMethods : NavigationEvent
    data object OpenRatings : NavigationEvent
    data object OpenProfileVerification : NavigationEvent
    data class OpenCounterOffer(val matchId: Int) : NavigationEvent
}
