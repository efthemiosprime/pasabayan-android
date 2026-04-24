package com.efthemiosprime.pasabayan.features.packages.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageType
import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageRequestStatus
import com.efthemiosprime.pasabayan.core.domain.`enum`.UrgencyLevel
import com.efthemiosprime.pasabayan.core.domain.model.PackageDimensions
import com.efthemiosprime.pasabayan.core.domain.model.UserSummary

/**
 * Browse model — separate from [PackageRequest].
 * iOS provides `toPackageRequest()` for UI reuse.
 */
data class AvailablePackage(
    val id: Int,
    val packageRequestId: Int?,
    val pickupCity: String,
    val pickupCountry: String?,
    val deliveryCity: String,
    val deliveryCountry: String?,
    val packageWeightKg: Double?,
    val packageDimensions: PackageDimensions?,
    val urgencyLevel: UrgencyLevel,
    val maxPriceBudget: Double?,
    val pickupDatePreferred: String,
    val pickupDateFlexible: Boolean,
    val deliveryDateNeeded: String,
    val fragile: Boolean,
    val packageType: PackageType,
    val packageDescription: String?,
    val createdAt: String,
    val daysSincePosted: Double?,
    val distanceKm: Double?,
    val shipper: UserSummary?,
    val serviceType: String?,
) {
    /** Use packageRequestId for API calls, fallback to id. */
    val effectiveId: Int get() = packageRequestId ?: id

    val isServiceRequest: Boolean
        get() = serviceType != null && serviceType != "delivery"
}

fun AvailablePackage.toPackageRequest(): PackageRequest = PackageRequest(
    id = effectiveId,
    shipperId = shipper?.id?.toInt(),
    pickupAddress = null,
    pickupCity = pickupCity,
    pickupCountry = pickupCountry,
    deliveryAddress = null,
    deliveryCity = deliveryCity,
    deliveryCountry = deliveryCountry,
    packageWeightKg = packageWeightKg,
    packageDimensions = packageDimensions,
    packageType = packageType,
    fragile = fragile,
    packageValue = null,
    packageDescription = packageDescription,
    urgencyLevel = urgencyLevel,
    maxPriceBudget = maxPriceBudget,
    pickupDatePreferred = pickupDatePreferred,
    pickupTimePreferred = null,
    pickupDateFlexible = pickupDateFlexible,
    deliveryDateNeeded = deliveryDateNeeded,
    deliveryTimeNeeded = null,
    specialHandlingRequirements = null,
    requestStatus = PackageRequestStatus.OPEN,
    createdAt = createdAt,
    updatedAt = null,
    compatibleTripsCount = null,
    shipper = shipper,
    images = null,
    imagesProcessing = null,
    serviceType = serviceType,
    shoppingList = null,
    storeName = null,
    storeAddress = null,
    receiptRequired = null,
)
