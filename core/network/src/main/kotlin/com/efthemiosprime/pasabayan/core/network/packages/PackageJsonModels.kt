package com.efthemiosprime.pasabayan.core.network.packages

import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageRequestStatus
import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageType
import com.efthemiosprime.pasabayan.core.domain.`enum`.UrgencyLevel
import com.efthemiosprime.pasabayan.core.domain.model.PackageDimensions
import com.efthemiosprime.pasabayan.core.domain.model.UserSummary
import com.efthemiosprime.pasabayan.core.domain.util.FlexibleBoolSerializer
import com.efthemiosprime.pasabayan.core.domain.util.FlexibleDoubleSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class PackageRequestJson(
    val id: Int,
    @SerialName("shipper_id") val shipperId: Int? = null,
    @SerialName("pickup_address") val pickupAddress: String? = null,
    @SerialName("pickup_city") val pickupCity: String? = null,
    @SerialName("pickup_country") val pickupCountry: String? = null,
    @SerialName("delivery_address") val deliveryAddress: String? = null,
    @SerialName("delivery_city") val deliveryCity: String? = null,
    @SerialName("delivery_country") val deliveryCountry: String? = null,
    @SerialName("package_weight_kg") @Serializable(with = FlexibleDoubleSerializer::class) val packageWeightKg: Double? = null,
    @SerialName("package_dimensions") val packageDimensions: PackageDimensions? = null,
    @SerialName("package_type") val packageType: PackageType? = null,
    @Serializable(with = FlexibleBoolSerializer::class) val fragile: Boolean? = null,
    @SerialName("package_value") @Serializable(with = FlexibleDoubleSerializer::class) val packageValue: Double? = null,
    @SerialName("package_description") val packageDescription: String? = null,
    @SerialName("urgency_level") val urgencyLevel: UrgencyLevel? = null,
    @SerialName("max_price_budget") @Serializable(with = FlexibleDoubleSerializer::class) val maxPriceBudget: Double? = null,
    @SerialName("pickup_date_preferred") val pickupDatePreferred: String? = null,
    @SerialName("pickup_time_preferred") val pickupTimePreferred: String? = null,
    @SerialName("pickup_date_flexible") @Serializable(with = FlexibleBoolSerializer::class) val pickupDateFlexible: Boolean? = null,
    @SerialName("delivery_date_needed") val deliveryDateNeeded: String? = null,
    @SerialName("delivery_time_needed") val deliveryTimeNeeded: String? = null,
    @SerialName("special_handling_requirements") val specialHandlingRequirements: String? = null,
    @SerialName("request_status") val requestStatus: PackageRequestStatus? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("compatible_trips_count") val compatibleTripsCount: Int? = null,
    val shipper: UserSummary? = null,
    val images: List<PackageImageJson>? = null,
    @SerialName("images_processing") @Serializable(with = FlexibleBoolSerializer::class) val imagesProcessing: Boolean? = null,
    // Service request fields
    @SerialName("service_type") val serviceType: String? = null,
    /**
     * Errand direction: `receive`, `send`, or `task`. Null on legacy delivery rows.
     * Parse via `ErrandDirection.fromApi(...)` — strict casing is intentional.
     */
    val direction: String? = null,
    @SerialName("shopping_list") val shoppingList: JsonElement? = null,
    @SerialName("store_name") val storeName: String? = null,
    @SerialName("store_address") val storeAddress: String? = null,
    @SerialName("receipt_required") @Serializable(with = FlexibleBoolSerializer::class) val receiptRequired: Boolean? = null,
    /** Recipient for `direction=send`. */
    @SerialName("recipient_name") val recipientName: String? = null,
    @SerialName("recipient_phone") val recipientPhone: String? = null,
    /** Custom-task name. iOS parity: returned for `service_type=general_errand` with direction `task`. */
    @SerialName("task_name") val taskName: String? = null,
    /** Custom-task description. iOS parity: returned for `service_type=general_errand` with direction `task`. */
    @SerialName("task_description") val taskDescription: String? = null,
    // Coordinates (iOS parity — used by detail-screen map pins + compatibility math)
    @SerialName("pickup_lat") @Serializable(with = FlexibleDoubleSerializer::class) val pickupLat: Double? = null,
    @SerialName("pickup_lng") @Serializable(with = FlexibleDoubleSerializer::class) val pickupLng: Double? = null,
    @SerialName("delivery_lat") @Serializable(with = FlexibleDoubleSerializer::class) val deliveryLat: Double? = null,
    @SerialName("delivery_lng") @Serializable(with = FlexibleDoubleSerializer::class) val deliveryLng: Double? = null,
    // Compatibility fields
    @SerialName("compatibility_score") @Serializable(with = FlexibleDoubleSerializer::class) val compatibilityScore: Double? = null,
    @SerialName("estimated_earnings") @Serializable(with = FlexibleDoubleSerializer::class) val estimatedEarnings: Double? = null,
    @SerialName("distance_km") @Serializable(with = FlexibleDoubleSerializer::class) val distanceKm: Double? = null,
)

