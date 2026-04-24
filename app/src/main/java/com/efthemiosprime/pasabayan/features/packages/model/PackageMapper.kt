package com.efthemiosprime.pasabayan.features.packages.model

import com.efthemiosprime.pasabayan.core.network.packages.AvailablePackageJson
import com.efthemiosprime.pasabayan.core.network.packages.PackageImageJson
import com.efthemiosprime.pasabayan.core.network.packages.PackageRequestJson
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

fun PackageRequestJson.toDomain(): PackageRequest = PackageRequest(
    id = id,
    shipperId = shipperId,
    pickupAddress = pickupAddress,
    pickupCity = pickupCity,
    pickupCountry = pickupCountry,
    deliveryAddress = deliveryAddress,
    deliveryCity = deliveryCity,
    deliveryCountry = deliveryCountry,
    packageWeightKg = packageWeightKg,
    packageDimensions = packageDimensions,
    packageType = packageType,
    fragile = fragile,
    packageValue = packageValue,
    packageDescription = packageDescription,
    urgencyLevel = urgencyLevel,
    maxPriceBudget = maxPriceBudget,
    pickupDatePreferred = pickupDatePreferred,
    pickupTimePreferred = pickupTimePreferred,
    pickupDateFlexible = pickupDateFlexible,
    deliveryDateNeeded = deliveryDateNeeded,
    deliveryTimeNeeded = deliveryTimeNeeded,
    specialHandlingRequirements = specialHandlingRequirements,
    requestStatus = requestStatus,
    createdAt = createdAt,
    updatedAt = updatedAt,
    compatibleTripsCount = compatibleTripsCount,
    shipper = shipper,
    images = images?.map { it.toDomain() },
    imagesProcessing = imagesProcessing,
    serviceType = serviceType,
    shoppingList = shoppingList.toDisplayString(),
    storeName = storeName,
    storeAddress = storeAddress,
    receiptRequired = receiptRequired,
)

fun PackageImageJson.toDomain(): PackageImage = PackageImage(
    id = id,
    packageRequestId = packageRequestId,
    imagePath = imagePath,
    displayOrder = displayOrder,
    originalFilename = originalFilename,
    url = url.ifBlank { imagePath },
    createdAt = createdAt,
)

fun AvailablePackageJson.toDomain(): AvailablePackage = AvailablePackage(
    id = id,
    packageRequestId = packageRequestId,
    pickupCity = pickupCity,
    pickupCountry = pickupCountry,
    deliveryCity = deliveryCity,
    deliveryCountry = deliveryCountry,
    packageWeightKg = packageWeightKg,
    packageDimensions = packageDimensions,
    urgencyLevel = urgencyLevel,
    maxPriceBudget = maxPriceBudget,
    pickupDatePreferred = pickupDatePreferred,
    pickupDateFlexible = pickupDateFlexible,
    deliveryDateNeeded = deliveryDateNeeded,
    fragile = fragile,
    packageType = packageType,
    packageDescription = packageDescription,
    createdAt = createdAt,
    daysSincePosted = daysSincePosted,
    distanceKm = distanceKm,
    shipper = shipper,
    serviceType = serviceType,
)

private fun JsonElement?.toDisplayString(): String? {
    val value = this ?: return null
    return when (value) {
        is JsonArray -> value.joinToString(separator = "\n") { entry ->
            val obj = entry as? JsonObject ?: return@joinToString entry.toString()
            val item = obj["item"]?.jsonPrimitive?.content.orEmpty()
            val quantity = obj["quantity"]?.jsonPrimitive?.content.orEmpty()
            val notes = obj["notes"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
            buildString {
                append(item)
                if (quantity.isNotBlank()) append(" x$quantity")
                if (notes != null) append(" - $notes")
            }.ifBlank { obj.toString() }
        }.ifBlank { null }
        else -> value.jsonPrimitive.contentOrNull ?: value.toString()
    }
}
