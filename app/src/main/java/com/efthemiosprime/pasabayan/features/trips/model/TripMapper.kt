package com.efthemiosprime.pasabayan.features.trips.model

import com.efthemiosprime.pasabayan.core.network.trips.PendingTripRequestJson
import com.efthemiosprime.pasabayan.core.network.trips.TripEarningsBreakdownJson
import com.efthemiosprime.pasabayan.core.network.trips.TripJson
import com.efthemiosprime.pasabayan.core.network.trips.TripMatchPackageJson

fun TripJson.toDomain(): Trip = Trip(
    id = id,
    carrierId = carrierId,
    originCity = originCity,
    originCountry = originCountry,
    originLat = originLat,
    originLng = originLng,
    destinationCity = destinationCity,
    destinationCountry = destinationCountry,
    destinationLat = destinationLat,
    destinationLng = destinationLng,
    departureDate = departureDate,
    arrivalDate = arrivalDate,
    availableWeightKg = availableWeightKg,
    availableSpaceLiters = availableSpaceLiters,
    pricePerKg = pricePerKg,
    tripStatus = tripStatus,
    transportationMethod = transportationMethod,
    specialNotes = specialNotes,
    carrier = carrier,
    createdAt = createdAt,
    updatedAt = updatedAt,
    pricingType = pricingType,
    pricingMethod = pricingMethod,
    flatTripPrice = flatTripPrice,
    basePrice = basePrice,
    calculatedPrice = calculatedPrice,
    pickupAddress = pickupAddress,
    pickupLandmark = pickupLandmark,
    dropoffAddress = dropoffAddress,
    dropoffLandmark = dropoffLandmark,
    tripEarningsTotal = tripEarningsTotal,
    tripEarningsCurrency = tripEarningsCurrency,
    tripEarningsBreakdown = tripEarningsBreakdown?.toDomain(),
    hasPendingRequests = hasPendingRequests,
    pendingRequestCount = pendingRequestCount,
    pendingRequests = pendingRequests?.map { it.toDomain() },
    distanceKm = distanceKm,
)

fun TripEarningsBreakdownJson.toDomain(): TripEarningsBreakdown = TripEarningsBreakdown(
    deliveredAmount = deliveredAmount ?: 0.0,
    deliveredCurrency = deliveredCurrency ?: "CAD",
    pendingAmount = pendingAmount ?: 0.0,
    pendingCurrency = pendingCurrency ?: "CAD",
)

fun PendingTripRequestJson.toDomain(): PendingTripRequest = PendingTripRequest(
    id = id,
    shipperId = shipperId,
    shipperName = shipperName,
    shipperAvatar = shipperAvatar,
    packageId = packageId,
    packageDescription = packageDescription,
    proposedPrice = proposedPrice,
    message = message,
    createdAt = createdAt,
)

fun TripMatchPackageJson.toDomain(): TripMatchPackage = TripMatchPackage(
    id = id,
    matchStatus = matchStatus,
    agreedPrice = agreedPrice,
    packageDescription = packageInfo?.description,
    packageWeightKg = packageInfo?.weightKg,
    packageId = packageInfo?.id,
    shipper = shipper,
    chatConversationId = chatConversationId,
    confirmedAt = confirmedAt,
    pickedUpAt = pickedUpAt,
    deliveredAt = deliveredAt,
    createdAt = createdAt,
)
