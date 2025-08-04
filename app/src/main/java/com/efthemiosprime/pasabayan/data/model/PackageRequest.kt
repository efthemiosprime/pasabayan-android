package com.efthemiosprime.pasabayan.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Limited shipper model for package listings
 * Used when API returns only basic shipper info without email
 */
@Serializable
data class PackageShipper(
    val id: Int,
    val name: String,
    val rating: String, // API returns rating as string, not Double
    @SerialName("total_ratings")
    val totalRatings: Int,
    @SerialName("verification_level")
    val verificationLevel: String
) {
    /**
     * Convert to User model for UI compatibility
     * Fills in required email field with placeholder
     */
    fun toUser(): User = User(
        id = id,
        name = name,
        email = "", // Not provided in package listings
        rating = rating.toDoubleOrNull(),
        totalRatings = totalRatings,
        verificationLevel = verificationLevel
    )
}

/**
 * Available package model for /api/packages/available endpoint
 * This endpoint returns a different structure than regular package requests
 */
@Serializable
data class AvailablePackageApiData(
    val id: Int,
    @SerialName("pickup_city")
    val pickupCity: String,
    @SerialName("pickup_country")
    val pickupCountry: String,
    @SerialName("delivery_city")
    val deliveryCity: String,
    @SerialName("delivery_country")
    val deliveryCountry: String,
    @SerialName("package_weight_kg")
    val packageWeightKg: String,
    @SerialName("package_dimensions")
    val packageDimensions: PackageDimensions? = null,
    @SerialName("volume_liters")
    val volumeLiters: Double? = null,
    @SerialName("package_type")
    val packageType: String,
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
    val specialHandlingRequirements: String? = null,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("days_since_posted")
    val daysSincePosted: Double? = null,
    @SerialName("urgency_display")
    val urgencyDisplay: String? = null,
    val shipper: PackageShipper
) {
    /**
     * Convert available package API data to UI model
     */
    fun toPackageRequest(): PackageRequest {
        return PackageRequest(
            id = id,
            shipperId = shipper.id,
            title = packageDescription,
            description = specialHandlingRequirements,
            pickupLocation = "$pickupCity, $pickupCountry",
            deliveryLocation = "$deliveryCity, $deliveryCountry",
            pickupCoordinates = null, // Not provided in available packages
            deliveryCoordinates = null, // Not provided in available packages
            preferredPickupDate = pickupDatePreferred,
            preferredPickupTime = null,
            preferredDeliveryDate = deliveryDateNeeded,
            packageSize = PackageSize.fromPackageType(packageType),
            packageWeight = packageWeightKg.toDoubleOrNull(),
            packageValue = null, // Not provided in available packages
            isFragile = fragile,
            specialInstructions = specialHandlingRequirements,
            maxBudget = maxPriceBudget.toDoubleOrNull(),
            status = PackageRequestStatus.OPEN, // Available packages are open for matching
            createdAt = createdAt,
            updatedAt = createdAt, // Use created_at as fallback since updated_at not provided
            shipper = shipper.toUser(),
            compatibleTripsCount = null
        )
    }
}

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
    @SerialName("volume_liters")
    val volumeLiters: Double? = null,
    @SerialName("days_since_posted")
    val daysSincePosted: Double? = null,
    @SerialName("urgency_display")
    val urgencyDisplay: String? = null,
    @SerialName("request_status")
    val requestStatus: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    val shipper: PackageShipper? = null
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
            shipper = shipper?.toUser()
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
 * Package dimensions model - Matching iOS PackageDimensions exactly
 */
