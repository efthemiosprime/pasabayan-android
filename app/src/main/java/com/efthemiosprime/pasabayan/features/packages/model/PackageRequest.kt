package com.efthemiosprime.pasabayan.features.packages.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageRequestStatus
import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageSize
import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageType
import com.efthemiosprime.pasabayan.core.domain.`enum`.UrgencyLevel
import com.efthemiosprime.pasabayan.core.domain.model.PackageDimensions
import com.efthemiosprime.pasabayan.core.domain.model.UserSummary

/**
 * Package request domain model — computed properties over the DTO.
 * Parity with iOS `PackageRequest.swift`.
 */
data class PackageRequest(
    val id: Int,
    val shipperId: Int?,
    val pickupAddress: String?,
    val pickupCity: String?,
    val pickupCountry: String?,
    val deliveryAddress: String?,
    val deliveryCity: String?,
    val deliveryCountry: String?,
    val packageWeightKg: Double?,
    val packageDimensions: PackageDimensions?,
    val packageType: PackageType?,
    val fragile: Boolean?,
    val packageValue: Double?,
    val packageDescription: String?,
    val urgencyLevel: UrgencyLevel?,
    val maxPriceBudget: Double?,
    val pickupDatePreferred: String?,
    val pickupTimePreferred: String?,
    val pickupDateFlexible: Boolean?,
    val deliveryDateNeeded: String?,
    val deliveryTimeNeeded: String?,
    val specialHandlingRequirements: String?,
    val requestStatus: PackageRequestStatus?,
    val createdAt: String?,
    val updatedAt: String?,
    val compatibleTripsCount: Int?,
    val shipper: UserSummary?,
    val images: List<PackageImage>?,
    val imagesProcessing: Boolean?,
    // Service request fields
    val serviceType: String?,
    val shoppingList: String?,
    val storeName: String?,
    val storeAddress: String?,
    val receiptRequired: Boolean?,
    /** Custom-task name. iOS parity — set when service_type=general_errand + direction=task. */
    val taskName: String? = null,
    /** Custom-task description. iOS parity — set when service_type=general_errand + direction=task. */
    val taskDescription: String? = null,
    // Coordinates (iOS parity — drive detail-screen map pins)
    val pickupLat: Double? = null,
    val pickupLng: Double? = null,
    val deliveryLat: Double? = null,
    val deliveryLng: Double? = null,
) {
    val title: String
        get() = packageDescription
            ?: packageType?.name.orEmpty()

    val pickupLocation: String
        get() = listOfNotNull(pickupAddress, pickupCity)
            .filter { it.isNotBlank() }
            .joinToString(", ")

    val deliveryLocation: String
        get() = listOfNotNull(deliveryAddress, deliveryCity)
            .filter { it.isNotBlank() }
            .joinToString(", ")

    val isFragile: Boolean
        get() = fragile ?: false

    val status: PackageRequestStatus
        get() = requestStatus ?: PackageRequestStatus.OPEN

    val packageSize: PackageSize
        get() = PackageSize.fromWeight(packageWeightKg ?: 0.0)

    val isServiceRequest: Boolean
        get() = serviceType != null && serviceType != "delivery"
}
