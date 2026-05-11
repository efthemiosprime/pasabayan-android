package com.efthemiosprime.pasabayan.core.network.notifications

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wire shape for a single push notification entry. Mirrors iOS `PushNotification` struct in
 * `NotificationModels.swift`. All fields snake_case on the wire.
 */
@Serializable
data class PushNotificationJson(
    val id: Int = 0,
    val title: String = "",
    val body: String = "",
    val type: String = "",
    val data: NotificationDataJson? = null,
    @SerialName("recipient_role") val recipientRole: String? = null,
    @SerialName("sent_at") val sentAt: String = "",
    @SerialName("read_at") val readAt: String? = null,
    @SerialName("clicked_at") val clickedAt: String? = null,
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("created_at") val createdAt: String = "",
)

/**
 * Optional nested data payload carried by [PushNotificationJson] and by FCM push `data` map.
 * Every field is nullable since the backend includes only the keys relevant to the notification
 * type.
 */
@Serializable
data class NotificationDataJson(
    @SerialName("match_id") val matchId: Int? = null,
    @SerialName("conversation_id") val conversationId: Int? = null,
    @SerialName("message_id") val messageId: Int? = null,
    @SerialName("requester_id") val requesterId: Int? = null,
    @SerialName("requester_name") val requesterName: String? = null,
    @SerialName("carrier_id") val carrierId: Int? = null,
    @SerialName("carrier_name") val carrierName: String? = null,
    @SerialName("shipper_id") val shipperId: Int? = null,
    @SerialName("shipper_name") val shipperName: String? = null,
    @SerialName("transaction_id") val transactionId: Int? = null,
    val amount: Double? = null,
    val currency: String? = null,
    @SerialName("rating_id") val ratingId: Int? = null,
    @SerialName("rating_value") val ratingValue: Int? = null,
    @SerialName("rater_id") val raterId: Int? = null,
    @SerialName("rater_name") val raterName: String? = null,
    val screen: String? = null,
    val reason: String? = null,
    @SerialName("tip_amount") val tipAmount: Double? = null,
    @SerialName("tipper_id") val tipperId: Int? = null,
    @SerialName("tipper_name") val tipperName: String? = null,
    @SerialName("refund_amount") val refundAmount: Double? = null,
    @SerialName("scheduled_at") val scheduledAt: String? = null,
    @SerialName("recipient_role") val recipientRole: String? = null,
    @SerialName("new_price") val newPrice: Double? = null,
    @SerialName("original_price") val originalPrice: Double? = null,
    @SerialName("price_difference") val priceDifference: Double? = null,
    val direction: String? = null,
    @SerialName("counter_offerer_name") val counterOffererName: String? = null,
    @SerialName("counter_offerer_id") val counterOffererId: Int? = null,
    @SerialName("counter_offer_round") val counterOfferRound: Int? = null,
    @SerialName("initiated_by") val initiatedBy: String? = null,
    @SerialName("remaining_counter_offers") val remainingCounterOffers: Int? = null,
    @SerialName("can_counter_offer") val canCounterOffer: Boolean? = null,
)
