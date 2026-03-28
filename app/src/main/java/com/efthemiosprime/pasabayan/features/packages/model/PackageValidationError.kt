package com.efthemiosprime.pasabayan.features.packages.model

sealed interface PackageValidationError {
    data object PickupCityRequired : PackageValidationError
    data object PickupCountryRequired : PackageValidationError
    data object DeliveryCityRequired : PackageValidationError
    data object DeliveryCountryRequired : PackageValidationError
    data object WeightRequired : PackageValidationError
    data object PackageTypeRequired : PackageValidationError
    data object UrgencyRequired : PackageValidationError
    data object PickupDateRequired : PackageValidationError
    data object DeliveryDateRequired : PackageValidationError
}
