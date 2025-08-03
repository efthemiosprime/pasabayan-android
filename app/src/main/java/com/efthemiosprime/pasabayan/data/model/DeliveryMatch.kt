package com.efthemiosprime.pasabayan.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * TrackingNote model - for tracking_notes array
 */
@Serializable
data class TrackingNote(
    val timestamp: String,
    val note: String
)

/**
 * DeliveryMatch model - exactly matching iOS DeliveryMatch structure
 * Replaces complex bidding system with simple iOS-style matching
 */
@Serializable
data class DeliveryMatch(
    val id: Int,
    @SerialName("carrier_trip_id")
    val carrierTripId: Int,
    @SerialName("package_request_id")
    val packageRequestId: Int,
    @SerialName("carrier_id")
    val carrierId: Int,
    @SerialName("shipper_id")
    val shipperId: Int,
    @SerialName("agreed_price")
    val agreedPrice: Double,
    @SerialName("match_status")
    val status: MatchStatus,
    @SerialName("pickup_confirmation_code")
    val pickupConfirmationCode: String? = null,
    @SerialName("delivery_confirmation_code")
    val deliveryConfirmationCode: String? = null,
    @SerialName("tracking_notes")
    val trackingNotes: List<TrackingNote>? = null,
    @SerialName("pickup_photo")
    val pickupPhoto: String? = null,
    @SerialName("delivery_photo")
    val deliveryPhoto: String? = null,
    @SerialName("confirmed_at")
    val confirmedAt: String? = null,
    @SerialName("picked_up_at")
    val pickedUpAt: String? = null,
    @SerialName("delivered_at")
    val deliveredAt: String? = null,
    @SerialName("carrier_requested_at")
    val carrierRequestedAt: String? = null,
    @SerialName("shipper_requested_at")
    val shipperRequestedAt: String? = null,
    @SerialName("shipper_responded_at")
    val shipperRespondedAt: String? = null,
    @SerialName("carrier_responded_at")
    val carrierRespondedAt: String? = null,
    @SerialName("carrier_message")
    val carrierMessage: String? = null,
    @SerialName("shipper_message")
    val shipperMessage: String? = null,
    @SerialName("decline_reason")
    val declineReason: String? = null,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("package_request")
    val packageRequestApi: PackageRequestApiData? = null,
    @SerialName("carrier_trip")
    val carrierTrip: Trip? = null,
    val carrier: User? = null,
    val shipper: User? = null
) {
    /**
     * Convert API PackageRequest to UI PackageRequest for backward compatibility
     */
    val packageRequest: PackageRequest?
        get() = packageRequestApi?.let { apiData ->
            PackageRequest(
                id = apiData.id,
                shipperId = apiData.shipperId,
                title = apiData.packageDescription,
                description = apiData.specialHandlingRequirements,
                pickupLocation = apiData.pickupAddress,
                deliveryLocation = apiData.deliveryAddress,
                pickupCoordinates = if (apiData.pickupLat != null && apiData.pickupLng != null) {
                    Coordinates(apiData.pickupLat, apiData.pickupLng)
                } else null,
                deliveryCoordinates = if (apiData.deliveryLat != null && apiData.deliveryLng != null) {
                    Coordinates(apiData.deliveryLat, apiData.deliveryLng)
                } else null,
                preferredPickupDate = apiData.pickupDatePreferred,
                preferredDeliveryDate = apiData.deliveryDateNeeded,
                packageSize = PackageSize.fromPackageType(apiData.packageType),
                packageWeight = apiData.packageWeightKg.toDoubleOrNull(),
                packageValue = apiData.packageValue.toDoubleOrNull(),
                isFragile = apiData.fragile,
                specialInstructions = apiData.specialHandlingRequirements,
                maxBudget = apiData.maxPriceBudget.toDoubleOrNull(),
                status = PackageRequestStatus.fromString(apiData.requestStatus),
                createdAt = apiData.createdAt,
                updatedAt = apiData.updatedAt,
                shipper = shipper
            )
        }
}

/**
 * MatchStatus enum - exactly matching iOS MatchStatus
 */
@Serializable
enum class MatchStatus {
    @SerialName("pending")
    PENDING,
    @SerialName("confirmed")
    CONFIRMED,
    @SerialName("picked_up")
    PICKED_UP,
    @SerialName("in_transit")
    IN_TRANSIT,
    @SerialName("delivered")
    DELIVERED,
    @SerialName("cancelled")
    CANCELLED,
    @SerialName("carrier_requested")
    CARRIER_REQUESTED,
    @SerialName("shipper_requested")
    SHIPPER_REQUESTED,
    @SerialName("shipper_accepted")
    SHIPPER_ACCEPTED,
    @SerialName("shipper_declined")
    SHIPPER_DECLINED,
    @SerialName("carrier_accepted")
    CARRIER_ACCEPTED,
    @SerialName("carrier_declined")
    CARRIER_DECLINED;
    
    val description: String
        get() = name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() }
}

/**
 * MatchCreationRequest - exactly matching iOS structure
 */
@Serializable
data class MatchCreationRequest(
    @SerialName("carrier_trip_id")
    val carrierTripId: Int,
    @SerialName("package_request_id")
    val packageRequestId: Int,
    @SerialName("agreed_price")
    val agreedPrice: Double
)

/**
 * MatchUpdateRequest - exactly matching iOS structure
 */
@Serializable
data class MatchUpdateRequest(
    @SerialName("confirmation_code")
    val confirmationCode: String? = null,
    @SerialName("photo_url")
    val photoURL: String? = null
)

/**
 * PackageAcceptRequest - for carrier accepting package requests
 */
@Serializable
data class PackageAcceptRequest(
    @SerialName("agreed_price")
    val agreedPrice: Double
)

/**
 * AcceptPackageRequest - for new trips/{trip_id}/packages/{package_id}/accept endpoint
 */
typealias AcceptPackageRequest = PackageAcceptRequest

/**
 * AcceptPackageResponse - for new trips/{trip_id}/packages/{package_id}/accept endpoint
 */
@Serializable
data class AcceptPackageResponse(
    val message: String,
    val data: DeliveryMatch
)

/**
 * PackageRejectionResponse - for handling package rejections
 */
@Serializable
data class PackageRejectionResponse(
    val success: Boolean,
    val message: String
)

/**
 * API Response wrappers matching iOS structure
 */
@Serializable
data class DataResponse<T>(
    val data: T
)

@Serializable
data class EmptyResponse(
    val success: Boolean = true,
    val message: String = ""
)

/**
 * Matches list response model - for delivery matches endpoints
 */
@Serializable
data class MatchesResponse(
    val success: Boolean = true,
    val message: String,
    val data: PaginatedResponse<DeliveryMatch>
)

// PaginatedResponse moved to shared location to avoid redeclaration 