@Serializable
data class PackageDimensions(
    val length: Double,
    val width: Double,
    val height: Double
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
 * Package request status enumeration - exactly matching iOS PackageRequestStatus
 */
@Serializable
enum class PackageRequestStatus {
    @SerialName("open")
    OPEN,
    @SerialName("pending_request")
    PENDING_REQUEST,
    @SerialName("matched")
    MATCHED,
    @SerialName("delivered")
    DELIVERED,
    @SerialName("cancelled")
    CANCELLED,
    
    // Legacy statuses for backward compatibility
    @SerialName("pending")
    PENDING,
    @SerialName("booked")
    BOOKED,
    @SerialName("in_transit")
    IN_TRANSIT;
    
    val displayName: String
        get() = when (this) {
            OPEN -> "Open"
            PENDING_REQUEST -> "Pending Request"
            MATCHED -> "Matched"
            DELIVERED -> "Delivered"
            CANCELLED -> "Cancelled"
            PENDING -> "Pending"
            BOOKED -> "Booked"
            IN_TRANSIT -> "In Transit"
        }
    
    val color: String
        get() = when (this) {
            OPEN -> "blue"
            PENDING_REQUEST -> "orange"
            MATCHED -> "purple"
            DELIVERED -> "green"
            CANCELLED -> "red"
            PENDING -> "orange"
            BOOKED -> "purple"
            IN_TRANSIT -> "yellow"
        }
    
    val icon: String
        get() = when (this) {
            OPEN -> "📂"
            PENDING_REQUEST -> "📋"
            MATCHED -> "🔗"
            DELIVERED -> "✅"
            CANCELLED -> "❌"
            PENDING -> "⏳"
            BOOKED -> "✅"
            IN_TRANSIT -> "🚛"
        }
    
    companion object {
        fun fromString(status: String): PackageRequestStatus {
            return when (status.lowercase()) {
                "open" -> OPEN
                "pending_request" -> PENDING_REQUEST
                "matched" -> MATCHED
                "delivered" -> DELIVERED
                "cancelled" -> CANCELLED
                "pending" -> PENDING
                "booked" -> BOOKED
                "in_transit" -> IN_TRANSIT
                else -> OPEN
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
 * Package type enumeration - Exactly matching iOS PackageType enum
 */
@Serializable
enum class PackageType {
    @SerialName("general")
    GENERAL,
    @SerialName("electronics")
    ELECTRONICS,
    @SerialName("clothing")
    CLOTHING,
    @SerialName("books")
    BOOKS,
    @SerialName("food")
    FOOD,
    @SerialName("furniture")
    FURNITURE,
    @SerialName("medical")
    MEDICAL,
    @SerialName("documents")
    DOCUMENTS,
    @SerialName("gifts")
    GIFTS,
    @SerialName("automotive")
    AUTOMOTIVE,
    @SerialName("beauty")
    BEAUTY,
    @SerialName("sports")
    SPORTS,
    @SerialName("toys")
    TOYS,
    @SerialName("household")
    HOUSEHOLD,
    @SerialName("jewelry")
    JEWELRY,
    @SerialName("art")
    ART,
    @SerialName("industrial")
    INDUSTRIAL,
    @SerialName("other")
    OTHER;
    
    val displayName: String
        get() = when (this) {
            GENERAL -> "General"
            ELECTRONICS -> "Electronics"
            CLOTHING -> "Clothing"
            BOOKS -> "Books"
            FOOD -> "Food"
            FURNITURE -> "Furniture"
            MEDICAL -> "Medical"
            DOCUMENTS -> "Documents"
            GIFTS -> "Gifts"
            AUTOMOTIVE -> "Automotive"
            BEAUTY -> "Beauty"
            SPORTS -> "Sports"
            TOYS -> "Toys"
            HOUSEHOLD -> "Household"
            JEWELRY -> "Jewelry"
            ART -> "Art"
            INDUSTRIAL -> "Industrial"
            OTHER -> "Other"
        }
    
    val icon: String
        get() = when (this) {
            GENERAL -> "📦"
            ELECTRONICS -> "💻"
            CLOTHING -> "👕"
            BOOKS -> "📚"
            FOOD -> "🍕"
            FURNITURE -> "🪑"
            MEDICAL -> "💊"
            DOCUMENTS -> "📄"
            GIFTS -> "🎁"
            AUTOMOTIVE -> "🚗"
            BEAUTY -> "💄"
            SPORTS -> "⚽"
            TOYS -> "🧸"
            HOUSEHOLD -> "🏠"
            JEWELRY -> "💎"
            ART -> "🎨"
            INDUSTRIAL -> "🏭"
            OTHER -> "📦"
        }
    
    val serializedName: String
        get() = when (this) {
            GENERAL -> "general"
            ELECTRONICS -> "electronics"
            CLOTHING -> "clothing"
            BOOKS -> "books"
            FOOD -> "food"
            FURNITURE -> "furniture"
            MEDICAL -> "medical"
            DOCUMENTS -> "documents"
            GIFTS -> "gifts"
            AUTOMOTIVE -> "automotive"
            BEAUTY -> "beauty"
            SPORTS -> "sports"
            TOYS -> "toys"
            HOUSEHOLD -> "household"
            JEWELRY -> "jewelry"
            ART -> "art"
            INDUSTRIAL -> "industrial"
            OTHER -> "other"
        }
    
    companion object {
        fun fromString(value: String): PackageType {
            return when (value.lowercase()) {
                "general" -> GENERAL
                "electronics" -> ELECTRONICS
                "clothing" -> CLOTHING
                "books" -> BOOKS
                "food" -> FOOD
                "furniture" -> FURNITURE
                "medical" -> MEDICAL
                "documents" -> DOCUMENTS
                "gifts" -> GIFTS
                "automotive" -> AUTOMOTIVE
                "beauty" -> BEAUTY
                "sports" -> SPORTS
                "toys" -> TOYS
                "household" -> HOUSEHOLD
                "jewelry" -> JEWELRY
                "art" -> ART
                "industrial" -> INDUSTRIAL
                "other" -> OTHER
                else -> OTHER
            }
        }
    }
}

/**
 * Urgency level enumeration - Exactly matching iOS UrgencyLevel enum
 */
@Serializable
enum class UrgencyLevel {
    @SerialName("low")
    LOW,
    @SerialName("normal")
    NORMAL,
    @SerialName("high")
    HIGH,
    @SerialName("urgent")
    URGENT;
    
    val displayName: String
        get() = when (this) {
            LOW -> "Low"
            NORMAL -> "Normal"
            HIGH -> "High"
            URGENT -> "Urgent"
        }
    
    val color: String
        get() = when (this) {
            LOW -> "gray"
            NORMAL -> "blue"
            HIGH -> "orange"
            URGENT -> "red"
        }
    
    val icon: String
        get() = when (this) {
            LOW -> "🐌"
            NORMAL -> "📦"
            HIGH -> "⚡"
            URGENT -> "🚨"
        }
    
    val serializedName: String
        get() = when (this) {
            LOW -> "low"
            NORMAL -> "normal"
            HIGH -> "high"
            URGENT -> "urgent"
        }
    
    companion object {
        fun fromString(value: String): UrgencyLevel {
            return when (value.lowercase()) {
                "low" -> LOW
                "normal" -> NORMAL
                "high" -> HIGH
                "urgent" -> URGENT
                else -> NORMAL
            }
        }
    }
}

/**
 * Compatible trip model - iOS parity
 * API returns Trip objects directly, so we use typealias to match iOS behavior
 * This matches how iOS handles compatible trips as Trip objects directly
 */
typealias CompatibleTrip = Trip

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
 * Available packages response model for /api/packages/available endpoint
 */
@Serializable
data class AvailablePackagesResponse(
    val success: Boolean = true,
    val message: String,
    val data: PaginatedResponse<AvailablePackageApiData>
)

/**
 * Compatible trips response model - uses paginated structure
 */
@Serializable
data class CompatibleTripsResponse(
    val success: Boolean = true,
    val message: String,
    val data: PaginatedResponse<CompatibleTrip>
)

/**
 * iOS AvailablePackage model - Exactly matching iOS structure
 * Used for /packages/available endpoint responses
 */
@Serializable
data class AvailablePackage(
    val id: Int,
    @SerialName("pickup_city")
    val pickupCity: String,
    @SerialName("delivery_city")
    val deliveryCity: String,
    @SerialName("package_weight_kg")
    val packageWeightKg: Double,
    @SerialName("package_dimensions")
    val packageDimensions: PackageDimensions? = null,
    @SerialName("volume_liters")
    val volumeLiters: Double? = null,
    @SerialName("urgency_level")
    val urgencyLevel: UrgencyLevel,
    @SerialName("max_price_budget")
    val maxPriceBudget: Double? = null,
    @SerialName("pickup_date_preferred")
    val pickupDatePreferred: String,
    @SerialName("delivery_date_needed")
    val deliveryDateNeeded: String,
    val fragile: Boolean,
    @SerialName("package_type")
    val packageType: PackageType,
    @SerialName("package_description")
    val packageDescription: String? = null,
    @SerialName("created_at")
    val createdAt: String,
    val shipper: AvailablePackageShipper? = null
)

/**
 * iOS AvailablePackageShipper model - Exactly matching iOS structure
 */
@Serializable
data class AvailablePackageShipper(
    val id: Int,
    val name: String,
    val rating: String
)

 