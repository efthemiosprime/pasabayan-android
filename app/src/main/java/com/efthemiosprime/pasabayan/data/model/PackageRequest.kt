package com.efthemiosprime.pasabayan.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Package request model representing a delivery request
 * Mirrors iOS PackageRequest model structure
 */
@Serializable
data class PackageRequest(
    val id: Int,
    @SerialName("shipper_id")
    val shipperId: Int,
    val title: String,
    val description: String? = null,
    @SerialName("pickup_location")
    val pickupLocation: String,
    @SerialName("delivery_location")
    val deliveryLocation: String,
    @SerialName("pickup_coordinates")
    val pickupCoordinates: Coordinates? = null,
    @SerialName("delivery_coordinates")
    val deliveryCoordinates: Coordinates? = null,
    @SerialName("preferred_pickup_date")
    val preferredPickupDate: String,
    @SerialName("preferred_pickup_time")
    val preferredPickupTime: String? = null,
    @SerialName("preferred_delivery_date")
    val preferredDeliveryDate: String? = null,
    @SerialName("package_size")
    val packageSize: PackageSize,
    @SerialName("package_weight")
    val packageWeight: Double? = null,
    @SerialName("package_value")
    val packageValue: Double? = null,
    @SerialName("is_fragile")
    val isFragile: Boolean = false,
    @SerialName("special_instructions")
    val specialInstructions: String? = null,
    @SerialName("max_budget")
    val maxBudget: Double? = null,
    val status: PackageRequestStatus,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    val shipper: User? = null,
    @SerialName("compatible_trips_count")
    val compatibleTripsCount: Int? = null
)

/**
 * Backend API response structure for package requests
 * Matches the actual backend JSON structure with snake_case fields
 */
@Serializable
data class PackageRequestApiData(
    val id: Int,
    @SerialName("shipper_id")
    val shipperId: Int,
    @SerialName("pickup_address")
    val pickupAddress: String,
    @SerialName("pickup_city")
    val pickupCity: String,
    @SerialName("pickup_country")
    val pickupCountry: String,
    @SerialName("pickup_lat")
    val pickupLat: Double? = null,
    @SerialName("pickup_lng")
    val pickupLng: Double? = null,
    @SerialName("delivery_address")
    val deliveryAddress: String,
    @SerialName("delivery_city")
    val deliveryCity: String,
    @SerialName("delivery_country")
    val deliveryCountry: String,
    @SerialName("delivery_lat")
    val deliveryLat: Double? = null,
    @SerialName("delivery_lng")
    val deliveryLng: Double? = null,
    @SerialName("package_weight_kg")
    val packageWeightKg: String,
    @SerialName("package_dimensions")
    val packageDimensions: PackageDimensions? = null,
    @SerialName("package_type")
    val packageType: String,
    val fragile: Boolean = false,
    @SerialName("package_value")
    val packageValue: String,
    @SerialName("package_description")
    val packageDescription: String,
    @SerialName("urgency_level")
    val urgencyLevel: String,
    @SerialName("max_price_budget")
    val maxPriceBudget: String,
    @SerialName("pickup_date_preferred")
    val pickupDatePreferred: String,
    @SerialName("pickup_date_flexible")
    val pickupDateFlexible: Boolean = false,
    @SerialName("delivery_date_needed")
    val deliveryDateNeeded: String,
    @SerialName("special_handling_requirements")
    val specialHandlingRequirements: String? = null,
    @SerialName("request_status")
    val requestStatus: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    val shipper: User? = null
) {
    /**
     * Convert backend API response to UI model
     */
    fun toPackageRequest(): PackageRequest {
        return PackageRequest(
            id = id,
            shipperId = shipperId,
            title = packageDescription,
            description = specialHandlingRequirements,
            pickupLocation = pickupAddress,
            deliveryLocation = deliveryAddress,
            pickupCoordinates = if (pickupLat != null && pickupLng != null) {
                Coordinates(pickupLat, pickupLng)
            } else null,
            deliveryCoordinates = if (deliveryLat != null && deliveryLng != null) {
                Coordinates(deliveryLat, deliveryLng)
            } else null,
            preferredPickupDate = pickupDatePreferred,
            preferredPickupTime = null,
            preferredDeliveryDate = deliveryDateNeeded,
            packageSize = PackageSize.fromPackageType(packageType),
            packageWeight = packageWeightKg.toDoubleOrNull(),
            packageValue = packageValue.toDoubleOrNull(),
            isFragile = fragile,
            specialInstructions = specialHandlingRequirements,
            maxBudget = maxPriceBudget.toDoubleOrNull(),
            status = PackageRequestStatus.fromString(requestStatus),
            createdAt = createdAt,
            updatedAt = updatedAt,
            shipper = shipper
        )
    }
}

