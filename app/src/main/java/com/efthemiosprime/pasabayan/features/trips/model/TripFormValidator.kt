package com.efthemiosprime.pasabayan.features.trips.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod

/**
 * Pure validation logic for trip creation form.
 * Rules from `android-spec/03-trips.md` § Form validation rules.
 */
object TripFormValidator {

    private const val MIN_DEPARTURE_OFFSET_MS = 5 * 60 * 1000L // 5 minutes
    private const val MIN_LAND_DURATION_MS = 20 * 60 * 1000L   // 20 minutes
    private const val MIN_FLIGHT_DURATION_MS = 3 * 60 * 60 * 1000L // 3 hours
    private const val MAX_NOTES_LENGTH = 1000

    /** iOS parity (`EditTripSheet.swift:1257`): weight is capped at 2000 kg. */
    const val MAX_WEIGHT_KG: Double = 2000.0

    /** iOS parity (`EditTripSheet.swift:1260`): space is capped at 5000 L. */
    const val MAX_SPACE_LITERS: Double = 5000.0

    /** iOS parity (`EditTripSheet.swift:1276`): non-land price-per-kg is capped at $100/kg. */
    const val MAX_PRICE_PER_KG: Double = 100.0

    fun validate(form: TripFormState): List<TripValidationError> = buildList {
        // Origin / destination
        if (form.originCity.isBlank()) add(TripValidationError.OriginRequired)
        if (form.destinationCity.isBlank()) add(TripValidationError.DestinationRequired)
        if (form.originCity.isNotBlank() && form.destinationCity.isNotBlank() &&
            form.originCity.trim().equals(form.destinationCity.trim(), ignoreCase = true)
        ) {
            add(TripValidationError.SameOriginDestination)
        }

        // Pickup / drop-off addresses — iOS parity (`TripCreationFormState.isFormValid` requires both).
        if (form.pickupAddress.isBlank()) add(TripValidationError.PickupAddressRequired)
        if (form.dropoffAddress.isBlank()) add(TripValidationError.DropoffAddressRequired)

        // Weight — required, > 0, ≤ 2000 kg (iOS parity).
        val weight = form.weightCapacityKg
        if (weight == null || weight <= 0.0) {
            add(TripValidationError.WeightRequired)
        } else if (weight > MAX_WEIGHT_KG) {
            add(TripValidationError.WeightOutOfRange)
        }

        // Space — optional, > 0 if provided, ≤ 5000 L (iOS parity).
        val space = form.spaceCapacityLiters
        if (space != null) {
            if (space <= 0.0) {
                add(TripValidationError.SpaceInvalid)
            } else if (space > MAX_SPACE_LITERS) {
                add(TripValidationError.SpaceOutOfRange)
            }
        }

        // Transportation method
        if (form.transportationMethod == TransportationMethod.NONE) {
            add(TripValidationError.TransportMethodRequired)
        }

        // Pricing — branches by transport method. Land uses flatTripPrice; flight/ship use
        // pricePerKg with an iOS-parity upper bound of $100/kg.
        if (form.transportationMethod != TransportationMethod.NONE) {
            if (form.transportationMethod.isLandTransport) {
                if (form.flatTripPrice == null || form.flatTripPrice <= 0.0) {
                    add(TripValidationError.PriceRequired)
                }
            } else {
                val perKg = form.pricePerKg
                if (perKg == null || perKg <= 0.0) {
                    add(TripValidationError.PriceRequired)
                } else if (perKg > MAX_PRICE_PER_KG) {
                    add(TripValidationError.PricePerKgOutOfRange)
                }
            }
        }

        // Departure
        val departure = form.departureDateMillis
        if (departure == null) {
            add(TripValidationError.DepartureRequired)
        } else if (departure < System.currentTimeMillis() + MIN_DEPARTURE_OFFSET_MS) {
            add(TripValidationError.DepartureTooSoon)
        }

        // Arrival
        val arrival = form.arrivalDateMillis
        if (arrival == null) {
            add(TripValidationError.ArrivalRequired)
        } else if (departure != null) {
            if (arrival <= departure) {
                add(TripValidationError.ArrivalBeforeDeparture)
            } else {
                // Min duration
                val duration = arrival - departure
                val minDuration = if (form.transportationMethod == TransportationMethod.FLIGHT) {
                    MIN_FLIGHT_DURATION_MS
                } else {
                    MIN_LAND_DURATION_MS
                }
                if (duration < minDuration) {
                    add(TripValidationError.DurationTooShort)
                }
            }
        }

        // Notes
        val notes = form.specialNotes
        if (notes != null && notes.length > MAX_NOTES_LENGTH) {
            add(TripValidationError.NotesTooLong)
        }
    }
}
