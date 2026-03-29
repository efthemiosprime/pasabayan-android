package com.efthemiosprime.pasabayan.features.bookings.model.nested

/** Projection of Trip — not the full Trip domain model. */
data class CarrierTripInfo(
    val id: Int,
    val originCity: String,
    val destinationCity: String,
    val departureDate: String? = null,
    val arrivalDate: String? = null,
    val transportationMethod: String? = null,
    val availableWeightKg: Double? = null,
    val pricePerKg: Double? = null,
    val flatTripPrice: Double? = null,
    val pricingType: String? = null,
) {
    val route: String get() = "$originCity → $destinationCity"
}
