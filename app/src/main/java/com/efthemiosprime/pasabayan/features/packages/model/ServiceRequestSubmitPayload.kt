package com.efthemiosprime.pasabayan.features.packages.model

import java.time.LocalDate

data class ServiceRequestSubmitPayload(
    val serviceTypeCode: String,
    val shoppingItems: List<ServiceRequestShoppingItem>,
    val deliveryCity: String,
    val deliveryAddress: String,
    val storeName: String?,
    val storeAddress: String?,
    val estimatedCost: Double?,
    val maxPriceBudget: Double?,
    val deliveryDateNeeded: LocalDate?,
    val urgencyLevelCode: String?,
    val directionCode: String?,
    val recipientName: String?,
    val recipientPhone: String?,
    /** iOS parity: custom-task name; only set when `general_errand` + direction `task`. */
    val taskName: String? = null,
    /** iOS parity: custom-task description; only set when `general_errand` + direction `task`. */
    val taskDescription: String? = null,
    /** Optional store coordinates. Populated by [PackageLocationMap] integration (Slice E). */
    val storeLat: Double? = null,
    val storeLng: Double? = null,
    /** Optional delivery coordinates. Populated by map integration (Slice E). */
    val deliveryLat: Double? = null,
    val deliveryLng: Double? = null,
)

data class ServiceRequestShoppingItem(
    val item: String,
    val quantity: String,
    val notes: String?,
)
