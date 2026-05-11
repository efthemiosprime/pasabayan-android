package com.efthemiosprime.pasabayan.features.trips.model

import com.efthemiosprime.pasabayan.core.network.trips.PendingTripRequestJson
import com.efthemiosprime.pasabayan.core.network.trips.PopularRouteJson
import com.efthemiosprime.pasabayan.core.network.trips.RouteActivitySummaryDataJson
import com.efthemiosprime.pasabayan.core.network.trips.TripTemplateJson
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
    pickupDate = pickupDate,
    deliveryDate = deliveryDate,
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
    distanceMultiplier = distanceMultiplier,
    passengerCapacity = passengerCapacity,
    pricePerPassenger = pricePerPassenger,
    passengerRequirements = passengerRequirements,
    ageRestrictions = ageRestrictions,
    passengerAmenities = passengerAmenities,
    pickupAddress = pickupAddress,
    pickupLandmark = pickupLandmark,
    pickupInstructions = pickupInstructions,
    dropoffAddress = dropoffAddress,
    dropoffLandmark = dropoffLandmark,
    dropoffInstructions = dropoffInstructions,
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
    deliveredCount = deliveredCount ?: 0,
    pendingAmount = pendingAmount ?: 0.0,
    pendingCurrency = pendingCurrency ?: "CAD",
    pendingCount = pendingCount ?: 0,
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
    packagePickupCity = packageInfo?.pickupCity,
    packageDeliveryCity = packageInfo?.deliveryCity,
    packageFragile = packageInfo?.fragile,
    packageType = packageInfo?.packageType,
    shipper = shipper,
    chatConversationId = chatConversationId,
    confirmedAt = confirmedAt,
    pickedUpAt = pickedUpAt,
    deliveredAt = deliveredAt,
    createdAt = createdAt,
)

fun PopularRouteJson.toDomain(): PopularRoute = PopularRoute(
    originCity = originCity ?: city.orEmpty(),
    destinationCity = destinationCity,
    packageCount = packageCount ?: tripCount ?: 0,
    averagePrice = averagePrice,
)

fun RouteActivitySummaryDataJson?.toDomainOrZero(): RouteActivitySummary {
    val summary = this
    val legacy = summary?.carrier
    return RouteActivitySummary(
        totalTrips = summary?.totalTrips ?: (
            (legacy?.packageDeliveryNearHome ?: 0) +
                (legacy?.serviceErrandNearHome ?: 0) +
                (legacy?.newPackagesThisWeekNearHome ?: 0)
            ),
        activeTrips = summary?.activeTrips ?: (legacy?.packageDeliveryNearHome ?: 0),
        completedTrips = summary?.completedTrips ?: (legacy?.serviceErrandNearHome ?: 0),
        totalEarnings = summary?.totalEarnings,
        currency = summary?.currency,
    )
}

fun TripTemplateJson.toDomain(packageId: Int): TripTemplateData = TripTemplateData(
    packageId = packageId,
    originCity = originCity,
    originCountry = originCountry,
    destinationCity = destinationCity,
    destinationCountry = destinationCountry,
    suggestedDepartureDate = suggestedDepartureDate,
    suggestedArrivalDate = suggestedArrivalDate,
    suggestedWeightKg = suggestedWeightKg,
    suggestedSpaceLiters = suggestedSpaceLiters,
    packageDescription = packageDetails?.description,
    packageWeightKg = packageDetails?.weightKg,
    packageUrgencyLevel = packageDetails?.urgencyLevel,
)
