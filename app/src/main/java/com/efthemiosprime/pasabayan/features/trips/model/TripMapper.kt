package com.efthemiosprime.pasabayan.features.trips.model

import com.efthemiosprime.pasabayan.core.network.trips.PendingTripRequestJson
import com.efthemiosprime.pasabayan.core.network.trips.PopularRouteJson
import com.efthemiosprime.pasabayan.core.network.trips.RouteActivityCarrierSummaryJson
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

fun PopularRouteJson.toDomain(): PopularRoute = PopularRoute(
    city = city,
    country = country,
    destinationCity = destinationCity,
    destinationCountry = destinationCountry,
    routeType = PopularRouteType.fromWire(routeType),
    displayName = displayName ?: "$destinationCity, $destinationCountry",
    tripCount = tripCount,
)

fun RouteActivityCarrierSummaryJson?.toDomainOrZero(): RouteActivitySummary = RouteActivitySummary(
    packageDeliveryNearHome = this?.packageDeliveryNearHome ?: 0,
    serviceErrandNearHome = this?.serviceErrandNearHome ?: 0,
    newPackagesThisWeekNearHome = this?.newPackagesThisWeekNearHome ?: 0,
)

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
