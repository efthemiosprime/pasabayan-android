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
    /**
     * Suggested transportation method (server lowercase string, e.g. `"car"`, `"flight"`).
     * iOS parity: `TripTemplate.transportationMethod` (Swift :304) — when present, the form
     * preselects this; otherwise the screen keeps its own default. Optional so we don't depend
     * on the server returning it.
     */
    val suggestedTransportationMethod: String? = null,
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
    /**
     * Optional shared pickup window at origin (ISO 8601). iOS auto-derives this from
     * [departureDate] at template-load time; Android does the same so the carrier doesn't
     * have to re-enter the shared schedule manually.
     */
    val sharedPickupDate: String? = null,
    /** Optional shared delivery / handoff window at destination, derived from [arrivalDate]. */
    val sharedDeliveryDate: String? = null,
)
