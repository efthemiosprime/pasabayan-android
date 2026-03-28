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

    fun validate(form: TripFormState): List<TripValidationError> = buildList {
        // Origin / destination
        if (form.originCity.isBlank()) add(TripValidationError.OriginRequired)
        if (form.destinationCity.isBlank()) add(TripValidationError.DestinationRequired)
        if (form.originCity.isNotBlank() && form.destinationCity.isNotBlank() &&
            form.originCity.trim().equals(form.destinationCity.trim(), ignoreCase = true)
        ) {
            add(TripValidationError.SameOriginDestination)
        }

        // Weight
        val weight = form.weightCapacityKg
        if (weight == null || weight <= 0.0) add(TripValidationError.WeightRequired)

        // Space (optional but if provided must be > 0)
        val space = form.spaceCapacityLiters
        if (space != null && space <= 0.0) add(TripValidationError.SpaceInvalid)

        // Transportation method
        if (form.transportationMethod == TransportationMethod.NONE) {
            add(TripValidationError.TransportMethodRequired)
        }

        // Pricing
        if (form.transportationMethod != TransportationMethod.NONE) {
            if (form.transportationMethod.isLandTransport) {
                if (form.flatTripPrice == null || form.flatTripPrice <= 0.0) {
                    add(TripValidationError.PriceRequired)
                }
            } else {
                if (form.pricePerKg == null || form.pricePerKg <= 0.0) {
                    add(TripValidationError.PriceRequired)
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
