package com.efthemiosprime.pasabayan.features.trips.model

sealed interface TripValidationError {
    data object OriginRequired : TripValidationError
    data object DestinationRequired : TripValidationError
    data object SameOriginDestination : TripValidationError
    /** Pickup address is required (iOS parity — `TripCreationFormState.isFormValid`). */
    data object PickupAddressRequired : TripValidationError
    /** Drop-off address is required (iOS parity). */
    data object DropoffAddressRequired : TripValidationError
    data object WeightRequired : TripValidationError
    /** Weight exceeds the iOS-parity upper bound of 2000 kg (`EditTripSheet.swift:1257`). */
    data object WeightOutOfRange : TripValidationError
    data object SpaceInvalid : TripValidationError
    /** Space exceeds the iOS-parity upper bound of 5000 L (`EditTripSheet.swift:1260`). */
    data object SpaceOutOfRange : TripValidationError
    data object PriceRequired : TripValidationError
    /**
     * Non-land price-per-kg exceeds the iOS-parity upper bound of $100/kg
     * (`EditTripSheet.swift:1276`).
     */
    data object PricePerKgOutOfRange : TripValidationError
    data object TransportMethodRequired : TripValidationError
    data object DepartureRequired : TripValidationError
    data object DepartureTooSoon : TripValidationError
    data object ArrivalRequired : TripValidationError
    data object ArrivalBeforeDeparture : TripValidationError
    data object DurationTooShort : TripValidationError
    data object NotesTooLong : TripValidationError
}
