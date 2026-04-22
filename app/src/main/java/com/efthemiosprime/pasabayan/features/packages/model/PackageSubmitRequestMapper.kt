package com.efthemiosprime.pasabayan.features.packages.model

import com.efthemiosprime.pasabayan.core.network.packages.CreatePackageRequestJson
import com.efthemiosprime.pasabayan.core.network.packages.CreateServiceRequestBodyJson
import com.efthemiosprime.pasabayan.core.network.packages.ShoppingItemJson
import java.time.format.DateTimeFormatter

object PackageSubmitRequestMapper {
    private val apiDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val apiTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun toCreatePackageRequestJson(payload: PackageSubmitPayload): CreatePackageRequestJson {
        return CreatePackageRequestJson(
            pickupAddress = payload.pickupAddress,
            pickupCity = payload.pickupCity,
            pickupCountry = payload.pickupCountryCode,
            deliveryAddress = payload.deliveryAddress,
            deliveryCity = payload.deliveryCity,
            deliveryCountry = payload.deliveryCountryCode,
            packageWeightKg = payload.packageWeightKg,
            packageType = payload.packageTypeCode,
            fragile = payload.fragile,
            urgencyLevel = payload.urgencyLevelCode,
            pickupDatePreferred = payload.pickupDatePreferred.format(apiDateFormatter),
            pickupDateFlexible = payload.pickupDateFlexible,
            deliveryDateNeeded = payload.deliveryDateNeeded.format(apiDateFormatter),
            packageValue = payload.packageValue,
            packageDescription = payload.packageDescription?.trim()?.takeIf { it.isNotBlank() },
            maxPriceBudget = payload.maxPriceBudget,
            pickupTimePreferred = payload.pickupTimePreferred?.format(apiTimeFormatter),
            deliveryTimeNeeded = payload.deliveryTimeNeeded?.format(apiTimeFormatter),
            specialHandlingRequirements = payload.specialHandlingRequirements?.trim()?.takeIf { it.isNotBlank() },
        )
    }

    fun toCreateServiceRequestBodyJson(payload: ServiceRequestSubmitPayload): CreateServiceRequestBodyJson {
        return CreateServiceRequestBodyJson(
            serviceType = payload.serviceTypeCode,
            shoppingList = payload.shoppingItems.map { item ->
                ShoppingItemJson(
                    item = item.item.trim(),
                    quantity = item.quantity.trim(),
                    notes = item.notes?.trim()?.takeIf { it.isNotBlank() },
                )
            },
            deliveryCity = payload.deliveryCity.trim(),
            deliveryAddress = payload.deliveryAddress.trim(),
            storeName = payload.storeName?.trim()?.takeIf { it.isNotBlank() },
            storeAddress = payload.storeAddress?.trim()?.takeIf { it.isNotBlank() },
            estimatedCost = payload.estimatedCost,
            maxPriceBudget = payload.maxPriceBudget,
            deliveryDateNeeded = payload.deliveryDateNeeded?.format(apiDateFormatter),
            urgencyLevel = payload.urgencyLevelCode,
            direction = payload.directionCode,
            recipientName = payload.recipientName?.trim()?.takeIf { it.isNotBlank() },
            recipientPhone = payload.recipientPhone?.trim()?.takeIf { it.isNotBlank() },
        )
    }
}
