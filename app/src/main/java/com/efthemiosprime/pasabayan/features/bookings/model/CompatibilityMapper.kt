package com.efthemiosprime.pasabayan.features.bookings.model

import com.efthemiosprime.pasabayan.core.network.bookings.CompatibleTripJson
import com.efthemiosprime.pasabayan.core.network.bookings.ReceiverAccessTokenJson

fun CompatibleTripJson.toDomain(): CompatibleTrip = CompatibleTrip(
    id = id,
    carrierId = carrierId,
    originCity = originCity,
    originCountry = originCountry,
    destinationCity = destinationCity,
    destinationCountry = destinationCountry,
    departureDate = departureDate,
    arrivalDate = arrivalDate,
    pickupDate = pickupDate,
    deliveryDate = deliveryDate,
    availableWeightKg = availableWeightKg,
    availableSpaceLiters = availableSpaceLiters,
    pricePerKg = pricePerKg,
    flatTripPrice = flatTripPrice,
    calculatedPrice = calculatedPrice,
    pricingType = pricingType,
    pricingMethod = pricingMethod,
    tripStatus = tripStatus,
    transportationMethod = transportationMethod,
    specialNotes = specialNotes,
    createdAt = createdAt,
    updatedAt = updatedAt,
    carrier = carrier,
    shipperRequestStatus = shipperRequestStatus,
    canRequest = canRequest,
    requestMessage = requestMessage,
    requestedAt = requestedAt,
    distanceKm = distanceKm,
)

fun ReceiverAccessTokenJson.toDomain(): ReceiverAccessToken = ReceiverAccessToken(
    id = id,
    shortCode = shortCode,
    shortUrl = shortUrl,
    hasPin = hasPin,
    pin = pin,
    pinNotice = pinNotice,
    isActive = isActive,
    accessCount = accessCount,
    trackingUrl = trackingUrl,
    firstAccessedAt = firstAccessedAt,
    lastAccessedAt = lastAccessedAt,
    createdAt = createdAt,
)
