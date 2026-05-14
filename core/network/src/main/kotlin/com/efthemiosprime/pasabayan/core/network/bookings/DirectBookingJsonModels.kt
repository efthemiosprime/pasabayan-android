package com.efthemiosprime.pasabayan.core.network.bookings

import com.efthemiosprime.pasabayan.core.domain.util.FlexibleDoubleSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body for `POST /trips/{id}/book` — shipper books a trip directly
 * without going through the package-request match flow. Mirrors iOS
 * `DirectTripBookingRequest` (BookingModels.swift line 545).
 */
@Serializable
data class DirectBookingRequestJson(
    @SerialName("booking_type") val bookingType: String,
    @SerialName("space_needed_liters") val spaceNeededLiters: Double,
    @SerialName("weight_needed_kg") val weightNeededKg: Double,
    @SerialName("pickup_location") val pickupLocation: String,
    @SerialName("delivery_location") val deliveryLocation: String,
    @SerialName("price_agreed") val priceAgreed: Double,
    @SerialName("special_requirements") val specialRequirements: String? = null,
)

/** Booking summary returned by `POST /trips/{id}/book`. */
@Serializable
data class DirectBookingInfoJson(
    val id: Int,
    @SerialName("trip_id") val tripId: Int,
    @SerialName("booker_id") val bookerId: Int? = null,
    @SerialName("booking_type") val bookingType: String? = null,
    val status: String? = null,
    @SerialName("price_agreed")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val priceAgreed: Double? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

/** Wrapper around the inner `booking` field — iOS `DirectTripBookingDataContainer`. */
@Serializable
data class DirectBookingDataJson(val booking: DirectBookingInfoJson)

@Serializable
data class DirectBookingResponseJson(
    val success: Boolean = false,
    val message: String = "",
    val data: DirectBookingDataJson? = null,
)
