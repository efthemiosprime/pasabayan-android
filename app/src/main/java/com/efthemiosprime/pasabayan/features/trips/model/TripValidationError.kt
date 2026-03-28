package com.efthemiosprime.pasabayan.features.trips.model

sealed interface TripValidationError {
    data object OriginRequired : TripValidationError
    data object DestinationRequired : TripValidationError
    data object SameOriginDestination : TripValidationError
    data object WeightRequired : TripValidationError
    data object SpaceInvalid : TripValidationError
    data object PriceRequired : TripValidationError
    data object TransportMethodRequired : TripValidationError
    data object DepartureRequired : TripValidationError
    data object DepartureTooSoon : TripValidationError
    data object ArrivalRequired : TripValidationError
    data object ArrivalBeforeDeparture : TripValidationError
    data object DurationTooShort : TripValidationError
    data object NotesTooLong : TripValidationError
}
