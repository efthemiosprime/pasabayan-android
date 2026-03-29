package com.efthemiosprime.pasabayan.features.bookings.model

import com.efthemiosprime.pasabayan.core.network.bookings.DeliveryMatchJson

fun DeliveryMatchJson.toDomain(): DeliveryMatch = DeliveryMatch(
    id = id,
    tripId = tripId,
    packageRequestId = packageRequestId,
    matchStatus = matchStatus,
    agreedPrice = agreedPrice ?: 0.0,
    initiatedBy = initiatedBy,
    isCounterOffer = isCounterOffer,
    originalPrice = originalPrice,
    canCounterOffer = canCounterOffer ?: false,
    remainingCounterOffers = remainingCounterOffers ?: 0,
    counterOfferRound = counterOfferRound,
    carrierMessage = carrierMessage,
    shipperMessage = shipperMessage,
    carrier = carrier,
    shipper = shipper,
    chatConversationId = chatConversationId,
    confirmedAt = confirmedAt,
    pickedUpAt = pickedUpAt,
    deliveredAt = deliveredAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
    platformFeePercent = platformFeePercent,
    transactionStatus = null, // Not on DeliveryMatchJson directly
    receiptPhoto = receiptPhoto,
    autoCancelAfterDays = autoCancelAfterDays,
    pickupConfirmationCode = pickupConfirmationCode,
    codeExpiresAt = codeExpiresAt,
    deliveryVerificationCode = deliveryVerificationCode,
    deliveryCodeExpiresAt = deliveryCodeExpiresAt,
)
