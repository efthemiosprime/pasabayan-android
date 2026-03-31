package com.efthemiosprime.pasabayan.features.bookings.model

import com.efthemiosprime.pasabayan.core.network.bookings.DeliveryMatchJson
import com.efthemiosprime.pasabayan.features.bookings.model.nested.CarrierTripInfo
import com.efthemiosprime.pasabayan.features.bookings.model.nested.PackageRequestInfo

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
    carrierTrip = carrierTrip?.toDomain(),
    packageRequest = packageRequest?.toDomain(),
)

private fun com.efthemiosprime.pasabayan.core.network.bookings.CarrierTripInfoJson.toDomain():
    CarrierTripInfo = CarrierTripInfo(
    id = id,
    originCity = originCity,
    destinationCity = destinationCity,
    departureDate = departureDate,
    arrivalDate = arrivalDate,
    transportationMethod = transportationMethod,
    availableWeightKg = availableWeightKg,
    pricePerKg = pricePerKg,
    flatTripPrice = flatTripPrice,
    pricingType = pricingType,
)

private fun com.efthemiosprime.pasabayan.core.network.bookings.PackageRequestInfoJson.toDomain():
    PackageRequestInfo = PackageRequestInfo(
    id = id,
    title = title,
    description = description,
    weightKg = weightKg,
    pickupCity = pickupCity,
    deliveryCity = deliveryCity,
    pickupAddress = pickupAddress,
    deliveryAddress = deliveryAddress,
    packageType = packageType,
    urgencyLevel = urgencyLevel,
    fragile = fragile,
)
