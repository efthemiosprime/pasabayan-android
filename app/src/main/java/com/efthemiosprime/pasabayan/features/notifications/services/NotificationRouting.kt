package com.efthemiosprime.pasabayan.features.notifications.services

import com.efthemiosprime.pasabayan.features.notifications.model.NotificationData
import com.efthemiosprime.pasabayan.features.notifications.model.NotificationType

/**
 * Pure routing mapping per spec
 * [08-notifications-device-tokens.md] § "Push notification routing".
 *
 * Returns `null` when the type carries no actionable navigation (e.g. `test`) or when a
 * required identifier (`matchId`, `conversationId`, …) is missing from the payload.
 */
object NotificationRouting {

    fun resolve(type: NotificationType, data: NotificationData?): NavigationEvent? = when (type) {
        NotificationType.MATCH_REQUEST,
        NotificationType.MATCH_ACCEPTED,
        NotificationType.MATCH_DECLINED,
        NotificationType.MATCH_AUTO_CANCELLED ->
            data?.matchId?.let(NavigationEvent::OpenMatch)

        NotificationType.DELIVERY_PICKED_UP,
        NotificationType.DELIVERY_IN_TRANSIT,
        NotificationType.DELIVERY_DELIVERED ->
            data?.matchId?.let(NavigationEvent::OpenMatch) ?: NavigationEvent.OpenMatchesTab

        NotificationType.CHAT_MESSAGE ->
            data?.conversationId?.let(NavigationEvent::OpenConversation)
                ?: NavigationEvent.OpenConversations

        NotificationType.PAYMENT_RECEIVED,
        NotificationType.PAYMENT_RELEASED,
        NotificationType.PAYOUT_COMPLETED,
        NotificationType.PAYOUT_SCHEDULED,
        NotificationType.REFUND_PROCESSED ->
            NavigationEvent.OpenTransactions

        NotificationType.TIP_RECEIVED ->
            data?.transactionId?.let(NavigationEvent::OpenTransactionDetail)
                ?: NavigationEvent.OpenTransactions

        NotificationType.PAYMENT_AUTO_CHARGE_FAILED ->
            NavigationEvent.OpenPaymentMethods

        NotificationType.RATING_RECEIVED ->
            NavigationEvent.OpenRatings

        NotificationType.PREMIUM_APPROVED,
        NotificationType.PREMIUM_REJECTED ->
            NavigationEvent.OpenProfileVerification

        NotificationType.COUNTER_OFFER ->
            data?.matchId?.let(NavigationEvent::OpenCounterOffer)

        NotificationType.TEST,
        NotificationType.UNKNOWN -> null
    }
}
