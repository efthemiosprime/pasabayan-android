package com.efthemiosprime.pasabayan.features.notifications.model

/**
 * Domain payload for a [PushNotification]. All fields nullable — backend only includes the
 * keys relevant to the notification type. Mirrors `NotificationData` in iOS
 * `NotificationModels.swift`.
 */
data class NotificationData(
    val matchId: Int? = null,
    val conversationId: Int? = null,
    val messageId: Int? = null,
    val requesterId: Int? = null,
    val requesterName: String? = null,
    val carrierId: Int? = null,
    val carrierName: String? = null,
    val shipperId: Int? = null,
    val shipperName: String? = null,
    val transactionId: Int? = null,
    val amount: Double? = null,
    val currency: String? = null,
    val ratingId: Int? = null,
    val ratingValue: Int? = null,
    val raterId: Int? = null,
    val raterName: String? = null,
    val screen: String? = null,
    val reason: String? = null,
    val tipAmount: Double? = null,
    val tipperId: Int? = null,
    val tipperName: String? = null,
    val refundAmount: Double? = null,
    val scheduledAt: String? = null,
    val recipientRole: String? = null,
    val newPrice: Double? = null,
    val originalPrice: Double? = null,
    val priceDifference: Double? = null,
    val direction: String? = null,
    val counterOffererName: String? = null,
    val counterOffererId: Int? = null,
    val counterOfferRound: Int? = null,
    val initiatedBy: String? = null,
    val remainingCounterOffers: Int? = null,
    val canCounterOffer: Boolean? = null,
)