/**
 * Coordinates model for location data
 */
@Serializable
data class Coordinates(
    val latitude: Double,
    val longitude: Double
)

/**
 * Package dimensions model
 */
@Serializable
data class PackageDimensions(
    val length: Int,
    val width: Int,
    val height: Int
)

/**
 * Package size enumeration
 */
@Serializable
enum class PackageSize {
    @SerialName("small")
    SMALL,
    @SerialName("medium")
    MEDIUM,
    @SerialName("large")
    LARGE,
    @SerialName("extra_large")
    EXTRA_LARGE;
    
    val displayName: String
        get() = when (this) {
            SMALL -> "Small"
            MEDIUM -> "Medium"
            LARGE -> "Large"
            EXTRA_LARGE -> "Extra Large"
        }
    
    val description: String
        get() = when (this) {
            SMALL -> "Up to 5kg - fits in a bag"
            MEDIUM -> "5-15kg - medium box"
            LARGE -> "15-30kg - large box"
            EXTRA_LARGE -> "30kg+ - requires special handling"
        }
    
    val icon: String
        get() = when (this) {
            SMALL -> "📦"
            MEDIUM -> "📦"
            LARGE -> "📦"
            EXTRA_LARGE -> "📦"
        }
    
    companion object {
        fun fromPackageType(type: String): PackageSize {
            return when (type.lowercase()) {
                "electronics", "documents" -> SMALL
                "clothing", "books" -> MEDIUM
                "furniture", "appliances" -> LARGE
                else -> MEDIUM
            }
        }
    }
}

/**
 * Package request status enumeration
 */
@Serializable
enum class PackageRequestStatus {
    @SerialName("pending")
    PENDING,
    @SerialName("open")
    OPEN,
    @SerialName("matched")
    MATCHED,
    @SerialName("booked")
    BOOKED,
    @SerialName("in_transit")
    IN_TRANSIT,
    @SerialName("delivered")
    DELIVERED,
    @SerialName("cancelled")
    CANCELLED;
    
    val displayName: String
        get() = when (this) {
            PENDING -> "Pending"
            OPEN -> "Open"
            MATCHED -> "Matched"
            BOOKED -> "Booked"
            IN_TRANSIT -> "In Transit"
            DELIVERED -> "Delivered"
            CANCELLED -> "Cancelled"
        }
    
    val color: String
        get() = when (this) {
            PENDING -> "orange"
            OPEN -> "blue"
            MATCHED -> "blue"
            BOOKED -> "purple"
            IN_TRANSIT -> "green"
            DELIVERED -> "gray"
            CANCELLED -> "red"
        }
    
    val icon: String
        get() = when (this) {
            PENDING -> "⏳"
            OPEN -> "📂"
            MATCHED -> "🔗"
            BOOKED -> "✅"
            IN_TRANSIT -> "🚛"
            DELIVERED -> "✅"
            CANCELLED -> "❌"
        }
    
