package com.efthemiosprime.pasabayan.features.packages.model

data class PackageFormState(
    val pickupAddress: String = "",
    val pickupCity: String = "",
    val pickupCountry: String = "",
    val deliveryAddress: String = "",
    val deliveryCity: String = "",
    val deliveryCountry: String = "",
    val packageWeightKg: Double? = null,
    val packageType: String = "",
    val urgencyLevel: String = "",
    val pickupDatePreferred: String = "",
    val deliveryDateNeeded: String = "",
    val packageDescription: String? = null,
    val maxPriceBudget: Double? = null,
    val fragile: Boolean = false,
)
