package com.efthemiosprime.pasabayan.core.network.bookings

import com.efthemiosprime.pasabayan.core.domain.`enum`.InitiatedBy
import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import com.efthemiosprime.pasabayan.core.domain.model.UserSummary
import com.efthemiosprime.pasabayan.core.domain.util.FlexibleBoolSerializer
import com.efthemiosprime.pasabayan.core.domain.util.FlexibleDoubleSerializer
import com.efthemiosprime.pasabayan.core.domain.util.FlexibleStringSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DeliveryMatch DTO — the central model for bookings/matches.
 * Mirrors iOS `DeliveryMatch` from MatchingModels.swift.
 */
@Serializable
data class DeliveryMatchJson(
    val id: Int,
    @SerialName("trip_id") val tripId: Int? = null,
    @SerialName("package_request_id") val packageRequestId: Int? = null,
    @SerialName("match_status") val matchStatus: MatchStatus = MatchStatus.PENDING,
    @SerialName("agreed_price") @Serializable(with = FlexibleDoubleSerializer::class) val agreedPrice: Double? = null,
    @SerialName("initiated_by") val initiatedBy: InitiatedBy = InitiatedBy.UNKNOWN,

    // Counter-offer fields
    @SerialName("is_counter_offer") val isCounterOffer: Boolean = false,
    @SerialName("original_price") val originalPrice: String? = null,
    @SerialName("counter_offerer_id") val counterOffererId: Int? = null,
    @SerialName("counter_offerer_name") val counterOffererName: String? = null,
    @SerialName("counter_offer_round") val counterOfferRound: Int? = null,
    @SerialName("remaining_counter_offers") val remainingCounterOffers: Int? = null,
    @SerialName("can_counter_offer") @Serializable(with = FlexibleBoolSerializer::class) val canCounterOffer: Boolean? = null,

    // Messages
    @SerialName("carrier_message") val carrierMessage: String? = null,
    @SerialName("shipper_message") val shipperMessage: String? = null,
    @SerialName("decline_reason") val declineReason: String? = null,

    // Photos
    @SerialName("pickup_photo") val pickupPhoto: String? = null,
    @SerialName("delivery_photo") val deliveryPhoto: String? = null,
    @SerialName("receipt_photo") val receiptPhoto: String? = null,

    // Pickup codes
    @SerialName("pickup_confirmation_code") val pickupConfirmationCode: String? = null,
    @SerialName("delivery_confirmation_code") val deliveryConfirmationCode: String? = null,
    @SerialName("shipper_generated_code") val shipperGeneratedCode: String? = null,
    @SerialName("code_expires_at") val codeExpiresAt: String? = null,
    @SerialName("code_generated_at") val codeGeneratedAt: String? = null,
    @SerialName("code_used_at") val codeUsedAt: String? = null,

    // Delivery codes
    @SerialName("delivery_verification_code") val deliveryVerificationCode: String? = null,
    @SerialName("delivery_code_expires_at") val deliveryCodeExpiresAt: String? = null,
    @SerialName("delivery_code_generated_at") val deliveryCodeGeneratedAt: String? = null,
    @SerialName("delivery_code_used_at") val deliveryCodeUsedAt: String? = null,

    // Pricing & transaction
    // NOTE: `auto_charge` is a sibling of `data` on the confirm-match envelope
    // (see [MatchConfirmResponseJson]), NOT a field on the match itself —
    // matches iOS `MatchConfirmResponse` (commit f59cd1c). The earlier
    // placement here was unused at the mapper boundary.
    @SerialName("transaction_status") val transactionStatus: String? = null,
    @SerialName("transaction") val transaction: MatchTransactionJson? = null,
    @SerialName("carrier_expected_price") @Serializable(with = FlexibleDoubleSerializer::class) val carrierExpectedPrice: Double? = null,
    @SerialName("price_difference") @Serializable(with = FlexibleDoubleSerializer::class) val priceDifference: Double? = null,
    @SerialName("is_below_rate") @Serializable(with = FlexibleBoolSerializer::class) val isBelowRate: Boolean? = null,
    @SerialName("platform_fee_percent") val platformFeePercent: Int? = null,
    @SerialName("auto_cancel_after_days") val autoCancelAfterDays: Int? = null,
    @SerialName("auto_cancelled_at") val autoCancelledAt: String? = null,
    @SerialName("auto_cancelled") @Serializable(with = FlexibleBoolSerializer::class) val autoCancelled: Boolean? = null,

    // Location tracking
    @SerialName("carrier_current_lat") @Serializable(with = FlexibleDoubleSerializer::class) val carrierCurrentLat: Double? = null,
    @SerialName("carrier_current_lng") @Serializable(with = FlexibleDoubleSerializer::class) val carrierCurrentLng: Double? = null,
    @SerialName("location_last_updated_at") val locationLastUpdatedAt: String? = null,

    // Related entities
    @SerialName("carrier_trip") val carrierTrip: CarrierTripInfoJson? = null,
    @SerialName("package_request") val packageRequest: PackageRequestInfoJson? = null,
    val carrier: UserSummary? = null,
    val shipper: UserSummary? = null,
    @SerialName("chat_conversation_id") val chatConversationId: Int? = null,

    // Timestamps
    @SerialName("confirmed_at") val confirmedAt: String? = null,
    @SerialName("picked_up_at") val pickedUpAt: String? = null,
    @SerialName("delivered_at") val deliveredAt: String? = null,
    @SerialName("carrier_requested_at") val carrierRequestedAt: String? = null,
    @SerialName("shipper_requested_at") val shipperRequestedAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class CarrierTripInfoJson(
    val id: Int,
    @SerialName("origin_city") val originCity: String,
    @SerialName("destination_city") val destinationCity: String,
    @SerialName("departure_date") val departureDate: String? = null,
    @SerialName("arrival_date") val arrivalDate: String? = null,
    @SerialName("pickup_date") val pickupDate: String? = null,
    @SerialName("delivery_date") val deliveryDate: String? = null,
    @SerialName("transportation_method") val transportationMethod: String? = null,
    @SerialName("available_weight_kg")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val availableWeightKg: Double? = null,
    @SerialName("price_per_kg")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val pricePerKg: Double? = null,
    @SerialName("flat_trip_price")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val flatTripPrice: Double? = null,
    @SerialName("pricing_type") val pricingType: String? = null,
)

@Serializable
data class PackageRequestInfoJson(
    val id: Int,
    val title: String? = null,
    @SerialName("package_description") val description: String? = null,
    @SerialName("package_weight_kg")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val weightKg: Double? = null,
    @SerialName("pickup_city") val pickupCity: String? = null,
    @SerialName("delivery_city") val deliveryCity: String? = null,
    @SerialName("pickup_address") val pickupAddress: String? = null,
    @SerialName("delivery_address") val deliveryAddress: String? = null,
    @SerialName("package_type") val packageType: String? = null,
    @SerialName("urgency_level") val urgencyLevel: String? = null,
    val fragile: Boolean = false,
)

/**
 * MatchTransaction DTO — Stripe-backed transaction tied to a match.
 * Mirrors iOS `MatchTransaction` from MatchingModels.swift (parity c871184).
 *
 * Amount fields (total/platform/carrier) accept either number or string from
 * the backend and are normalized to String via FlexibleStringSerializer.
 */
@Serializable
data class MatchTransactionJson(
    val id: Int,
    val status: String? = null,
    @SerialName("total_amount")
    @Serializable(with = FlexibleStringSerializer::class)
    val totalAmount: String? = null,
    @SerialName("platform_fee")
    @Serializable(with = FlexibleStringSerializer::class)
    val platformFee: String? = null,
    @SerialName("carrier_amount")
    @Serializable(with = FlexibleStringSerializer::class)
    val carrierAmount: String? = null,
    val currency: String? = null,
    @SerialName("requires_action_at") val requiresActionAt: String? = null,
    @SerialName("error_code") val errorCode: String? = null,
    @SerialName("error_message") val errorMessage: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

/**
 * Auto-charge envelope-level payload returned by `PUT /matches/{id}/confirm`.
 * Mirrors iOS `AutoChargeInfo` (MatchingModels.swift). `queued` is true when
 * the backend has queued the charge against the shipper's default payment
 * method; `shipperHasDefaultPaymentMethod` lets the client know it must
 * prompt the user to add one before retry.
 */
@Serializable
data class AutoChargeInfoJson(
    val queued: Boolean = false,
    @SerialName("shipper_has_default_payment_method") val shipperHasDefaultPaymentMethod: Boolean = false,
)

@Serializable
data class MatchCreationRequestJson(
    @SerialName("trip_id") val tripId: Int,
    @SerialName("package_request_id") val packageRequestId: Int,
    @SerialName("agreed_price") val agreedPrice: Double,
    @SerialName("carrier_message") val carrierMessage: String? = null,
    @SerialName("shipper_message") val shipperMessage: String? = null,
)

// -- Response wrappers --

@Serializable
data class MatchResponseJson(
    val success: Boolean = false,
    val message: String = "",
    val data: DeliveryMatchJson? = null,
)

/**
 * Response envelope for `PUT /matches/{id}/confirm` — carries the auto-charge
 * payload at the root alongside the match (iOS `MatchConfirmResponse`).
 */
@Serializable
data class MatchConfirmResponseJson(
    val success: Boolean = false,
    val message: String = "",
    val data: DeliveryMatchJson? = null,
    @SerialName("chat_conversation_id") val chatConversationId: Int? = null,
    @SerialName("auto_charge") val autoCharge: AutoChargeInfoJson? = null,
)

@Serializable
data class MatchListResponseJson(
    val message: String = "",
    val data: PaginatedMatchesJson? = null,
)

@Serializable
data class PaginatedMatchesJson(
    val data: List<DeliveryMatchJson> = emptyList(),
    @SerialName("current_page") val currentPage: Int = 1,
    @SerialName("last_page") val lastPage: Int = 1,
    val total: Int = 0,
    @SerialName("per_page") val perPage: Int = 15,
)

@Serializable
data class CancelMatchResponseJson(
    val message: String = "",
    val data: DeliveryMatchJson? = null,
    @SerialName("chat_conversation_id") val chatConversationId: Int? = null,
    val refund: RefundResultJson? = null,
)

@Serializable
data class RefundResultJson(
    val processed: Boolean = false,
    @Serializable(with = FlexibleDoubleSerializer::class) val amount: Double? = null,
    @SerialName("transaction_id") val transactionId: Int? = null,
    val error: String? = null,
)
