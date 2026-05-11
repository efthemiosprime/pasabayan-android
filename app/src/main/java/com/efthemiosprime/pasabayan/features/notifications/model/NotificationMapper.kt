package com.efthemiosprime.pasabayan.features.notifications.model

import com.efthemiosprime.pasabayan.core.network.notifications.NotificationDataJson
import com.efthemiosprime.pasabayan.core.network.notifications.PushNotificationJson

/** DTO → domain mappers for spec [08-notifications-device-tokens.md]. */
object NotificationMapper {

    fun toDomain(dto: PushNotificationJson): PushNotification = PushNotification(
        id = dto.id,
        title = dto.title,
        body = dto.body,
        type = NotificationType.fromRaw(dto.type),
        data = dto.data?.let { toDomain(it) },
        recipientRole = dto.recipientRole,
        sentAt = dto.sentAt,
        readAt = dto.readAt,
        clickedAt = dto.clickedAt,
        isRead = dto.isRead,
        createdAt = dto.createdAt,
    )

    fun toDomain(dto: NotificationDataJson): NotificationData = NotificationData(
        matchId = dto.matchId,
        conversationId = dto.conversationId,
        messageId = dto.messageId,
        requesterId = dto.requesterId,
        requesterName = dto.requesterName,
        carrierId = dto.carrierId,
        carrierName = dto.carrierName,
        shipperId = dto.shipperId,
        shipperName = dto.shipperName,
        transactionId = dto.transactionId,
        amount = dto.amount,
        currency = dto.currency,
        ratingId = dto.ratingId,
        ratingValue = dto.ratingValue,
        raterId = dto.raterId,
        raterName = dto.raterName,
        screen = dto.screen,
        reason = dto.reason,
        tipAmount = dto.tipAmount,
        tipperId = dto.tipperId,
        tipperName = dto.tipperName,
        refundAmount = dto.refundAmount,
        scheduledAt = dto.scheduledAt,
        recipientRole = dto.recipientRole,
        newPrice = dto.newPrice,
        originalPrice = dto.originalPrice,
        priceDifference = dto.priceDifference,
        direction = dto.direction,
        counterOffererName = dto.counterOffererName,
        counterOffererId = dto.counterOffererId,
        counterOfferRound = dto.counterOfferRound,
        initiatedBy = dto.initiatedBy,
        remainingCounterOffers = dto.remainingCounterOffers,
        canCounterOffer = dto.canCounterOffer,
    )
}