@Serializable
data class PackageImageJson(
    val id: Int,
    @SerialName("package_request_id") val packageRequestId: Int = 0,
    @SerialName("image_path") val imagePath: String = "",
    @SerialName("display_order") val displayOrder: Int = 0,
    @SerialName("original_filename") val originalFilename: String? = null,
    val url: String = "",
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class CreatePackageRequestJson(
    @SerialName("pickup_address") val pickupAddress: String,
    @SerialName("pickup_city") val pickupCity: String,
    @SerialName("pickup_country") val pickupCountry: String,
    @SerialName("delivery_address") val deliveryAddress: String,
    @SerialName("delivery_city") val deliveryCity: String,
    @SerialName("delivery_country") val deliveryCountry: String,
    @SerialName("package_weight_kg") val packageWeightKg: Double,
    @SerialName("package_type") val packageType: String,
    val fragile: Boolean,
    @SerialName("urgency_level") val urgencyLevel: String,
    @SerialName("pickup_date_preferred") val pickupDatePreferred: String,
    @SerialName("pickup_date_flexible") val pickupDateFlexible: Boolean,
    @SerialName("delivery_date_needed") val deliveryDateNeeded: String,
    @SerialName("package_value") val packageValue: Double? = null,
    @SerialName("package_description") val packageDescription: String? = null,
    @SerialName("max_price_budget") val maxPriceBudget: Double? = null,
    @SerialName("pickup_time_preferred") val pickupTimePreferred: String? = null,
    @SerialName("delivery_time_needed") val deliveryTimeNeeded: String? = null,
    @SerialName("special_handling_requirements") val specialHandlingRequirements: String? = null,
    @SerialName("pickup_city_id") val pickupCityId: Int? = null,
    @SerialName("delivery_city_id") val deliveryCityId: Int? = null,
)

@Serializable
data class PackageUpdateRequestJson(
    @SerialName("max_price_budget") val maxPriceBudget: Double? = null,
    @SerialName("urgency_level") val urgencyLevel: String? = null,
    @SerialName("pickup_date_flexible") val pickupDateFlexible: Boolean? = null,
    @SerialName("delivery_date_needed") val deliveryDateNeeded: String? = null,
    @SerialName("special_handling_requirements") val specialHandlingRequirements: String? = null,
    @SerialName("request_status") val requestStatus: String? = null,
)

// -- Response wrappers --

@Serializable
data class PackageRequestResponseJson(
    val success: Boolean? = null,
    val message: String = "",
    val data: PackageRequestJson? = null,
)

@Serializable
data class PackageRequestsResponseJson(
    val message: String = "",
    val data: PaginatedPackageRequestsJson? = null,
)

@Serializable
data class PaginatedPackageRequestsJson(
    val data: List<PackageRequestJson> = emptyList(),
    @SerialName("current_page") val currentPage: Int = 1,
    @SerialName("last_page") val lastPage: Int = 1,
    val total: Int = 0,
    @SerialName("per_page") val perPage: Int = 15,
)
