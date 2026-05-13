package com.efthemiosprime.pasabayan.features.bookings.model

import com.efthemiosprime.pasabayan.core.network.bookings.CancelMatchResponseJson
import com.efthemiosprime.pasabayan.core.network.bookings.CarrierRequestResponseJson
import com.efthemiosprime.pasabayan.core.network.bookings.DeliveryMatchJson
import com.efthemiosprime.pasabayan.core.network.bookings.MatchCompatibilityJson
import com.efthemiosprime.pasabayan.core.network.bookings.RefundResultJson
import com.efthemiosprime.pasabayan.core.network.bookings.ShipperRequestResponseJson
import com.efthemiosprime.pasabayan.features.bookings.model.nested.CarrierTripInfo
import com.efthemiosprime.pasabayan.features.bookings.model.nested.PackageRequestInfo
import com.efthemiosprime.pasabayan.features.bookings.model.nested.RefundResult

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
    counterOffererId = counterOffererId,
    counterOffererName = counterOffererName,
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
    transactionStatus = transactionStatus,
    transaction = transaction?.toDomain(),
    receiptPhoto = receiptPhoto,
    autoCancelAfterDays = autoCancelAfterDays,
    pickupConfirmationCode = pickupConfirmationCode,
    codeExpiresAt = codeExpiresAt,
    deliveryVerificationCode = deliveryVerificationCode,
    deliveryCodeExpiresAt = deliveryCodeExpiresAt,
    carrierTrip = carrierTrip?.toDomain(),
    packageRequest = packageRequest?.toDomain(),
)

fun CancelMatchResponseJson.toDomain(): CancelMatchResult? {
    val match = data?.toDomain() ?: return null
    return CancelMatchResult(
        match = match,
        chatConversationId = chatConversationId,
        refund = refund?.toDomain(),
    )
}

fun CarrierRequestResponseJson.toDomain(): RequestMatchResult? {
    val match = data?.toDomain() ?: return null
    return RequestMatchResult(
        match = match,
        negotiation = NegotiationMetadata(
            warnings = warnings.orEmpty(),
            negotiationNeeded = negotiationNeeded ?: false,
            isCounterOffer = isCounterOffer ?: false,
        ),
        compatibility = compatibility?.toDomain(),
    )
}

fun ShipperRequestResponseJson.toDomain(): RequestMatchResult? {
    val match = data?.toDomain() ?: return null
    return RequestMatchResult(
        match = match,
        negotiation = NegotiationMetadata(
            warnings = warnings.orEmpty(),
            negotiationNeeded = negotiationNeeded ?: false,
            isCounterOffer = isCounterOffer ?: false,
        ),
        compatibility = compatibility?.toDomain(),
    )
}

fun MatchCompatibilityJson.toDomain(): MatchCompatibility = MatchCompatibility(
    weightOverCapacity = weightOverCapacity,
    packageWeightKg = packageWeightKg,
    tripAvailableWeightKg = tripAvailableWeightKg,
    overageKg = overageKg,
    datesMisaligned = datesMisaligned,
    routeUncertain = routeUncertain,
    requiresCapacityAcknowledgment = requiresCapacityAcknowledgment,
)

fun RefundResultJson.toDomain(): RefundResult = RefundResult(
    processed = processed,
    amount = amount,
    transactionId = transactionId,
    error = error,
)

private fun com.efthemiosprime.pasabayan.core.network.bookings.MatchTransactionJson.toDomain():
    MatchTransaction = MatchTransaction(
    id = id,
    status = status,
    totalAmount = totalAmount,
    platformFee = platformFee,
    carrierAmount = carrierAmount,
    currency = currency,
    requiresActionAt = requiresActionAt,
    errorCode = errorCode,
    errorMessage = errorMessage,
    createdAt = createdAt,
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