    companion object {
        fun fromString(status: String): PackageRequestStatus {
            return when (status.lowercase()) {
                "pending" -> PENDING
                "open" -> OPEN
                "matched" -> MATCHED
                "booked" -> BOOKED
                "in_transit" -> IN_TRANSIT
                "delivered" -> DELIVERED
                "cancelled" -> CANCELLED
                else -> PENDING
            }
        }
    }
}

/**
 * Create package request model for backend API
 * Matches the exact field names and structure expected by the backend
 */
@Serializable
data class CreatePackageRequestApi(
    @SerialName("pickup_address")
    val pickupAddress: String,
    @SerialName("pickup_city")
    val pickupCity: String,
    @SerialName("pickup_country")
    val pickupCountry: String,
    @SerialName("delivery_address")
    val deliveryAddress: String,
    @SerialName("delivery_city")
    val deliveryCity: String,
    @SerialName("delivery_country")
    val deliveryCountry: String,
    @SerialName("package_weight_kg")
    val packageWeightKg: String,
    @SerialName("package_dimensions")
    val packageDimensions: PackageDimensions,
    @SerialName("package_type")
    val packageType: String,
    @SerialName("package_value")
    val packageValue: String,
    @SerialName("package_description")
    val packageDescription: String,
    @SerialName("urgency_level")
    val urgencyLevel: String,
    @SerialName("max_price_budget")
    val maxPriceBudget: String,
    @SerialName("pickup_date_preferred")
    val pickupDatePreferred: String,
    @SerialName("pickup_date_flexible")
    val pickupDateFlexible: Boolean = false,
    @SerialName("delivery_date_needed")
    val deliveryDateNeeded: String,
    val fragile: Boolean = false,
    @SerialName("special_handling_requirements")
    val specialHandlingRequirements: String? = null
)

/**
 * Create package request model - Updated to collect all required fields
 */
@Serializable
data class CreatePackageRequest(
    val title: String,
    val description: String? = null,
    @SerialName("pickup_address")
    val pickupAddress: String,
    @SerialName("pickup_city")
    val pickupCity: String,
    @SerialName("pickup_country")
    val pickupCountry: String = "Canada", // Default for now
    @SerialName("delivery_address")
    val deliveryAddress: String,
    @SerialName("delivery_city")
    val deliveryCity: String,
    @SerialName("delivery_country")
    val deliveryCountry: String = "Philippines", // Default for now
    @SerialName("pickup_coordinates")
    val pickupCoordinates: Coordinates? = null,
    @SerialName("delivery_coordinates")
    val deliveryCoordinates: Coordinates? = null,
    @SerialName("preferred_pickup_date")
    val preferredPickupDate: String,
    @SerialName("preferred_pickup_time")
    val preferredPickupTime: String? = null,
    @SerialName("preferred_delivery_date")
    val preferredDeliveryDate: String? = null,
    @SerialName("package_size")
    val packageSize: PackageSize,
    @SerialName("package_weight")
    val packageWeight: Double? = null,
    @SerialName("package_dimensions")
    val packageDimensions: PackageDimensions,
    @SerialName("package_type")
    val packageType: String,
    @SerialName("package_value")
    val packageValue: Double? = null,
    @SerialName("is_fragile")
    val isFragile: Boolean = false,
    @SerialName("urgency_level")
    val urgencyLevel: String = "normal",
    @SerialName("special_instructions")
    val specialInstructions: String? = null,
    @SerialName("max_budget")
    val maxBudget: Double? = null,
    @SerialName("pickup_date_flexible")
    val pickupDateFlexible: Boolean = false
) {
    /**
     * Convert to backend API format
     */
    fun toApiRequest(): CreatePackageRequestApi {
        return CreatePackageRequestApi(
            pickupAddress = pickupAddress,
            pickupCity = pickupCity,
            pickupCountry = pickupCountry,
            deliveryAddress = deliveryAddress,
            deliveryCity = deliveryCity,
            deliveryCountry = deliveryCountry,
            packageWeightKg = packageWeight?.toString() ?: "0",
            packageDimensions = packageDimensions,
            packageType = packageType,
            packageValue = packageValue?.toString() ?: "0",
            packageDescription = title,
            urgencyLevel = urgencyLevel,
            maxPriceBudget = maxBudget?.toString() ?: "0",
            pickupDatePreferred = preferredPickupDate,
            pickupDateFlexible = pickupDateFlexible,
            deliveryDateNeeded = preferredDeliveryDate ?: preferredPickupDate,
            fragile = isFragile,
            specialHandlingRequirements = specialInstructions
        )
    }
}

