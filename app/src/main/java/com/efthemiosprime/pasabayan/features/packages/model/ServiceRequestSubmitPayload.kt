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
)

data class ServiceRequestShoppingItem(
    val item: String,
    val quantity: String,
    val notes: String?,
)
