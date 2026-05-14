package com.efthemiosprime.pasabayan.features.bookings.model

/**
 * Form payload for a direct trip booking — `POST /trips/{id}/book`. Mirrors
 * the body iOS' DirectBookingSheet sends. Optional numeric fields default
 * to 0 to match iOS' decode behaviour for empty inputs.
 */
data class DirectBookingPayload(
    /** Wire value for `booking_type`. iOS defaults to `space_only` on the carrier sheet. */
    val bookingType: String = BOOKING_TYPE_SPACE_ONLY,
    val spaceNeededLiters: Double = 0.0,
    val weightNeededKg: Double = 0.0,
    val pickupLocation: String = "",
    val deliveryLocation: String = "",
    val priceAgreed: Double,
    val specialRequirements: String? = null,
) {
    companion object {
        const val BOOKING_TYPE_SPACE_ONLY: String = "space_only"
        const val BOOKING_TYPE_PASSENGER: String = "passenger"
        const val BOOKING_TYPE_PACKAGE: String = "package"
    }
}
