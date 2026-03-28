package com.efthemiosprime.pasabayan.features.packages.model

/**
 * Pure validation logic for package creation form.
 * Required fields from `android-spec/04-packages.md` § CreatePackageRequest.
 */
object PackageFormValidator {

    fun validate(form: PackageFormState): List<PackageValidationError> = buildList {
        if (form.pickupCity.isBlank()) add(PackageValidationError.PickupCityRequired)
        if (form.pickupCountry.isBlank()) add(PackageValidationError.PickupCountryRequired)
        if (form.deliveryCity.isBlank()) add(PackageValidationError.DeliveryCityRequired)
        if (form.deliveryCountry.isBlank()) add(PackageValidationError.DeliveryCountryRequired)

        val weight = form.packageWeightKg
        if (weight == null || weight <= 0.0) add(PackageValidationError.WeightRequired)

        if (form.packageType.isBlank()) add(PackageValidationError.PackageTypeRequired)
        if (form.urgencyLevel.isBlank()) add(PackageValidationError.UrgencyRequired)
        if (form.pickupDatePreferred.isBlank()) add(PackageValidationError.PickupDateRequired)
        if (form.deliveryDateNeeded.isBlank()) add(PackageValidationError.DeliveryDateRequired)
    }
}
