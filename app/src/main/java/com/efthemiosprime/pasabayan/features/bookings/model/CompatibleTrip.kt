package com.efthemiosprime.pasabayan.features.bookings.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod
import com.efthemiosprime.pasabayan.core.domain.model.UserSummary

/**
 * A trip that is compatible with a shipper's package request — returned by
 * `GET /packages/{id}/compatible-trips`. Mirrors iOS `CompatibleTrip`
 * (PackageRequest.swift line 1102).
 *
 * Numeric capacity/price fields arrive as strings; nullable variants are
 * exposed alongside for consumers that prefer parsed `Double`s.
 */
data class CompatibleTrip(
    val id: Int,
    val carrierId: Int,
    val originCity: String,
    val originCountry: String,
    val destinationCity: String,
    val destinationCountry: String,
    val departureDate: String?,
    val arrivalDate: String?,
    val pickupDate: String?,
    val deliveryDate: String?,
    val availableWeightKg: String?,
    val availableSpaceLiters: String?,
    val pricePerKg: String?,
    val flatTripPrice: String?,
    val calculatedPrice: String?,
    val pricingType: String?,
    val pricingMethod: String?,
    val tripStatus: String?,
    val transportationMethod: String?,
    val specialNotes: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val carrier: UserSummary?,
    val shipperRequestStatus: String?,
    val canRequest: Boolean?,
    val requestMessage: String?,
    val requestedAt: String?,
    val distanceKm: Double?,
) {
    val transportationMethodEnum: TransportationMethod
        get() = transportationMethod?.let { raw ->
            TransportationMethod.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) }
        } ?: TransportationMethod.NONE

    /** True when the trip uses flat (land) pricing rather than per-kg. */
    val usesFlatPricing: Boolean
        get() = pricingType?.let { it == "flat" }
            ?: transportationMethodEnum.isLandTransport

    /** Prefer the server-calculated price; fall back to the carrier-set flat price. */
    val effectiveFlatPrice: Double?
        get() = calculatedPrice?.toDoubleOrNull()?.takeIf { it > 0 }
            ?: flatTripPrice?.toDoubleOrNull()?.takeIf { it > 0 }

    val availableWeightKgDouble: Double?
        get() = availableWeightKg?.trim()?.toDoubleOrNull()

    val pricePerKgDouble: Double?
        get() = pricePerKg?.trim()?.toDoubleOrNull()

    /** Default to `true` when missing — server is the source of truth. */
    val canRequestTrip: Boolean
        get() = canRequest ?: true

    val hasActiveRequest: Boolean
        get() = shipperRequestStatus == "shipper_requested"

    /** `true` when the package weight fits, or when capacity info is missing. */
    fun canCarryPackageWeight(packageWeightKg: Double?): Boolean {
        val weight = packageWeightKg ?: return true
        val available = availableWeightKgDouble ?: return true
        return weight <= available
    }
}
