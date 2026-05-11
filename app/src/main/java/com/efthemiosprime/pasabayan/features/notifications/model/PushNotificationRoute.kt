package com.efthemiosprime.pasabayan.features.notifications.model

/**
 * Route data extracted from an incoming FCM push payload (`remoteMessage.data` map).
 * Mirrors iOS `PushNotificationRoute` used by `AppDelegate.didReceive`.
 *
 * Every field is optional — only those keys present in the payload for the given
 * [NotificationType] will be populated.
 */
data class PushNotificationRoute(
    val notificationId: Int? = null,
    val type: NotificationType = NotificationType.UNKNOWN,
    val matchId: Int? = null,
    val conversationId: Int? = null,
    val transactionId: Int? = null,
    val ratingId: Int? = null,
    val screen: String? = null,
    val reason: String? = null,
    val recipientRole: String? = null,
    val amount: Double? = null,
    val tipAmount: Double? = null,
    val refundAmount: Double? = null,
    val currency: String? = null,
    val tipperName: String? = null,
    val data: NotificationData? = null,
) {
    companion object {
        /**
         * Parse a string→string FCM data map into a route. FCM payload values are always
         * strings, so int/double coercion happens here. Mirrors the iOS `intValue` /
         * `doubleValue` helpers.
         */
        fun fromPushData(data: Map<String, String>): PushNotificationRoute {
            val type = NotificationType.fromRaw(data["type"])
            return PushNotificationRoute(
                notificationId = data["notification_id"]?.toIntOrNull(),
                type = type,
                matchId = data["match_id"]?.toIntOrNull(),
                conversationId = data["conversation_id"]?.toIntOrNull(),
                transactionId = data["transaction_id"]?.toIntOrNull(),
                ratingId = data["rating_id"]?.toIntOrNull(),
                screen = data["screen"],
                reason = data["reason"],
                recipientRole = data["recipient_role"],
                amount = data["amount"]?.toDoubleOrNull(),
                tipAmount = data["tip_amount"]?.toDoubleOrNull(),
                refundAmount = data["refund_amount"]?.toDoubleOrNull(),
                currency = data["currency"],
                tipperName = data["tipper_name"],
                data = NotificationData(
                    matchId = data["match_id"]?.toIntOrNull(),
                    conversationId = data["conversation_id"]?.toIntOrNull(),
                    transactionId = data["transaction_id"]?.toIntOrNull(),
                    ratingId = data["rating_id"]?.toIntOrNull(),
                    newPrice = data["new_price"]?.toDoubleOrNull(),
                    originalPrice = data["original_price"]?.toDoubleOrNull(),
                    priceDifference = data["price_difference"]?.toDoubleOrNull(),
                    direction = data["direction"],
                    counterOffererName = data["counter_offerer_name"],
                    counterOffererId = data["counter_offerer_id"]?.toIntOrNull(),
                    counterOfferRound = data["counter_offer_round"]?.toIntOrNull(),
                    initiatedBy = data["initiated_by"],
                    remainingCounterOffers = data["remaining_counter_offers"]?.toIntOrNull(),
                    canCounterOffer = data["can_counter_offer"]?.toBooleanStrictOrNull(),
                ).takeIf { it != NotificationData() },
            )
        }
    }
}
