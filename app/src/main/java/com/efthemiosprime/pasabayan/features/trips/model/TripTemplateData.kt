package com.efthemiosprime.pasabayan.features.trips.model

data class TripTemplateData(
    val packageId: Int,
    val originCity: String,
    val originCountry: String,
    val destinationCity: String,
    val destinationCountry: String,
    val suggestedDepartureDate: String?,
    val suggestedArrivalDate: String?,
    val suggestedWeightKg: Double?,
    val suggestedSpaceLiters: Double?,
    val packageDescription: String?,
    val packageWeightKg: Double?,
    val packageUrgencyLevel: String?,
    // iOS parity (`PackageTemplateDetails`): used by the create-from-package package info card.
    val packageFragile: Boolean = false,
    val packageType: String? = null,
)

data class CreateTripFromPackageRequest(
    val packageId: Int,
    val originCity: String,
    val originCountry: String,
    val destinationCity: String,
    val destinationCountry: String,
    val departureDate: String,
    val arrivalDate: String,
    val availableWeightKg: Double,
    val availableSpaceLiters: Double,
    val transportationMethod: String,
    val pricePerKg: Double?,
    val flatTripPrice: Double?,
    val specialNotes: String?,
    val pickupAddress: String?,
    val dropoffAddress: String?,
    val proposedPrice: Double?,
    val requestMessage: String?,
)
