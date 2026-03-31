package com.efthemiosprime.pasabayan.features.bookings.model.nested

import com.efthemiosprime.pasabayan.core.domain.model.PackageDimensions

/** Projection of PackageRequest — not the full domain model. */
data class PackageRequestInfo(
    val id: Int,
    val title: String? = null,
    val description: String? = null,
    val weightKg: Double? = null,
    val dimensions: PackageDimensions? = null,
    val pickupCity: String? = null,
    val deliveryCity: String? = null,
    val pickupAddress: String? = null,
    val deliveryAddress: String? = null,
    val packageType: String? = null,
    val urgencyLevel: String? = null,
    val fragile: Boolean = false,
)
