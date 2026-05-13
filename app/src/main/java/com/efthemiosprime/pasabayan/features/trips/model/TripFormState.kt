package com.efthemiosprime.pasabayan.features.trips.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod

data class TripFormState(
    val originCity: String = "",
    val originCountry: String = "",
    val destinationCity: String = "",
    val destinationCountry: String = "",
    /** Required (iOS parity — `TripCreationFormState.isFormValid` enforces non-empty). */
    val pickupAddress: String = "",
    /** Required (iOS parity). */
    val dropoffAddress: String = "",
    val weightCapacityKg: Double? = null,
    val spaceCapacityLiters: Double? = null,
    val pricePerKg: Double? = null,
    val flatTripPrice: Double? = null,
    val transportationMethod: TransportationMethod = TransportationMethod.NONE,
    val departureDateMillis: Long? = null,
    val arrivalDateMillis: Long? = null,
    val specialNotes: String? = null,
)
