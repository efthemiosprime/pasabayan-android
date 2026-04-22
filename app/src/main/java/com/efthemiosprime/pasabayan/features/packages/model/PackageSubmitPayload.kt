package com.efthemiosprime.pasabayan.features.packages.model

import java.time.LocalDate
import java.time.LocalTime

data class PackageSubmitPayload(
    val pickupAddress: String,
    val pickupCity: String,
    val pickupCountryCode: String,
    val deliveryAddress: String,
    val deliveryCity: String,
    val deliveryCountryCode: String,
    val packageWeightKg: Double,
    val packageTypeCode: String,
    val fragile: Boolean,
    val urgencyLevelCode: String,
    val pickupDatePreferred: LocalDate,
    val pickupTimePreferred: LocalTime?,
    val pickupDateFlexible: Boolean,
    val deliveryDateNeeded: LocalDate,
    val deliveryTimeNeeded: LocalTime?,
    val packageValue: Double?,
    val packageDescription: String?,
    val maxPriceBudget: Double?,
    val specialHandlingRequirements: String?,
)
