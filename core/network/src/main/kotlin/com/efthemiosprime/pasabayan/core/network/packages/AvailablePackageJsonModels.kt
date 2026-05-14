package com.efthemiosprime.pasabayan.core.network.packages

import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageType
import com.efthemiosprime.pasabayan.core.domain.`enum`.UrgencyLevel
import com.efthemiosprime.pasabayan.core.domain.model.PackageDimensions
import com.efthemiosprime.pasabayan.core.domain.model.UserSummary
import com.efthemiosprime.pasabayan.core.domain.util.FlexibleBoolNotNullSerializer
import com.efthemiosprime.pasabayan.core.domain.util.FlexibleDoubleSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AvailablePackageJson(
    val id: Int,
    @SerialName("package_request_id") val packageRequestId: Int? = null,
    @SerialName("pickup_city") val pickupCity: String = "",
    @SerialName("pickup_country") val pickupCountry: String? = null,
    @SerialName("delivery_city") val deliveryCity: String = "",
    @SerialName("delivery_country") val deliveryCountry: String? = null,
    @SerialName("package_weight_kg") @Serializable(with = FlexibleDoubleSerializer::class) val packageWeightKg: Double? = null,
    @SerialName("package_dimensions") val packageDimensions: PackageDimensions? = null,
    @SerialName("urgency_level") val urgencyLevel: UrgencyLevel = UrgencyLevel.NORMAL,
    @SerialName("max_price_budget") @Serializable(with = FlexibleDoubleSerializer::class) val maxPriceBudget: Double? = null,
    @SerialName("pickup_date_preferred") val pickupDatePreferred: String = "",
    @SerialName("pickup_time_preferred") val pickupTimePreferred: String? = null,
    @SerialName("pickup_date_flexible") @Serializable(with = FlexibleBoolNotNullSerializer::class) val pickupDateFlexible: Boolean = false,
    @SerialName("delivery_date_needed") val deliveryDateNeeded: String = "",
    @SerialName("delivery_time_needed") val deliveryTimeNeeded: String? = null,
    @Serializable(with = FlexibleBoolNotNullSerializer::class) val fragile: Boolean = false,
    @SerialName("package_type") val packageType: PackageType = PackageType.GENERAL,
    @SerialName("package_description") val packageDescription: String? = null,
    @SerialName("special_handling_requirements") val specialHandlingRequirements: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("days_since_posted") @Serializable(with = FlexibleDoubleSerializer::class) val daysSincePosted: Double? = null,
    val shipper: UserSummary? = null,
    @SerialName("service_type") val serviceType: String? = null,
    // Service / errand fields. `direction` is `receive`, `send`, or `task`;
    // legacy delivery rows omit it. Parsed via `ErrandDirection.fromApi(...)`.
    val direction: String? = null,
    @SerialName("store_name") val storeName: String? = null,
    @SerialName("recipient_name") val recipientName: String? = null,
    @SerialName("recipient_phone") val recipientPhone: String? = null,
    @SerialName("task_name") val taskName: String? = null,
    @SerialName("task_description") val taskDescription: String? = null,
    @SerialName("distance_km") @Serializable(with = FlexibleDoubleSerializer::class) val distanceKm: Double? = null,
)

@Serializable
data class AvailablePackagesResponseJson(
    val success: Boolean = false,
    val message: String = "",
    val nearby: Boolean? = null,
    val data: PaginatedAvailablePackagesJson? = null,
)

@Serializable
data class PaginatedAvailablePackagesJson(
    val data: List<AvailablePackageJson> = emptyList(),
    @SerialName("current_page") val currentPage: Int = 1,
    @SerialName("last_page") val lastPage: Int = 1,
    val total: Int = 0,
    @SerialName("per_page") val perPage: Int = 15,
)