/**
 * Package type enumeration - Updated to match iOS implementation
 */
enum class PackageType(val value: String, val displayName: String) {
    ELECTRONICS("electronics", "Electronics"),
    CLOTHING("clothing", "Clothing"),
    BOOKS("books", "Books"),
    FOOD("food", "Food"),
    FURNITURE("furniture", "Furniture"),
    MEDICAL("medical", "Medical"),
    DOCUMENTS("documents", "Documents"),
    GIFTS("gifts", "Gifts"),
    AUTOMOTIVE("automotive", "Automotive"),
    BEAUTY("beauty", "Beauty"),
    SPORTS("sports", "Sports"),
    TOYS("toys", "Toys"),
    HOUSEHOLD("household", "Household"),
    JEWELRY("jewelry", "Jewelry"),
    ART("art", "Art"),
    OTHER("other", "Other");
    
    companion object {
        fun fromString(value: String): PackageType {
            return values().find { it.value == value } ?: OTHER
        }
    }
}

/**
 * Urgency level enumeration
 */
enum class UrgencyLevel(val value: String, val displayName: String) {
    LOW("low", "Low Priority"),
    NORMAL("normal", "Normal"),
    HIGH("high", "High Priority"),
    URGENT("urgent", "Urgent");
    
    companion object {
        fun fromString(value: String): UrgencyLevel {
            return values().find { it.value == value } ?: NORMAL
        }
    }
}

/**
 * Compatible trip model for matching trips with package requests
 */
@Serializable
data class CompatibleTrip(
    val trip: Trip,
    @SerialName("match_score")
    val matchScore: Double,
    @SerialName("estimated_price")
    val estimatedPrice: Double,
    @SerialName("compatibility_reasons")
    val compatibilityReasons: List<String> = emptyList()
) {
    val id: Int get() = trip.id
}

/**
 * Paginated response model for Laravel pagination
 * Handles the pagination structure returned by the server
 */
@Serializable
data class PaginatedResponse<T>(
    @SerialName("current_page")
    val currentPage: Int,
    val data: List<T>,
    @SerialName("first_page_url")
    val firstPageUrl: String,
    val from: Int?,
    @SerialName("last_page")
    val lastPage: Int,
    @SerialName("last_page_url")
    val lastPageUrl: String,
    val links: List<PaginationLink>,
    @SerialName("next_page_url")
    val nextPageUrl: String?,
    val path: String,
    @SerialName("per_page")
    val perPage: Int,
    @SerialName("prev_page_url")
    val prevPageUrl: String?,
    val to: Int?,
    val total: Int
)

/**
 * Pagination link model
 */
@Serializable
data class PaginationLink(
    val url: String?,
    val label: String,
    val active: Boolean
)

/**
 * Package request response model
 */
@Serializable
data class PackageRequestResponse(
    val success: Boolean = true,
    val message: String,
    val data: PackageRequestApiData
)

/**
 * Package requests list response model - uses API data format
 */
@Serializable
data class PackageRequestsResponse(
    val success: Boolean = true,
    val message: String,
    val data: PaginatedResponse<PackageRequestApiData>
)

/**
 * Compatible trips response model
 */
@Serializable
data class CompatibleTripsResponse(
    val success: Boolean = true,
    val message: String,
    val data: List<CompatibleTrip>
) 